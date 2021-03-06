/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.spi;

import java.util.Set;

import org.hibernate.search.v6poc.entity.pojo.mapping.PojoSearchTarget;
import org.hibernate.search.v6poc.entity.pojo.search.PojoReference;
import org.hibernate.search.v6poc.search.ObjectLoader;
import org.hibernate.search.v6poc.search.dsl.query.SearchQueryResultDefinitionContext;

public interface PojoSearchTargetDelegate<T> extends PojoSearchTarget<PojoReference> {

	Set<Class<? extends T>> getTargetedIndexedTypes();

	<O> SearchQueryResultDefinitionContext<PojoReference, O> query(ObjectLoader<PojoReference, O> objectLoader);

}
