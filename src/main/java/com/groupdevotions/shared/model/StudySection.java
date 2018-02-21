package com.groupdevotions.shared.model;

import com.google.appengine.api.datastore.Text;
import com.google.code.twig.annotation.Embedded;
import com.google.code.twig.annotation.Store;
import com.google.code.twig.annotation.Type;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

public class StudySection implements Serializable  {
	private static final long serialVersionUID = 5107302373329313495L;
	@Type(Text.class) public String content;
	public SectionType type;
	public String creationTimestamp = new Date().toString();
	@Store(false) public boolean rawHtml = false;

	@Embedded public Collection<String> answers;
	// User's answer
	@Store(false) public String answer;

	public boolean isAccountabilityQuestion() {
		return SectionType.TEXT_QUESTION.equals(type) || SectionType.YESNO_QUESTION.equals(type);
	}
}
