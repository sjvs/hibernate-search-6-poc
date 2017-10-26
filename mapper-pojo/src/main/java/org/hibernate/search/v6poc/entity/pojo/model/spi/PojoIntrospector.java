/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.spi;

/**
 * @author Yoann Rodiere
 */
public interface PojoIntrospector {

	<T> TypeModel<T> getEntityTypeModel(Class<T> type);

}
