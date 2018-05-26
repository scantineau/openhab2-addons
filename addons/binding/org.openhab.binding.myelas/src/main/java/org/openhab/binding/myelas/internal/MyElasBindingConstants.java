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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MyElasBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sébastien Cantineau - Initial contribution
 */
@NonNullByDefault
public class MyElasBindingConstants {

    public static final String BINDING_ID = "myelas";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONNECTION = new ThingTypeUID(BINDING_ID, "connection");

    // List of all Channel ids
    public static final String CHANNEL_OFFLINE_STATUS = "offlineStatus";
    public static final String CHANNEL_ONGOING_ALARM = "ongoingAlarm";
    public static final String CHANNEL_ARMED_PARTS_NB = "armedPartitionsNb";
    public static final String CHANNEL_DISARMED_PARTS_NB = "disarmedPartitionsNb";
    public static final String CHANNEL_PARTIALLYARMED_PARTS_NB = "partiallyArmedPartitionsNb";

    // List of all Server constants
    public static final String USER_PASS_PIN = "USER_PASS_PIN";
    public static final String POLL = "POLL";

    // Array of all basic and handled channels
    public static final String[] MYELAS_CHANNELS = { CHANNEL_OFFLINE_STATUS, CHANNEL_ONGOING_ALARM,
            CHANNEL_ARMED_PARTS_NB, CHANNEL_DISARMED_PARTS_NB, CHANNEL_PARTIALLYARMED_PARTS_NB };

}
