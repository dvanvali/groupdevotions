package com.groupdevotions.server;

import org.apache.commons.codec.binary.Base64;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Note that gwt imports will cause a failure to load source.  Gwt only makes sense in the browser.
 */
public class ServerUtils {
	private static final String GROUP_TIMEZONE = "EST5EDT";
	private static final String DEFAULT_TIMEZONE = "GMT-4";
	protected static final Logger logger = Logger
			.getLogger(ServerUtils.class.getName());

	public static String todayDateAsString() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		formatter.setTimeZone(TimeZone.getTimeZone(GROUP_TIMEZONE));
		return formatter.format(cal.getTime());
	}	
	
	public static Calendar todayForGroup() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(GROUP_TIMEZONE));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	public static String yesterdayDateAsString() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		formatter.setTimeZone(TimeZone.getTimeZone(GROUP_TIMEZONE));
		return new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
	}	
	
	public static int daysInThePast(Date date) {
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        Calendar lesson = Calendar.getInstance(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
        lesson.setTime(date);
		return (int) ((today.getTime().getTime() - lesson.getTime().getTime())/(1000*60*60*24L));
	}
	
	public static String formatDatetimeForDisplay(Date date) {
		String dateInStringFormat = "";
		Date lastWeek = new Date();
		lastWeek.setTime(lastWeek.getTime()-7L*24L*60L*60L*1000L);
		SimpleDateFormat formatter;
		if (lastWeek.after(date)) {
			formatter = new SimpleDateFormat("MMMMM d, yyyy h:mm a");
		} else {
			formatter = new SimpleDateFormat("E, h:mm a");
		}
		formatter.setTimeZone(TimeZone.getTimeZone(GROUP_TIMEZONE));
		
		dateInStringFormat = formatter.format(date);
		return dateInStringFormat;
	}

	public static String formatDate(Date date) {
		return formatDate(date, "MM/dd/yyyy");
	}

	public static String formatDate(Date date, String format) {
		String formattedDate = "";
		if (date != null) {
			SimpleDateFormat formatter = new SimpleDateFormat(format);
			formatter.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
			formattedDate = formatter.format(date);
		}
		return formattedDate;
	}

	public static String formatDateDisplayForTitle(Date date) {
		String formattedDate = "";
		Date today = removeTime(new Date());
		Date yesterday = dateMinusOneDay(today);
		Date fakeJournalDate = fakeJournalDate();
		if (today.equals(date)) {
			formattedDate = "Today";
		} else if (yesterday.equals(date)) {
			formattedDate = "Yesterday";
		} else if (fakeJournalDate.equals(date)) {
			formattedDate = "Goals/Reminders";
		} else if (date != null) {
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
			formatter.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
		    formattedDate = formatter.format(date);
		}
		return formattedDate;
	}
	
	public static String formatFullDateTime(Date date) {
		String formattedDate = "";
		if (date != null) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");
			formatter.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
		    formattedDate = formatter.format(date);
		}
		return formattedDate;
	}
	
	public static Date fakeJournalDate() {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-4"));
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.YEAR, 2050);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static Date parseFullDateTime(String date) {
		Date parsedDate = null;
		if (date != null) {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");
				formatter.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
				parsedDate = formatter.parse(date);
			} catch (Exception e) {
				
			}
		}
		return parsedDate;
	}

	public static Date normalizeDate(Date date) {
		// a date coming from a browser - could have any time
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
		cal.clear();
		cal.set(date.getYear()+1900, date.getMonth(), date.getDate());
		return cal.getTime();
	}
	
	public static Date removeTime(Date date) {
		// todo - make timezone configurable by group
		// This really does not work because the time is actually still there, just shifted by the timezone.
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}	
	
	public static Date dateMinusOneDay(Date date) {
		return dateAddDays(date, -1);
	}

	public static Date dateAddDays(Date date, int days) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}
	
	public static Date dateAddMinutes(Date date, int minutes) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
		cal.setTime(date);
		cal.add(Calendar.MINUTE, minutes);
		return cal.getTime();
	}
	
	public static boolean sendEmail(String email, String subject, String body) {
		boolean emailSent = false;
		try { 
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("groupdevotions.noreply@gmail.com", "Group Devotions (Do not reply)"));
			message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
			message.setSubject(subject);
			message.setText(body);
			logger.log(Level.INFO, "Email body: " + body);
			Transport.send(message);
			emailSent = true;
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, "Unable to send create account email for " + email, e);
		} catch (MessagingException e) {
			logger.log(Level.WARNING, "Unable to send create account email for " + email, e);
		}
		return emailSent;
	}
	
	public static boolean isGoogleUser(String userId) {
		return userId != null && !userId.startsWith("apx");
	}
	
	public static String hashPassword(String salt, String password) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
			byte[] byteHash = messageDigest.digest((password + salt).getBytes());
			password = new String(Base64.encodeBase64String(byteHash));
			// Version 1 replaced the standard /+ with _$, so convert for backwards compatibility
			password = password.replace("/", "_").replace("+", "$");
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.WARNING, "SHA-512 is missing", e);
		}
		return password;
	}
	
	public static String urlDecode(String url) {
		try {
			return URLDecoder.decode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLDecoder.decode(url);
		}
	}
	
	public static String urlEncode(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(url);
		}
		
	}

	public static String preserveWhitespace(String content) {
		if (content != null) {
			return content.trim().replace("\n", "<br/>").replace("  ", "&nbsp ");
		} else {
			return "";
		}
	}

	public static Date turnIntoTodayOrDayPast(String monthDay) {
		// Can't be a future date...
		int month = Integer.valueOf(monthDay.substring(0, 2));
		int day = Integer.valueOf(monthDay.substring(3));
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
		int thisYear = cal.get(Calendar.YEAR);
		cal.clear();
		cal.set(thisYear, month-1, day);
		if (cal.getTime().after(normalizeDate(new Date()))) {
			cal.set(thisYear-1, month-1, day);
		}
		return cal.getTime();
	}
}
