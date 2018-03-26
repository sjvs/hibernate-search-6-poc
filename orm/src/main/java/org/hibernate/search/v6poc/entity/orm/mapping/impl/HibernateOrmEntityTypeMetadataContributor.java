/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.orm.mapping.impl;

import java.util.List;

import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoMappingCollectorTypeNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoTypeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.spi.PojoAugmentedModelCollectorTypeNode;

final class HibernateOrmEntityTypeMetadataContributor implements PojoTypeMetadataContributor {
	private final List<PojoTypeMetadataContributor> delegates;

	HibernateOrmEntityTypeMetadataContributor(List<PojoTypeMetadataContributor> delegates) {
		this.delegates = delegates;
	}

	@Override
	public void contributeModel(PojoAugmentedModelCollectorTypeNode collector) {
		collector.markAsEntity();
		for ( PojoTypeMetadataContributor delegate : delegates ) {
			delegate.contributeModel( collector );
		}
	}

	@Override
	public void contributeMapping(PojoMappingCollectorTypeNode collector) {
		for ( PojoTypeMetadataContributor delegate : delegates ) {
			delegate.contributeMapping( collector );
		}
		/*
		 * TODO register the default identifier mapping? We would need new APIs in the collector, though,
		 * or to allow identifier mapping overrides.
		 */
	}
}
