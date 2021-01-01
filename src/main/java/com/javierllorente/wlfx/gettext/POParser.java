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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author javier
 */
public class POParser implements TranslationParser {

    private enum Type {
        UNKNOWN,
        COMMENT,
        MSGCTXT,
        MSGID,
        MSGID_PLURAL,
        MSGSTR_PLURAL,
        MSGSTR,
        FUZZY,
        BLANK
    }

    private final TranslationFile poFile;    

    public POParser() {
        poFile = new POFile();
    }
    
    @Override
    public TranslationFile parse(String str) throws IOException {
        
        try (BufferedReader reader = new BufferedReader(new StringReader(str))) {
            String line;
            Type type = Type.UNKNOWN;
            List<String> comments = null;
            List<String> msgCtxt = null;
            TranslationElement msgIdElement = null;
            TranslationElement msgStrElement = null;
            List<String> fuzzyEntries = null;
            boolean msgCtxtFound = false;
            boolean msgIdFound = false;
            boolean msgIdPluralFound = false;
            boolean msgStrFound = false;
            boolean fuzzyFound = false;
            boolean commentFound = false;
            
            boolean header = true;
            boolean endOfFile = false;
            
//            System.out.println("line: ");
            // "\n" == null 
            while (!endOfFile) {

                // Do not skip last \n
                if ((line = reader.readLine()) == null) {
                    endOfFile = true;
                    line = "";
                }
                
//                System.out.println(line);
                if (line.startsWith("#~")) {
                    type = Type.FUZZY;
                    if (!fuzzyFound) {
                        fuzzyEntries = new ArrayList<>();
                        fuzzyFound = true;
                    }
                } else if(line.matches("^#(\\s*|\\.|\\:|\\,).*$")) {
                    type = Type.COMMENT;
                    if (!commentFound) {
                        comments = new ArrayList<>();
                        commentFound = true;
                    }
                } else if (line.startsWith("msgctxt")) {
                    type = Type.MSGCTXT;
                    msgCtxtFound = true;
                    msgCtxt = new ArrayList<>();
                } else if (line.startsWith("msgid_plural")) {
                    type = Type.MSGID_PLURAL;
                    msgIdPluralFound = true;
//                    msgIdElement = new POElement();
                    
                } else if (line.startsWith("msgid")) {
                    type = Type.MSGID;
                    msgIdFound = true;
                    msgIdElement = new POElement();

                } else if (line.startsWith("msgstr[")) {
                    type = Type.MSGSTR_PLURAL;
                    msgStrFound = true;                    
                    if (msgIdPluralFound) {
                        msgStrElement = new POElement();
                        msgIdPluralFound = false;
                    }
                } else if (line.startsWith("msgstr")) {
                    type = Type.MSGSTR;
                    msgStrFound = true;
                    msgStrElement = new POElement();
                    
                } else if (line.isEmpty()) {
                    type = Type.BLANK;
                }
                
                if (type.toString().toLowerCase().startsWith("msg")) {
                    line = cleanDoubleQuotes(line);
                }

                switch (type) {
                    case COMMENT:
                        comments.add(line);
                        break;
                    case MSGCTXT:
                        addMsgLine(line, msgCtxt, type);
                        break;
                    case MSGID:
                        addMsgLine(line, msgIdElement, type);
                        break;
                    case MSGID_PLURAL:
                        addMsgLine(line, msgIdElement, type);
                        break;
                    case MSGSTR_PLURAL:                        
                        addMsgLine(line, msgStrElement, type);                        
                        break;
                    case MSGSTR:
                        addMsgLine(line, msgStrElement, type);
                        break;
                    case FUZZY:
                        if (fuzzyFound) {
                            fuzzyEntries.add(line);
                        }
                        break;
                    case BLANK:
                        if (header) {
                            TranslationEntry entry = new POEntry();
                            entry.setComments(comments);

                            cleanFirstLineBreak(msgIdElement);
                            cleanFirstLineBreak(msgStrElement);
                            cleanLastLineBreak(msgIdElement);
                            cleanLastLineBreak(msgStrElement);
                            
                            entry.setMsgIdElement(msgIdElement);
                            entry.setMsgStrElement(msgStrElement);                            
                            poFile.addHeader(entry);
                            
                            header = false;
                            commentFound = false;
                            msgIdFound = false;
                            msgStrFound = false;
                        } else if (msgIdFound && msgStrFound) {
                            TranslationEntry entry = new POEntry();
                            if (commentFound) {
                                entry.setComments(comments); 
                                commentFound = false;
                            }
                            if (msgCtxtFound) {
                                cleanLastLineBreak(msgCtxt);
                                entry.setMsgCtxt(msgCtxt);
                                msgCtxtFound = false;
                            }
                            if (msgIdPluralFound) {
                                entry.setPlural(true);
                            }                            
                            
                            cleanFirstLineBreak(msgIdElement);
                            cleanFirstLineBreak(msgStrElement);
                            cleanLastLineBreak(msgIdElement);
                            cleanLastLineBreak(msgStrElement);
                            
                            entry.setMsgIdElement(msgIdElement);
                            entry.setMsgStrElement(msgStrElement);                            
                            poFile.addEntry(entry);
                            
                            msgIdFound = false;
                            msgStrFound = false;
                            msgIdPluralFound = false;
                        } else if (fuzzyFound) {
                            TranslationEntry entry = new POEntry(true);
                            entry.setFuzzy(fuzzyEntries);
                            poFile.addFuzzyEntry(entry);
                            fuzzyFound = false;
                            if (commentFound) {
                                entry.setComments(comments); 
                                commentFound = false;
                            }
                        }
                        break;
                }

            }
            System.out.println("end_line");
            System.out.println("POParser entries size: " + poFile.getEntries().size());
        }

        return poFile;
    }

    private String cleanDoubleQuotes(String line) {
        if (line.startsWith("\"")) {
            line = line.substring(1);
        } else if (line.startsWith("msg")) {
            line = line.replaceFirst("\"", "");
        }
        if (line.endsWith("\"")) {
            line = line.substring(0, line.length() - 1) + "";
        }
        return line;
    }

    private void addMsgLine(String line, List<String> msg, Type type) {  
        msg.add(line.replace(type.toString().toLowerCase() + " ", "") + "\n");
    }
    
    private void addMsgLine(String line, TranslationElement element, Type type) {

        if (line.startsWith("msg")) {           
            String[] split = line.split("\\s", 2);
            String tag = split[0];
            String splitLine = "";
            String newLine = "";
            if (split.length > 1) {
                splitLine = split[1];
                newLine = "\n";
            }
            element.addTag(tag);
            element.add(splitLine + newLine);
        } else {
            element.addTag("");
            element.add(line + "\n");
        }

    }
    
    private void cleanLastLineBreak(List<String> msg) {
        if (msg.size() > 0) {
            msg.set(msg.size() - 1, msg.get(msg.size() - 1).replaceFirst("\n$", ""));
        }
    }   
    
    private void cleanFirstLineBreak(TranslationElement element) {
        if (element.get().size() > 1 && element.get().get(0).equals("\n")) {
            element.get().set(0, "");
        }
    }
    
    private void cleanLastLineBreak(TranslationElement element) {
        if (element.get().size() > 0) {
            element.get().set(element.get().size() - 1, element.get().get(
                    element.get().size() - 1).replaceFirst("\n$", ""));
        }
    }    
}
