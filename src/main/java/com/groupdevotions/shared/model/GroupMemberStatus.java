package com.groupdevotions.shared.model;

import java.io.Serializable;

public enum GroupMemberStatus implements Serializable {
    NONE("None"), 
    EMAILED("Emailed"), 
    JOINED("Joined"), 
    DECLINED("Declined");
	
    private static final long serialVersionUID = 6877364338857864351L;
	
    private String description = null;

    GroupMemberStatus() {
    }
    
    GroupMemberStatus(String description) {
    	this.description = description;
    }
    
    static public GroupMemberStatus getInstance(String description) {
    	for(GroupMemberStatus sectionType : GroupMemberStatus.values()) {
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
