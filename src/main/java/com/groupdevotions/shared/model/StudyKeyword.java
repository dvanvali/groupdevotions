package com.groupdevotions.shared.model;

import java.io.Serializable;

import com.google.code.twig.annotation.Index;

public class StudyKeyword implements Serializable  {
	private static final long serialVersionUID = 3921600676520218748L;
	@Index public String keyword;
	@Index public String studyKey;
}
