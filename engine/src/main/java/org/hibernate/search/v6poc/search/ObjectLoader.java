/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.search;

import java.util.List;

/**
 * Loads objects into memory using a reference and implementation-specific context.
 *
 * @param <R> The expected reference type (input)
 * @param <O> The resulting object type (output)
 */
public interface ObjectLoader<R, O> {

	/**
	 * @param references A list of references to the objects to load.
	 * @return A list of loaded objects, in the same order the references were given.
	 * {@code null} is inserted when an object is not found.
	 */
	List<O> load(List<R> references);

	static <T> ObjectLoader<T, T> identity() {
		return IdentityObjectLoader.get();
	}

}
