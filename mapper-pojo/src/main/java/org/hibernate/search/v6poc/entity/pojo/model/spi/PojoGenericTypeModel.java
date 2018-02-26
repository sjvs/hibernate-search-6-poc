/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.spi;

import java.util.Optional;

/**
 * A {@link PojoTypeModel} offering additional reflection options,
 * such as the ability to retrieve the type argument for a given generic supertype.
 *
 * @param <T> The type represented by this model
 */
public interface PojoGenericTypeModel<T> extends PojoTypeModel<T> {

	/**
	 * @param rawSuperType The supertype to resolve type parameters for
	 * @param typeParameterIndex The index of the type parameter to resolve
	 * @return The model for the type argument for the type parameter defined in {@code rawSuperType}
	 * at index {@code typeParameterIndex}, or an empty optional if the current type
	 * does not extend {@code rawSuperType}.
	 * Implementations may decide to return a model of the raw type argument, or to retain generics information.
	 */
	Optional<? extends PojoGenericTypeModel<?>> getTypeArgument(Class<?> rawSuperType, int typeParameterIndex);

}