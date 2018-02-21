package com.groupdevotions.shared.model;

import java.io.Serializable;

public enum SectionType implements Serializable {
    DIALOG("Dialog","dialog"), 
    SCRIPTURE("Scripture","scripture"),
    SCRIPTURE_TO_LOAD("Scripture To Load","scripture_to_load"),
    QUOTE("Quote","quote"),
    GROUP_QUESTION("Group Question","gr_question"),
    TEXT_QUESTION("Accountability Text Question","question"),
    YESNO_QUESTION("Accountability Yes/No Question", "ynquestion"); 
	private static final long serialVersionUID = 5495189475622229621L;
    private String description = null;
    private String style = null;
    
    SectionType() {
    }

    SectionType(String description, String style) {
    	this.description = description;
    	this.style = style;
    }
    
    public String getStyle() {
    	return this.style;
    }
    
    static public SectionType getInstance(String description) {
    	for(SectionType sectionType : SectionType.values()) {
    		if (sectionType.description.equalsIgnoreCase(description)) {
    			return sectionType;
    		}
    	}
    	throw new IllegalArgumentException("Unrecognized description: " + description);
    }

    public boolean isPrivateQuestion() {
        return SectionType.TEXT_QUESTION.equals(this) || SectionType.YESNO_QUESTION.equals(this);
    }
}
