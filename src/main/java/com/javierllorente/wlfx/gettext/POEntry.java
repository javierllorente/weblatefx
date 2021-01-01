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
public class POEntry implements TranslationEntry {
    private List<String> comments;
    private List<String> msgCtxt;
    private TranslationElement msgIdElement;
    private TranslationElement msgStrElement;
    private List<String> fuzzyEntries;
    private boolean plural;
    private enum MsgType {
        MSGCTXT,
        MSGID,
        MSGID_PLURAL,
        MSGSTR
    }

    public POEntry() {
        init();
    }
    
    public POEntry(boolean fuzzy) {
        if (fuzzy) {
            fuzzyEntries = new ArrayList<>();
        } else {
            init();
        }
    }
    
    private void init() {
        comments = new ArrayList<>();
        msgIdElement = new POElement();
        msgStrElement = new POElement();
        plural = false;
    }
    
    @Override
    public List<String> getComments() {
        return comments;
    }

    @Override
    public void setComments(List<String> comments) {
        this.comments = comments;
    }
    
    @Override
    public void addComment(String comment) {
        comments.add(comment);
    }    

    @Override
    public List<String> getMsgCtxt() {
        return msgCtxt;
    }

    @Override
    public void setMsgCtxt(List<String> msgCtxt) {
        this.msgCtxt = msgCtxt;
    }

    @Override
    public TranslationElement getMsgIdElement() {
        return msgIdElement;
    }
    
    @Override
    public void setMsgIdElement(TranslationElement msgIdElement) {
        this.msgIdElement = msgIdElement;
    }
    
    @Override
    public List<String> getMsgId() {
        return msgIdElement.get();
    }

    @Override
    public void setMsgId(List<String> msgId) {
        msgIdElement.set(msgId);
    }
    
    @Override
    public void addMsgIdEntry(String msgIdEntry) {
        msgIdElement.add(msgIdEntry);
    }
    
    @Override
    public TranslationElement getMsgStrElement() {
        return msgStrElement;
    }
    
    @Override
    public void setMsgStrElement(TranslationElement msgStrElement) {
        this.msgStrElement = msgStrElement;
    }

    @Override
    public List<String> getMsgStr() {
        return msgStrElement.get();
    }

    @Override
    public void setMsgStr(List<String> msgStr) {
        msgStrElement.set(msgStr);
    }
    
    @Override
    public void addMsgStrEntry(String msgStrEntry) {
        msgStrElement.add(msgStrEntry);
    }

    @Override
    public List<String> getFuzzy() {
        return fuzzyEntries;
    }

    @Override
    public void setFuzzy(List<String> fuzzyEntries) {
        this.fuzzyEntries = fuzzyEntries;
    }
    
    @Override
    public void addFuzzyEntry(String fuzzyEntry) {
        fuzzyEntries.add(fuzzyEntry);
    }
    
    @Override
    public boolean isPlural() {
        return plural;
    }

    @Override
    public void setPlural(boolean plural) {
        this.plural = plural;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        if (comments != null && !comments.isEmpty()) {
            sb.append(String.join("\n", comments)).append("\n");
        }

        if (msgCtxt != null) {
            appendMsg(sb, msgCtxt, MsgType.MSGCTXT);
        }
        
        if (msgIdElement != null){
            sb.append(msgIdElement.toString());
        }
        
        if (msgStrElement != null) {
            sb.append(msgStrElement.toString());
        }
        
        if (fuzzyEntries != null && !fuzzyEntries.isEmpty()) {
            sb.append(String.join("\n", fuzzyEntries)).append("\n");
        }
        
        return sb.toString();
    }
    
    private void appendMsg(StringBuilder sb, List<String> msg, MsgType type) {
        String typeStr = type.toString().toLowerCase();
        
        if (!(plural && typeStr.startsWith("msgstr"))) {
            sb.append(typeStr).append(" ");
        }
        
        if (msg.size() == 1) {
            sb.append("\"").append(msg.get(0)).append("\"\n");
        } else if (msg.isEmpty() || msg.size() > 1) {           
         
            if (!(plural && typeStr.startsWith("msgstr"))) {
                sb.append("\"\"\n");
            }
            
            for (int i=0; i<msg.size(); i++) {                
                sb.append("\"");
                
                if (msg.get(i).contains("\\n")) {
                    sb.append(msg.get(i).replace("\\n", "\\n\""));
                } else {
                    if (i == msg.size() - 1) {
                        sb.append(msg.get(i)).append("\"");
                    } else {
                        sb.append(msg.get(i).replace("\n", "\"\n"));
                    }
                }
                
                // Check last header
                if (!plural && (i == msg.size() - 1) && !msg.get(i).endsWith("\\n\n")) {
                    sb.append("\n");
                }
                
            }
        }
    }    
}
