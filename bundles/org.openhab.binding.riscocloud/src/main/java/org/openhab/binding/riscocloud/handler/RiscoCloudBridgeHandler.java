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
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.riscocloud.internal.RiscoCloudException;
import org.openhab.binding.riscocloud.internal.config.RiscoCloudBridgeConfiguration;
import org.openhab.binding.riscocloud.model.RiscoCloudLoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.openhab.binding.riscocloud.internal.RiscoCloudBindingConstants.RISCO_CLOUD_WEBUI_URL;

/**
 * {@link RiscoCloudBridgeHandler} is the handler for RiscoCloud API and connects it
 * to the webservice.
 *
 * @author Sebastien Cantineau - Initial contribution
 *
 */
@NonNullByDefault
public class RiscoCloudBridgeHandler extends ConfigStatusBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(RiscoCloudBridgeHandler.class);

    // Instantiate and configure the SslContextFactory
    private SslContextFactory sslContextFactory = new SslContextFactory();

    // Instantiate HttpClient with the SslContextFactory
    private HttpClient httpClient = new HttpClient(sslContextFactory);

    /**
     * Future to poll for updated
     */
    private @Nullable ScheduledFuture<?> pollFuture = null;

    /**
     * Our configuration
     */
    protected RiscoCloudBridgeConfiguration thingConfig = new RiscoCloudBridgeConfiguration();

    // Gson & parser
    private final Gson gson = new Gson();

    public RiscoCloudBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {

        logger.debug("Initializing RiscoCloud bridge handler.");
        String thingUid = getThing().getUID().toString();
        thingConfig = getConfigAs(RiscoCloudBridgeConfiguration.class);
        thingConfig.setThingUid(thingUid);

        httpClient.setFollowRedirects(false);

        scheduler.execute(() -> {
            login();
            initPolling(thingConfig.getRefresh());
            logger.debug("Initialize done...");
        });

    }

    /**
     * starts this things polling future
     */
    private void initPolling(int refresh) {
        stopPolling();
        pollFuture = scheduler.scheduleWithFixedDelay(() -> {
//            updateTahomaStates();
        }, 10, refresh, TimeUnit.SECONDS);

    }

    public synchronized void login() {
        if (StringUtils.isEmpty(thingConfig.getUsername()) || StringUtils.isEmpty(thingConfig.getWebpass())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Can not access Risco cloud as username and/or web password are null");
            return;
        }

        try {
            if (httpClient.isStarted()) {
                httpClient.stop();
            }
            httpClient.start();

            String content = "{'username' : '" + thingConfig.getUsername() + "',"
                    + "'password' : '" + thingConfig.getWebpass() + "'}";
            ContentResponse response = httpClient.newRequest(RISCO_CLOUD_WEBUI_URL)
                    .method(HttpMethod.POST)
                    .content(new StringContentProvider(content), "application/json")
                    .send();

            logger.trace("Login response: {}", response.getContentAsString());
            RiscoCloudLoginResponse data = gson.fromJson(response.getContentAsString(), RiscoCloudLoginResponse.class);
            if (data.isErrorDetected()) {
                logger.debug("Login response: {}", response.getContentAsString());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error logging in");
                throw new RiscoCloudException(response.getContentAsString());
            } else {
                logger.debug("Login successful");
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Received invalid data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Received invalid data");
        } catch (RiscoCloudException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unauthorized. Please check credentials");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Cannot get login cookie!", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot get login cookie");
        } catch (Exception e) {
            logger.debug("Cannot start http client", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot start http client");
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not needed
    }

    @Override
    public void updateStatus(ThingStatus newStatus) {
        super.updateStatus(newStatus);
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
//        logout();
    }

    public ThingUID getID() {
        return getThing().getUID();
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        return Collections.emptyList();
    }

    @Override
    public void dispose() {
        stopPolling();

        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.error("Cannot stop http client", e);
        }
    }

    /**
     * Stops this thing's polling future
     */
    private void stopPolling() {
        if (pollFuture != null && !pollFuture.isCancelled()) {
            pollFuture.cancel(true);
            pollFuture = null;
        }
    }

/*
    private void updateTahomaStates() {
        logger.debug("Updating Tahoma States...");
        if (thing.getStatus().equals(ThingStatus.OFFLINE)) {
            logger.debug("Doing relogin");
            login();
        }

        getThing().getThings().stream()
                .filter(thing1 -> thing1.getThingTypeUID().equals(THING_TYPE_SITE))
                .forEach();
        ArrayList<SomfyTahomaEvent> events = getEvents();
        logger.debug("Got total of {} events", events.size());
        for (SomfyTahomaEvent event : events) {
            String deviceUrl = event.getDeviceUrl();
            ArrayList<SomfyTahomaState> states = event.getDeviceStates();
            logger.debug("States for device {} : {}", deviceUrl, states.toString());
            Thing thing = getThingByDeviceUrl(deviceUrl);

            if (thing != null) {
                logger.debug("Updating status of thing: {}", thing.getUID().getId());
                SomfyTahomaBaseThingHandler handler = (SomfyTahomaBaseThingHandler) thing.getHandler();

                if (handler != null) {
                    // update thing status
                    handler.updateThingStatus(states);
                    handler.updateThingChannels(states);
                }
            } else {
                logger.debug("Thing handler is null, probably not bound thing.");
            }
        }

        //force update states of things which have not sent any event for a long time
        for (Thing th : getThing().getThings()) {
            updateThingStates(th);
        }
    }
*/
}
