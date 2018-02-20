package com.google.appengine.api.datastore;

import com.google.appengine.api.datastore.Query.SortPredicate;
import com.google.apphosting.api.DatastorePb;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class EntityComparatorAccessor
{
	public static Comparator<Entity> newEntityComparator(List<SortPredicate> sorts)
	{
		return new EntityComparator(sorts);
	}

	static final class EntityComparator implements Comparator<Entity> {
		private final EntityProtoComparators.EntityProtoComparator delegate;

		EntityComparator(List<SortPredicate> sortPreds) {
			this.delegate = new EntityProtoComparators.EntityProtoComparator(sortPredicatesToOrders(sortPreds));
		}

		private static List<DatastorePb.Query.Order> sortPredicatesToOrders(List<SortPredicate> sortPreds) {
			ArrayList orders = new ArrayList();
			Iterator i$ = sortPreds.iterator();

			while(i$.hasNext()) {
				SortPredicate sp = (SortPredicate)i$.next();
				orders.add(QueryTranslator.convertSortPredicateToPb(sp));
			}

			return orders;
		}

		public int compare(Entity e1, Entity e2) {
			return this.delegate.compare(EntityTranslator.convertToPb(e1), EntityTranslator.convertToPb(e2));
		}
	}


}
