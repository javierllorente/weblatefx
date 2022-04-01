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
package com.javierllorente.weblatefx;

import com.javierllorente.weblatefx.alert.ExceptionAlert;
import com.javierllorente.weblatefx.alert.UncommittedChangesAlert;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.ServerErrorException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Window;

/**
 *
 * @author javier
 */
public class SelectionPanelController implements Initializable {

    private static final Logger logger = Logger.getLogger(SelectionPanelController.class.getName());
    private String selectedProject;
    private String selectedComponent;
    private String lastComponent;    
    private BooleanProperty noSelection;
    private BooleanProperty fetchingDataProperty;
    private Window window;
    private Preferences preferences;

    @FXML
    private ListView<String> projectsListView;

    @FXML
    private ListView<String> componentsListView;

    @FXML
    private ComboBox<String> languagesComboBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {       
        noSelection = new SimpleBooleanProperty(true);
        fetchingDataProperty = new SimpleBooleanProperty(false);
        preferences = Preferences.userNodeForPackage(getClass());
        
        setupProjectListView();
        setupComponentsListView();
        setupBindings();

    }
    
    public BooleanProperty noSelectionProperty() {
        return noSelection;
    }
    
    public BooleanProperty fetchingDataProperty() {
        return fetchingDataProperty;
    }
    
    public ReadOnlyObjectProperty<String> selectedProjectProperty() {
        return projectsListView.getSelectionModel().selectedItemProperty();
    }
    
    public ReadOnlyObjectProperty<String> selectedComponentProperty() {
        return componentsListView.getSelectionModel().selectedItemProperty();
    }
    
    public ReadOnlyObjectProperty<String> selectedLanguageProperty() {
        return languagesComboBox.getSelectionModel().selectedItemProperty();
    }
        
    public ObservableList<String> getProjects() {
        return projectsListView.getItems();
    }
    
    public void setProjects(ObservableList<String> ol) {
        projectsListView.setItems(ol);
    }
    
    public void selectProject(String project) {
        projectsListView.getSelectionModel().select(project);
        projectsListView.scrollTo(project);
    }
    
    public void clearProjects() {
        projectsListView.getItems().clear();
    }
    
    public ObservableList<String> getComponents() {
        return componentsListView.getItems();
    }
    
    public void setComponents(ObservableList<String> ol) {
        componentsListView.setItems(ol);
    }
    
    public void selectComponent(String component) {
        componentsListView.getSelectionModel().select(component);
        componentsListView.scrollTo(component);
    }
    
    public void clearComponents() {
        componentsListView.getItems().clear();
    }
    
    public ObservableList<String> getLanguages() {
        return languagesComboBox.getItems();
    }
    
    public void setLanguages(ObservableList<String> ol) {
        languagesComboBox.setItems(ol);
    }
    
    public void selectLanguage(String component) {
        languagesComboBox.getSelectionModel().select(component);
    }
    
    public void clearLanguages() {
        languagesComboBox.getItems().clear();
    }
    
    public void setWindow(Window window) {
        this.window = window;
    }
    
    public void setupCellFactories(History history, BorderPane borderPane) {
        projectsListView.setCellFactory(new SelectionCellFactory(
                history, borderPane, UncommittedChangesAlert.ActionType.SWITCH_PROJECT));
        componentsListView.setCellFactory(new SelectionCellFactory(
                history, borderPane, UncommittedChangesAlert.ActionType.SWITCH_COMPONENT));
        languagesComboBox.setCellFactory(new SelectionCellFactory(
                history, borderPane, UncommittedChangesAlert.ActionType.SWITCH_LANGUAGE));
    }

    private void setupProjectListView() {
        ObservableList<String> components = FXCollections.observableArrayList();
        componentsListView.setItems(components);

        projectsListView.getSelectionModel().selectedItemProperty().
                addListener((ov, t, t1) -> {
                    selectedProject = t1;
                    componentsListView.getItems().clear();
                    languagesComboBox.getItems().clear();

                    if (selectedProject == null) {
                        lastComponent = null;
                    } else {
                        logger.log(Level.INFO, "Selected project: {0}", selectedProject);

                        new Thread(() -> {
                            try {
                                fetchingDataProperty.set(true);

                                List<String> items = App.getTranslationProvider()
                                        .getComponents(selectedProject);
                                Platform.runLater(() -> {
                                    components.setAll(items);
                                    if (lastComponent != null && components.contains(lastComponent)) {
                                        componentsListView.getSelectionModel().select(lastComponent);
                                    }

                                });
                            } catch (ClientErrorException | ServerErrorException | ProcessingException ex) {
                                Logger.getLogger(SelectionPanelController.class.getName())
                                        .log(Level.SEVERE, null, ex);
                                Platform.runLater(() -> {
                                    showExceptionAlert(ex);
                                });
                            }
                            fetchingDataProperty.set(false);
                        }).start();
                    }
                });
    }

    private void setupComponentsListView() {
        ObservableList<String> languages = FXCollections.observableArrayList();
        languagesComboBox.setItems(languages);

        componentsListView.getSelectionModel().selectedItemProperty().
                addListener((ov, t, t1) -> {
                    selectedComponent = t1;
                    languagesComboBox.getItems().clear();

                    if (selectedComponent != null) {
                        lastComponent = selectedComponent;
                        logger.log(Level.INFO, "Selected component: {0}", selectedComponent);

                        new Thread(() -> {
                            try {
                                fetchingDataProperty.set(true);
                                
                                List<String> items = App.getTranslationProvider().getTranslations(
                                        selectedProject, selectedComponent.toLowerCase());
                                Collections.sort(items);
                                
                                Platform.runLater(() -> {
                                    languages.setAll(items);
                                    
                                    String translatorLanguage = preferences
                                            .get(App.TRANSLATOR_LANGUAGE, "");

                                    if (!translatorLanguage.isEmpty()) {
                                        if (languages.contains(translatorLanguage)) {
                                            languagesComboBox.getSelectionModel()
                                                    .select(translatorLanguage);
                                        } else {
                                            // Clear selection
                                            languagesComboBox.setValue(null);
                                        }
                                    }
                                });

                            } catch (ClientErrorException | ServerErrorException | ProcessingException ex) {
                                Logger.getLogger(SelectionPanelController.class.getName()).log(Level.SEVERE, null, ex);
                                Platform.runLater(() -> {
                                    showExceptionAlert(ex);
                                });
                            }
                            fetchingDataProperty.set(false);
                        }).start();
                    }
                });
    }

    private void setupBindings() {
        noSelection.bind(projectsListView.getSelectionModel().selectedItemProperty().isNull()
                .or(componentsListView.getSelectionModel().selectedItemProperty().isNull())
                .or(languagesComboBox.getSelectionModel().selectedItemProperty().isNull()));
    }
    
    private void showExceptionAlert(Throwable throwable) {
        ExceptionAlert exceptionAlert = new ExceptionAlert(window);
        exceptionAlert.setHeader(throwable.getMessage());
        exceptionAlert.setThrowable(throwable);
        exceptionAlert.showAndWait();
    }
}
