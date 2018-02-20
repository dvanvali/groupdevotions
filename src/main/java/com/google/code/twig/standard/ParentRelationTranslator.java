/**
 *
 */
package com.google.code.twig.standard;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.google.code.twig.Path;
import com.google.code.twig.Property;
import com.google.code.twig.util.reference.ReadOnlyObjectReference;

final class ParentRelationTranslator extends RelationTranslator
{
	/**
	 * @param datastore
	 */
	ParentRelationTranslator(TranslatorObjectDatastore datastore)
	{
		super(datastore);
	}

	public Object decode(Set<Property> properties, Path prefix, Type type)
	{
		// properties are not used as the ancestors is found by the key
		assert properties.isEmpty();

		// put the key in a property
		Key parentKey = datastore.decodeKey.getParent();

		if (parentKey == null)
		{
			return NULL_VALUE;
		}

		return keyToInstance(parentKey);
	}

	public Set<Property> encode(final Object instance, final Path prefix, final boolean indexed)
	{
		if (instance != null)
		{
			ReadOnlyObjectReference<Key> keyReference = new ReadOnlyObjectReference<Key>()
			{
				public Key get()
				{
					return instanceToKey(instance);
				}
			};
	
			// an existing ancestors key ref shows ancestors is still being stored
			if (datastore.encodeKeyDetails != null && datastore.encodeKeyDetails.getParentKeyReference() == null)
			{
				// store the ancestors key inside the current key
				datastore.encodeKeyDetails.setParentKeyReference(keyReference);
			}
		}

		// no fields are stored for ancestors
		return Collections.emptySet();
	}
}