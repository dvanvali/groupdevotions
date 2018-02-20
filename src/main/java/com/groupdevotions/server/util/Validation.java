package com.groupdevotions.server.util;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public class Validation {

	public static Key getValidDSKey(String key) {
		if (key == null || "".equals(key)) {
			throw new IllegalArgumentException("Null or empty key");
		}

		Key dsKey;
		try {
			dsKey = KeyFactory.stringToKey(key);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid key " + key);
		}

		return dsKey;
	}
}
