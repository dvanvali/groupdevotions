package com.groupdevotions.shared.model;

import com.google.appengine.api.datastore.Text;
import com.google.code.twig.annotation.Index;
import com.google.code.twig.annotation.Store;
import com.google.code.twig.annotation.Type;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

public class Journal implements Serializable  {
	private static final long serialVersionUID = -1017380971780863624L;
	@Index public String accountKey;
	@Index public Date forDay;
	@Store(false) public String forDateDisplay;
    @Type(Text.class) public String content = "";
	@Store(false) public String htmlContent;

	public static boolean find(Collection<Journal> journals, final Date forDay) {
		return !Collections2.filter(journals, new Predicate<Journal>() {
			@Override
			public boolean apply(Journal journal) {
				return journal.forDay.equals(forDay);
			}
		}).isEmpty();
	}
}
