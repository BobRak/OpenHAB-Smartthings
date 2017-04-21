/**
 *
 */
package org.openhab.binding.smartthings.handler;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringListType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.smartthings.config.SmartthingsThingConfig;
import org.openhab.binding.smartthings.internal.SmartthingsHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bob
 *
 */
public class SmartthingsThingHandler extends SmartthingsAbstractHandler {

    private Logger logger = LoggerFactory.getLogger(SmartthingsThingHandler.class);

    private SmartthingsThingConfig config;

    /**
     * The constructor
     *
     * @param thing The "Thing" to be handled
     */
    public SmartthingsThingHandler(Thing thing) {
        super(thing);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.smartthings.handler.SmartthingsAbstractHandler#handleCommand(org.eclipse.smarthome.core.thing
     * .ChannelUID, org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        Bridge bridge = getBridge();

        if (bridge != null) {
            SmartthingsBridgeHandler smartthingsBridgeHandler = (SmartthingsBridgeHandler) bridge.getHandler();
            if (smartthingsBridgeHandler != null
                    && smartthingsBridgeHandler.getThing().getStatus().equals(ThingStatus.ONLINE)) {

                String thingTypeId = thing.getThingTypeUID().getId();
                String smartthingsName = config.smartthingsName;
                String smartthingsType = getSmartthingsAttributeFromChannel(channelUID);

                SmartthingsHttpClient httpClient = smartthingsBridgeHandler.getSmartthingsHttpClient();

                if (command instanceof RefreshType) {
                    // Go to ST hub and ask for current state
                    String jsonMsg = String.format(
                            "{\"capabilityKey\": \"%s\", \"deviceDisplayName\": \"%s\", \"capabilityAttribute\": \"%s\"}",
                            thingTypeId, smartthingsName, smartthingsType);
                    try {
                        httpClient.sendDeviceCommand("/state", jsonMsg);
                        // Smartthings will not return a response to this message but will send it's response message
                        // which will get picked up by the SmartthingBridgeHandler.receivedPushMessage handler
                    } catch (InterruptedException | TimeoutException | ExecutionException e) {
                        logger.error("Attempt to send command to the Smartthings hub failed with: ", e);
                    }

                } else {
                    // Send update to ST hub
                    String smartthingsValue = convertToSmartthingsValue(command);
                    String jsonMsg = String.format(
                            "{\"capabilityKey\": \"%s\", \"deviceDisplayName\": \"%s\", \"value\": %s}", thingTypeId,
                            smartthingsName, smartthingsValue);

                    try {
                        httpClient.sendDeviceCommand("/update", jsonMsg);
                    } catch (InterruptedException | TimeoutException | ExecutionException e) {
                        logger.error("Attempt to send command to the Smartthings hub failed with: ", e);
                    }
                    // The smartthings hub won't (can't) return a response to this call. But, it will send a separate
                    // message back to the SmartthingBridgeHandler.receivedPushMessage handler
                }
            }
        }
    }

