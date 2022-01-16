/*
 * Copyright (C) 2021 Javier Llorente <javier@opensuse.org>
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
package com.javierllorente.wlfx.alert;

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

        initOwner(window.getScene().getWindow());   
        setTitle("Uncommitted changes");
        setContentText(buildContextText(actionType));
        setupButtons();
    }

    private String buildContextText(ActionType actionType) {
        String contentText = "There are uncommitted changes. Are you sure you want to ";
        String component = "";
        switch (actionType) {
            case SWITCH_PROJECT:
                component = "project";
                break;
            case SWITCH_COMPONENT:
                component = "component";
                break;
            case SWITCH_LANGUAGE:
                component = "language";
                break;
            case LOGOUT:
                contentText += "log out";
                break;
            case EXIT:
                contentText += "exit";
                break;
            default:
                contentText += "???";
        }
        if (!component.isEmpty()) {
            contentText += "switch to another " + component;
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
