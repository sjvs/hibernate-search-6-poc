/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.additionalmetadata.building.spi;

import java.util.Set;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.ReindexOnUpdate;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPathValueNode;

public interface PojoAdditionalMetadataCollectorValueNode extends PojoAdditionalMetadataCollector {

	void associationInverseSide(PojoModelPathValueNode inverseSidePath);

	void associationEmbedded();

	void reindexOnUpdate(ReindexOnUpdate reindexOnUpdate);

	void derivedFrom(Set<PojoModelPathValueNode> path);

}
