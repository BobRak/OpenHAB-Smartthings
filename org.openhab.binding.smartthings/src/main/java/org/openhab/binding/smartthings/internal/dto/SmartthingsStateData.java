/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.smartthings.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Data object for smartthings state data
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsStateData {
    public String deviceDisplayName;
    public String capabilityAttribute;
    public String value;
    public long hubTime;
    public long openHabStartTime;
    public long hubEndTime;
}
