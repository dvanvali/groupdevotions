package com.groupdevotions.server.service;

/**
 * Navigate to relative lesson index (if lessonIndexRelativeToToday is 0 then it is today)
 * direction 0=reset back to today, 1=next, -1=previous, 2=absolute index to group study infos
 */
public enum StudyLessonNavigation {
    TODAY(0),
    NEXT(1),
    PREVIOUS(-1),
    INDEX(2);

    private int direction;

    StudyLessonNavigation(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }
}
