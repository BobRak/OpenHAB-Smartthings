/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.smartthings.config.SmartthingsBridgeConfig;
import org.openhab.binding.smartthings.internal.SmartthingsHttpClient;
//import org.eclipse.equinox.event.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartthingsBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bob Raker - Initial contribution
 */
public class SmartthingsBridgeHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(SmartthingsBridgeHandler.class);

    private Bridge bridge;

    private SmartthingsBridgeConfig config;

    private SmartthingsHttpClient httpClient;

    public SmartthingsBridgeHandler(Bridge thing) {
        super(thing);
        this.bridge = thing;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Commands are handled by the "Things" which include Cm11aSwitchHandler and Cm11aLampHandler
    }

    @Override
    public void initialize() {

        // Get config data and validate
        config = getThing().getConfiguration().as(SmartthingsBridgeConfig.class);
        if (validateConfig(this.config) == false) {
            return;
        }

        // Initialize the Smartthings http client
        try {
            httpClient = new SmartthingsHttpClient(config.smartthingsIp, config.smartthingsPort);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Failed to start Smartthings Communications services because: " + e.getMessage());
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Smartthings Handler disposed.");

        httpClient.StopHttpClient();

        super.dispose();

    }

    /**
     * This is where smartthings /state messages are received and dispatched to the proper handler
     *
     * @param rd - The data from the Smartthings Hub
     */
    public void receivedStateMessage(Map<String, Object> rd) {
        logger.debug("received message from Smartthings hub");

        List<Thing> things = bridge.getThings();

        // This block goes through the Things attached to this bridge and find the name that matches the name that came
        // in the Smartthings message. Then it looks at the channels in that thing and looks for a channel that matches
        // one of the channel names on the thing. Then it updates the status of that channel
        synchronized (rd) {
            String thingTypeId = (String) rd.get("capabilityKey");
            String deviceDisplayName = (String) rd.get("deviceDisplayName");
            String channelId = (String) rd.get("capabilityAttribute");
            List<Channel> desiredChannels = new ArrayList<Channel>();
            // Find thing that matches the message capabilityKey and smartthingName
            for (Thing thing : things) {
                String thingTypeUid = thing.getThingTypeUID().getId();
                String name = (String) thing.getConfiguration().get("smartthingsName");
                // There are two cases that can occur here:
                // 1. Message was from an /state message sent from OpenHAB and in that case the capabilityKey
                // (thingTypeId) is returned.
                // 2. Message was from a device update event and in that case the capabilityKey (thingTypeId) is NOT
                // returned.
                // Have to handle both in the following if statement.
                if (name != null && (thingTypeUid.equals(thingTypeId) && name.equals(deviceDisplayName))
                        || thingTypeId == null && name != null && name.equals(deviceDisplayName)) {
                    List<Channel> channels = thing.getChannels();
                    // The channel we want should end with the attribute from the Smartthings hub.
                    // In reality there is probably only one
                    // channel since most things only define one channel
                    for (Channel ch : channels) {
                        if (ch.getUID().toString().endsWith(channelId)) {
                            desiredChannels.add(ch);
                        }
                    }
                    if (desiredChannels.size() > 0) {
                        for (Channel desiredChannel : desiredChannels) {
                            String dataType = desiredChannel.getProperties().get("smartthings-dataType");
                            if (dataType == null) {
                                logger.error(
                                        "Configuration error: No \"smartthings-dataType\" is specified for channel: {}",
                                        desiredChannel.getUID().toString());
                                return;
                            }
                            State state = convertToState(rd, dataType);
                            updateState(desiredChannel.getUID(), state);
                            logger.info("Smartthings updated State for device: {} - {} to {}", deviceDisplayName,
                                    channelId, state.toString());
                        }
                        break;
                    }
                }
            }

            if (desiredChannels.size() == 0) {
                logger.info(
                        "Could not map the message from the Smartthings hub: capabilityKey=\"{}\" with device name=\"{}\", capabilityAttribute=\"{}\", into an OpenHAB thing/channel, maybe this isn't configured in OpenHAB",
                        thingTypeId, deviceDisplayName, channelId);
            }
        }

    }

    private boolean validateConfig(SmartthingsBridgeConfig config) {
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "smartthings configuration is missing");
            return false;
        }

        String ip = config.smartthingsIp;
        if (ip == null || ip.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Smartthings IP address is not specified");
            return false;
        }

        int port = config.smartthingsPort;
        if (port <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Smartthings Port is not specified");
            return false;
        }

        return true;
    }

    /**
     * Convert the Smartthings data into an OpenHAB State
     *
     * @param statusMap
     * @return
     */
    private State convertToState(Map<String, Object> statusMap, String dataType) {

        // If there is no stateMap the just return null State
        if (statusMap == null) {
            return UnDefType.NULL;
        }

        String deviceType = (String) statusMap.get("capabilityAttribute");
        Object deviceValue = statusMap.get("value");
        State result;
        switch (dataType) {
            case "Color":
                logger.error(
                        "Converstion of Color Contol-color is not currently supported. Need to provide support for message {}.",
                        deviceValue);
                result = UnDefType.UNDEF;
                break;
            case "ENUM":
                result = new StringType((String) deviceValue);
                break;
            case "Number":
                if (deviceValue instanceof String) {
                    result = new DecimalType(Double.parseDouble((String) deviceValue));
                } else if (deviceValue instanceof Double) {
                    result = new DecimalType((Double) deviceValue);
                } else if (deviceValue instanceof Long) {
                    result = new DecimalType((Long) deviceValue);
                } else {
                    logger.error("Failed to convert {} with a value of {} from class {} to an appropriate type.",
                            deviceType, deviceValue, deviceValue.getClass().getName());
                    result = UnDefType.UNDEF;
                }
                break;
            case "OpenClosed":
                result = "open".equals(deviceValue) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                break;
            case "OnOff":
                result = "on".equals(deviceValue) ? OnOffType.ON : OnOffType.OFF;
                break;
            case "Percent":
                if (deviceValue instanceof String) {
                    result = new PercentType((String) deviceValue);
                } else if (deviceValue instanceof Double) {
                    result = new PercentType(((Double) deviceValue).intValue());
                } else {
                    logger.error("Failed to convert {} with a value of {} from class {} to an appropriate type.",
                            deviceType, deviceValue, deviceValue.getClass().getName());
                    result = UnDefType.UNDEF;
                }
                break;
            case "Text":
                result = new StringType((String) deviceValue);
                break;
            case "Vector3":
                // This is a weird result from Smartthings. If the messages is from a "state" request the result will
                // look like: "value":{"z":22,"y":-36,"x":-987}
                // But if the result is from sensor change via a subscription to a a threeAxis device the results will
                // be a String of the format "value":"-873,-70,484"
                // which GSON returns as a LinkedTreeMap
                if (deviceValue instanceof String) {
                    result = new StringType((String) deviceValue);
                    break;
                } else if (deviceValue instanceof Map) {
                    Map map = (Map) deviceValue;
                    String s = String.format("%.0f,%.0f,%.0f", map.get("x"), map.get("y"), map.get("z"));
                    result = new StringType(s);
                    break;
                } else {
                    logger.error(
                            "Unable to convert {} which should be in Smartthings Vector3 format to a string. The returned datatype from Smartthings is {}.",
                            deviceType, deviceValue.getClass().getName());
                    result = UnDefType.UNDEF;
                }

            default:
                logger.error("No type defined to convert {} with a value of {} from class {} to an appropriate type.",
                        deviceType, deviceValue, deviceValue.getClass().getName());
                result = UnDefType.UNDEF;
        }

        return result;
    }

    public SmartthingsHttpClient getSmartthingsHttpClient() {
        return httpClient;
    }

}
