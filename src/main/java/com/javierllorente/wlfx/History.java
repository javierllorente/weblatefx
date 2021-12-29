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

import com.javierllorente.jgettext.TranslationElement;
import com.javierllorente.jgettext.TranslationEntry;
import com.javierllorente.jgettext.TranslationFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author javier
 */
public class History {
    
    private final Map<String, List<String>> historyMap;
    private final Map<String, Boolean> changesMap;
    private TranslationFile translationFile;
    private TranslationTabController ttc;
    private final IntegerProperty entryIndexProperty;
        
    public History() {
        historyMap = new HashMap<>();
        changesMap = new HashMap<>();
        entryIndexProperty = new SimpleIntegerProperty();
    }    

    public void set(TranslationFile translationFile, TranslationTabController ttc) {
        this.translationFile = translationFile;
        this.ttc = ttc;
     }

    public IntegerProperty entryIndexProperty() {
        return entryIndexProperty;
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
                changesMap.put(key, translationLinesChanged);
            }            
        } else {
            changesMap.remove(key);
        }
    }
    
    public void check(int entryIndex) {
        if (translationFile == null || ttc == null || entryIndex == -1) {
            return;
        }

        compare(translationFile.getEntries().get(entryIndex), ttc.getTranslations(), entryIndex);
    }
    
    public boolean hasTranslationChanged() {
        if (translationFile == null || ttc == null || entryIndexProperty.get() == -1) {
            return false;
        }

        compare(translationFile.getEntries().get(entryIndexProperty.get()), ttc.getTranslations(), entryIndexProperty.get());
        
        return !changesMap.isEmpty();
    }
    
    public void clear() {
        historyMap.clear();
        changesMap.clear();
    }
        
}
