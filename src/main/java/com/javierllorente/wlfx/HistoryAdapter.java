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
