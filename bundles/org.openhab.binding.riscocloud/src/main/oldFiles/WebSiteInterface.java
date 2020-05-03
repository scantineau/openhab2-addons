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
package org.openhab.binding.riscocloud.handler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openhab.binding.riscocloud.internal.RiscoCloudBindingConstants;
import org.openhab.binding.riscocloud.json.ServerDatasHandler;
import org.openhab.binding.riscocloud.model.RiscoCloudLoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openhab.binding.riscocloud.internal.RiscoCloudBindingConstants.*;

/**
 * The {@link WebSiteInterface} class is the interface with Risco Cloud Website.
 *
 * @author Sébastien Cantineau - Initial contribution
 */
public final class WebSiteInterface {
    final static Logger logger = LoggerFactory.getLogger(WebSiteInterface.class);
    private final static Gson gson = new Gson();

    public static RiscoCloudLoginResponse webSiteLogin(Configuration config) {
        RiscoCloudLoginResponse riscoCloudLoginResponse = new RiscoCloudLoginResponse();

        if (config.get(RiscoCloudBindingConstants.USERNAME) == null
                || config.get(RiscoCloudBindingConstants.WEBPASS) == null) {
            riscoCloudLoginResponse.error += " Parameter 'username' and 'webpass' must be configured.";
            riscoCloudLoginResponse.statusDescr = "Missing credentials";
        } else {
            try {
                Document document = null;
                String loginPage = null;
                String content = genInputJson(USER_PASS, config, 0);
                InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                loginPage = HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.RISCO_CLOUD_WEBUI_URL), null,
                        stream, "application/json", 20000);
                // logger.debug("loginPage=" + loginPage);

                document = Jsoup.parse(loginPage);
                Element form = document.select("form").first();
                // logger.debug("form=" + form.toString());

                Elements sites = form.select("label[id$=_label_name]");
                // logger.debug("sites = " + sites.toString());
                for (Element site : sites) {
                    // logger.debug("site = " + site.toString());
                    Pattern pattern = Pattern.compile("site_(.*?)_");
                    Matcher matcher = pattern.matcher(site.attr("id"));
                    if (matcher.find()) {
                        // logger.debug("site added " + matcher.group(1) + "-" + site.text());
                        riscoCloudLoginResponse.siteList.put(Integer.parseInt(matcher.group(1)), site.text());
                    }
                }
                // logger.debug("siteList : " + loginResult.siteList.values() + " siteList.isEmpty() : " +
                // loginResult.siteList.isEmpty());
                if (riscoCloudLoginResponse.siteList.isEmpty()) {
                    riscoCloudLoginResponse.error += "No site found";
                    riscoCloudLoginResponse.statusDescr = "@text/offline.site-error";
                }
            } catch (IOException e) {
                riscoCloudLoginResponse.error += "Connection error to " + config.get(RiscoCloudBindingConstants.RISCO_CLOUD_WEBUI_URL);
                riscoCloudLoginResponse.errorDetail = e.getMessage();
                riscoCloudLoginResponse.statusDescr = "@text/offline.uri-error-1";

            } catch (IllegalArgumentException e) {
                riscoCloudLoginResponse.error += "caught exception !";
                riscoCloudLoginResponse.errorDetail = e.getMessage();
                riscoCloudLoginResponse.statusDescr = "@text/offline.uri-error-2";
            }
        }
        return riscoCloudLoginResponse;
    }

    public static RiscoCloudLoginResponse webSitePoll(Configuration config) throws IOException {
        RiscoCloudLoginResponse riscoCloudLoginResponse = new RiscoCloudLoginResponse();

        if (config == null) {
            riscoCloudLoginResponse = doConnect(config);
        } else {

            String jsonResponse = null;
            String content = genInputJson(POLL, config, 0);
            // logger.debug("jsonSent = {}", content);
            InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            jsonResponse = HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.REST_URL), null,
                    stream, "application/json", 20000);
            // logger.debug("webSitePoll() : apiResponse global = {}", jsonResponse);

            JsonParser parser = new JsonParser();
            JsonObject jsonValueObject = parser.parse(jsonResponse).getAsJsonObject();
            JsonElement jsonElement = jsonValueObject.get("error");
            if (!jsonElement.isJsonNull() && jsonElement.getAsInt() != 0) {
                riscoCloudLoginResponse = doConnect(config);
            } else {
                // logger.debug("apiResponse before fill = {}", jsonResponse);
                for (Map.Entry<String, String> entry : REST_URLS.entrySet()) {
                    String myJsonResponse = null;
                    // logger.debug("REST URL : Property = {} : url = {}", entry.getKey(), entry.getValue());
                    String property = entry.getKey();
                    String url = (String) config.get(entry.getValue());
                    // logger.debug("REST URL : key = {} : value = {} : Property = {} : url = {}", entry.getKey(),
                    // entry.getValue(), property, url);
                    myJsonResponse = HttpUtil.executeUrl("POST", url, null, null, 20000);
                    if (myJsonResponse != null) {
                        jsonResponse = jsonBuilder(jsonResponse, property, myJsonResponse);
                    }
                }
                // logger.debug("apiResponse after fill = {}", jsonResponse);
                // Map the JSON response to an object
                riscoCloudLoginResponse.serverDatasHandler = gson.fromJson(jsonResponse, ServerDatasHandler.class);
                if (riscoCloudLoginResponse.serverDatasHandler.getError() != 0) {
                    riscoCloudLoginResponse = doConnect(config);
                }
            }
        }

        return riscoCloudLoginResponse;
    }

    public static RiscoCloudLoginResponse webSiteSendCommand(Configuration config, String command, int idPart) throws IOException {
        RiscoCloudLoginResponse riscoCloudLoginResponse = new RiscoCloudLoginResponse();
        if (config == null) {
            return riscoCloudLoginResponse;
        }

        // switch (command) {
        // case ARM_FULL:
        // break;
        // default:
        // logger.debug("handleSiteUpdate() : function = '{}': idPart = '{}'", function, idPart);
        //
        // }

        String jsonResponse = null;

        String content = genInputJson(command, config, idPart);

        InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        jsonResponse = HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.ARM_DISARM_URL), null,
                stream, "application/json", 20000);

        return riscoCloudLoginResponse;
    }

    private @Nullable static RiscoCloudLoginResponse doConnect(Configuration config) {
        RiscoCloudLoginResponse riscoCloudLoginResponse = new RiscoCloudLoginResponse();
        ServerDatasHandler newServerDatasHandler = null;

        // Check if a pincode has been provided during the bridge creation
        if (config.get(RiscoCloudBindingConstants.PINCODE) == null) {
            riscoCloudLoginResponse.error += " Parameter 'pincode' must be configured.";
            riscoCloudLoginResponse.statusDescr = "Missing credentials";
        } else {
            try {
                // Run the HTTP request login
                String jsonResponse = null;

                String content = genInputJson(SITEID_PIN, config, 0);
                // logger.debug("jsonSent = {}", content);

                InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
                HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.SITE_URL), null, stream,
                        "application/json", 20000);

                // HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.OVERVIEWURL), null, null,
                // 20000);

                jsonResponse = HttpUtil.executeUrl("POST", (String) config.get(RiscoCloudBindingConstants.REST_URL),
                        null, null, 20000);

                // logger.debug("doConnect() : apiResponse = {}", jsonResponse);
                // Map the JSON response to an object
                newServerDatasHandler = gson.fromJson(jsonResponse, ServerDatasHandler.class);
                if (newServerDatasHandler.getError() != 0) {
                    riscoCloudLoginResponse.error = "Login refused";
                    riscoCloudLoginResponse.errorDetail = "" + newServerDatasHandler.getError();
                    riscoCloudLoginResponse.statusDescr = "Error : possible invalid credentials";
                } else {
                }

            } catch (IllegalArgumentException e) {
                riscoCloudLoginResponse.errorDetail = e.getMessage();
                riscoCloudLoginResponse.statusDescr = "@text/offline.uri-error";
            } catch (IOException e) {
                riscoCloudLoginResponse.error += "Connection error to " + config.get(RiscoCloudBindingConstants.SITE_URL);
                riscoCloudLoginResponse.statusDescr = "@text/offline.uri-error";
            }

        }

        return riscoCloudLoginResponse;
    }

    private static String genInputJson(String typeJson, Configuration config, int idPart) {
        String jsonGenerated = "{}";
        if (typeJson.equals(USER_PASS)) {
            jsonGenerated = "{'username' : '" + config.get(RiscoCloudBindingConstants.USERNAME) + "',"
                    + "'password' : '" + config.get(RiscoCloudBindingConstants.WEBPASS) + "'}";
        } else if (typeJson.equals(SITEID_PIN)) {
            jsonGenerated = "{'SelectedSiteId' : '" + config.get(RiscoCloudBindingConstants.SITE_ID) + "',"
                    + "'Pin' : '" + config.get(RiscoCloudBindingConstants.PINCODE) + "'}";
        } else if (typeJson.equals(POLL)) {
            jsonGenerated = "{'IsAlive' : 'true'}";
        } else if (typeJson.equals(ARM_FULL)) {
            jsonGenerated = "{'type' : '" + idPart + ":armed', 'passcode' : '"
                    + config.get(RiscoCloudBindingConstants.PINCODE) + "', 'bypassZoneId' : -1 }";
        } else if (typeJson.equals(ARM_PART)) {
            jsonGenerated = "{'type' : '" + idPart + ":partially', 'passcode' : '"
                    + config.get(RiscoCloudBindingConstants.PINCODE) + "', 'bypassZoneId' : -1 }";
        } else if (typeJson.equals(DISARM)) {
            jsonGenerated = "{'type' : '" + idPart + ":disarmed', 'passcode' : '"
                    + config.get(RiscoCloudBindingConstants.PINCODE) + "', 'bypassZoneId' : -1 }";
        }
        return jsonGenerated;
    }

    private static String jsonBuilder(String jsonToFill, String property, String jsonFrom) {
        // logger.debug("jsonBuilder() : before : jsonToFill = {} : property = {} : jsonFrom = {}", jsonToFill,
        // property,jsonFrom);
        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonValueObject = parser.parse(jsonFrom).getAsJsonObject();
            JsonElement jsonElement = jsonValueObject.get(property);
            JsonObject jsonToFillObject = null;
            // logger.debug("jsonBuilder() : jsonValueObject = {}", jsonValueObject);
            if (jsonValueObject.has(property) && !jsonElement.isJsonNull()) {
                jsonToFillObject = parser.parse(jsonToFill).getAsJsonObject();
                jsonToFillObject.add(property, jsonElement);
                // logger.debug("jsonBuilder() : jsonToFillObject = {}", jsonToFillObject);
                jsonToFill = jsonToFillObject.toString();
            }
        } catch (Exception e) {
            logger.debug("got exception in jsonBuilder()", e);
        }
        // logger.debug("jsonBuilder() : after : jsonToFill = {} : property = {} : jsonFrom = {}", jsonToFill,
        // property,jsonFrom);
        return jsonToFill;
    }
}
