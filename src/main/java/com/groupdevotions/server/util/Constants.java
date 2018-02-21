package com.groupdevotions.server.util;

public class Constants {
	public static final String securityCookieName = "security";
	public static final String defaultDateFormat = "MM-dd-yyyy hh:mm a";
	public static final String serverDateFormat = "yyyy-MM-dd HH:mm z";
	public static final String serverTimeZone = "GMT";

	public static final String anonymousUserPrefix = "anonymous-";

	public static final Integer visibleRangeStart = 0;
	public static final Integer visibleRangeLength = 50;

	public enum ManageActionType {
		CREATE, READ, UPDATE, DELETE
	};

	public static long oneDayMiliseconds = 24 * 60 * 60 * 1000;
}
