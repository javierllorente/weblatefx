/*
 * Copyright (C) 2020 Javier Llorente <javier@opensuse.org>
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
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Window;

/**
 *
 * @author javier
 */
public class SubmitAlert extends Alert {
    
    private final TextArea textArea;
    
    public SubmitAlert(AlertType at, Window w) {
        super(at);
        initOwner(w);
        setTitle("Submit changes");
        setContentText("Please review your changes");
        setResizable(true); // FIXME: Workaround for JavaFX 11
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        
        GridPane gridPane = new GridPane();
        gridPane.setPrefSize(700, 500);
        gridPane.add(textArea, 0, 1);
        getDialogPane().setExpandableContent(gridPane);
        getDialogPane().setExpanded(true);
    }

    public String getDiff() {
        return textArea.getText();
    }

    public void setDiff(String diff) {
        textArea.setText(diff);
    }
}
