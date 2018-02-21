package com.groupdevotions.shared.model;

import java.io.Serializable;

public enum PostingNotification implements Serializable {
    NO("No"), 
    EMAIL("Email Me");
    
	private static final long serialVersionUID = 3658677536336486561L;
    
    private String description = null;
    
    PostingNotification() {
    }
    
    PostingNotification(String description) {
    	this.description = description;
    }
    
    static public PostingNotification getInstance(String description) {
    	for(PostingNotification sectionType : PostingNotification.values()) {
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
