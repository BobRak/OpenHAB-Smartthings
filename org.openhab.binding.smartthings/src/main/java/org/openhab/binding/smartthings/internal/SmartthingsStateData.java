/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartthings.internal;

/**
 * Data object for smartthings state data
 *
 * @author Bob Raker
 *
 */
public class SmartthingsStateData {
    private String deviceDisplayName;
    private String capabilityAttribute;
    private String value;

    SmartthingsStateData() {
    }

    public String getDeviceDisplayName() {
        return deviceDisplayName;
    }

    public String getCapabilityAttribute() {
        return capabilityAttribute;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("State message: Display Name:\"").append(deviceDisplayName);
        sb.append("\", Attribute: \"").append(capabilityAttribute);
        sb.append("\", Value: \"").append(value).append("\"");
        return sb.toString();
    }
}
