/*
 * Copyright (C) 2021-2022 Javier Llorente <javier@opensuse.org>
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

import com.javierllorente.wlfx.App;
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
