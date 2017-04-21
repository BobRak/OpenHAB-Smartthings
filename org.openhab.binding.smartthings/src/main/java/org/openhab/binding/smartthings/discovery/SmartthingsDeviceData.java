package org.openhab.binding.smartthings.discovery;

public class SmartthingsDeviceData {
    private String capability;
    private String attribute;
    private String name;
    private String id;

    SmartthingsDeviceData() {

    }

    public String getCapability() {
        return capability;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

}
