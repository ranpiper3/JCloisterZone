package com.jcloisterzone.ui;

import com.jcloisterzone.online.Channel;
import com.jcloisterzone.ui.panel.ChannelPanel;

public class ChannelController extends EventProxyUiController<Channel> {

    private final Channel channel;
    private ChannelPanel channelPanel;

    public ChannelController(Channel channel) {
        super(channel);
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public ChannelPanel getChannelPanel() {
        return channelPanel;
    }

    public void setChannelPanel(ChannelPanel channelPanel) {
        this.channelPanel = channelPanel;
    }
}
