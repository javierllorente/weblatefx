/*
 * Copyright (C) 2021-2022 Javier Llorente <javier@opensuse.org>
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
package com.javierllorente.weblatefx.alert;

import com.javierllorente.weblatefx.App;
import java.text.MessageFormat;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 *
 * @author javier
 */
public class UncommittedChangesAlert extends Alert {
    
    public enum ActionType {
        SWITCH_PROJECT, SWITCH_COMPONENT, SWITCH_LANGUAGE, LOGOUT, EXIT
    };
    
    public UncommittedChangesAlert(Window window, ActionType actionType) {

        super(AlertType.WARNING, "", ButtonType.NO, ButtonType.YES);

        initOwner(window);   
        setTitle(App.getBundle().getString("uncommittedchanges.title"));
        setContentText(buildContextText(actionType));
        setupButtons();
    }

    private String buildContextText(ActionType actionType) {
        String contentText = App.getBundle().getString("uncommittedchanges.content");
        String component = "";
        switch (actionType) {
            case SWITCH_PROJECT:
                component = App.getBundle().getString("project");
                break;
            case SWITCH_COMPONENT:
                component = App.getBundle().getString("component");
                break;
            case SWITCH_LANGUAGE:
                component = App.getBundle().getString("language");
                break;
            case LOGOUT:
                contentText += App.getBundle().getString("logout");
                break;
            case EXIT:
                contentText += App.getBundle().getString("exit");
                break;
            default:
                contentText += "???";
        }
        if (!component.isEmpty()) {
            contentText += MessageFormat.format(App.getBundle().getString("uncommittedchanges.switch {0}"), 
                    new Object[] {component});
        }
        contentText += "?";
        return contentText;
    }

    private void setupButtons() {
        Button yesButton = (Button) getDialogPane().lookupButton(ButtonType.YES);
        yesButton.setDefaultButton(false);
        Button noButton = (Button) getDialogPane().lookupButton(ButtonType.NO);
        noButton.setDefaultButton(true);

        ButtonBar buttonBar = (ButtonBar) getDialogPane().lookup(".button-bar");
        buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_WINDOWS);
    }
    
}
