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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author javier
 */
public class History {

    private final Map<String, List<String>> historyMap;
    private final Set<String> changesSet;
    private HistoryAdapter adapter;
    private final IntegerProperty entryIndexProperty;
    private final IntegerProperty pluralIndexProperty;
    private boolean plural;
        
    public History() {
        historyMap = new HashMap<>();
        changesSet = new HashSet<>();
        entryIndexProperty = new SimpleIntegerProperty();
        pluralIndexProperty = new SimpleIntegerProperty();
        plural = false;
    }

    public void setAdapter(HistoryAdapter adapter) {
        this.adapter = adapter;
    }
    
    public IntegerProperty entryIndexProperty() {
        return entryIndexProperty;
    }
    
    public IntegerProperty pluralIndexProperty() {
        return pluralIndexProperty;
    }

    public void setPlural(boolean plural) {
        this.plural = plural;
    }
    
    private void compare(TranslationEntry entry, List<TranslationElement> elements, int entryIndex) {
        if (entry.isPlural()) {
            for (int i = 0; i<entry.getMsgStrElements().size(); i++) {
                    compareEntries(entry.getMsgStrElements().get(i).get(), elements.get(i).get(), 
                            entryIndex + "p" + i);
            }
        } else {
            compareEntries(entry.getMsgStrElement().get(), elements.get(0).get(), 
                    Integer.toString(entryIndex));
        }
    }

    private void compareEntries(List<String> oldEntries, List<String> newEntries, String key) {     
        List<String> entries = (historyMap.get(key) == null)
                ? oldEntries
                : historyMap.get(key);

        entries.replaceAll(item -> item.replaceAll("\n", ""));
        
        boolean translationLinesChanged = !entries.equals(newEntries);
        
        if (translationLinesChanged) {
            if (historyMap.get(key) == null) {
                historyMap.put(key, new ArrayList<>(entries));
                changesSet.add(key);
            }            
        } else {
            changesSet.remove(key);
        }
    }
    
    public void check(int entryIndex) {
        if (adapter.getOldTranslations() == null || adapter.getNewTranslations() == null 
                || entryIndex == -1) {
            return;
        }

        compare(adapter.getOldTranslations().get(entryIndex), adapter.getNewTranslations(), entryIndex);
    }
    
    public void check() {
        check(entryIndexProperty.get());
    }
    
    public boolean hasTranslationChanged() {
        if (adapter.getOldTranslations() == null || adapter.getNewTranslations() == null
                || entryIndexProperty.get() == -1) {
            return false;
        }

        compare(adapter.getOldTranslations().get(entryIndexProperty.get()), 
                adapter.getNewTranslations(), entryIndexProperty.get());
        
        return !changesSet.isEmpty();
    }
    
    private String getKey() {
        String key = Integer.toString(entryIndexProperty().get());
        if (plural) {
            key += "p" + pluralIndexProperty.get();
        }
        return key;
    }
    
    public List<String> get() {
        return historyMap.get(getKey());
    }
        
    public void clear() {
        historyMap.clear();
        changesSet.clear();
    }
        
}
