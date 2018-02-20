package com.groupdevotions.shared.model;

import java.io.Serializable;

import com.google.appengine.api.datastore.Text;
import com.google.code.twig.annotation.Type;

public class EnhancedString implements Serializable {
	private static final long serialVersionUID = -433594517354872998L;
	public EnhancedString() {}
	public EnhancedString(String value) {
		this.value = value;
	}
	@Type(Text.class) public String value;
}
