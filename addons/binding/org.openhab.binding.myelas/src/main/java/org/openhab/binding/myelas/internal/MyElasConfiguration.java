/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.myelas.internal;

/**
 * The {@link MyElasConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Sébastien Cantineau - Initial contribution
 */
public class MyElasConfiguration {

    private String restUrl;
    private String webUIUrl;
    private int SyncTimeout;
    private String username;
    private String webpass;
    private int pincode;

    public String getRestUrl() {
        return restUrl;
    }

    public String getWebUIUrl() {
        return webUIUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getWebpass() {
        return webpass;
    }

    public int getPinCode() {
        return pincode;
    }

    public int getSyncTimeout() {
        return SyncTimeout;
    }

}
