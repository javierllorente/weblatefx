/*
 * Copyright (C) 2021-2022 Javier Llorente <javier@opensuse.org>
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;

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
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("TranslationArea.fxml"));
                        try {
                            tabPane.getTabs().add(loader.load());
                            tac = loader.getController();
                            tac.setHistory(history);
                        } catch (IOException ex) {
                            Logger.getLogger(TranslationTabController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        tacList.add(tac);
                        propertyList.add(tac.translationChangedProperty());
                    } else {
                        tac = tacList.get(i);
                    }
                    tac.setSource(entry.getMsgIdPluralElement().get());
                }

                tac.setTitle("Plural " + (i + 1));
                tac.setTranslation(entry.getMsgStrElements().get(i).get());
            }

        } else {
            tacList.get(0).setTitle("Translation");
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
