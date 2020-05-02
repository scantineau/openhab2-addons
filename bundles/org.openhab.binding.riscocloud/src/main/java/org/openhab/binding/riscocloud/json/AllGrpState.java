/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.riscocloud.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * This class is generated with http://www.jsonschema2pojo.org/
 * Use json provided by RiscoCloud server and choose these options :
 * Package : org.openhab.binding.riscocloud.json
 * Class Name : ServerDatasObject
 * Target language : Java
 * Source type : JSON
 * Annotation style : Gson
 * Tick :
 * - Use double numbers
 * - Include getters and setters
 * - Allow additional properties
 *
 * @author Sébastien Cantineau - Initial contribution
 */

public class AllGrpState {

    @SerializedName("GlobalState")
    @Expose
    private Object globalState;
    @SerializedName("PartState")
    @Expose
    private List<Object> partState = null;

    public Object getGlobalState() {
        return globalState;
    }

    public void setGlobalState(Object globalState) {
        this.globalState = globalState;
    }

    public List<Object> getPartState() {
        return partState;
    }

    public void setPartState(List<Object> partState) {
        this.partState = partState;
    }

}