    /**
     * Convert the OpenHAB command to a string that can be sent to Smartthings.
     * If the value is a string it will be wrapped with quotes
     *
     * @param command
     * @return
     */
    private String convertToSmartthingsValue(Command command) {
        String value;

        if (command instanceof DateTimeType) {
            DateTimeType dt = (DateTimeType) command;
            value = dt.format("%m/%d/%Y %H.%M.%S");
        } else if (command instanceof DecimalType) {
            value = command.toString();
        } else if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;
            value = String.format("{\"hue\": %d, \"saturation\": %d, \"brightness\": %d }", hsb.getHue().intValue(),
                    hsb.getSaturation().intValue(), hsb.getBrightness().intValue());
        } else if (command instanceof IncreaseDecreaseType) { // Need to surround with double quotes
            value = "\"" + command.toString().toLowerCase() + "\"";
        } else if (command instanceof NextPreviousType) { // Need to surround with double quotes
            value = "\"" + command.toString().toLowerCase() + "\"";
        } else if (command instanceof OnOffType) { // Need to surround with double quotes
            value = "\"" + command.toString().toLowerCase() + "\"";
        } else if (command instanceof OpenClosedType) { // Need to surround with double quotes
            value = "\"" + command.toString().toLowerCase() + "\"";
        } else if (command instanceof PercentType) {
            value = command.toString();
        } else if (command instanceof PointType) { // Not really sure how to deal with this one and don't see a use for
                                                   // it in Smartthings right now
            value = command.toFullString();
        } else if (command instanceof RefreshType) { // Need to surround with double quotes
            value = "\"" + command.toString().toLowerCase() + "\"";
        } else if (command instanceof RewindFastforwardType) { // Need to surround with double quotes
            value = "\"" + command.toString().toLowerCase() + "\"";
        } else if (command instanceof StopMoveType) { // Need to surround with double quotes
            value = "\"" + command.toString().toLowerCase() + "\"";
        } else if (command instanceof PlayPauseType) { // Need to surround with double quotes
            value = "\"" + command.toString().toLowerCase() + "\"";
        } else if (command instanceof StringListType) {
            value = command.toString();
        } else if (command instanceof StringType) {
            value = command.toString();
        } else if (command instanceof UpDownType) { // Need to surround with double quotes
            value = "\"" + command.toString().toLowerCase() + "\"";
        } else {
            value = command.toString().toLowerCase();
        }

        return value;
    }

    /**
     * Get the Smartthings capability reference "attribute" from the channel properties.
     * In OpenHAB each channel id corresponds to the Smartthings attribute. In the ChannelUID the
     * channel id is the last segment
     *
     * @param channelUID
     * @return channel id
     */
    private String getSmartthingsAttributeFromChannel(ChannelUID channelUID) {

        String id = channelUID.getId();
        return id;
    }

    @Override
    public void initialize() {
        config = getThing().getConfiguration().as(SmartthingsThingConfig.class);
        if (validateConfig(this.config) == false) {
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Handle an update to the configuration
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("Received configuration update for thing: {}", thing.getUID().getAsString());

        boolean configChanged = false;

        Configuration configuration = editConfiguration();
        // Examine each new config parameter and if it is different than the existing then update it
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            String paramName = configurationParameter.getKey();
            Object valueObject = configurationParameter.getValue();
            Object existingValue = configuration.get(paramName);

            // Only 2 parameters to check and verify: smartthingsName and smartthingsLocation which is optional
            if (paramName.equals("smartthingsName")) {
                if (valueObject == null || !(valueObject instanceof String)) {
                    logger.info(
                            "Configuration update of smartthingsName for {} is not valid because the new value is missing or is not a String, change ignored",
                            thing.getUID().getAsString());
                    return;
                }

                String valueString = (String) valueObject;
                if (valueString.length() == 0) {
                    logger.info(
                            "Configuration update of smartthingsName for {} is not valid because the new value is 0 length, change ignored",
                            thing.getUID().getAsString());
                    return;
                }

                if (!existingValue.equals(valueString)) {
                    // Found a change
                    configuration.put("smartthingsName", valueString);
                    configChanged = true;
                    logger.info("Configuration updated for {} smartthingsName changed from {} to {}",
                            thing.getUID().getAsString(), existingValue, valueString);
                }
            }

            if (paramName.equals("smartthingsLocation")) {
                if ((valueObject == null || !(valueObject instanceof String))
                        && (existingValue == null || !(existingValue instanceof String))) {
                    // OK No change
                    return;
                }

                String valueString = (String) valueObject;
                if (valueString.equals(existingValue)) {
                    // OK No change
                    return;
                }

                // Found a change
                configuration.put("smartthingsLocation", valueString);
                configChanged = true;
                logger.info("Configuration updated for {} smartthingsLocation changed from {} to {}",
                        thing.getUID().getAsString(), existingValue, valueString);

            }
        }

        if (configChanged) {
            // Persist changes
            updateConfiguration(configuration);
        }

    }

    @Override
    public void dispose() {
        logger.info("Disposing of the Smartthings Thing Handler");
    }

    private boolean validateConfig(SmartthingsThingConfig config) {
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "smartthings Thing configuration is missing");
            return false;
        }

        String name = config.smartthingsName;
        if (name == null || name.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Smartthings device name is missing");
            return false;
        }

        return true;
    }
}
