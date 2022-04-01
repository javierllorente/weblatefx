/*
 * Copyright (C) 2020-2022 Javier Llorente <javier@opensuse.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.javierllorente.weblatefx.dialog;

import com.javierllorente.weblatefx.App;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 *
 * @author javier
 */
public class LoginDialog extends Dialog<String> {

    public LoginDialog(Window window, Preferences preferences) {
        super();
        initOwner(window);
        setTitle(App.getBundle().getString("login"));
        setHeaderText(App.getBundle().getString("login"));
        setGraphic(new FontIcon("icm-user"));

        ButtonType loginButtonType = new ButtonType(App.getBundle().getString("login"), 
                ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        ButtonBar buttonBar = (ButtonBar) getDialogPane().lookup(".button-bar");
        buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_WINDOWS);        

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 40, 10, 10));

        TextField authTokenField = new TextField();
        authTokenField.setPromptText(App.getBundle().getString("login.authtoken"));
        authTokenField.setText(App.getAuthTokenEncryptor().decrypt(preferences.get(App.AUTH_TOKEN, "")));
        authTokenField.setPrefWidth(360.0);
        gridPane.add(new Label(App.getBundle().getString("login.authtoken")), 0, 0);
        gridPane.add(authTokenField, 1, 0);

        Node loginButton = getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        authTokenField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        getDialogPane().setContent(gridPane);
        setResizable(true); // FIXME: Workaround for JavaFX 11

        Platform.runLater(() -> authTokenField.requestFocus());

        setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return authTokenField.getText();
            }
            return null;
        });
    }    
}
