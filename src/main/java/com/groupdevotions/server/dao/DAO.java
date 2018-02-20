package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.code.twig.ObjectDatastore;
import com.groupdevotions.server.util.Validation;
import com.groupdevotions.shared.model.KeyMirror;
import com.groupdevotions.shared.model.PostLoad;
import com.groupdevotions.shared.model.PreSave;

import java.util.List;
import java.util.logging.Logger;

/*
 * DAO providing CRUD
 */
public abstract class DAO<T> {
	protected Class<T> tClass;

	protected static final Logger logger = Logger
			.getLogger(DAO.class.getName());

	protected DAO(Class<T> tClass) {
		this.tClass = tClass;
	}

	public void validateKeySameAsClass(Key key) {
		// getCanonicalName().replaceAll("\\.", "_")
		String name = tClass.getName();
		if (name.lastIndexOf('.') > 0) {
			name = name.substring(name.lastIndexOf('.') + 1);
		}
		if (!name.equals(key.getKind())) {
			throw new IllegalArgumentException("Key kind " + key.getKind()
					+ " != class " + name);
		}
	}

	public Key getKey(final ObjectDatastore datastore, T entity) {
		return datastore.associatedKey(entity);
	}

	public Key create(final ObjectDatastore datastore, T entity) {
		// store creates a Key in the datastore and keeps it in the
		// ObjectDatastore associated with this theater instance.
		// Basically, every OD has a Map<Object, Key> which is used to look up
		// the Key for every operation.
		executeSaveCallbacks(entity);
		boolean txStarted = startTxIfNone(datastore);
		Key key = datastore.store(entity);
		// Entity may be used still, so populate like a read
		executeReadEntityCallbacks(datastore, entity);
		commitTxIfStarted(datastore, txStarted);
		return key;
	}

	private boolean startTxIfNone(final ObjectDatastore datastore) {
		if (datastore.getTransaction() != null) {
			if (!datastore.getTransaction().isActive()) {
				datastore.beginTransaction();
				return true;
			}
		}
		return false;
	}

	private void commitTxIfStarted(final ObjectDatastore datastore, boolean txStarted) {
		if (txStarted) {
			datastore.getTransaction().commit();
		}
	}
	
	public T read(final ObjectDatastore datastore, Key key) {
		validateKeySameAsClass(key);
		T entity = datastore.load(key);
		executeReadEntityCallbacks(datastore, entity);
		return entity;
	}
	
	public T read(final ObjectDatastore datastore, String key) {
		return read(datastore, Validation.getValidDSKey(key));
	}

	public void update(final ObjectDatastore datastore, T entity, Key key) {
		executeSaveCallbacks(entity);
		boolean txStarted = startTxIfNone(datastore);
		validateKeySameAsClass(key);
		if (!datastore.isAssociated(entity)) {
			datastore.associate(entity, key, 1);
		}
		datastore.update(entity);
		commitTxIfStarted(datastore, txStarted);
	}

	public void update(final ObjectDatastore datastore, T entity) {
		Key key;
		if (entity instanceof KeyMirror) {
			key = KeyFactory.stringToKey(((KeyMirror) entity).getKey());
		} else {
			key = getKey(datastore, entity);
		}
		executeSaveCallbacks(entity);
		boolean txStarted = startTxIfNone(datastore);
		validateKeySameAsClass(key);
		if (!datastore.isAssociated(entity)) {
			datastore.associate(entity, key, 1);
		}
		datastore.update(entity);
		commitTxIfStarted(datastore, txStarted);
	}

	public void delete(final ObjectDatastore datastore, Key key) {
		boolean txStarted = startTxIfNone(datastore);
		validateKeySameAsClass(key);
        // todo test deleteKey()
		T entity = datastore.load(key);
		datastore.deleteKey(key);
		datastore.disassociate(entity);
		commitTxIfStarted(datastore, txStarted);
	}

	public void executeReadEntityCallbacks(final ObjectDatastore datastore, T entity) {
		if (entity instanceof KeyMirror) {
			((KeyMirror)entity).setKey(KeyFactory.keyToString(getKey(datastore, entity)));
		}
		if (entity instanceof PostLoad) {
			((PostLoad)entity).postLoad();
		}
	}

	public void executeReadEntityCallbacksMultipleEntities(final ObjectDatastore datastore, List<T> entities) {
		for(T entity : entities) {
			executeReadEntityCallbacks(datastore, entity);
		}
	}


	private void executeSaveCallbacks(T entity) {
		if (entity instanceof PreSave) {
			((PreSave)entity).preSave();
		}
	}
}
