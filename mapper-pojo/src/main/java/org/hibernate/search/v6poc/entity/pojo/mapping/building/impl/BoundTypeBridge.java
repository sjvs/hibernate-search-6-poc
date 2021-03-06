/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.building.impl;

import org.hibernate.search.v6poc.entity.pojo.bridge.TypeBridge;
import org.hibernate.search.v6poc.entity.pojo.model.impl.PojoModelTypeRootElement;

public final class BoundTypeBridge<T> {
	private final TypeBridge bridge;
	private final PojoModelTypeRootElement<T> pojoModelRootElement;

	BoundTypeBridge(TypeBridge bridge, PojoModelTypeRootElement<T> pojoModelRootElement) {
		this.bridge = bridge;
		this.pojoModelRootElement = pojoModelRootElement;
	}

	public TypeBridge getBridge() {
		return bridge;
	}

	public PojoModelTypeRootElement<T> getPojoModelRootElement() {
		return pojoModelRootElement;
	}
}
