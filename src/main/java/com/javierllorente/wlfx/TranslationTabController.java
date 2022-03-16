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
package com.javierllorente.wlfx;

import com.javierllorente.jgettext.TranslationElement;
import com.javierllorente.jgettext.TranslationEntry;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 *
 * @author javier
 */
public class TranslationTabController implements Initializable {

    private static final Logger logger = Logger.getLogger(TranslationTabController.class.getName());
    private List<TranslationAreaController> tacList;
    private ReadOnlyBooleanWrapper translationChangedProperty;
    private ObservableList<BooleanProperty> propertyList;
    private BooleanBinding anyValid;
    private History history;
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    TranslationAreaController translationAreaController;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tacList = new ArrayList<>();
        translationChangedProperty = new ReadOnlyBooleanWrapper(false);
        propertyList = FXCollections.observableArrayList(
                t -> new Observable[]{t});
        anyValid = Bindings.createBooleanBinding(() -> propertyList.stream()
                .anyMatch(BooleanProperty::get), propertyList);
        translationChangedProperty.bind(anyValid);
        translationChangedProperty.addListener((o) -> {
            logger.log(Level.INFO, "translationChangedProperty value: {0}",
                    translationChangedProperty.get());
        });

        tacList.add(translationAreaController);
        propertyList.add(translationAreaController.translationChangedProperty());
    }
   
    public void setupAccelerators(ObservableMap<KeyCombination, Runnable> accelerators) {
        KeyCombination quickFilterShortcut = new KeyCodeCombination(KeyCode.D,
                KeyCombination.CONTROL_DOWN);
        accelerators.put(quickFilterShortcut, () -> {
            for (int i = 0; i < tabPane.getTabs().size(); i++) {
                if (tabPane.getTabs().get(i).isSelected()) {
                    tacList.get(i).requestTranslationTextAreaFocus();
                    return;
                }
            }
        });
        
        KeyCombination switchTabsShortcut = new KeyCodeCombination(KeyCode.T,
                KeyCombination.CONTROL_DOWN);
        accelerators.put(switchTabsShortcut, () -> {
            if (tabPane.getSelectionModel().isSelected(tabPane.getTabs().size() - 1)) {
                tabPane.getSelectionModel().selectFirst();
            } else {
                tabPane.getSelectionModel().selectNext();
            }
        });
    }

    public int size() {
        return tacList.size();
    }

    public TranslationAreaController get(int i) {
        return tacList.get(i);
    }
    
    public void clearAllButFirst() {
        tabPane.getTabs().subList(1, tabPane.getTabs().size()).clear();
        tacList.subList(1, tacList.size()).clear();
        propertyList.subList(1, propertyList.size()).clear();
    }

    public void loadTranslations(TranslationEntry entry) {
        boolean plural = entry.isPlural();
        history.setPlural(plural);
        
        if (plural) {
            logger.log(Level.INFO, "Entry is plural");
            TranslationAreaController tac = null;
            boolean moreTranslationAreasNeeded = (tacList.size() == 1);

            for (int i = 0; i < entry.getMsgStrElements().size(); i++) {

                if (i == 0) {
                    tac = tacList.get(0);
                    tac.clear();
                    tac.setSource(entry.getMsgId());
                } else {

                    if (moreTranslationAreasNeeded) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("TranslationArea.fxml"), 
                                App.getBundle());
                        try {
                            tabPane.getTabs().add(loader.load());
                            tac = loader.getController();
                            tac.setHistory(history);
                            tacList.add(tac);
                            propertyList.add(tac.translationChangedProperty());
                        } catch (IOException ex) {
                            Logger.getLogger(TranslationTabController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        tac = tacList.get(i);
                    }
                    tac.setSource(entry.getMsgIdPluralElement().get());
                }

                tac.setTitle(App.getBundle().getString("plural") + (i + 1));
                tac.setTranslation(entry.getMsgStrElements().get(i).get());
            }

        } else {
            tacList.get(0).setTitle(App.getBundle().getString("translation"));
            tacList.get(0).setSource(entry.getMsgId());
            tacList.get(0).setTranslation(entry.getMsgStr());
        }
    }

    public void setHistory(History history) {
        this.history = history;
        history.pluralIndexProperty().bind(tabPane.getSelectionModel().selectedIndexProperty());
        translationAreaController.setHistory(history);
    }

    public List<TranslationElement> getTranslations() {
        List<TranslationElement> elements = new ArrayList<>();
        tacList.forEach((t) -> {
            elements.add(t.getTranslation());
        });
        return elements;
    }

    public void clearTranslationAreas() {
        for (TranslationAreaController tac : tacList) {
            tac.clear();
        }
    }

    public ReadOnlyBooleanProperty translationChangedProperty() {
        return translationChangedProperty.getReadOnlyProperty();
    }

}
