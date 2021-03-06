/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.lucene.types.formatter.impl;

/**
 * @author Guillaume Smet
 */
public interface LuceneFieldFormatter<T> {

	T format(Object value);

	// equals()/hashCode() needs to be implemented if the formatter is not a singleton

	boolean equals(Object obj);

	int hashCode();
}
