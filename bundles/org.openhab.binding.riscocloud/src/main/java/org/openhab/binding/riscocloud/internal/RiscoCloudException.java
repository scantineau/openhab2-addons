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
package org.openhab.binding.riscocloud.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RiscoCloudException} represents an exception in the response of
 * the Risco cloud service.
 *
 * @author Sébastien Cantineau - Initial contribution
 */
@NonNullByDefault
public class RiscoCloudException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RiscoCloudException(String message) {
        super(message);
    }

    public RiscoCloudException(final Throwable cause) {
        super(cause);
    }

    public RiscoCloudException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
