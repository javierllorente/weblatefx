/*
 * Copyright (C) 2020-2022 Javier Llorente <javier@opensuse.org>
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
package com.javierllorente.wlfx.alert;

import com.javierllorente.wlfx.App;
import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

/**
 *
 * @author javier
 */
public class ExceptionAlert extends Alert {
    
    private final TextArea textArea;

    public ExceptionAlert(Window w) {
        super(AlertType.ERROR);
        initOwner(w);
        setTitle(App.getBundle().getString("exception.title"));
        setHeaderText(App.getBundle().getString("exception.header"));
        Label label = new Label(App.getBundle().getString("exception.stacktrace"));

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expandableContent = new GridPane();
        expandableContent.setMaxWidth(Double.MAX_VALUE);
        expandableContent.add(label, 0, 0);
        expandableContent.add(textArea, 0, 1);

        setResizable(true);  // FIXME: Workaround for JavaFX 11
        getDialogPane().setExpandableContent(expandableContent);
    }
    
    public void setHeader(String text) {
        setHeaderText(text);
    }
    
    public void setThrowable(Throwable throwable) {
        setContentText(throwable.getClass().getCanonicalName());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        textArea.setText(sw.toString());
    }    
}
