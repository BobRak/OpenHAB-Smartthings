# Installation of Smartthings code
To use the Smartthings OpenHAB binding code needs to be installed on the Smartthings Hub.  Currently the Smartthings code is bundled with the binding. Eventually I hope to publish the code in the Smartthings repository.

## Please help make these instructions better
As you follow these instructions please provide feedback on actions you have to perform that don't match these instructions.

## Installation of artifacts on the Smartthings HUB
The following steps need to be done on the Smartthings hub using the web based [Smartthings developers tools](https://graph.api.smartthings.com/). 
### Initial steps
These steps assume you already have a Smartthings Hub and have set it up. And, you have created an account.
1. Open the developers website using the link above.
2. Logon using the same email and password as on your Smartthings phone app.
3. Click on locations.
4. Verify your hub is listed.

### Copying files
The files are located within the **target** directory structure. The following files need to be deployed
* OpenHabAppV2 - This is a SmartApp that receives requests from OpenHAB and returns the needed data
* OpenHabDeviceHandler - This is a lower level module that provides a connection between OpenHAB and the Hub using the LAN connection

### Install OpenHabAppV2
1. Locate OpenHabAppV2.groovy in the /target/smartthings/SmartApps Directory.
2. Open OpenHabAppV2.groovy in an editor (Some program you can use to copy the contents to the clipboard)
3. Copy the contents to the clipboard
4. Using the Smartthings developers tools:
5. Logon, if you are not logged on
6. Select **My SmartApps** 
7. Click on the **+ New SmartApp** near the top right
8. Click on the **From Code** tab
9. Paste the contents of the clipboard
10. Click on the **Create** button near the bottom left
10. Click on **Publish -> For Me**
11. The SmartApp is now ready

### Install OpenHabDeviceHandler
1. Locate OpenHabDeviceHandler.groovy in the /target/smartthings/DeviceHandlers Directory.
2. Open OpenHabDeviceHandler.groovy in an editor (Some program you can use to copy the contents to the clipboard)
3. Copy the contents to the clipboard
4. Using the Smartthings developers tools:
5. Select **My Device Handlers** 
7. Click on the **+ Create New Device Handler** near the top right
8. Click on the **From Code** tab
9. Paste the contents of the clipboard
10. Click on the **Create** button near the bottom left
10. Click on **Publish -> For Me**
11. The Device Handler is now ready

### Create the Device
4. Using the Smartthings developers tools:
5. Select **My Devices** 
7. Click on the **+ New Device** near the top right
8. Enter the following data in the form:
    * Name: OpenHabDevice
    * Label: OpenHabDevice
    * Device Network ID: This needs to be the MAC address of your OpenHAB server with no spaces or punctuation
    * Type: OpenHabDeviceHandler (This should be the last one on the list)
    * Location: (Select from the dropdown)
    * Hub: (Select from the dropdown)
10. Click on the **Create** button near the bottom left
11. In the Preferences section enter the following:
     * ip: (This is the IP address of your OpenHAB server)
     * mac: (This is the same as the Device Network ID but with : between segments
     * port: 8080 (This is the port of the OpenHAB application on your server)
     * Save the preferences

## Configuration in the Smartthings App
Next the App needs to be configured using **the Smartthings App on your smartphone**.
1. Start the Smartthings App on your phone
2. Select **Automation** from the bottom menu
3. Select **SmartApps** from the top menu
4. Click on **+ Add a SmartApp**
5. Scroll to the bottom and select **My Apps**
6. Select **OpenHabAppV2**
     * In the selection screen select the devices you want to interact with OpenHAB. **Warning** devices not selected can not be used with OpenHAB. 
     * Near the bottom of the screen is **Notify this virtual device**, click on it and select **OpenHabDevice**. 
     * Finally click **Done** on the upper right.


