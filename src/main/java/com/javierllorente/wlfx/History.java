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
    
    private void compare(TranslationEntry entry, List<TranslationElement> elements, int entryIndex) {
        plural = entry.isPlural();
        if (plural) {
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
