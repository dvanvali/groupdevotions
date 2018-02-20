package com.groupdevotions.shared.model;

import com.google.appengine.api.datastore.Text;
import com.google.code.twig.annotation.Store;
import com.google.code.twig.annotation.Type;
import com.groupdevotions.server.ServerUtils;

import java.io.Serializable;
import java.util.Date;

public class BlogEntry implements Serializable {
	private static final long serialVersionUID = -4036691260389104404L;
	public String studySectionCreationTimestamp;
	public String name;
	public String groupMemberKey;
    public Date postedOn;
	@Type(Text.class) public String content;
    @Store(false) public String formattedPostedOn;
    @Store(false) public String postedOnFullDateTime;
	@Store(false) public boolean modifiable;

	public void populateNonStoredFields() {
		formattedPostedOn = ServerUtils.formatDatetimeForDisplay(postedOn);
		postedOnFullDateTime = ServerUtils.formatFullDateTime(postedOn);
	}
}
