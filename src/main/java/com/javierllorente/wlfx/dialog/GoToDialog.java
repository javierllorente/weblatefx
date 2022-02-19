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
package com.javierllorente.wlfx.dialog;

import com.javierllorente.wlfx.App;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 *
 * @author javier
 */
public class GoToDialog extends Dialog<String> {

    private final ListView<String> listView;
    private final ObservableList<String> list;
    private final TextField textField;
            
    public GoToDialog(Window window, String title) {
        super();
        initOwner(window);
        getDialogPane().setPrefSize(375, 280);
        setTitle(title);
        setHeaderText(App.getBundle().getString("go_to"));
        
        FontIcon rocketIcon = new FontIcon("icm-rocket");
        rocketIcon.setIconSize(20);
        setGraphic(rocketIcon);
        
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ButtonBar buttonBar = (ButtonBar) getDialogPane().lookup(".button-bar");
        buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_WINDOWS);

        textField = new TextField();        
        Platform.runLater(() -> textField.requestFocus());

        listView = new ListView<>();
        list = FXCollections.observableArrayList();
        FilteredList<String> filteredList = new FilteredList<>(list, f -> true);
        listView.setItems(filteredList);

        filteredList.addListener((ListChangeListener)(change) -> {
            listView.getSelectionModel().selectFirst();
            listView.scrollTo(0);
        });

        ChangeListener<String> changeListener = ((ov, oldValue, newValue) -> {
            filteredList.setPredicate((f) -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                return f.toLowerCase().contains(newValue.toLowerCase());
            });
        });

        textField.textProperty().addListener(changeListener);
        
        textField.addEventHandler(KeyEvent.KEY_PRESSED, (t) -> {
            int selected = listView.getSelectionModel().getSelectedIndex();
            int last = listView.getItems().size() - 1;

            switch (t.getCode()) {
                case DOWN:
                    if (selected != last) {
                        listView.getSelectionModel().selectNext();
                    } else {
                        listView.getSelectionModel().selectFirst();
                    }
                    listView.scrollTo(listView.getSelectionModel().getSelectedIndex());
                    t.consume();
                    break;
                case UP:
                    if (selected != 0) {
                        listView.getSelectionModel().selectPrevious();
                    } else {
                        listView.getSelectionModel().selectLast();
                    }
                    listView.scrollTo(listView.getSelectionModel().getSelectedIndex());
                    t.consume();
                    break;
                case PAGE_DOWN:
                    if (selected != last) {
                        if (selected + 5 < last) {
                            listView.getSelectionModel().select(selected + 5);
                            listView.scrollTo(selected + 5);
                        } else {
                            listView.getSelectionModel().select(last);
                            listView.scrollTo(last);
                        }
                    }
                    t.consume();
                    break;
                case PAGE_UP:
                    if (selected != 0) {
                        if (selected - 5 > 0) {
                            listView.getSelectionModel().select(selected - 5);
                            listView.scrollTo(selected - 5);
                        } else {
                            listView.getSelectionModel().select(0);
                            listView.scrollTo(0);
                        }
                    }
                    t.consume();
                    break;
            }
        });

        VBox vBox = new VBox();
        vBox.getChildren().add(textField);
        vBox.getChildren().add(listView);
        getDialogPane().setContent(vBox);
        setResizable(true);
        
        setResultConverter(b -> {
            if (b == ButtonType.OK) {
                return listView.getSelectionModel().getSelectedItem();
            }            
            return null; 
        });
    }
    
    public void setData(ObservableList<String> items) {
        list.addAll(items);
    }
    
}
