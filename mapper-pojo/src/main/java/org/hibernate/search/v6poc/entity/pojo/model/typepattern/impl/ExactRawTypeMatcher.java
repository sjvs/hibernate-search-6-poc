/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.typepattern.impl;

import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoGenericTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;

class ExactRawTypeMatcher implements TypePatternMatcher {
	private final PojoRawTypeModel<?> exactTypeToMatch;

	ExactRawTypeMatcher(PojoRawTypeModel<?> exactTypeToMatch) {
		this.exactTypeToMatch = exactTypeToMatch;
	}

	@Override
	public String toString() {
		return "hasExactRawType(" + exactTypeToMatch.getName() + ")";
	}

	@Override
	public boolean matches(PojoGenericTypeModel<?> typeToInspect) {
		PojoRawTypeModel<?> typeToMatchRawType = typeToInspect.getRawType();
		return typeToInspect.getRawType().isSubTypeOf( exactTypeToMatch )
				&& exactTypeToMatch.isSubTypeOf( typeToMatchRawType );
	}
}
