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
package com.javierllorente.wlfx;

import com.javierllorente.jgettext.TranslationElement;
import com.javierllorente.jgettext.TranslationEntry;
import java.util.List;

/**
 *
 * @author javier
 */
public class HistoryAdapter {
    
    private TranslationTabController translationTabController;
    private List<TranslationEntry> oldTranslations;

    public List<TranslationEntry> getOldTranslations() {
        return oldTranslations;
    }

    public void setOldTranslations(List<TranslationEntry> oldTranslations) {
        this.oldTranslations = oldTranslations;
    }
    
    public void setTranslationTabController(TranslationTabController translationTabController) {
        this.translationTabController = translationTabController;
    }    
    
    public List<TranslationElement> getNewTranslations() {
        return translationTabController.getTranslations();
    }

}
