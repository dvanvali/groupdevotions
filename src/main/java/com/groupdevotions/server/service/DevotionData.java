package com.groupdevotions.server.service;

import com.groupdevotions.shared.model.BlogEntry;
import com.groupdevotions.shared.model.StudyLesson;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * The result of looking up a devotional for the devotionService
 */
public class DevotionData {
    public StudyLesson readingCompleteStudyLesson;
    public Collection<StudyLesson> studyLessons;
    public HashMap<String, List<BlogEntry>> lessonBlogMap;
    public boolean firstLesson;
    public boolean accountabilityConfigured;

    public DevotionData() {
    }

    public DevotionData(Collection<StudyLesson> studyLessons, HashMap<String, List<BlogEntry>> lessonBlogMap, boolean firstLesson) {
        this.studyLessons = studyLessons;
        this.lessonBlogMap = lessonBlogMap;
        this.firstLesson = firstLesson;
    }
}
