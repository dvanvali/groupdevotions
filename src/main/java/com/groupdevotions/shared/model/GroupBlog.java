package com.groupdevotions.shared.model;

import com.google.code.twig.annotation.Embedded;
import com.google.code.twig.annotation.Index;
import com.google.code.twig.annotation.Store;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.util.SharedUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class GroupBlog implements Serializable {
	private static final long serialVersionUID = -1032561779872232971L;
	@Index public String groupKey;
	@Index public Date blogDate;
	@Store(false) public String blogDateDisplay;
	
	@Embedded public List<BlogEntry> blogEntries = new ArrayList<BlogEntry>();

	public void populateNonStoredFields() {
		blogDateDisplay = ServerUtils.formatDateDisplayForTitle(blogDate);
		for (BlogEntry blogEntry : blogEntries) {
			blogEntry.populateNonStoredFields();
		}
	}

	public void insertOrUpdate(BlogEntry blogEntry) {
		populateNonStoredFields();
		boolean found = false;
		for (BlogEntry existingBlogEntry : blogEntries) {
			if (SharedUtils.safeEquals(existingBlogEntry.groupMemberKey, blogEntry.groupMemberKey) 
					&& SharedUtils.safeEquals(existingBlogEntry.postedOnFullDateTime, blogEntry.postedOnFullDateTime)) {
				existingBlogEntry.content = blogEntry.content;
				existingBlogEntry.postedOn = blogEntry.postedOn;
				existingBlogEntry.populateNonStoredFields();
				found = true;
				break;
			}
		}
		if (!found) {
			blogEntries.add(blogEntry);
		}
	}

	public static boolean find(Collection<GroupBlog> groupBlogs, final Date blogDate) {
		return !Collections2.filter(groupBlogs, new Predicate<GroupBlog>() {
			@Override
			public boolean apply(GroupBlog groupBlog) {
				return groupBlog.blogDate.equals(blogDate);
			}
		}).isEmpty();
	}
}
