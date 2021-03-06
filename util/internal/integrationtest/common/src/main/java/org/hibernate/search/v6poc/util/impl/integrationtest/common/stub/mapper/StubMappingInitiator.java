/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.engine.SearchMappingRepositoryBuilder;
import org.hibernate.search.v6poc.entity.mapping.spi.MappingBuildContext;
import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.v6poc.entity.mapping.building.spi.Mapper;
import org.hibernate.search.v6poc.entity.mapping.building.spi.MappingConfigurationCollector;
import org.hibernate.search.v6poc.entity.mapping.building.spi.MappingInitiator;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataContributorProvider;
import org.hibernate.search.v6poc.entity.mapping.spi.MappingKey;

public class StubMappingInitiator implements MappingInitiator<StubTypeMetadataContributor, StubMapping> {

	private final SearchMappingRepositoryBuilder searchBuilder;
	private final boolean multiTenancyEnabled;
	private final List<StubTypeMetadataContributor> mappingContributors = new ArrayList<>();
	private final MappingKey<StubMapping> mappingKey = new MappingKey<StubMapping>() {
		@Override
		public String render() {
			return "Stub mapping";
		}
	};

	public StubMappingInitiator(SearchMappingRepositoryBuilder searchBuilder, boolean multiTenancyEnabled) {
		this.searchBuilder = searchBuilder;
		this.multiTenancyEnabled = multiTenancyEnabled;
		searchBuilder.addMappingInitiator( this );
	}

	public void add(String typeIdentifier, String indexName, Consumer<IndexModelBindingContext> mappingContributor) {
		mappingContributors.add( new StubTypeMetadataContributor( new StubTypeModel( typeIdentifier ), indexName, mappingContributor ) );
	}

	@Override
	public MappingKey<StubMapping> getMappingKey() {
		return mappingKey;
	}

	@Override
	public void configure(MappingBuildContext buildContext, ConfigurationPropertySource propertySource,
			MappingConfigurationCollector<StubTypeMetadataContributor> configurationCollector) {
		if ( multiTenancyEnabled ) {
			configurationCollector.enableMultiTenancy();
		}
		for ( StubTypeMetadataContributor mappingContributor : mappingContributors ) {
			mappingContributor.contribute( configurationCollector );
		}
	}

	@Override
	public Mapper<StubMapping> createMapper(MappingBuildContext buildContext, ConfigurationPropertySource propertySource,
			TypeMetadataContributorProvider<StubTypeMetadataContributor> contributorProvider) {
		return new StubMapper( contributorProvider );
	}

	public StubMapping getResult() {
		return searchBuilder.getBuiltResult().getMapping( mappingKey );
	}

}
