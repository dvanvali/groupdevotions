package com.groupdevotions.shared.model;

import java.io.Serializable;
import java.util.Date;

import com.google.code.twig.annotation.Index;
import com.groupdevotions.server.util.SharedUtils;

public class StudyLessonInfo implements Serializable {
	private static final long serialVersionUID = -3046551140521965660L;
	@Index public String studyLessonKey;
	public String title;
    public Integer month;
    public Integer day;
    // public Date forDate;
    
    public boolean after(Integer month, Integer day) {
    	if (SharedUtils.notEmptyMonthDay(month, day) && SharedUtils.notEmptyMonthDay(this.month, this.day)) {
    		if (this.month > month) { 
    			return true;
    		}
    		if (this.month == month && this.day > day) {
    			return true;
    		}
    	}
    	return false;
    }
}
