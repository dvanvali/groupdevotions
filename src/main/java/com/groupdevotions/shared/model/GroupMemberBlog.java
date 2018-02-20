package com.groupdevotions.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
 

import com.google.code.twig.annotation.Embedded;
import com.google.code.twig.annotation.Index;
import com.google.code.twig.annotation.Store;
import com.groupdevotions.server.util.SharedUtils;

public class GroupMemberBlog implements Serializable, KeyMirror {
	private static final long serialVersionUID = -9009561779121732971L;
	@Store(false) public String key;
	@Index public String lessonKey;
	@Index public String groupMemberKey;
	public String name; 
	@Embedded public List<BlogEntry> blogEntries = new ArrayList<BlogEntry>();
	
	public void insertOrUpdate(BlogEntry blogEntry, String originalPostedOn) {
		boolean found = false;
		for (BlogEntry existingBlogEntry : blogEntries) {
			if (SharedUtils.safeEquals(existingBlogEntry.studySectionCreationTimestamp, blogEntry.studySectionCreationTimestamp) 
					&& SharedUtils.safeEquals(existingBlogEntry.postedOn, originalPostedOn)) {
				existingBlogEntry.content = blogEntry.content;
				existingBlogEntry.postedOn = blogEntry.postedOn;
			}
		}
		if (!found) {
			blogEntries.add(blogEntry);
		}
	}

	@Override
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String getKey() {
		return key;
	}
}
