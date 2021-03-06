/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.search.dsl.predicate.impl;

import org.hibernate.search.v6poc.search.SearchPredicate;
import org.hibernate.search.v6poc.search.predicate.spi.SearchPredicateFactory;

public final class SearchTargetPredicateRootContext<B> extends SearchPredicateContainerContextImpl<SearchPredicate, B> {

	public SearchTargetPredicateRootContext(SearchPredicateFactory<?, B> factory) {
		super( factory, new RootSearchPredicateDslContextImpl<>( factory ) );
	}

}
