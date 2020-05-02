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

public class LastAlarm {

    @SerializedName("dateStr")
    @Expose
    private String dateStr;
    @SerializedName("timeStr")
    @Expose
    private String timeStr;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("ViUID")
    @Expose
    private String viUID;
    @SerializedName("YTime")
    @Expose
    private String yTime;

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getViUID() {
        return viUID;
    }

    public void setViUID(String viUID) {
        this.viUID = viUID;
    }

    public String getYTime() {
        return yTime;
    }

    public void setYTime(String yTime) {
        this.yTime = yTime;
    }

}
