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

import com.javierllorente.jgettext.POElement;
import com.javierllorente.jgettext.TranslationElement;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;

/**
 * FXML Controller class
 *
 * @author javier
 */
public class TranslationAreaController implements Initializable {

    @FXML
    private Tab translationTab;

    @FXML
    private TextArea translationTextArea;

    @FXML
    private TextArea sourceTextArea;

    private BooleanProperty translationChangedProperty;

    public TranslationAreaController() {
    }

    public TranslationAreaController(TabPane pane) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("TranslationArea.fxml"));
        loader.setController(this);
        try {
            pane.getTabs().add(loader.load());
        } catch (IOException ex) {
            Logger.getLogger(TranslationAreaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        translationChangedProperty = new SimpleBooleanProperty(false);
        translationTextArea.scrollTopProperty().bindBidirectional(sourceTextArea.scrollTopProperty());
        translationTextArea.setOnKeyTyped((KeyEvent t) -> {
            translationChangedProperty.set(true);
        });
        translationTextArea.editableProperty().bind(sourceTextArea.textProperty().isNotEmpty());
    }

    public void setTitle(String title) {
        translationTab.setText(title);
    }

    public void addSourceEntry(String line) {
        sourceTextArea.appendText(line);
    }

    public void setSource(List<String> source) {
        addEntries(source, sourceTextArea);
    }

    public void addTranslationEntry(String line) {
        translationTextArea.appendText(line);
        translationChangedProperty.set(false);
    }

    public void setTranslation(List<String> translation) {
        addEntries(translation, translationTextArea);
        translationChangedProperty.set(false);
    }

    private void addEntries(List<String> entries, TextArea textArea) {
        textArea.clear();
        Platform.runLater(() -> {
            entries.forEach(entry -> {
                textArea.appendText(entry);
            });
        });
    }

    public TranslationElement getTranslation() {
        List<String> paragraphs = new ArrayList<>(Arrays
                .asList(translationTextArea.getText().split("\n")));
        if (paragraphs.size() > 1) {
            paragraphs.add(0, "");
        }
        TranslationElement element = new POElement();
        element.set(paragraphs);
        return element;
    }

    public void clearTranslation() {
        translationTextArea.clear();
    }

    public void clearSource() {
        sourceTextArea.clear();
    }

    public void clear() {
        clearSource();
        clearTranslation();
    }

    public BooleanProperty translationChangedProperty() {
        return translationChangedProperty;
    }
    
}
