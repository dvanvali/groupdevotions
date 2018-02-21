package com.groupdevotions.shared.model;

import com.google.code.twig.annotation.Store;

import java.io.Serializable;
import java.util.Date;


public class GroupMemberActivity implements Serializable {
	private static final long serialVersionUID = 83865599658355L;
	public String groupMemberKey;
	public String name;
	public Date lastDevotionDate;
	@Store(false) public String tooltip;
	@Store(false) public String image;
	
	public int getDaysSinceSeen() {
		Long millisecondsInDay = 24L * 60L * 60L * 1000L;
		Date beginningOfToday = new Date();
		beginningOfToday.setMinutes(0);
		beginningOfToday.setHours(0);
		beginningOfToday.setSeconds(0);
		int daysSinceToday = (int) (((long) beginningOfToday.getTime() - lastDevotionDate.getTime()) 
				/ millisecondsInDay);
		return daysSinceToday < 0 ? 0 : daysSinceToday;
	}

	public void populateNonStoredFields() {
		int days = getDaysSinceSeen();
		if (days == 0 || days == 1) {
			image = "OK";
			tooltip = "On in the last two days.";
		} else if (days == 2 || days == 3) {
			image = "Warning";
			tooltip = "On three or four days ago.";
		} else if (days == 4) {
			image = "Error";
			tooltip = "On five days ago.";
		}
	}
}
