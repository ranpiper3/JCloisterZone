package com.jcloisterzone.ui;

import com.jcloisterzone.config.Config;
import com.jcloisterzone.plugin.Plugin;
import com.jcloisterzone.ui.resources.ConvenientResourceManager;
import com.jcloisterzone.ui.theme.Theme;
import com.jcloisterzone.ui.view.UiView;
import javafx.stage.Stage;

import java.util.List;

public interface UiMixin {

    default AppMenuBar getMenuBar() {
        return FxClient.getInstance().getMenuBar();
    }

    default Theme getTheme() {
        return FxClient.getInstance().getTheme();
    }

    default Config getConfig() {
        return FxClient.config;
    }

    default ConvenientResourceManager getResourceManager() { return FxClient.resourceManager; }

    default void saveConfig() {
        FxClient.getInstance().saveConfig();
    }

    default List<Plugin> getPlugins() {
        return FxClient.plugins;
    }

    default UiView getUiView() { return FxClient.getInstance().getUiView(); }

    default boolean mountView(UiView view) {
        return FxClient.getInstance().mountView(view);
    }

    default Stage getPrimaryStage() { return FxClient.getInstance().getPrimaryStage(); }

    default AudioManager getAudioManager() { return FxClient.getInstance().getAudioManger(); }
}
