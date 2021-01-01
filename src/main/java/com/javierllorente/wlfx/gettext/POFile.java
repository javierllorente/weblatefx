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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author javier
 */
public class POFile implements TranslationFile {
    List<TranslationEntry> header;
    List<TranslationEntry> entries;
    List<TranslationEntry> fuzzyEntries;

    public POFile() {
        header = new ArrayList<>();
        entries = new ArrayList<>();
        fuzzyEntries = null;
    }

    @Override
    public List<TranslationEntry> getHeader() {
        return header;
    }
    
    private String getHeader(String key) {
        String value = "";
        for (TranslationEntry entry : header) {
            for (int i = 0; i < entry.getMsgStr().size(); i++) {
                if (entry.getMsgStr().get(i).contains(key + ": ")) {
                    value = entry.getMsgStr().get(i).split(key + ": ")[1];
                }
            }
        }
        return value;
    }

    private boolean setHeader(String key, String value) {
        for (TranslationEntry entry : header) {
            for (int i = 0; i < entry.getMsgStr().size(); i++) {
                if (entry.getMsgStr().get(i).contains(key)) {
                    entry.getMsgStr().set(i, key + ": " + value + "\\n\n");
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public String getRevisionDate() {
        return getHeader("PO-Revision-Date");
    }
    
    @Override
    public boolean setRevisionDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmZ");
        return setHeader("PO-Revision-Date", ZonedDateTime.now().format(dateTimeFormatter));
    }
    
    @Override
    public String getTranslator() {
        return getHeader("Last-Translator");
    }
    
    @Override
    public boolean setTranslator(String name, String email) {
        return setHeader("Last-Translator", name + " <" + email + ">");
    }
    
    @Override
    public String getGenerator() {
        return getHeader("X-Generator");
    }
    
    @Override
    public boolean setGenerator(String generator) {
        return setHeader("X-Generator", generator);
    }
    
    @Override
    public void addHeader(TranslationEntry headerEntry) {
        header.add(headerEntry);
    }
    
    @Override
    public List<TranslationEntry> getEntries() {
        return entries;
    }
    
    @Override
    public void addEntry(TranslationEntry entry) {
        entries.add(entry);
    }
    
    @Override
    public void addFuzzyEntry(TranslationEntry fuzzyEntry) {
        if (fuzzyEntries == null) {
            fuzzyEntries = new ArrayList<>();
        }
        fuzzyEntries.add(fuzzyEntry);
    }

    @Override
    public void updateEntry(int index, List<CharSequence> newLines) {

        if (newLines.size() < entries.get(index).getMsgStr().size() - 1) {

            for (int k = entries.get(index).getMsgStr().size() - 1; k > newLines.size(); k--) {
                entries.get(index).getMsgStr().remove(k);
                entries.get(index).getMsgStrElement().getTags().remove(k);
            }
            
            if ((entries.get(index).getMsgStr().size() == 2) 
                    && (entries.get(index).getMsgStr().get(0).isEmpty())) {
                
                entries.get(index).getMsgStr().set(0, 
                        entries.get(index).getMsgStr().get(1));
                
                entries.get(index).getMsgStr().remove(1);
                entries.get(index).getMsgStrElement().getTags().remove(1);
            }
            
        } else if ((newLines.size() != 1) && newLines.size() > entries.get(index).getMsgStr().size() - 1) {

            String lineBreak = "\n";
            
            if (entries.get(index).getMsgStr().size() == 1) {
                entries.get(index).getMsgStr().set(0, "");
            }

            int lastPosition = entries.get(index).getMsgStr().size() - 1;
            if (lastPosition != 0) {
                entries.get(index).getMsgStr().set(lastPosition, 
                        entries.get(index).getMsgStr().get(lastPosition) + lineBreak);
            }
            
            for (int i = entries.get(index).getMsgStr().size() - 1; i < newLines.size(); i++) {
                if (i == newLines.size() - 1) {
                    lineBreak = "";
                }
                
                entries.get(index).getMsgStrElement().getTags().add("");
                entries.get(index).addMsgStrEntry(
                        newLines.get(i).toString() + lineBreak);
            }
            
        }
        
        String lineBreak = "\n";
        int j = 1;        
        if (newLines.size() == 1) {
            j = 0;
        }
        
        for (int i = 0; i < newLines.size(); i++) {
            if (i == newLines.size() - 1) {
                lineBreak = "";
            }

            if ((newLines.size() == 1)
                    || (j <= entries.get(index).getMsgStr().size() - 1)
                    && !(newLines.get(i).toString() + lineBreak)
                            .equals(entries.get(index).getMsgStr().get(j))) {
                
                if (j == 0 && entries.get(index).getMsgStr().size() == 1
                        || j != 0 && entries.get(index).getMsgStr().size() > 1) {
                    entries.get(index).getMsgStr().set(j,
                            newLines.get(i).toString() + lineBreak);
                }
            }

            j++;
        }
        
    }
    
    @Override
    public String toString() {
        String str = "";

        int lastHeaderIndex = header.get(0).getMsgStr().size() - 1;
        String lastHeader = header.get(0).getMsgStr().get(lastHeaderIndex);
        if (!lastHeader.endsWith("\\n\n")) {
            header.get(0).getMsgStr().set(lastHeaderIndex, lastHeader + "\n");
        }
        str += entriesToString(header);
        str = str.replaceFirst("\n$", "");

        str += entriesToString(entries);

        if (fuzzyEntries != null) {
            str += entriesToString(fuzzyEntries);
        }
        str = str.replaceFirst("\n$", "");

        return str;
    }

    private String entriesToString(List<TranslationEntry> entries) {
        String str = "";
        for (TranslationEntry entry : entries) {
            str += entry.toString() + "\n";
        }
        return str;
    }    
}
