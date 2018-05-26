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

import static org.openhab.binding.myelas.internal.MyElasBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.myelas.server.handler.ServerDatasHandler;
import org.openhab.binding.myelas.server.io.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyElasHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sébastien Cantineau - Initial contribution
 */
public class MyElasHandler extends BaseThingHandler implements StatusUpdateCallback {

    private final Logger logger = LoggerFactory.getLogger(MyElasHandler.class);

    /**
     * Initial delay in s for polling job.
     */
    private static final int INITIAL_DELAY = 10; // wait for 10 seconds for all channels to be ready
    /**
     * Refresh interval which is used to poll values from the FRITZ!Box web interface (optional, defaults to 30 s)
     */
    private long refreshInterval = 30;
    /**
     * Schedule for polling
     */
    @Nullable
    private ScheduledFuture<?> pollingJob;

    private ServerHandler serverHandler;
    private ServerDatasHandler serverDatasHandler;

    public MyElasHandler(Thing thing) {
        super(thing);
    }

    /**
     * Updates thing channels.
     *
     * @param thing Thing which channels should be updated.
     * @param channelId ID of the channel to be updated.
     * @param state State to be set.
     */
    private void updateThingChannelState(Thing thing, String channelId, State state) {
        final Channel channel = thing.getChannel(channelId);
        if (channel != null) {
            updateState(channel.getUID(), state);
        } else {
            logger.warn("Channel {} in thing {} does not exist, please recreate the thing", channelId, thing.getUID());
        }
    }

    /**
     * Update all channels by getting all channels in thing.
     */
    private void updateChannels() {
        thing.getChannels().stream().map(chan -> chan.getChannelTypeUID().getId())
                .filter(chan -> ArrayUtils.contains(MYELAS_CHANNELS, chan)).forEach(chan -> updateChannel(chan));
    }

    /**
     * Update channel with correct value
     */
    private void updateChannel(String chan) {
        switch (chan) {
            case CHANNEL_OFFLINE_STATUS:
                updateThingChannelState(thing, chan,
                        this.serverDatasHandler.getIsOffline() ? OnOffType.ON : OnOffType.OFF); // should be improved by
                                                                                                // getting direct OnOff
                                                                                                // value
                break;
            case CHANNEL_ARMED_PARTS_NB:
                updateThingChannelState(thing, chan, this.serverDatasHandler.getArmedPartNb());
                break;
            case CHANNEL_DISARMED_PARTS_NB:
                updateThingChannelState(thing, chan, this.serverDatasHandler.getDisarmedPartNb());
                break;
            case CHANNEL_PARTIALLYARMED_PARTS_NB:
                updateThingChannelState(thing, chan, this.serverDatasHandler.getPartiallyArmedPartNb());
                break;
        }
    }

    @Override
    public void initialize() {
        Thing thing = this.getThing();
        logger.debug("Initializing MyElas Alarm Thing handler - Thing ID: {}.", thing.getUID());

        MyElasConfiguration config = getConfigAs(MyElasConfiguration.class);

        this.serverHandler = new ServerHandler(config, this);

        ThingBuilder thingBuilder = editThing();

        createChannel(thingBuilder, CHANNEL_OFFLINE_STATUS, "Switch");
        createChannel(thingBuilder, CHANNEL_ONGOING_ALARM, "Switch");
        createChannel(thingBuilder, CHANNEL_ARMED_PARTS_NB, "Number");
        createChannel(thingBuilder, CHANNEL_DISARMED_PARTS_NB, "Number");
        createChannel(thingBuilder, CHANNEL_PARTIALLYARMED_PARTS_NB, "Number");

        thingBuilder.withLabel(thing.getLabel());
        updateThing(thingBuilder.build());

        onUpdate();
    }

    /**
     * Check existence and create channel.
     * Should by improved with getting acceptedItem from xml configuration
     */
    private void createChannel(ThingBuilder thingBuilder, String channelName, String acceptedItem) {
        if (thing.getChannels().stream().anyMatch(chan -> chan.getChannelTypeUID().getId().equals(channelName))) {
            logger.debug("channel : " + channelName + " already exists in thing : {}.", thing.getUID());
        } else {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelName);
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelName);
            Channel channel = ChannelBuilder.create(channelUID, acceptedItem).withType(channelTypeUID).build();
            logger.debug("Adding channel : " + channelName + " to thing : {}.", thing.getUID());
            thingBuilder.withChannel(channel);
        }

    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    /**
     * Disposes
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        if (pollingJob != null && !pollingJob.isCancelled()) {
            logger.debug("stop polling job");
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    /**
     * Start the polling.
     */
    private synchronized void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("start polling job at intervall {}s", refreshInterval);
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, INITIAL_DELAY, refreshInterval, TimeUnit.SECONDS);
        } else {
            logger.debug("pollingJob active");
        }
    }

    /**
     * Polls the server.
     */
    private void poll() {
        logger.debug("polling MyElas {}", getThing().getUID());
        this.serverDatasHandler = serverHandler.getServerData();
        if (this.serverHandler.getIsConnected()) {
            updateChannels(); // will be called in callback in future release
        }
    }

    /**
     * Just logging - nothing to do.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command '{}' for channel {}", command, channelUID);
        if (command instanceof RefreshType) {
            scheduler.submit(() -> poll());
            return;
        }
    }
}
