package com.groupdevotions.shared.model;

import java.io.Serializable;

public enum ScreenFormat  implements Serializable {
    DETECT("Detect"), 
    PHONE("Phone"), 
    MONITOR("Monitor");
	private static final long serialVersionUID = 68773846694864351L;
    
    private String description = null;
    
    ScreenFormat() {
    }
    
    ScreenFormat(String description) {
    	this.description = description;
    }
    
    static public ScreenFormat getInstance(String description) {
    	for(ScreenFormat sectionType : ScreenFormat.values()) {
    		if (sectionType.description.equals(description)) {
    			return sectionType;
    		}
    	}
    	throw new IllegalArgumentException("Unrecognized description: " + description);
    }
    
    public String getDescription() {
    	return description;
    }
}
