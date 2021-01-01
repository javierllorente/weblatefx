/*
 * Copyright (C) 2020 Javier Llorente <javier@opensuse.org>
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
package com.javierllorente.wlfx.gettext;

import java.util.List;

/**
 *
 * @author javier
 */
public interface TranslationFile {

    public List<TranslationEntry> getHeader();

    public String getRevisionDate();

    public boolean setRevisionDate();

    public String getTranslator();

    public boolean setTranslator(String name, String email);

    public String getGenerator();

    public boolean setGenerator(String generator);

    public void addHeader(TranslationEntry headerEntry);

    public List<TranslationEntry> getEntries();

    public void addEntry(TranslationEntry entry);

    public void addFuzzyEntry(TranslationEntry fuzzyEntry);

    public void updateEntry(int index, List<CharSequence> newLines);
}
