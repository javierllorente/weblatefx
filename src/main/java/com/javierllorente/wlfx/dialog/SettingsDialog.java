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
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 *
 * @author javier
 */
public class SettingsDialog extends Dialog<Map<String, String>> {
    
    private TextField apiUriField;

    public SettingsDialog(Window window, Preferences preferences) {
        super();
        initOwner(window);
        setTitle("Settings");
        setHeaderText("Identity");
        setGraphic(new FontIcon("icm-wrench"));
        
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        ButtonBar buttonBar = (ButtonBar) getDialogPane().lookup(".button-bar");
        buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_WINDOWS);
                
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(30, 40, 30, 30));

        TextField nameField = new TextField();
        nameField.setMaxWidth(260.0);
        nameField.setText(preferences.get(App.TRANSLATOR_NAME, ""));
        
        TextField emailField = new TextField();
        emailField.setMaxWidth(260.0);
        emailField.setText(preferences.get(App.TRANSLATOR_EMAIL, ""));
        
        apiUriField = new TextField();
        apiUriField.setPrefWidth(260.0);
        apiUriField.setText(preferences.get(App.API_URI, ""));
        
        TextField authTokenField = new TextField();
        authTokenField.setPrefWidth(360.0);
        authTokenField.setText(App.getAuthTokenEncryptor().decrypt(preferences.get(App.AUTH_TOKEN, "")));
        
        CheckBox autoLoginCheckBox = new CheckBox();
        autoLoginCheckBox.setSelected(preferences.getBoolean(App.AUTOLOGIN, false));

        gridPane.add(new Label("Name:"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("Email:"), 0, 1);
        gridPane.add(emailField, 1, 1);
        gridPane.add(new Label("API URI:"), 0, 2);
        gridPane.add(apiUriField, 1, 2);
        gridPane.add(new Label("Auth Token:"), 0, 3);
        gridPane.add(authTokenField, 1, 3);
        gridPane.add(new Label("Autologin"), 0, 4);
        gridPane.add(autoLoginCheckBox, 1, 4);
        
        getDialogPane().setContent(gridPane);
        setResizable(true); // FIXME: Workaround for JavaFX 11

        Platform.runLater(() -> {
            nameField.requestFocus();
            nameField.selectEnd();
        });
        
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Map<String, String> data = new HashMap();
                data.put(App.TRANSLATOR_NAME, nameField.getText());
                data.put(App.TRANSLATOR_EMAIL, emailField.getText());
                data.put(App.API_URI, apiUriField.getText());
                data.put(App.AUTH_TOKEN, App.getAuthTokenEncryptor().encrypt(authTokenField.getText()));
                data.put(App.AUTOLOGIN, String.valueOf(autoLoginCheckBox.isSelected()));
                return data;
            }
            return null;
        });
    }
    
    public void focusApiUriField() {
        Platform.runLater(() -> {
            apiUriField.requestFocus();
            apiUriField.end();
        });
    }
    
}
