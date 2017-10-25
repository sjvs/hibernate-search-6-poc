/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.mapping.building.spi;

import org.hibernate.search.v6poc.cfg.spi.ConfigurationPropertySource;
import org.hibernate.search.v6poc.entity.mapping.MappingKey;
import org.hibernate.search.v6poc.entity.mapping.spi.MappingImplementor;
import org.hibernate.search.v6poc.entity.model.spi.IndexableTypeOrdering;

/**
 * @author Yoann Rodiere
 */
public interface MapperFactory<C, M extends MappingImplementor>
		extends MappingKey<M> {

	IndexableTypeOrdering getTypeOrdering();

	Mapper<C, M> createMapper(ConfigurationPropertySource propertySource);

}
