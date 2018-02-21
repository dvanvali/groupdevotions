package com.groupdevotions.server;


import com.google.inject.*;
import com.google.code.twig.*;
import com.google.code.twig.annotation.*;
import com.google.code.twig.configuration.DefaultConfiguration;
import com.groupdevotions.shared.model.*;

public class AnnotationObjectDatastoreProvider implements
		Provider<ObjectDatastore> {

	static {
		DefaultConfiguration.registerTypeName(Account.class, "Account");
		DefaultConfiguration.registerTypeName(Group.class, "Group");
		DefaultConfiguration.registerTypeName(GroupMember.class, "GroupMember");
		DefaultConfiguration.registerTypeName(GroupBlog.class, "GroupBlog");
		DefaultConfiguration.registerTypeName(GroupMemberBlog.class, "GroupMemberBlog");
		DefaultConfiguration.registerTypeName(GroupMemberLessonAnswer.class, "GroupMemberLessonAnswer");
		DefaultConfiguration.registerTypeName(Journal.class, "Journal");
		DefaultConfiguration.registerTypeName(Keyword.class, "Keyword");
		DefaultConfiguration.registerTypeName(Organization.class, "Organization");
		DefaultConfiguration.registerTypeName(Study.class, "Study");
		DefaultConfiguration.registerTypeName(StudyContributor.class, "StudyContributor");
		DefaultConfiguration.registerTypeName(StudyKeyword.class, "StudyKeyword");
		DefaultConfiguration.registerTypeName(StudyLesson.class, "StudyLesson");
		DefaultConfiguration.registerTypeName(StudySection.class, "StudySection");
		DefaultConfiguration.registerTypeName(Config.class, "Config");
    }

	// don't index fields by default
	private Boolean indexed = false;

	// load just the first instances and their fields
//	/private int activationDepth = 2;

	public ObjectDatastore get() {
		ObjectDatastore datastore = new AnnotationObjectDatastore(indexed);

//		datastore.setActivationDepth(activationDepth);

		return datastore;
	}
}
