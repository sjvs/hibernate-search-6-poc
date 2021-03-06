/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.impl;

import java.util.Collection;

import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRuntimeIntrospector;
import org.hibernate.search.v6poc.util.impl.common.ToStringTreeBuilder;

/**
 * A {@link PojoImplicitReindexingResolverNode} working at the type level, without casting.
 * <p>
 * This node may delegate to a {@link PojoImplicitReindexingResolverMarkingNode marking node}
 * to mark the input as "to reindex" as well as delegate  to
 * {@link PojoImplicitReindexingResolverPropertyNode property nodes} for deeper resolution.
 *
 * @param <T> The type of "dirty" objects received as input.
 * @param <S> The expected type of the object describing the "dirtiness state".
 */
public class PojoImplicitReindexingResolverOriginalTypeNode<T, S> extends PojoImplicitReindexingResolverNode<T, S> {

	private final Collection<PojoImplicitReindexingResolverNode<? super T, S>> nestedNodes;

	public PojoImplicitReindexingResolverOriginalTypeNode(
			Collection<PojoImplicitReindexingResolverNode<? super T, S>> nestedNodes) {
		this.nestedNodes = nestedNodes;
	}

	@Override
	public void appendTo(ToStringTreeBuilder builder) {
		builder.attribute( "class", getClass().getSimpleName() );
		builder.startList( "nestedNodes" );
		for ( PojoImplicitReindexingResolverNode<?, ?> node : nestedNodes ) {
			builder.value( node );
		}
		builder.endList();
	}

	@Override
	public void resolveEntitiesToReindex(PojoReindexingCollector collector,
			PojoRuntimeIntrospector runtimeIntrospector, T dirty, S dirtinessState) {
		for ( PojoImplicitReindexingResolverNode<? super T, S> node : nestedNodes ) {
			node.resolveEntitiesToReindex( collector, runtimeIntrospector, dirty, dirtinessState );
		}
	}
}
