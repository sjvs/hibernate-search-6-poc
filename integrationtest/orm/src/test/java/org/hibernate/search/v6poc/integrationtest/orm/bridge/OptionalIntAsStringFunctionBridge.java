/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.orm.bridge;

import java.util.OptionalInt;

import org.hibernate.search.v6poc.entity.pojo.bridge.FunctionBridge;
import org.hibernate.search.v6poc.integrationtest.util.common.rule.StaticCounters;

public final class OptionalIntAsStringFunctionBridge implements FunctionBridge<OptionalInt, String> {

	public static final StaticCounters.Key INSTANCE_COUNTER_KEY = StaticCounters.createKey();
	public static final StaticCounters.Key CLOSE_COUNTER_KEY = StaticCounters.createKey();

	public OptionalIntAsStringFunctionBridge() {
		StaticCounters.get().increment( INSTANCE_COUNTER_KEY );
	}

	@Override
	public String toIndexedValue(OptionalInt propertyValue) {
		return propertyValue == null || !propertyValue.isPresent()
				? "empty"
				: String.valueOf( propertyValue.getAsInt() );
	}

	@Override
	public void close() {
		StaticCounters.get().increment( CLOSE_COUNTER_KEY );
	}
}
