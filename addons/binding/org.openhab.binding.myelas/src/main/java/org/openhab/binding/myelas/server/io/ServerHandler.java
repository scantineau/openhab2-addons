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
package org.openhab.binding.myelas.server.io;

import static org.openhab.binding.myelas.internal.MyElasBindingConstants.*;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.myelas.internal.MyElasConfiguration;
import org.openhab.binding.myelas.internal.StatusUpdateCallback;
import org.openhab.binding.myelas.server.handler.ServerDatasHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServerHandler} is responsible for handling server connection
 *
 * @author Sébastien Cantineau - Initial contribution
 */
public class ServerHandler {

    private final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private boolean isConnected;
    private CookieStore cookieStore = new BasicCookieStore();
    private MyElasConfiguration config;
    private ServerDatasHandler serverDatasHandler;
    private StatusUpdateCallback statusUpdateCallback;

    public ServerHandler(MyElasConfiguration config, StatusUpdateCallback statusUpdateCallback) {
        this.config = config;
        this.statusUpdateCallback = statusUpdateCallback;
    }

    public boolean getIsConnected() {
        return isConnected;
    }

    public ServerDatasHandler getServerData() {
        if (this.config.getWebpass() == null) {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Please configure credentials first");
            return null;
        }

        if (!this.isConnected) {
            doConnect();
        } else {
            sendPoll();
            if (!this.isConnected) {
                // This can happen if cookie is expired or connection interrupt
                doConnect(); // Try to connect again
            }
        }
        return this.serverDatasHandler;
    }

    private void doConnect() {
        String loginJson = null;
        String content = genInputJson(USER_PASS_PIN);
        // First we have to connect to WebUI to initiate login
        initCookieSession();
        callURL_Apache(content, this.config.getWebUIUrl()); // nothing will be returned

        // Then we can get loginJson
        loginJson = callURL_Apache(content, this.config.getRestUrl());

        if (loginJson == "") {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "MyElas does not respond");
            logger.debug("unable to login and json is empty : {}", loginJson);
            return;
        } else {
            handleResponse(loginJson);
        }

    }

    private void sendPoll() {
        String pollJson = null;
        String content = genInputJson(POLL);

        pollJson = callURL_Apache(content, this.config.getRestUrl());

        if (pollJson == "") {
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "MyElas does not respond");
            this.isConnected = false;
            logger.debug("unable to login and json is empty : {}", pollJson);
            return;
        } else {
            handleResponse(pollJson);
        }

    }

    private void handleResponse(String json) {

        this.serverDatasHandler = ServerDatasHandler.createServerDatasHandler(json);

        if (this.serverDatasHandler.getError() > 0) {
            logger.debug("unable to login, error : {} complete received json : {}", this.serverDatasHandler.getError(),
                    json);
            statusUpdateCallback.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Error while connecting to MyElas");
            return;
        } else {
            logger.debug("Response ok from MyElas Server");
        }
        statusUpdateCallback.updateStatus(ThingStatus.ONLINE);
    }

    private String genInputJson(String typeJson) {
        String jsonGenerated = "{}";
        if (typeJson.equals(USER_PASS_PIN)) {
            jsonGenerated = "{'username' : '" + this.config.getUsername() + "'," + "'password' : '"
                    + this.config.getWebpass() + "'," + "'code' : '" + this.config.getPinCode() + "'}";
        }
        return jsonGenerated;
    }

    // Not working because of no cookies
    // private String callURL(String json, String url) {
    // String response = "";
    // InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    //
    // try {
    // response = HttpUtil.executeUrl("POST", url, null, stream, "application/json",
    // 10 * this.config.getSyncTimeout());
    //
    // } catch (IOException e) {
    // logger.debug("Failed to get loginJson {}", e.getLocalizedMessage(), e);
    // }
    // return response;
    // }

    private String callURL_Apache(String json, String url) {

        String response = "";
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpPost httpPost = new HttpPost(url);
        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);
        HttpResponse httpResponse;
        // ...

        try {
            httpResponse = httpClient.execute(httpPost, httpContext);
            HttpEntity entity = httpResponse.getEntity();
            response = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            logger.debug("Failed to get loginJson {}", e.getLocalizedMessage(), e);
        }
        return response;
    }

    private void initCookieSession() {
        this.cookieStore = new BasicCookieStore();
    }

}
