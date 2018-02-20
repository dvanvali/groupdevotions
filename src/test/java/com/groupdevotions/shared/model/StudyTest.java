package com.groupdevotions.shared.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by DanV on 12/1/2015.
 */
public class StudyTest {
    private Study study = new Study();

    @Before
    public void setUp() {
        study.studyLessonInfos.add(buildStudyLessonInfo(1, 1));
        study.studyLessonInfos.add(buildStudyLessonInfo(1, 2));
        study.studyLessonInfos.add(buildStudyLessonInfo(1, 3));
        study.studyLessonInfos.add(buildStudyLessonInfo(1, 4));
        study.studyLessonInfos.add(buildStudyLessonInfo(2, 1));
        study.studyLessonInfos.add(buildStudyLessonInfo(2, 2));
        study.studyLessonInfos.add(buildStudyLessonInfo(2, 3));
        study.studyLessonInfos.add(buildStudyLessonInfo(2, 4));
        study.studyLessonInfos.add(buildStudyLessonInfo(3, 1));
        study.studyLessonInfos.add(buildStudyLessonInfo(3, 3));
        study.studyLessonInfos.add(buildStudyLessonInfo(3, 4));
    }

    @Test
    public void testFindStudyLessonInfoIndex_start() throws Exception {
        Integer result = 0;
        assertEquals(result, study.findStudyLessonInfoIndex(buildStudyLesson(0, 5)));
    }

    @Test
    public void testFindStudyLessonInfoIndex_endOfMiddleMonth() throws Exception {
        Integer result = 4;
        assertEquals(result, study.findStudyLessonInfoIndex(buildStudyLesson(1, 5)));
    }

    @Test
    public void testFindStudyLessonInfoIndex_middleOfMonth() throws Exception {
        Integer result = 9;
        assertEquals(result, study.findStudyLessonInfoIndex(buildStudyLesson(3, 2)));
    }

    @Test
    public void testFindStudyLessonInfoIndex_overWrite() throws Exception {
        Integer result = 6;
        assertEquals(result, study.findStudyLessonInfoIndex(buildStudyLesson(2, 3)));
    }

    @Test
    public void testFindStudyLessonInfoIndex_veryEndSameMonth() throws Exception {
        Integer result = 11;
        assertEquals(result, study.findStudyLessonInfoIndex(buildStudyLesson(3, 5)));
    }

    @Test
    public void testFindStudyLessonInfoIndex_veryEndDifferentMonth() throws Exception {
        Integer result = 11;
        assertEquals(result, study.findStudyLessonInfoIndex(buildStudyLesson(4, 5)));
    }

    private StudyLessonInfo buildStudyLessonInfo(int month, int day) {
        StudyLessonInfo studyLessonInfo = new StudyLessonInfo();
        studyLessonInfo.month = month;
        studyLessonInfo.day = day;
        studyLessonInfo.studyLessonKey = month + "-" + day;
        return studyLessonInfo;
    }

    private StudyLesson buildStudyLesson(int month, int day) {
        StudyLesson studyLesson = new StudyLesson();
        studyLesson.month = month;
        studyLesson.day = day;
        studyLesson.key = month + "-" + day;
        return studyLesson;
    }
}