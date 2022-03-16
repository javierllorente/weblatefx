/*
 * Copyright (C) 2022 Javier Llorente <javier@opensuse.org>
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
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 *
 * @author javier
 */
public class ShortcutsAlert extends Alert {

    public ShortcutsAlert(Window window) {
        super(AlertType.INFORMATION);
        initOwner(window);
        getDialogPane().setPrefSize(375, 280);
        setTitle(App.getBundle().getString("shortcuts"));
        setHeaderText(App.getBundle().getString("keyboard_shortcuts"));
        
        FontIcon keyboardIcon = new FontIcon("icm-keyboard");
        keyboardIcon.setIconSize(20);
        setGraphic(keyboardIcon);
        
        TableView<Map.Entry<String, String>> tableView = createTable();        
        getDialogPane().setContent(tableView);
        setResizable(true);
    }

    private TableView<Map.Entry<String, String>> createTable() {
        TableView<Map.Entry<String, String>> tableView = new TableView<>();
        tableView.setId("shortcutsTable");
        TableColumn<Map.Entry<String, String>, String> labelColumn = new TableColumn<>();
        labelColumn.setMinWidth(260);
        labelColumn.setCellValueFactory((p) -> {
            return new ReadOnlyStringWrapper(p.getValue().getKey());
        });
        TableColumn<Map.Entry<String, String>, String> shortcutColumn = new TableColumn<>();
        shortcutColumn.setMinWidth(40);
        shortcutColumn.setCellValueFactory((p) -> {
            return new ReadOnlyStringWrapper(p.getValue().getValue());
        });
        tableView.getColumns().addAll(labelColumn, shortcutColumn);
        List<Map.Entry<String, String>> data = Arrays.asList(
                new AbstractMap.SimpleEntry<>(App.getBundle().getString("projects"), "Ctrl + 1"),
                new AbstractMap.SimpleEntry<>(App.getBundle().getString("components"), "Ctrl + 2"),
                new AbstractMap.SimpleEntry<>(App.getBundle().getString("languages"), "Ctrl + 3"),
                new AbstractMap.SimpleEntry<>(App.getBundle().getString("previous_entry"), "Ctrl + ,"),
                new AbstractMap.SimpleEntry<>(App.getBundle().getString("next_entry"), "Ctrl + ."),
                new AbstractMap.SimpleEntry<>(App.getBundle().getString("quick_search"), "Ctrl + F"),
                new AbstractMap.SimpleEntry<>(App.getBundle().getString("translate"), "Ctrl + D"),
                new AbstractMap.SimpleEntry<>(App.getBundle().getString("switch_tabs"), "Ctrl + T"),
                new AbstractMap.SimpleEntry<>(App.getBundle().getString("submit.title"), "Ctrl + S"),
                new AbstractMap.SimpleEntry<>(App.getBundle().getString("quit"), "Ctrl + Q")        
        );
        tableView.getItems().addAll(data);
        return tableView;
    }
    
}
