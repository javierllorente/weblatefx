/*
 * Copyright (C) 2020-2021 Javier Llorente <javier@opensuse.org>
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

import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Window;

/**
 *
 * @author javier
 */
public class SubmitAlert extends Alert {
    
    private final TextFlow textFlow;
    
    public SubmitAlert(AlertType at, Window w) {
        super(at);
        initOwner(w);
        setTitle("Submit changes");
        setContentText("Please review your changes");
        setResizable(true); // FIXME: Workaround for JavaFX 11
        getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setMaxWidth(Double.MAX_VALUE);
        scrollPane.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(scrollPane, Priority.ALWAYS);
        GridPane.setHgrow(scrollPane, Priority.ALWAYS);
        
        textFlow = new TextFlow();
        textFlow.setLineSpacing(3);
        textFlow.setMaxWidth(Double.MAX_VALUE);
        textFlow.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textFlow, Priority.ALWAYS);
        GridPane.setHgrow(textFlow, Priority.ALWAYS);        
        scrollPane.setContent(textFlow);
        
        GridPane gridPane = new GridPane();
        gridPane.setPrefSize(700, 500);
        gridPane.add(scrollPane, 0, 1);
        getDialogPane().setExpandableContent(gridPane);
        getDialogPane().setExpanded(true);
    }

    public String getDiff() {
        String diff = "";
        for (Node node : textFlow.getChildren()) {
            if (node instanceof Text) {
                diff += ((Text) node).getText();
            }
        }
        return diff;
    }
    
    public void setDiff(List<String> diff) {
        Text text;
        for (String line : diff) {
            text = new Text();
            
            if (line.startsWith("+")) {
                text.setFill(Color.BLUE);
            } else if (line.startsWith("-")) {
                text.setFill(Color.RED);
            }
            
            text.setText(line + "\n");
            textFlow.getChildren().add(text);
        }       
    }
    
}
