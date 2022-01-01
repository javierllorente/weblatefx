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
    
    public UncommittedChangesAlert(Window w, String component) {

        super(AlertType.WARNING, 
                "There are uncommitted changes. Are you sure you want to " 
                + (component.equals("close") ? "exit" : "switch to another " + component)
                + "?",
                ButtonType.NO, ButtonType.YES
        );
        initOwner(w.getScene().getWindow());        
        setTitle("Uncommitted changes");
        
        setupButtons();
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
