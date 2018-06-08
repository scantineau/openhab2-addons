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
package org.openhab.binding.myelas.server.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.openhab.binding.myelas.server.io.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ServerDatasHandler} is responsible for handling
 * and interpreting json from server
 *
 * @author Sébastien Cantineau - Initial contribution
 */
public class ServerDatasHandler extends ServerDatasObject implements Cloneable {

    private final static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private Overview myOverview;
    private PartInfo myPartInfo;

    public static ServerDatasHandler createServerDatasHandler(String json) {
        logger.debug("debug response complete: {}", json);
        Gson gson = new Gson();
        return gson.fromJson(json, ServerDatasHandler.class);
    }

    public boolean isValidObject() {
        readOverview();
        return myOverview != null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public DecimalType getArmedPartNb() {
        readPartInfo();
        return DecimalType.valueOf(myPartInfo == null ? "0" : myPartInfo.getArmedStr().split(" ")[0]);
    }

    public DecimalType getDisarmedPartNb() {
        readPartInfo();
        return DecimalType.valueOf(myPartInfo == null ? "0" : myPartInfo.getDisarmedStr().split(" ")[0]);
    }

    public DecimalType getPartiallyArmedPartNb() {
        readPartInfo();
        return DecimalType.valueOf(myPartInfo == null ? "0" : myPartInfo.getPartarmedStr().split(" ")[0]);
    }

    private void readPartInfo() {
        readOverview();
        if (myOverview != null) {
            myPartInfo = myOverview.getPartInfo();
        }
    }

    private void readOverview() {
        myOverview = getOverview();
    }
}
