/*
 * Copyright (C) 2020-2022 Javier Llorente <javier@opensuse.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.javierllorente.wlfx.dialog;

import com.javierllorente.wlfx.App;
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
        setTitle("Log in");
        setHeaderText("Log in");
        setGraphic(new FontIcon("icm-user"));

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        ButtonBar buttonBar = (ButtonBar) getDialogPane().lookup(".button-bar");
        buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_WINDOWS);        

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 40, 10, 10));

        TextField authTokenField = new TextField();
        authTokenField.setPromptText("Auth Token:");
        authTokenField.setText(preferences.get(App.AUTH_TOKEN, ""));
        authTokenField.setPrefWidth(360.0);
        gridPane.add(new Label("Auth Token:"), 0, 0);
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
