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
package org.openhab.binding.riscocloud.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.riscocloud.internal.RiscoCloudHandlerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link RiscoCloudLoginResponse} holds information about login
 * response to your Risco cloud account.
 *
 * @author Sébastien Cantineau - Initial contribution
 */
@NonNullByDefault
public class RiscoCloudLoginResponse {

    private String errorDetail = "";
    private String error = "";
    private String statusDescr = "";
    private Map<Integer, String> siteList = new HashMap<Integer, String>();

    public String getErrorDetail() {
        return errorDetail;
    }

    public String getError() {
        return error;
    }

    public boolean isErrorDetected() {
        return !error.equals("0");
    }

    public String getStatusDescr() {
        return statusDescr;
    }

    public Map<Integer, String> getSiteList() {
        return siteList;
    }
}
