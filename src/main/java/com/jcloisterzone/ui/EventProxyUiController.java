package com.jcloisterzone.ui;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.EventProxy;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.server.RemoteClient;

public class EventProxyUiController<T extends EventProxy> implements UiMixin {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final EventBus eventBus;
    private final T eventProxy;

    private InvokeInSwingUiAdapter invokeInSwingUiAdapter;

    private final List<RemoteClient> remoteClients = new ArrayList<RemoteClient>();

    public EventProxyUiController(T eventProxy) {
        this.eventProxy = eventProxy;

        eventBus = new EventBus(new EventBusExceptionHandler(getClass().getName() + " event bus"));
        eventBus.register(this);
        invokeInSwingUiAdapter = new InvokeInSwingUiAdapter(eventBus);
        eventProxy.getEventBus().register(invokeInSwingUiAdapter);
    }

    public void register(Object subscriber) {
        logger.debug(String.format("Registering %s to UI event bus", subscriber));
        eventBus.register(subscriber);
    }

    public void unregister(Object subscriber) {
        try {
            logger.debug(String.format("Unregistering %s to UI event bus", subscriber));
            eventBus.unregister(subscriber);
        } catch (IllegalArgumentException ex) {
            logger.warn("Subscriber not registered.");
        }
    }

    public T getEventProxy() {
        return eventProxy;
    }

    public List<RemoteClient> getRemoteClients() {
        return remoteClients;
    }

    protected InvokeInSwingUiAdapter getInvokeInSwingUiAdapter() {
        return invokeInSwingUiAdapter;
    }

    public Connection getConnection() {
        return FxClient.getInstance().getConnection();
    }
}
