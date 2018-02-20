package com.groupdevotions.shared.model;

import java.io.Serializable;

import com.google.code.twig.annotation.Index;

public class StudyContributor implements Serializable {
	private static final long serialVersionUID = -5434039563547165139L;
	@Index public String accountKey;
    @Index public String studyKey;
    public Boolean studyAdmin;
    
    public StudyContributor() {
    }
}
