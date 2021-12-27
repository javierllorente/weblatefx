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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author javier
 */
public class History {
    
    private final Map<Integer, List<String>> translationChangedMap;

    public History() {
        translationChangedMap = new HashMap<>();
    }
    
    public void compare(TranslationEntry entry, List<TranslationElement> elements, int entryIndex) {        
        if (entry.isPlural()) {
            for (TranslationElement oldElement : entry.getMsgStrElements()) {
                for (TranslationElement newElement : elements) {
                    compareEntries(oldElement.get(), newElement.get(), entryIndex);
                }
            }
        } else {
            compareEntries(entry.getMsgStrElement().get(), elements.get(0).get(), entryIndex);
        }        
    }

    private void compareEntries(List<String> oldEntries, List<String> newEntries, int entryIndex) {

        List<String> entries = (translationChangedMap.get(entryIndex) == null)
                ? oldEntries
                : translationChangedMap.get(entryIndex);
        
        boolean translationLinesChanged = !entries.equals(newEntries);
        
        if (translationLinesChanged) {
            if (translationChangedMap.get(entryIndex) == null) {
                translationChangedMap.put(entryIndex, new ArrayList<>(entries));
            }            
        } else {
            translationChangedMap.remove(entryIndex);
        }        
    }
    
    public boolean hasTranslationChanged() {
        return !translationChangedMap.isEmpty();
    }
    
    public void clear() {
        translationChangedMap.clear();
    }
        
}
