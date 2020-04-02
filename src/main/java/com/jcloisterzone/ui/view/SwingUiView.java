package com.jcloisterzone.ui.view;

import java.awt.Container;
import java.awt.event.KeyEvent;

public interface SwingUiView extends UiView {

    void show(Container pane);
    boolean dispatchKeyEvent(KeyEvent e);

}
