/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.internal;

import static org.openhab.binding.smartthings.SmartthingsBindingConstants.*;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.smartthings.discovery.SmartthingsDiscoveryService;
import org.openhab.binding.smartthings.handler.SmartthingsBridgeHandler;
import org.openhab.binding.smartthings.handler.SmartthingsThingHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SmartthingsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bob Raker - Initial contribution
 */
/**
 * @author Bob
 *
 */
/**
 * @author Bob
 *
 */
public class SmartthingsHandlerFactory extends BaseThingHandlerFactory implements EventHandler {

    private Logger logger = LoggerFactory.getLogger(SmartthingsHandlerFactory.class);
    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private SmartthingsBridgeHandler bridge;
    private Gson gson;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return THING_TYPE_SMARTTHINGS.equals(thingTypeUID) || SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    public SmartthingsHandlerFactory() {
        // Get a Gson instance
        gson = new Gson();
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("SmartthingsHandlerFactory is now processing ThingTypeUID {}", thingTypeUID.getAsString());

        if (thingTypeUID.equals(THING_TYPE_SMARTTHINGS)) {
            bridge = new SmartthingsBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(bridge);
            return bridge;
        } else if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            // Everything but the bridge is handled by this one handler
            logger.debug("Creating thing handler for {}", thingTypeUID.getAsString());
            return new SmartthingsThingHandler(thing);
        }

        return null;
    }

    /**
     * Remove handler of things.
     */
    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SmartthingsBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    /**
     * Register a new discovery service
     *
     * @param handler
     */
    private void registerDeviceDiscoveryService(SmartthingsBridgeHandler handler) {

        SmartthingsDiscoveryService discoveryService = new SmartthingsDiscoveryService(handler);
        this.discoveryServiceRegs.put(handler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));

        // Have discovery messages from the hub delivered to the DiscoveryService
        Dictionary<String, Object> eventProperties = new Hashtable<String, Object>();
        eventProperties.put("event.topics", DISCOVERY_EVENT_TOPIC);
        bundleContext.registerService(EventHandler.class.getName(), discoveryService, eventProperties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
     *
     * "state" messages from the Hub are delivered here.
     */
    @Override
    public void handleEvent(Event event) {
        String topic = event.getTopic();
        String data = (String) event.getProperty("data");
        logger.debug("Event received on topic: {}", topic);
        Map<String, Object> map = new HashMap<String, Object>();
        map = gson.fromJson(data, map.getClass());

        if (bridge != null) {
            bridge.receivedStateMessage(map);
        }
    }

}
