package com.groupdevotions.server.logic;

import com.google.code.twig.ObjectDatastore;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.StudyLessonDAO;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.*;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class RssStudyLogicImpl extends BaseStudyLogicImpl {
	public RssStudyLogicImpl(StudyLessonDAO studyLessonDAO, Study study) {
		super(studyLessonDAO, study);
	}

	protected static final Logger logger = Logger
			.getLogger(RssStudyLogicImpl.class.getName());
	
	public StudyLesson readLesson(ObjectDatastore datastore, GroupMember groupMember, int relativeIndex) {
	    try {
		    URL url = new URL(study.rssUrl);
		    InputStream inStream = url.openStream();
	        SyndFeedInput input = new SyndFeedInput();
	        SyndFeed feed = input.build(new XmlReader(inStream));
	        @SuppressWarnings("unchecked")
			List<com.sun.syndication.feed.synd.SyndEntry> entries = feed.getEntries();
	        Iterator<com.sun.syndication.feed.synd.SyndEntry> itEntries = entries.iterator();
	 
	        while (itEntries.hasNext()) {
	        	SyndEntry entry = itEntries.next();
	        	if (relativeIndex == 0) {
			        StudyLesson studyLesson = new StudyLesson();
			        studyLesson.title = entry.getTitle();
			        studyLesson.author = entry.getAuthor();
			        if (SharedUtils.isEmpty(studyLesson.author)) {
			        	studyLesson.author = study.author;
			        }
			        studyLesson.studySections.addAll(parseContent(entry.getDescription().getValue()));
					if (entry.getLink() != null) {
						studyLesson.studySections.add(buildReadMoreSection(entry.getLink()));
					}
			        studyLesson.copyright = study.copyright;
	
			        populateLessonDescription(studyLesson, entry.getPublishedDate());
			        return studyLesson;
	        	}
	        	relativeIndex++;
		    }
		    return null;
	    } catch (Exception e) {
	    	logger.warning(e.getMessage());
	    	return null;
	    }
	}
	
    protected void populateLessonDescription(StudyLesson lesson, Date date) {
    	Calendar calendar = ServerUtils.todayForGroup();
    	calendar.setTime(date);
    	String pageTagLine = "";
		String month[] = new String[] {"January","February","March","April","May","June","July","August","September","October","November","December"};
		String day[] = new String[] {"", "1st","2nd","3rd","4th","5th","6th","7th","8th","9th",
				"10th","11th","12th","13th","14th","15th","16th","17th","18th","19th",
				"20th","21st","22nd","23rd","24th","25th","26th","27th","28th","29th",
				"30th","31st"};
		pageTagLine = month[calendar.get(Calendar.MONTH)] + " " + day[calendar.get(Calendar.DAY_OF_MONTH)];
		
		lesson.devotionPageTagLine = pageTagLine;
	}
    
    private List<StudySection> parseContent(String content) {
    	List<StudySection> sections = new ArrayList<StudySection>();
    	
    	String[] chunks = content.split("((</p>)|(<br>)|(<br/>)|(<br />)|(</div>))\\s*");
    	for (String chunk : chunks) {
	        StudySection studySection = new StudySection();
	        studySection.type = determineSectionType(chunk);
	        studySection.rawHtml = true;

   			chunk = chunk.substring(chunk.indexOf(">")+1);
	        studySection.content = chunk.replace("\n", "");
	        sections.add(studySection);
    	}

        return sections;
    }

	private SectionType determineSectionType(String chunk) {
		SectionType sectionType = SectionType.DIALOG;

		if (chunk.substring(0, Math.min(8, chunk.length())).contains("\"") &&
				chunk.substring(Math.max(chunk.length()-20,0)).contains("\"")) {
			sectionType = SectionType.SCRIPTURE;
		}
		if (chunk.indexOf("center;") > -1) {
			sectionType = SectionType.SCRIPTURE;
		}

		return sectionType;
	}

	private StudySection buildReadMoreSection(String link) {
		StudySection studySection = new StudySection();
		studySection.type = SectionType.DIALOG;
		studySection.rawHtml = true;
		studySection.content = "<a target='_blank' href='" + link + "'>Read more...</a>";

		return studySection;
	}
}
