package com.groupdevotions.server.util;


public class SharedUtils {
	public static String minutesToHHMM(int minutes) {
		int h = minutes / 60;
		int m = minutes % 60;
		return (h < 10 ? "0" : "") + Integer.toString(h) + ":"
				+ (m < 10 ? "0" : "") + Integer.toString(m);
	}
	
	public static boolean safeEquals(Object object1, Object object2) {
		if (object1 == null) {
			return object2 == null;
		}
		return object1.equals(object2);
	}
	
	public static String safeToLowerCase(String value) {
		if (value != null) {
			value = value.toLowerCase();
		}
		return value;
	}
	
    public static boolean isValidEmail(String value) {
        if(value == null) return false;
        
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(?:[a-zA-Z]{2,6})$";
        
        return value.matches(emailPattern);
    }
    
    public static boolean isEmpty(String value) {
    	return value == null || "".equals(value.trim());  			
    }
    
    public static String valueOrNullForEmpty(String value) {
    	if (value != null) {
    		value = value.trim();
    		if ("".equals(value)) {
    			value = null;
    		}
    	}
    	return value;
    }
    
    public static boolean validateEmails(String emails) {
    	if (emails == null || isEmpty(emails)) {
    		return true;
    	}
    	
    	for(String oneEmail : emails.split(",")) {
        	oneEmail = oneEmail.replace(" ","");
    		if (!isValidEmail(oneEmail)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public static boolean notEmptyMonthDay(Integer month, Integer day) {
    	return month != null && month > 0 && day != null && day > 0;
    }
    
	public static boolean isValidDayForMonth(int month, int day) {
		int daysPerMonth[] = {0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		return month > 0 && month < 13 && day > 0 && day <= daysPerMonth[month];
	}
}
