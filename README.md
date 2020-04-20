# OpenHAB-Smartthings

This is an openHAB binding for use with the Samsung Smartthings hub. On 3/5/19 it was submitted to openHAB for review and addition to openHAB bindings collection. Until it has been accepted and added to the openHAB platform you can install it from here.

## Requires Smartthings Classic App

This binding only works with the **Smartthings Classic** app for your phone.

## Installation instructions

1. The org.openhab.binding.smartthings-2.5.0-SNAPSHOT.jar file which is located at org.openhab.binding.smartthings/target has to be copied to the addons folder in your openHAB installation.  If you are using Openhabian this will be in the Samba share: openHAB/addons. 
2. If openHAB is currently running it will need to be restarted (On Linux: sudo /etc/init.d/openhab2 stop followed by sudo /etc/init.d/openhab2 start).
3. Then follow the setup instructions in the README.md file in the org.openhab.binding.smartthing directory. Make sure to perform the **Smartthings Configuration** steps described in that file.

## How to report issues

If you discover one of your devices doesn't work as expected please follow the instructions in the [Troubleshooting file](org.openhab.binding.smartthings/Troubleshooting.md) and raise an issue on my Github Repo [BobRak](https://github.com/BobRak/)

## 4/4/2019 Release Notes

1. This update requires that you redeploy the smartthings code as described in the [Smartthings Installation document](https://github.com/BobRak/OpenHAB-Smartthings/blob/master/org.openhab.binding.smartthings/SmartthingsInstallation.md). **The location of the smartthings code has changed.** It is now [here](https://github.com/BobRak/OpenHAB-Smartthings/tree/master/org.openhab.binding.smartthings/contrib)
2. The RGB bulb wasn't previously working. I recently purchased a Sengled bulb just to test this. I discovered that Samrtthings defines the hue as 0-100 and openHAB defines hue in the industry standard range of 0-360. The code has been updated. The README file has been updated with an example configuration for the Sengled RGB bulb.
3. Since I started development of this binding Samsung has added capabilties for many of their appliances. I have had some requests to add that to this binding. I will do that but not until I complete code changes requested bu openHAB and it has been added to the base openHAB system. One reason for this delay is because I don't own any of these appliances and will have to build simulators first. So overall this will be a considerable amount of work.

## 4/5/2019 Release notes

Right after the 4/5 release I had reports that Discovery was no longer working. I retested discovery in my development environment and found it working. I also discovered some files that should have been deleted were still present so I removed them.

## 4/7/2020 Release notes

Added support for some of the proposed capabilities in the [Samsung Smartthings Capabilities document](https://docs.smartthings.com/en/latest/capabilities-reference.html). In particular support was added for the DryerOperatingState and DryerMode capabilitles. Sample config files can be found in my repo: **Smartthings-SimulatedDryer**. This addition included changes to the Smartthings Hub code so make sure you reinstall the SmartApp.

## 4/10/2020

Added support the following capabilities:
1. Air Conditioner Mode
2. Temperature Measurement
3. Thermostat Cooling Setpoint - Note: I created a Simulated Air Conditioner Device Handler to test the capabilities 1 - 3. I could not make this one work in the simulator but I do beleive the code is correct.  The openHAB code creates the correct message to the ST hub. The hub processes the message and calls the correct function to set the coolongSeptpoint but the attribute is not updated. I have given up on that after almost 2 days.
4. Lock Only - I updated the code for this. I can confirm it will go to locked status. But, beacuse I don't have a way to set it to unlocked there is not as much testing as I would like.

## 4/20/2020 

Added additional states to the following capabilities And additional testing of those capabilities
1. Air Conditioner Mode
2. Temperature Measurement
3. Thermostat Cooling Setpoint
4. Thermostat Fan Mode
5. Thermostat Heating Setpoint
6. Thermostat Mode
7. Thermostat Operating State
8. Thermostat