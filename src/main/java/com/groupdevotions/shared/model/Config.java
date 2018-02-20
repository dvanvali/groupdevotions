package com.groupdevotions.shared.model;

import com.google.appengine.api.datastore.Text;
import com.google.code.twig.annotation.Index;
import com.google.code.twig.annotation.Type;

import java.io.Serializable;
import java.util.Date;

public class Config implements Serializable {
	private Config() {}
	static public Config getInstance() {return new Config();}

	private static final long serialVersionUID = 1070873756557817619L;
	@Index public String id = "config";
	@Type(Text.class) public String terms = "By using groupdevotions.com and/or groupdevotions.org owned by groupdevotions.com LLC ('the site' will refer to the website and the company that owns it), you agree to the following:\n" +
			"\n" +
			"* You will not copy or allow others to copy the contents of the site, studies or postings either electronically, via print nor will you duplicate such content by any means without prior written permission from the copyright holder(s).  Postings are private and you will not copy them or repost them by any means without written permission of the author of the posting.\n" +
			"\n" +
			"* Groups are self-monitoring and postings that reveal illegal activity may be, and likely is required by law to be reported to authorities by group leadership.  However, the site is not responsible for monitoring of postings or reporting such activity.\n" +
			"\n" +
			"* You agree to not to post explicit, offensive or threatening material on the site.\n" +
			"\n" +
			"* You will not hold the site responsible for study content or postings and you agree that the site is not responsible for the actions of any user of the site including group leaders and study providers.\n" +
			"\n" +
			"* You are free to terminate your use of the site at any time and the site is free to terminate your use of the site at any time and for any reason.\n" +
			"\n" +
			"* You agree to adhere to this agreement even after termination.\n" +
			"\n" +
			"* The site will attempt to keep all group postings private, but the site is not liable for damages or losses that may result from software defects that may reveal postings to non-group members.\n" +
			"\n" +
			"* The site will attempt to retain postings and study content for your use, but the site is not liable for damages of losses to any user of the site due to loss of data including studies and postings.\n" +
			"\n" +
          	"* The site only collects data about users for use internally by the site.  User data is not shared with third parties with the exception of aggregated statistics on usage for the use of advertising and promotion.\n" +
			"\n" +
          	"* The site is not responsible for the content of studies hosted by the site or the enforcement of copyrights.  However, groupdevotions.com LLC is committed to address copyright violations by removing content after investigation of complaints.  As such, complaints should be reported to groupdevotions.com LLC, 8521 W Eaton Hwy, Grand Ledge, MI 48837 in writing.\n";
	public Date timestamp = new Date(112, 5, 27);  // month is base 0, year example is 112=2012-1900
	public int resetPasswordExpirationMinutes = 60;
	public String siteUrl = "http://localhost:8080/";
	public int failedLoginCountBeforeLocked = 2;
	public boolean development = true;
	public String fromNoReplyEmailAddr = "groupdevotions.noreply@gmail.com";
	public String fromNoReplyEmailAddrDesc = "Group Devotions (Do not reply)";
	public String bibleOrgApiKey = "fill me in";
	public String forwardContactUsEmailsTo = "somewhere@gmail.com";
}

