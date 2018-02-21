package com.groupdevotions.shared.model;

import java.io.Serializable;

import com.google.code.twig.annotation.Index;

public class Keyword implements Serializable  {
	private static final long serialVersionUID = 5495148627722229621L;
	@Index public String keyword;
	public String description;
}
