package com.google.code.twig.standard;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityComparatorAccessor;
import com.google.appengine.api.datastore.Query.SortPredicate;
import com.google.code.twig.util.RestrictionToPredicateAdaptor;
import com.google.code.twig.util.SortedMergeIterator;
import com.google.common.collect.Iterators;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Contains functionality common to all both TypedFindCommand and ParentsCommand
 * 
 * @author John Patterson <john@vercer.com>
 *
 * @param <T> The type of the instance that will be returned
 * @param <C> The concrete type that is returned from chained methods  
 */
abstract class StandardRestrictedFindCommand<C extends StandardRestrictedFindCommand<C>> extends StandardDecodeCommand<C>
{
	StandardRestrictedFindCommand(TranslatorObjectDatastore datastore, int depth)
	{
		super(datastore, depth);
	}

	Iterator<Entity> applyEntityFilter(Iterator<Entity> entities)
	{
		if (this.entityRestriction != null)
		{
			entities = Iterators.filter(entities, new RestrictionToPredicateAdaptor<Entity>(entityRestriction));
		}
		return entities;
	}

	Iterator<Entity> mergeEntities(List<Iterator<Entity>> iterators, List<SortPredicate> sorts)
	{
		Iterator<Entity> merged;
		Comparator<Entity> comparator = EntityComparatorAccessor.newEntityComparator(sorts);
		merged = new SortedMergeIterator<Entity>(comparator, iterators);

		/*
		Comparator<Entity> entityComparator = new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
				for (SortPredicate sortPredicate : sorts) {
					String propertyName = sortPredicate.getPropertyName();
					Object p1 = o1.getProperty(propertyName);
					Object p2 = o2.getProperty(propertyName);
					Index.Property property;
					property.c
				}
				return o1.getKey().compareTo(o2.getKey());
			}
		};
*/

		return merged;
	}
}
