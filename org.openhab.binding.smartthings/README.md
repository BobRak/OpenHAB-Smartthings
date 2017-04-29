# Samsung Smartthings Binding
This binding integrates the Samsung Smartthings Hub into OpenHAB. This is implemented as an OpenHAB 2 binding.

## Supported things
The goal is to support all of the bindings in the [Smartthings Capabilities list](http://docs.smartthings.com/en/latest/capabilities-reference.html). However I have a very limited number of devices and have currently tested the following devices:

* Acceleration
* Indicator - This can be viewed in OpenHAB. Configuration can be done in the Smartthings App and should not need to be changed in OpenHAB.
* Power Meter
* Switch
* Switch Level (i.e. dimmer)
* Three Axis

Please test with your devices. When (I won't even say if) you find one that doesn't work [follow these instructions](Troubleshooting.md) to collect the required data so I can work to support it.

## Discovery
Discovery allows OpenHAB to examine a binding and automatically find the Things available on binding. Discovery is supported and does work but needs more testing.

Discovery is not run automatically on startup. Therefore to run the discovery process perform the following:
1. Start the PaperUI
2. Click on **Inbox**
3. At the bottom of the screen click on **SEARCH FOR THINGS**
4. Select **Smartthings Binding**
5. You should now see the Smartthings Things in the Inbox
6. More information on using discovery is available in the [configuration Tutorial](http://docs.openhab.org/tutorials/beginner/configuration.html)

## OpenHAB Configuration
This binding is an OpenHAB 2 binding and uses the Bridge / Thing design with the Smartthings Hub being the Bridge and the controlled modules being the Things. The following definitions are specified in the .things file.

### Bridge Configuration
The bridge requires the IP address and port used to connect the OpenHAB server to the Smarrthings Hub.

    Bridge smartthings:smartthings:Home    [ smartthingsIp="192.168.1.12", smartthingsPort=39500 ] {

where:

* **smartthings:smartthings:Home** identifies this is a smartthings hub named Home. The first two segments must be smartthings:smartthings. You can choose any unique name for the the last segment. The last segment is used when you identify items connected to this hubthingTypeId. 
* **smartthingsIp** is the IP address of theSmartthings Hub. Your router should be configured such that the Smartthings Hub is always assigned to this IP address.
* **smartthingsPort** is the port the Smartthings hub listens on. 39500 is the port assigned my Smartthings so it should be used unless you have a good reason for using another port.


### Thing Configuration
Each attached thing must specify the type of device and it's Smartthings device name. The formart of the Thing description is:

    Thing <thingTypeId> name [ smartthingsName="<deviceName>" ]
    
where:

* **thingTypeId** corresponds to the "Preferences Reference" in the Smartthings Capabilities document but without the capability. prefix. i.e. A dimmer switch in the Capabilities document has a Preferences reference of capability.switchLevel, therefore the <thingTypeId> is switchLevel.
* **name** is what you want to call this thing and is used in defining the items that use this thing. 
* **deviceName** is the name you assigned to the device when you discovered and connected to it in the Smartthings App


### Example

    Bridge smartthings:smartthings:Home    [ smartthingsIp="192.168.1.12", smartthingsPort=39500 ] {
        Thing switchLevel              KitchenLights           [ smartthingsName="Kitchen lights" ]
        Thing contactSensor            MainGarageDoor          [ smartthingsName="Garage Door Open Sensor" ]
        Thing temperatureMeasurement   MainGarageTemp          [ smartthingsName="Garage Door Open Sensor" ]
        Thing battery                  MainGarageBattery       [ smartthingsName="Garage Door Open Sensor" ]
        Thing switch                   OfficeLight             [ smartthingsName="Family Room" ]
    }

## Items
These are specified in the .items file. This section describes the specifics related to this binding. Please see the [Items documentation](http://docs.openhab.org/configuration/items.html) for a full explanation of configuring items.

The most important thing is getting the **channel** specification correct. The general format is:

    { channel="smartthings:<thingTypeId>:<hubName>:<thingName>:<channelId>" }

The parts (separated by :) are defined as:

1. **smartthings** to specify this is a smartthings device
2. **thingTypeId** specifies the type of the thing  you are connecting to. This is the same as described in the last section.
3. **hubName** identifies the name of the hub specified above. This corresponds to the third segment in the **Bridge** definition.
4. **thingName** identifes the thing this is attached to and is the "name" you specified in the **Thing** definition.
5. **channelId** corresponds the the attribute in the [Smartthings Capabilities list](http://docs.smartthings.com/en/latest/capabilities-reference.html). for switch it would be "switch".

### Example

    Dimmer  KitchenLights        "Kitchen lights"           <slider>          { channel="smartthings:switchLevel:Home:KitchenLights:level" }
    Contact MainGarageDoor       "Garage door status [%s]" <garagedoor>       { channel="smartthings:contactSensor:Home:MainGarageDoor:contact" }  
    Number  MainGarageTemp       "Garage temperature [%.0f]"  <temperature>   { channel="smartthings:temperatureMeasurement:Home:MainGarageTemp:temperature" }  
    Number  MainGarageBattery    "Garage battery [%.0f]"  <battery>           { channel="smartthings:battery:Home:MainGarageBattery:battery" }  
    Switch           OfficeLight          "Office light"    <light>           { channel="smartthings:switch:Home:OfficeLight:switch" }

## Smartthings Configuration
Prior to running starting the binding the Smartthings hub must have the required OpenHAB software installed. [Follow these instructions](SmartthingsInstallation.md)

## Installation
Until this binding has been added to the OpenHAB repository you will need to copy the binding "jar" file to your OpenHAB server.

1. Locate the org.openhab.binding.smartthings-2.1.0-SNAPSHOT.jar file in the /target folder of the distribution.
2. Copy this file to the addons directory of your OpenHAB server. If you are using Openhabian this will be in the Samba share: openHAB/addons.
3. If openHAB is currently running it will need to be restarted (On Linux: sudo /etc/init.d/openhab2 stop followed by sudo /etc/init.d/openhab2 start)
4. Add the appropriate configuration files (.things, .items, .sitemaps)

## Known issues 
1. An exception is thrown when the the .things file is changed. Needs further research
2. Testing has been limited to the small set of devices I own.

## How to report issues
Testing has been limited to the small number of devices I own which are listed near the top of this document. If you discover one of your devices doesn't work as expected please follow the instructions in the [Troubleshooting file](Troubleshooting.md) and raise an issue on my Github Repo [BobRak](https://github.com/BobRak/)

## References

1. [OpenHAB configuration documentation](http://docs.openhab.org/configuration/index.html)
2. [Smartthings Capabilities Reference](http://docs.smartthings.com/en/latest/capabilities-reference.html)
3. [Smartthings Developers Documentation](http://docs.smartthings.com/en/latest/index.html)
4. [Smartthings Development Environment](https://graph.api.smartthings.com/)