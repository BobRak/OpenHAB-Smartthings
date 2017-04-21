/**
 *
 */
package org.openhab.binding.smartthings.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

/**
 * This is an abstract base class for the "Thing" handlers (i.e. SmartthingsXxxxeHandler).
 * It is not used the the Bridge handler (SmartthingsBridgeHandler)
 *
 * @author Bob
 *
 */
public abstract class SmartthingsAbstractHandler extends BaseThingHandler {

    /**
     * The device name that corresponds to the name in the Smartthings Hub
     */
    protected String smartthingsDeviceName;

    /**
     * The device location
     */
    protected String deviceLocation;

    /**
     * The constructor
     *
     * @param thing The "Thing" to be handled
     */
    public SmartthingsAbstractHandler(Thing thing) {
        super(thing);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.ThingHandler#handleCommand(org.eclipse.smarthome.core.thing.ChannelUID,
     * org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public abstract void handleCommand(ChannelUID channelUID, Command command);
}
