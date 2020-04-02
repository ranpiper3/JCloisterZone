package com.jcloisterzone.ui.view;

import com.jcloisterzone.ui.FxClient;

public interface UiView {

    default boolean requestHide(UiView nextView) {
        return true;
    }
    default void hide(UiView nextView) {
    }

    default void onWebsocketError(Exception ex) {
        FxClient.getInstance().onUnhandledWebsocketError(ex);
    }

    default void onWebsocketClose(int code, String reason, boolean remote) {
    }
}
