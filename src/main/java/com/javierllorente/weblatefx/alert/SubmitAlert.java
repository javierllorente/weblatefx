/*
 * Copyright (C) 2020-2021 Javier Llorente <javier@opensuse.org>
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
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
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
        setTitle(App.getBundle().getString("submit.title"));
        setContentText(App.getBundle().getString("submit.content"));
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
        
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDefaultButton(false);
        Button cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setDefaultButton(true);        
        ButtonBar buttonBar = (ButtonBar) getDialogPane().lookup(".button-bar");
        buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_WINDOWS);
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
