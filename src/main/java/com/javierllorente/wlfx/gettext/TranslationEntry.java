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
public interface TranslationEntry {

    public List<String> getComments();

    public void setComments(List<String> comments);

    public void addComment(String comment);

    public List<String> getMsgCtxt();

    public void setMsgCtxt(List<String> msgCtxt);

    public TranslationElement getMsgIdElement();

    public void setMsgIdElement(TranslationElement element);

    public List<String> getMsgId();

    public void setMsgId(List<String> msgId);

    public void addMsgIdEntry(String msgIdEntry);

    public TranslationElement getMsgStrElement();

    public void setMsgStrElement(TranslationElement element);

    public List<String> getMsgStr();

    public void setMsgStr(List<String> msgStr);

    public void addMsgStrEntry(String msgStrEntry);

    public List<String> getFuzzy();

    public void setFuzzy(List<String> fuzzyEntries);

    public void addFuzzyEntry(String fuzzyEntry);

    public boolean isPlural();

    public void setPlural(boolean plural);    
}
