package com.groupdevotions.server.logic;

import com.google.code.twig.ObjectDatastore;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.StudyLessonDAO;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.*;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BibleStudyLogicImpl extends BaseStudyLogicImpl {
	public BibleStudyLogicImpl(StudyLessonDAO studyLessonDAO, Study study) {
		super(studyLessonDAO, study);
	}

	protected static final Logger logger = Logger
			.getLogger(BibleStudyLogicImpl.class.getName());

	public StudyLesson readLesson(ObjectDatastore datastore, GroupMember groupMember, int relativeIndex) {
	    try {
			Date lessonDate = new Date();
			int goalIndex = goalIndex(study, groupMember);
			int todaysIndex = todaysIndex(study, groupMember, goalIndex);

			StudyLesson studyLesson = new StudyLesson();
			studyLesson.title = study.title;
			studyLesson.copyright = study.copyright;
			studyLesson.studyType = study.studyType;
			studyLesson.bibleReadingIndex = todaysIndex;
			studyLesson.studyKey = study.key;
			studyLesson.bibleReadingVersion = groupMember.bibleReadingVersion;
			studyLesson.dailyReadingStartsEachMonth = study.dailyReadingStartsEachMonth;

			populateLessonDescriptionWithDate(studyLesson, lessonDate, todaysIndex - goalIndex, relativeIndex);
			populateLessonContent(studyLesson, study, todaysIndex, goalIndex, relativeIndex);
			return studyLesson;
	    } catch (Exception e) {
	    	logger.warning(e.getMessage());
	    	return null;
	    }
	}

	private int goalIndex(Study study, GroupMember groupMember) {
		if (study.dailyReadingStartsEachMonth) {
			Calendar today = ServerUtils.todayForGroup();
			int goalIndex = today.get(Calendar.DAY_OF_MONTH) - 1;  // 6/1 will be 0
			if (goalIndex > study.dailyReadingList.length()-1) {
				goalIndex = study.dailyReadingList.length()-1;
			}
			return goalIndex;
		}
		if (!SharedUtils.isEmpty(study.dailyReadingStartingMonthDay)) {
			int goalIndex = ServerUtils.daysInThePast(ServerUtils.turnIntoTodayOrDayPast(study.dailyReadingStartingMonthDay));
			return goalIndex;
		}
		if (groupMember.lastCompletedBibleReadingIndex != null) {
			// no startdate, so use the next indexed lesson
			return Integer.valueOf(groupMember.lastCompletedBibleReadingIndex) + 1;
		}
		// No start date and no previous completed lessons
		return 0;
	}

	private int todaysIndex(Study study, GroupMember groupMember, int goalIndex) {
		if (study.dailyReadingStartsEachMonth) {
			return goalIndex;
		}
		if (groupMember.lastCompletedBibleReadingIndex == null) {
			return 0;
		}
		return Integer.valueOf(groupMember.lastCompletedBibleReadingIndex)+1;
	}

	private void populateLessonContent(StudyLesson studyLesson, Study study, int todaysIndex, int goalIndex, int relativeIndex) {
		List<StudySection> sections = new ArrayList<StudySection>();
		int lessonIndex = todaysIndex + relativeIndex;
		if (lessonIndex < 0) {
			if (study.dailyReadingStartsEachMonth) {
				Calendar relativeDate = ServerUtils.todayForGroup();
				relativeDate.add(Calendar.DATE, relativeIndex);
				lessonIndex = relativeDate.get(Calendar.DAY_OF_MONTH)-1;
			} else {
				lessonIndex = 0;
			}
		}

		String[] days = study.dailyReadingList.split("\n");
		String scriptureReferenceList = days[days.length-1];
		if (days.length > lessonIndex) {
			scriptureReferenceList = days[lessonIndex];
			if (relativeIndex == 0) {
				studyLesson.devotionPageTagLine += buildStatus(lessonIndex, goalIndex);
			}
		} else {
			studyLesson.bibleReadingComplete = true;
			studyLesson.devotionPageTagLine += " (Done with reading plan)";
		}
		Collection<String> scriptureReference = parseScriptureReferences(scriptureReferenceList);

		// Add a section per reference
		for (String scripture : scriptureReference) {
			StudySection studySection = new StudySection();
			studySection.type = SectionType.SCRIPTURE_TO_LOAD;
			studySection.content = scripture;
			sections.add(studySection);
		}

		studyLesson.studySections.addAll(sections);
	}

	private Collection<String> parseScriptureReferences(String scriptureReferenceList) {
		Collection<String> scriptureReference = new ArrayList<String>();
		// Turn Book x-y into Book x, Book x+1 ... Book y
		Pattern bookRange = Pattern.compile("(.*) (\\d*)\\-(\\d*)");
		for (String reference : scriptureReferenceList.split(";")) {
			Matcher bookMatcher = bookRange.matcher(reference.trim());
			if (bookMatcher.matches()) {
				String book = bookMatcher.group(1);
				int startChapter = Integer.parseInt(bookMatcher.group(2));
				int endChapter = Integer.parseInt(bookMatcher.group(3));
				for (int chapter = startChapter; chapter <= endChapter; chapter++) {
					scriptureReference.add(book + " " + chapter);
				}
			} else {
				scriptureReference.add(reference.trim());
			}
		}
		return scriptureReference;
	}

	protected String buildStatus(int todaysIndex, int goalIndex) {
		String status = "";
		if (!study.dailyReadingStartsEachMonth) {
			if (study.dailyReadingStartingMonthDay != null) {
				int daysAhead = todaysIndex - goalIndex;
				int days = Math.abs(daysAhead);
				if (days > 0) {
					status += " (" + days + " day";
					if (days > 1) {
						status += "s";
					}
					if (daysAhead < 0) {
						status += " behind)";
					} else {
						status += " ahead)";
					}
				}
			} else {
				status += " (Day " + (todaysIndex + 1) + ")";
			}
		}

		return status;
	}

	protected void populateLessonDescriptionWithDate(StudyLesson lesson, Date date, int delta, int relativeIndex) {
		Calendar calendar = ServerUtils.todayForGroup();
		// The date you are reading, not today's date
		calendar.setTime(ServerUtils.dateAddDays(date, delta + relativeIndex));
		String pageTagLine = "";
		String month[] = new String[] {"January","February","March","April","May","June","July","August","September","October","November","December"};
		String day[] = new String[] {"", "1st","2nd","3rd","4th","5th","6th","7th","8th","9th",
				"10th","11th","12th","13th","14th","15th","16th","17th","18th","19th",
				"20th","21st","22nd","23rd","24th","25th","26th","27th","28th","29th",
				"30th","31st"};
		pageTagLine = month[calendar.get(Calendar.MONTH)] + " " + day[calendar.get(Calendar.DAY_OF_MONTH)];

		lesson.devotionPageTagLine = "Reading for " + pageTagLine;
	}

	public void initializeGroupMember(GroupMember groupMember) {
		// Start off reading on the correct date.
		if (!SharedUtils.isEmpty(study.dailyReadingStartingMonthDay)) {
			int target = goalIndex(study, groupMember)-1;
			groupMember.lastCompletedBibleReadingIndex = String.valueOf(target < 0 ? 0 : target);
		}
		groupMember.bibleReadingVersion = "nasb";
	}
}
