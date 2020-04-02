package com.jcloisterzone.ui.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

import com.jcloisterzone.ui.FxClient;
import com.jcloisterzone.ui.EventProxyUiController;
import com.jcloisterzone.ui.UIEventListener;
import com.jcloisterzone.ui.UiMixin;

public abstract class AbstractSwingUiView implements SwingUiView, UiMixin {

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        return false;
    }




    public void registerChildComponents(Container root, EventProxyUiController<?> ctrl) {
        LinkedList<Component> list = new LinkedList<>();
        list.add(root);

        while (!list.isEmpty()) {
            Component comp = list.pop();
            if (comp instanceof UIEventListener) {
                ((UIEventListener)comp).registerTo(ctrl);
            }
            if (comp instanceof Container) {
                for (Component child : ((Container)comp).getComponents()) {
                    list.push(child);
                }
            }
        }
    }

    public void unregisterChildComponents(Container root, EventProxyUiController<?> ctrl) {
        LinkedList<Component> list = new LinkedList<>();
        list.add(root);

        while (!list.isEmpty()) {
            Component comp = list.pop();
            if (comp instanceof UIEventListener) {
                ((UIEventListener)comp).unregisterFrom(ctrl);
            }
            if (comp instanceof Container) {
                for (Component child : ((Container)comp).getComponents()) {
                    list.push(child);
                }
            }
        }
    }

}
