/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.smartthings.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.internal.SmartthingsHttpClient;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Smartthings Discovery service
 *
 * @author Bob Raker
 *
 */
public class SmartthingsDiscoveryService extends AbstractDiscoveryService implements EventHandler {

    private static final int SEARCH_TIME = 30;
    private static final int INITIAL_DELAY = 5;
    private static final int SCAN_INTERVAL = 180;

    private Logger logger = LoggerFactory.getLogger(SmartthingsDiscoveryService.class);

    private SmartthingsBridgeHandler bridgeHandler;

    private Gson gson;

    private SmartthingsHttpClient httpClient = null;

    private SmartthingsScan scanningRunnable;

    private ScheduledFuture<?> scanningJob;

    public SmartthingsDiscoveryService(SmartthingsBridgeHandler bridgeHandler) {
        super(SmartthingsBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
        logger.debug("Initializing discovery service");
        this.bridgeHandler = bridgeHandler;

        // Get a Gson instance
        gson = new Gson();

        this.scanningRunnable = new SmartthingsScan(this);
        if (bridgeHandler == null) {
            logger.warn("No bridge handler for scan given");
        }
        this.activate(null);
    }

    public SmartthingsDiscoveryService() {
        super(SmartthingsBindingConstants.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME);
        logger.debug("Initializing discovery service with default constructor");
    }

    @Override
    protected void activate(Map<String, Object> config) {
        logger.debug("SmartthingsDiscoveryService.activate() called");
    }

    protected void deactivate(ComponentContext componentContext) {
        logger.debug("SmartthingsDiscoveryService.deactivate() called");
    }

    protected void modified(ComponentContext componentContext) {
        logger.debug("SmartthingsDiscoveryService.modified() called");
    }

    /**
     * Called from the UI when starting a search.
     */
    @Override
    public void startScan() {
        logger.debug("Starting discovery scan on bridge {}", bridgeHandler.getThing().getUID());
        sendSmartthingsDiscoveryRequest();
    }

    /**
     * Stops a running scan.
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    /**
     * Starts background scanning for attached devices.
     */
    @Override
    protected void startBackgroundDiscovery() {
        if (scanningJob == null || scanningJob.isCancelled()) {
            logger.debug("Starting background scanning job");
            this.scanningJob = AbstractDiscoveryService.scheduler.scheduleWithFixedDelay(this.scanningRunnable,
                    INITIAL_DELAY, SCAN_INTERVAL, TimeUnit.SECONDS);
        } else {
            logger.debug("ScanningJob active");
        }
    }

    /**
     * Stops background scanning for attached devices.
     */
    @Override
    protected void stopBackgroundDiscovery() {
        if (scanningJob != null && !scanningJob.isCancelled()) {
            scanningJob.cancel(false);
            scanningJob = null;
        }
    }

    /**
     * Start the discovery process by sending a discovery request to the Smartthings Hub
     */
    private void sendSmartthingsDiscoveryRequest() {
        httpClient = bridgeHandler.getSmartthingsHttpClient();
        if (httpClient != null) {
            try {
                httpClient.sendDeviceCommand("/discovery", "{\"discovery\": \"yes\"}"); // this is a dummy message,
                // Smartthings will not return a response to this message but will send it's response message
                // which will get picked up by the SmartthingBridgeHandler.receivedPushMessage handler
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Attempt to send command to the Smartthings hub failed with: ", e.getMessage());
            }
        }
    }

    /**
     * Handle discovery data returned from the Smartthings hub.
     * The data is delivered into the SmartthingServlet. From there it is sent here via the Event service
     */
    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();
        String data = (String) event.getProperty("data");
        logger.debug("Event received on topic: {}", topic);

        // The data returned from the Smartthings hub is a list of strings where each
        // element is the data for one device. That device string is another json object
        List<String> devices = new ArrayList<String>();
        devices = gson.fromJson(data, devices.getClass());
        for (String device : devices) {
            SmartthingsDeviceData deviceData = gson.fromJson(device, SmartthingsDeviceData.class);
            createDevice(deviceData);
        }
    }

    /**
     * Create a device with the data from the Smartthings hub
     *
     * @param deviceData
     */
    private void createDevice(SmartthingsDeviceData deviceData) {
        logger.debug("Discovery: Creating device: ThingType {} with name {}", deviceData.getCapability(),
                deviceData.getName());

        // Build the UID as a string smartthings:{ThingType}:{BridgeName}:{DeviceName}
        String deviceNameNoSpaces = deviceData.getName().replaceAll("\\s", "");
        ThingUID bridgeUid = bridgeHandler.getThing().getUID();
        String bridgeId = bridgeUid.getId();
        String uidStr = String.format("smartthings:%s:%s:%s", deviceData.getCapability(), bridgeId, deviceNameNoSpaces);

        Map<String, Object> properties = new HashMap<>();
        properties.put("smartthingsName", deviceData.getName());
        properties.put("deviceId", deviceData.getId());

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(new ThingUID(uidStr)).withProperties(properties)
                .withRepresentationProperty(deviceData.getId()).withBridge(bridgeUid).withLabel(deviceData.getName())
                .build();

        thingDiscovered(discoveryResult);
    }

    /**
     * Scanning worker class.
     */
    public class SmartthingsScan implements Runnable {
        /**
         * Handler for delegation to callbacks.
         */
        private SmartthingsDiscoveryService service;

        /**
         * Constructor.
         *
         * @param handler
         */
        public SmartthingsScan(SmartthingsDiscoveryService service) {
            this.service = service;
        }

        /**
         * Poll Smartthings hub one time.
         */
        @Override
        public void run() {
            logger.debug("Starting scan on bridge {}", bridgeHandler.getThing().getUID());
            sendSmartthingsDiscoveryRequest();
        }
    }
}
