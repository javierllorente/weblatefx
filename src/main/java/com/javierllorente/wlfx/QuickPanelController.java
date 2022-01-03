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
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;

/**
 *
 * @author javier
 */
public class QuickPanelController implements Initializable {
    
    private int quickTableIndex;
    private ObservableList<TranslationEntry> quickTableData;
    private FilteredList<TranslationEntry> quickTableFilteredData;
    private IntegerProperty entryIndexProperty;
    
    @FXML
    private TableView<TranslationEntry> quickTable;
    
    @FXML
    private TableColumn<Integer, String> entryColumn;
    
    @FXML
    private TableColumn<TranslationEntry, String> sourceColumn;
    
    @FXML
    private TableColumn<TranslationEntry, String> targetColumn;
    
    @FXML
    private TextField quickFilter;
    
    @FXML
    private ChoiceBox quickChoice;
    
    @FXML
    private TextArea metadataTextArea;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        entryIndexProperty = new ReadOnlyIntegerWrapper();
        quickTableIndex = 0;
        setupTable();
    }
    
    private void setupTable() {        
        quickTableData = FXCollections.observableArrayList();
        quickTableFilteredData = new FilteredList<>(quickTableData, f -> true);
        
        SortedList<TranslationEntry> quickTableSortedData = new SortedList<>(quickTableFilteredData);
        quickTableSortedData.comparatorProperty().bind(quickTable.comparatorProperty());
        quickTable.setItems(quickTableSortedData);
        
        quickTable.setOnSort((t) -> {
            quickTableIndex = quickTable.getSelectionModel().getSelectedIndex();
            quickTable.scrollTo(quickTableIndex);
        });
        
        quickTable.setRowFactory((p) -> {
            TableRow<TranslationEntry> row = new TableRow<>();
            row.setOnMouseClicked((t) -> {
                if (t.getButton() == MouseButton.PRIMARY) {
                    quickTableIndex = row.getIndex();
                    entryIndexProperty.set(entryIndexToInt(p, row.getIndex()));
                }
            });
            return row;
        });
        
        entryColumn.setCellValueFactory((p) -> {
           return new ReadOnlyObjectWrapper(quickTableData.indexOf(p.getValue()) + "");
        });

        sourceColumn.setCellValueFactory((p) -> {
            return new ReadOnlyObjectWrapper(
                    p.getValue().isPlural()
                    ? getMessageDisplayString(p.getValue().getMsgId())
                    + ", "
                    + getMessageDisplayString(p.getValue().getMsgIdPluralElement().get())
                    : getMessageDisplayString(p.getValue().getMsgId()));
        });

        targetColumn.setCellValueFactory((p) -> {
            TranslationEntry entry = p.getValue();

            return new SimpleStringProperty(
                    entry.isPlural()
                    ? getElementsDisplayString(entry.getMsgStrElements())
                    : getMessageDisplayString(entry.getMsgStr()));
        });
        
        ChangeListener<String> quickFilterListener = ((ov, oldValue, newValue) -> {
            
            if (!oldValue.equals(newValue)) {
                quickTableIndex = -1;
            }
            
            quickTableFilteredData.setPredicate((f) -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String entry;
                
                // Filter by source/target
                if (quickChoice.getSelectionModel()
                        .selectedIndexProperty().get() == 0) {
                    entry = f.isPlural()
                            ? getMessageDisplayString(f.getMsgId())
                            + ", " + getMessageDisplayString(f
                                    .getMsgIdPluralElement().get())
                            : getMessageDisplayString(f.getMsgId());
                } else {                    
                    entry = f.isPlural() 
                            ? getElementsDisplayString(f.getMsgStrElements()) 
                            : getMessageDisplayString(f.getMsgStr());  
                }                

                String lowerCaseFilter = newValue.toLowerCase();
                return entry.toLowerCase().contains(lowerCaseFilter);
           });
        });
        
        quickFilter.textProperty().addListener(quickFilterListener);
        
        quickChoice.setItems(FXCollections.observableArrayList("Source", "Target"));
        quickChoice.getSelectionModel().select(1);
        quickChoice.setOnAction((t) -> {
            quickFilterListener.changed(quickFilter.textProperty(), "",
                    quickFilter.textProperty().get());
        });
    }
       
    private String getMessageDisplayString(List<String> msg) {
        return String.join(", ", msg).replace("\n", "").replaceFirst(", ", "");
    }

    private String getElementsDisplayString(List<TranslationElement> elements) {
        return elements
                .stream().map(Object::toString)
                .collect(Collectors.joining(", "))
                .replace("\n", "").replaceAll("msgstr\\[\\d\\]", "");
    }
    
    private int entryIndexToInt(TableView<TranslationEntry> table, 
            int index) {
        return Integer.parseInt((String) table.getColumns().get(0).getCellData(index));
    }
    
    public int incrementTableIndex() {
        return entryIndexToInt(quickTable, ++quickTableIndex);
    }
    
    public int decrementTableIndex() {
        return entryIndexToInt(quickTable, --quickTableIndex);
    }
    
    public IntegerProperty entryIndexProperty() {
        return entryIndexProperty;
    }

    public int getQuickTableIndex() {
        return quickTableIndex;
    }
    
    public void selectAndScroll() {
        if (!quickTable.getSelectionModel().isSelected(quickTableIndex)) {
            quickTable.getSelectionModel().select(quickTableIndex);
            quickTable.scrollTo(quickTableIndex);
        }
    }
    
    public void clearMetadata() {
        metadataTextArea.clear();
    }
    
    public void addMetadata(List<String> metadata) {
        metadata.forEach((t) -> {
            metadataTextArea.appendText(t
                    .replaceAll("^#(\\.|\\:|\\,)\\s", "") + "\n");
        });
    }
    
    public void clearTableData() {
        quickTableData.clear();
    }
    
    public void addTableData(List<TranslationEntry> entries) {
        quickTableData.addAll(entries);
    }
    
    public void updateTableEntry(int index, TranslationEntry entry) {
        quickTableData.set(index, entry);
    }
    
    public boolean isTableDataEmpty() {
        return quickTableData.isEmpty();
    }
    
    public void clear() {
        quickTableData.clear();
        metadataTextArea.clear();
        quickTableIndex = 0;
    }
    
    public ReadOnlyIntegerProperty tableSelectedIndexProperty() {
        return quickTable.getSelectionModel().selectedIndexProperty();
    }
    
    public StringProperty filterTextProperty() {
        return quickFilter.textProperty();
    }
    
    public boolean isFilterIndexAtEnd() {
        return quickTable.getSelectionModel().getSelectedIndex()
                == quickTableFilteredData.size() - 1;
    }
    
}
