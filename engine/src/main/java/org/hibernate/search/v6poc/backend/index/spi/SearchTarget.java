/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.index.spi;

import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.search.DocumentReference;
import org.hibernate.search.v6poc.search.spi.SearchResultDefinitionContext;

/**
 * @author Yoann Rodiere
 */
public interface SearchTarget {

	void add(SearchTarget other);

	SearchResultDefinitionContext<DocumentReference> search(SessionContext context);

}
