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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author javier
 */
public class POElement implements TranslationElement {

    private List<String> lines;
    private List<String> tags;

    public POElement() {
        lines = new ArrayList<>();
        tags = new ArrayList<>();
    }
            
    @Override
    public List<String> get() {
        return lines;
    }

    @Override
    public void set(List<String> lines) {
        this.lines = lines;
    }

    @Override
    public void add(String line) {
        lines.add(line);
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public void addTag(String tag) {
        tags.add(tag);
    }

    @Override
    public String toString() {
        String str = "";
        String line;
        
        for (int i=0; i<lines.size(); i++) {            
            str += tags.get(i);
            
            if (!tags.get(i).isEmpty()) {
                str += " ";
            }
            
            line = lines.get(i);
            
            if (line.contains("\\n")) {
                if (i == lines.size() - 1) {
                    line = line.replace("\\n", "\\n\"\n");
                } else {
                    line = line.replace("\\n", "\\n\"");
                }
            } else if (line.contains("\n")) {
                line = line.replace("\n", "\"\n");
            } else if (!line.contains("\n")) {
                line += "\"\n";            
            }

            str += "\"" + line;            
        }
        
        return str;
    }
    
}
