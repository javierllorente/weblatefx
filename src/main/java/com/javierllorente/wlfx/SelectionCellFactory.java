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
package com.javierllorente.wlfx;

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
    private final String element;

    public SelectionCellFactory(History history, Pane pane, String element) {
        this.history = history;
        this.pane = pane;
        this.element = element;
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
                Alert alert = new UncommittedChangesAlert(pane.getScene().getWindow(), element);

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
