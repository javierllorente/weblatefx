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
package com.javierllorente.weblatefx;

import com.javierllorente.weblatefx.alert.UncommittedChangesAlert;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

/**
 *
 * @author javier
 */
public class SelectionCellFactory implements Callback<ListView<String>, ListCell<String>> {

    private final History history;
    private final Pane pane;
    private final UncommittedChangesAlert.ActionType actionType;

    public SelectionCellFactory(History history, Pane pane, UncommittedChangesAlert.ActionType actionType) {
        this.history = history;
        this.pane = pane;
        this.actionType = actionType;
    }

    @Override
    public ListCell<String> call(ListView<String> p) {
        ListCell<String> cell = new ListCell<>() {
            @Override
            protected void updateItem(String t, boolean bln) {
                super.updateItem(t, bln);
                setText(t == null ? "" : t);
            }
        };

        cell.addEventFilter(MouseEvent.MOUSE_PRESSED, (e) -> {
            if (history.hasTranslationChanged()) {
                Alert alert = new UncommittedChangesAlert(pane.getScene().getWindow(), actionType);

                alert.showAndWait().ifPresent((response) -> {
                    if (response == ButtonType.NO) {
                        e.consume();
                    }
                });
            }
        });
        return cell;
    }
    
}
