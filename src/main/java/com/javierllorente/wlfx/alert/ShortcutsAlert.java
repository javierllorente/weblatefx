/*
 * Copyright (C) 2022 Javier Llorente <javier@opensuse.org>
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
