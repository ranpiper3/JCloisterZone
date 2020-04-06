package com.jcloisterzone.ui.view;

import javafx.scene.Node;

import java.io.IOException;

public interface FxUiView extends UiView {

    Node show() throws IOException;
}
