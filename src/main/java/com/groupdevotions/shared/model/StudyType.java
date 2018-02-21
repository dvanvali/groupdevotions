package com.groupdevotions.shared.model;

import java.io.Serializable;

public enum StudyType implements Serializable {
    SERIES("Study Series","series"), 
    DAILY("Daily For One Year","daily"), 
    RSS("RSS Feed","rss"),
    BIBLE("Bible Reading Plan","bible");
    private static final long serialVersionUID = 5492189475623209633L;
    private String description = null;
    private String style = null;
    
    StudyType() {
    }

    StudyType(String description, String style) {
    	this.description = description;
    	this.style = style;
    }
    
    public String getStyle() {
    	return this.style;
    }
    
    static public StudyType getInstance(String description) {
    	for(StudyType studyType : StudyType.values()) {
    		if (studyType.description.equalsIgnoreCase(description)) {
    			return studyType;
    		}
    	}
    	throw new IllegalArgumentException("Unrecognized description: " + description);
    }


}
