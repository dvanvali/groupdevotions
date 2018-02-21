package com.groupdevotions.shared.model;

import com.google.appengine.api.datastore.Text;
import com.google.code.twig.annotation.Embedded;
import com.google.code.twig.annotation.Index;
import com.google.code.twig.annotation.Store;
import com.google.code.twig.annotation.Type;

import java.io.Serializable;
import java.util.*;

public class Study implements Serializable, KeyMirror, PostLoad  {
	private static final long serialVersionUID = -4827375778596213936L;
	@Store(false) public String key;
    public String title;
    public String author;
    @Type(Text.class) public String purpose;
    @Embedded public List<StudyLessonInfo> studyLessonInfos = new ArrayList<StudyLessonInfo>();
    public String accountabilityLessonKey;
    @Type(Text.class) public String copyright;
    public StudyType studyType;
    // todo remove by date once data is converted
    public boolean studyByDate = false;
    @Index public boolean publicStudy = false;
    public String rssUrl;
	@Type(Text.class) public String dailyReadingList;
	// Month/Day
	public String dailyReadingStartingMonthDay;
	public boolean dailyReadingStartsEachMonth;
	@Index
	public String ownerOrganizationKey;

    public Study() {
    }
    
    public void setKey(String key) {
    	this.key = key;
    }

	@Override
	public String getKey() {
		return key;
	}

	public void updateMaintainFields(Study study) {
    	this.title = study.title;
    	this.purpose = study.purpose;
    	this.copyright = study.copyright;
    	this.publicStudy = study.publicStudy;
    	this.studyType = study.studyType;
    	this.author = study.author;
    	this.rssUrl = study.rssUrl;
		this.dailyReadingList = study.dailyReadingList;
		this.dailyReadingStartingMonthDay = study.dailyReadingStartingMonthDay;
		this.dailyReadingStartsEachMonth = study.dailyReadingStartsEachMonth;
    }

	public Integer findStudyLessonInfoIndex(StudyLesson studyLesson) {
		int index = 0;
		for (StudyLessonInfo info : studyLessonInfos) {
			if (studyLesson.day != null && studyLesson.month != null) {
				if (studyLesson.month < info.month || (info.month == studyLesson.month && studyLesson.day <= info.day)) {
					return index;
				}
			}
			if (studyLesson.day == null && studyLesson.month == null && info.studyLessonKey.equals(studyLesson.key)) {
				return index;
			}
			index++;
		}
		return index;
	}

	public StudyLessonInfo findStudyLessonInfo(StudyLesson studyLesson) {
		for (StudyLessonInfo info : studyLessonInfos) {
			if (studyLesson.day != null && studyLesson.month != null) {
				if (studyLesson.month == info.month && studyLesson.day == info.day) {
					return info;
				}
			}
			if (studyLesson.day == null && studyLesson.month == null && info.studyLessonKey.equals(studyLesson.key)) {
				return info;
			}
		}
		return null;
	}

	public void addStudyLessonInfo(StudyLesson studyLesson) {
		StudyLessonInfo studyLessonInfo = new StudyLessonInfo();
		studyLessonInfo.month = studyLesson.month;
		studyLessonInfo.day = studyLesson.day;
		studyLessonInfo.studyLessonKey = studyLesson.key;
		studyLessonInfo.title = studyLesson.title;
		Integer newLessonIndex = studyLesson.studyInfoIndex;
		if (studyType.equals(StudyType.DAILY) || studyLesson.studyInfoIndex == null) {
			newLessonIndex = findStudyLessonInfoIndex(studyLesson);
		}
		if (newLessonIndex == null || newLessonIndex == -1) {
			studyLessonInfos.add(studyLessonInfo);
		} else {
			studyLessonInfos.add(newLessonIndex, studyLessonInfo);
		}
    }
    
    public void updateStudyLessonInfo(StudyLesson studyLesson) {
		Iterator<StudyLessonInfo> it = studyLessonInfos.iterator();
		while(it.hasNext()) {
			StudyLessonInfo studyLessonInfo = it.next();
			if (studyLessonInfo.studyLessonKey.equals(studyLesson.key)) {
				it.remove();
				addStudyLessonInfo(studyLesson);
				return;
			}
		}
		throw new RuntimeException("Unable to find studyLessonKey: " + studyLesson.key + " for study: " + title);
    }

	public void sortStudyLessonInfoForDaily() {
		// Bug introduced out of order lessons.  Can be removed soon...
		if (StudyType.DAILY.equals(studyType)) {
			Collections.sort(studyLessonInfos, new Comparator<StudyLessonInfo>() {
				@Override
				public int compare(StudyLessonInfo o1, StudyLessonInfo o2) {
					if (o1.month == null && o2.month != null) {
						return -1;
					}
					if (o1.month != null && o2.month == null) {
						return 1;
					}
					if (o1.month == null && o2.month == null) {
						return o1.studyLessonKey.compareTo(o2.studyLessonKey);
					}
					return Integer.compare(o1.month * 31 + o1.day, o2.month * 31 + o2.day);
				}
			});
		}
	}

    public void deleteStudyLessonInfo(String studyLessonKey) {
		for(StudyLessonInfo studyLessonInfo: studyLessonInfos) {
			if (studyLessonInfo.studyLessonKey.equals(studyLessonKey)) {
				studyLessonInfos.remove(studyLessonInfo);
				return;
			}
		}
		throw new RuntimeException("Unable to find studyLessonKey: " + studyLessonKey + " for study: " + title);
    }
    
    public boolean validStudyLessonKey(String keyToValidate) {
		for(StudyLessonInfo studyLessonInfo : studyLessonInfos) {
			if (studyLessonInfo.studyLessonKey.equals(keyToValidate)) {
				return true;
			}
		}
		return false;
    }

	@Override
	public void postLoad() {
		sortStudyLessonInfoForDaily();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Study study = (Study) o;

		if (key != null ? !key.equals(study.key) : study.key != null) return false;
		if (!title.equals(study.title)) return false;
		if (author != null ? !author.equals(study.author) : study.author != null) return false;
		return !(purpose != null ? !purpose.equals(study.purpose) : study.purpose != null);

	}

	@Override
	public int hashCode() {
		int result = key != null ? key.hashCode() : 0;
		result = 31 * result + (title != null ? title.hashCode() : 0);
		return result;
	}
}
