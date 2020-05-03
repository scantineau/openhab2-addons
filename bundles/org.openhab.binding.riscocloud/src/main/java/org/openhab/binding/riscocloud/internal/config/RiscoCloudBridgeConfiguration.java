/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.riscocloud.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RiscoCloudBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Sébastien Cantineau - Initial contribution
 */
@NonNullByDefault
public class RiscoCloudBridgeConfiguration {
    private String username = "";
    private String webpass = "";
    private String thingUid = "";
    private int refresh = 30;
    private int statusTimeout = 300;

    public String getUsername() {
        return username;
    }

    public String getWebpass() {
        return webpass;
    }

    public String getThingUid() {
        return thingUid;
    }

    public void setThingUid(String thingUid) {
        this.thingUid = thingUid;
    }

    public int getRefresh() {
        return refresh;
    }

    public int getStatusTimeout() {
        return statusTimeout;
    }
}
