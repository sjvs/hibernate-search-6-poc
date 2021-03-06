/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.mapper.pojo;

import static org.hibernate.search.v6poc.util.impl.integrationtest.common.assertion.SearchResultAssert.assertThat;
import static org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.StubBackendUtils.reference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.hibernate.search.v6poc.entity.javabean.JavaBeanMapping;
import org.hibernate.search.v6poc.entity.javabean.JavaBeanMappingInitiator;
import org.hibernate.search.v6poc.entity.pojo.bridge.builtin.impl.DefaultIntegerIdentifierBridge;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.extractor.builtin.MapKeyExtractor;
import org.hibernate.search.v6poc.entity.pojo.mapping.PojoSearchManager;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.ProgrammaticMappingDefinition;
import org.hibernate.search.v6poc.entity.pojo.mapping.impl.PojoReferenceImpl;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPath;
import org.hibernate.search.v6poc.entity.pojo.search.PojoReference;
import org.hibernate.search.v6poc.integrationtest.mapper.pojo.bridge.CustomPropertyBridge;
import org.hibernate.search.v6poc.integrationtest.mapper.pojo.bridge.CustomTypeBridge;
import org.hibernate.search.v6poc.integrationtest.mapper.pojo.bridge.IntegerAsStringValueBridge;
import org.hibernate.search.v6poc.integrationtest.mapper.pojo.bridge.OptionalIntAsStringValueBridge;
import org.hibernate.search.v6poc.search.ProjectionConstants;
import org.hibernate.search.v6poc.search.SearchQuery;
import org.hibernate.search.v6poc.util.impl.common.CollectionHelper;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.rule.BackendMock;
import org.hibernate.search.v6poc.integrationtest.mapper.pojo.test.util.rule.JavaBeanMappingSetupHelper;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.rule.StubSearchWorkBehavior;
import org.hibernate.search.v6poc.util.impl.test.rule.StaticCounters;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Yoann Rodiere
 */
public class JavaBeanProgrammaticMappingIT {

	@Rule
	public BackendMock backendMock = new BackendMock( "stubBackend" );

	@Rule
	public JavaBeanMappingSetupHelper setupHelper = new JavaBeanMappingSetupHelper();

	@Rule
	public StaticCounters counters = new StaticCounters();

	private JavaBeanMapping mapping;

	@Before
	public void setup() {
		backendMock.expectSchema( OtherIndexedEntity.INDEX, b -> b
				.field( "numeric", Integer.class )
				.field( "numericAsString", String.class )
		);
		backendMock.expectSchema( YetAnotherIndexedEntity.INDEX, b -> b
				.objectField( "customBridgeOnProperty", b2 -> b2
						.field( "date", LocalDate.class )
						.field( "text", String.class )
				)
				.field( "myLocalDateField", LocalDate.class )
				.field( "numeric", Integer.class )
				.field( "optionalText", String.class )
				.field( "optionalInt", Integer.class )
				.field( "optionalIntAsString", String.class )
				.field( "numericArray", Integer.class )
				.objectField( "embeddedIterable", b2 -> b2
						.objectField( "embedded", b3 -> b3
								.field( "prefix_myTextField", String.class )
						)
				)
				.objectField( "embeddedList", b2 -> b2
						.objectField( "otherPrefix_embedded", b3 -> b3
								.objectField( "prefix_customBridgeOnClass", b4 -> b4
										.field( "text", String.class )
								)
						)
				)
				.objectField( "embeddedArrayList", b2 -> b2
						.objectField( "embedded", b3 -> b3
								.objectField( "prefix_customBridgeOnProperty", b4 -> b4
										.field( "text", String.class )
								)
						)
				)
				.field( "embeddedMapKeys", String.class )
				.objectField( "embeddedMap", b2 -> b2
						.objectField( "embedded", b3 -> b3
								.field( "prefix_myLocalDateField", LocalDate.class )
						)
				)
		);
		backendMock.expectSchema( IndexedEntity.INDEX, b -> b
				.objectField( "customBridgeOnClass", b2 -> b2
						.field( "date", LocalDate.class )
						.field( "text", String.class )
				)
				.objectField( "customBridgeOnProperty", b2 -> b2
						.field( "date", LocalDate.class )
						.field( "text", String.class )
				)
				.objectField( "embedded", b2 -> b2
						.objectField( "prefix_customBridgeOnClass", b3 -> b3
								.field( "date", LocalDate.class )
								.field( "text", String.class )
						)
						.objectField( "prefix_customBridgeOnProperty", b3 -> b3
								.field( "date", LocalDate.class )
								.field( "text", String.class )
						)
						.objectField( "prefix_embedded", b3 -> b3
								.objectField( "prefix_customBridgeOnClass", b4 -> b4
										.field( "text", String.class )
								)
						)
						.field( "prefix_myLocalDateField", LocalDate.class )
						.field( "prefix_myTextField", String.class )
				)
				.field( "myTextField", String.class )
				.field( "myLocalDateField", LocalDate.class )
		);

		mapping = setupHelper.withBackendMock( backendMock )
				.setup( mappingRepositoryBuilder -> {
					JavaBeanMappingInitiator initiator = JavaBeanMappingInitiator.create( mappingRepositoryBuilder );

					initiator.addEntityTypes( CollectionHelper.asSet(
							IndexedEntity.class,
							OtherIndexedEntity.class,
							YetAnotherIndexedEntity.class
					) );

					ProgrammaticMappingDefinition mappingDefinition = initiator.programmaticMapping();
					mappingDefinition.type( IndexedEntity.class )
							.indexed( IndexedEntity.INDEX )
							.bridge(
									new CustomTypeBridge.Builder()
									.objectName( "customBridgeOnClass" )
							)
							.property( "id" )
									.documentId()
							.property( "text" )
									.field( "myTextField" )
							.property( "embedded" )
									.indexedEmbedded()
											.prefix( "embedded.prefix_" )
											.maxDepth( 1 )
											.includePaths( "customBridgeOnClass.text", "embedded.prefix_customBridgeOnClass.text" );

					ProgrammaticMappingDefinition secondMappingDefinition = initiator.programmaticMapping();
					secondMappingDefinition.type( ParentIndexedEntity.class )
							.property( "localDate" )
									.field( "myLocalDateField" )
							.property( "embedded" )
									.associationInverseSide(
											PojoModelPath.fromRoot( "embeddingAsSingle" )
													.value( ContainerValueExtractorPath.defaultExtractors() )
									)
									.bridge(
											new CustomPropertyBridge.Builder()
											.objectName( "customBridgeOnProperty" )
									);
					secondMappingDefinition.type( OtherIndexedEntity.class )
							.indexed( OtherIndexedEntity.INDEX )
							.property( "id" )
									.documentId().identifierBridge( DefaultIntegerIdentifierBridge.class )
							.property( "numeric" )
									.field()
									.field( "numericAsString" ).valueBridge( IntegerAsStringValueBridge.class );
					secondMappingDefinition.type( YetAnotherIndexedEntity.class )
							.indexed( YetAnotherIndexedEntity.INDEX )
							.property( "id" )
									.documentId()
							.property( "numeric" )
									.field()
							.property( "optionalText" )
									.field()
							.property( "optionalInt" )
									.field()
									.field( "optionalIntAsString" )
											.valueBridge( OptionalIntAsStringValueBridge.class )
											.withoutExtractors()
							.property( "numericArray" )
									.field()
							.property( "embeddedIterable" )
									.associationInverseSide(
											PojoModelPath.fromRoot( "embeddingAsIterable" )
													.value( ContainerValueExtractorPath.defaultExtractors() )
									)
									.indexedEmbedded().includePaths( "embedded.prefix_myTextField" )
							.property( "embeddedList" )
									.associationInverseSide(
											PojoModelPath.fromRoot( "embeddingAsList" )
													.value( ContainerValueExtractorPath.defaultExtractors() )
									)
									.indexedEmbedded()
											.prefix( "embeddedList.otherPrefix_" )
											.includePaths( "embedded.prefix_customBridgeOnClass.text" )
							.property( "embeddedArrayList" )
									.associationInverseSide(
											PojoModelPath.fromRoot( "embeddingAsArrayList" )
													.value( ContainerValueExtractorPath.defaultExtractors() )
									)
									.indexedEmbedded().includePaths( "embedded.prefix_customBridgeOnProperty.text" )
							.property( "embeddedMap" )
									.associationInverseSide(
											PojoModelPath.fromRoot( "embeddingAsMap" )
													.value( ContainerValueExtractorPath.defaultExtractors() )
									)
									.field( "embeddedMapKeys" ).withExtractor( MapKeyExtractor.class )
									.indexedEmbedded().includePaths( "embedded.prefix_myLocalDateField" );

					return initiator;
				} );

		backendMock.verifyExpectationsMet();
	}

	@Test
	public void index() {
		try ( PojoSearchManager manager = mapping.createSearchManager() ) {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );
			entity1.setText( "this is text (1)" );
			entity1.setLocalDate( LocalDate.of( 2017, 11, 1 ) );
			IndexedEntity entity2 = new IndexedEntity();
			entity2.setId( 2 );
			entity2.setText( "some more text (2)" );
			entity2.setLocalDate( LocalDate.of( 2017, 11, 2 ) );
			IndexedEntity entity3 = new IndexedEntity();
			entity3.setId( 3 );
			entity3.setText( "some more text (3)" );
			entity3.setLocalDate( LocalDate.of( 2017, 11, 3 ) );
			OtherIndexedEntity entity4 = new OtherIndexedEntity();
			entity4.setId( 4 );
			entity4.setNumeric( 404 );
			YetAnotherIndexedEntity entity5 = new YetAnotherIndexedEntity();
			entity5.setId( 5 );
			entity5.setNumeric( 405 );
			entity5.setOptionalText( Optional.of( "some more text (5)" ) );
			entity5.setOptionalInt( OptionalInt.of( 42 ) );
			entity5.setNumericArray( new Integer[] { 1, 2, 3 } );
			IndexedEntity entity6 = new IndexedEntity();
			entity6.setId( 6 );
			entity6.setText( "some more text (6)" );
			entity6.setLocalDate( LocalDate.of( 2017, 11, 6 ) );


			entity1.setEmbedded( entity2 );
			entity2.getEmbeddingAsSingle().add( entity1 );

			entity2.setEmbedded( entity3 );
			entity3.getEmbeddingAsSingle().add( entity2 );

			entity3.setEmbedded( entity2 );
			entity2.getEmbeddingAsSingle().add( entity3 );

			entity5.setEmbeddedIterable( new LinkedHashSet<>( Arrays.asList( entity1, entity2 ) ) );
			entity1.getEmbeddingAsIterable().add( entity5 );
			entity2.getEmbeddingAsIterable().add( entity5 );

			entity5.setEmbeddedList( Arrays.asList( entity2, entity3, entity6 ) );
			entity2.getEmbeddingAsList().add( entity5 );
			entity3.getEmbeddingAsList().add( entity5 );
			entity6.getEmbeddingAsList().add( entity5 );

			entity5.setEmbeddedArrayList( new ArrayList<>( Arrays.asList( entity3, entity1 ) ) );
			entity3.getEmbeddingAsArrayList().add( entity5 );
			entity1.getEmbeddingAsArrayList().add( entity5 );

			Map<String, List<IndexedEntity>> embeddedMap = new LinkedHashMap<>();
			embeddedMap.computeIfAbsent( "entity3", ignored -> new ArrayList<>() ).add( entity3 );
			embeddedMap.computeIfAbsent( "entity2", ignored -> new ArrayList<>() ).add( entity2 );
			embeddedMap.computeIfAbsent( "entity2", ignored -> new ArrayList<>() ).add( entity3 );
			entity5.setEmbeddedMap( embeddedMap );
			entity3.getEmbeddingAsMap().add( entity5 );
			entity2.getEmbeddingAsMap().add( entity5 );
			entity3.getEmbeddingAsMap().add( entity5 );

			manager.getMainWorker().add( entity1 );
			manager.getMainWorker().add( entity2 );
			manager.getMainWorker().add( entity4 );
			manager.getMainWorker().delete( entity1 );
			manager.getMainWorker().add( entity3 );
			manager.getMainWorker().add( entity5 );
			manager.getMainWorker().add( entity6 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "2", b -> b
							.field( "myLocalDateField", entity2.getLocalDate() )
							.field( "myTextField", entity2.getText() )
							.objectField( "customBridgeOnClass", b2 -> b2
									.field( "text", entity2.getText() )
									.field( "date", entity2.getLocalDate() )
							)
							.objectField( "customBridgeOnProperty", b2 -> b2
									.field( "text", entity2.getEmbedded().getText() )
									.field( "date", entity2.getEmbedded().getLocalDate() )
							)
							.objectField( "embedded", b2 -> b2
									.field( "prefix_myTextField", entity2.getEmbedded().getText() )
									.field( "prefix_myLocalDateField", entity2.getEmbedded().getLocalDate() )
									.objectField( "prefix_customBridgeOnClass", b3 -> b3
											.field( "text", entity2.getEmbedded().getText() )
											.field( "date", entity2.getEmbedded().getLocalDate() )
									)
									.objectField( "prefix_customBridgeOnProperty", b3 -> b3
											.field( "text", entity2.getEmbedded().getEmbedded().getText() )
											.field( "date", entity2.getEmbedded().getEmbedded().getLocalDate() )
									)
									.objectField( "prefix_embedded", b3 -> b3
											.objectField( "prefix_customBridgeOnClass", b4 -> b4
													.field( "text", entity2.getEmbedded().getEmbedded().getText() )
											)
									)
							)
					)
					.add( "3", b -> b
							.field( "myLocalDateField", entity3.getLocalDate() )
							.field( "myTextField", entity3.getText() )
							.objectField( "customBridgeOnClass", b2 -> b2
									.field( "text", entity3.getText() )
									.field( "date", entity3.getLocalDate() )
							)
							.objectField( "customBridgeOnProperty", b2 -> b2
									.field( "text", entity3.getEmbedded().getText() )
									.field( "date", entity3.getEmbedded().getLocalDate() )
							)
							.objectField( "embedded", b2 -> b2
									.field( "prefix_myTextField", entity3.getEmbedded().getText() )
									.field( "prefix_myLocalDateField", entity3.getEmbedded().getLocalDate() )
									.objectField( "prefix_customBridgeOnClass", b3 -> b3
											.field( "text", entity3.getEmbedded().getText() )
											.field( "date", entity3.getEmbedded().getLocalDate() )
									)
									.objectField( "prefix_customBridgeOnProperty", b3 -> b3
											.field( "text", entity3.getEmbedded().getEmbedded().getText() )
											.field( "date", entity3.getEmbedded().getEmbedded().getLocalDate() )
									)
									.objectField( "prefix_embedded", b3 -> b3
											.objectField( "prefix_customBridgeOnClass", b4 -> b4
													.field( "text", entity3.getEmbedded().getEmbedded().getText() )
											)
									)
							)
					)
					.add( "6", b -> b
							.field( "myLocalDateField", entity6.getLocalDate() )
							.field( "myTextField", entity6.getText() )
							.objectField( "customBridgeOnClass", b2 -> b2
									.field( "text", entity6.getText() )
									.field( "date", entity6.getLocalDate() )
							)
					)
					.preparedThenExecuted();
			backendMock.expectWorks( OtherIndexedEntity.INDEX )
					.add( "4", b -> b
							.field( "numeric", entity4.getNumeric() )
							.field( "numericAsString", String.valueOf( entity4.getNumeric() ) )
					)
					.preparedThenExecuted();
			backendMock.expectWorks( YetAnotherIndexedEntity.INDEX )
					.add( "5", b -> b
							.field( "myLocalDateField", entity5.getLocalDate() )
							.field( "numeric", entity5.getNumeric() )
							.field( "optionalText", entity5.getOptionalText().get() )
							.field( "optionalInt", entity5.getOptionalInt().getAsInt() )
							.field( "optionalIntAsString", String.valueOf( entity5.getOptionalInt().getAsInt() ) )
							.field( "numericArray", entity5.getNumericArray()[0] )
							.field( "numericArray", entity5.getNumericArray()[1] )
							.field( "numericArray", entity5.getNumericArray()[2] )
							.objectField( "embeddedIterable", b2 -> b2
									.objectField( "embedded", b3 -> b3
											.field( "prefix_myTextField", entity1.getEmbedded().getText() )
									)
							)
							.objectField( "embeddedIterable", b2 -> b2
									.objectField( "embedded", b3 -> b3
											.field( "prefix_myTextField", entity2.getEmbedded().getText() )
									)
							)
							.objectField( "embeddedList", b2 -> b2
									.objectField( "otherPrefix_embedded", b3 -> b3
											.objectField( "prefix_customBridgeOnClass", b4 -> b4
													.field( "text", entity2.getEmbedded().getText() )
											)
									)
							)
							.objectField( "embeddedList", b2 -> b2
									.objectField( "otherPrefix_embedded", b3 -> b3
											.objectField( "prefix_customBridgeOnClass", b4 -> b4
													.field( "text", entity3.getEmbedded().getText() )
											)
									)
							)
							.objectField( "embeddedList", b2 -> { } )
							.objectField( "embeddedArrayList", b2 -> b2
									.objectField( "embedded", b3 -> b3
											.objectField( "prefix_customBridgeOnProperty", b4 -> b4
													.field( "text", entity3.getEmbedded().getEmbedded().getText() )
											)
									)
							)
							.objectField( "embeddedArrayList", b2 -> b2
									.objectField( "embedded", b3 -> b3
											.objectField( "prefix_customBridgeOnProperty", b4 -> b4
													.field( "text", entity1.getEmbedded().getEmbedded().getText() )
											)
									)
							)
							.field( "embeddedMapKeys", "entity3", "entity2" )
							.objectField( "embeddedMap", b2 -> b2
									.objectField( "embedded", b3 -> b3
											.field( "prefix_myLocalDateField", entity3.getEmbedded().getLocalDate() )
									)
							)
							.objectField( "embeddedMap", b2 -> b2
									.objectField( "embedded", b3 -> b3
											.field( "prefix_myLocalDateField", entity2.getEmbedded().getLocalDate() )
									)
							)
							.objectField( "embeddedMap", b2 -> b2
									.objectField( "embedded", b3 -> b3
											.field( "prefix_myLocalDateField", entity3.getEmbedded().getLocalDate() )
									)
							)
					)
					.preparedThenExecuted();
		}
	}

	@Test
	public void search() {
		try ( PojoSearchManager manager = mapping.createSearchManager() ) {
			SearchQuery<PojoReference> query = manager.search(
					Arrays.asList( IndexedEntity.class, YetAnotherIndexedEntity.class )
			)
					.query()
					.asReferences()
					.predicate().matchAll().end()
					.build();
			query.setFirstResult( 3L );
			query.setMaxResults( 2L );

			backendMock.expectSearchReferences(
					Arrays.asList( IndexedEntity.INDEX, YetAnotherIndexedEntity.INDEX ),
					b -> b
							.firstResultIndex( 3L )
							.maxResultsCount( 2L ),
					StubSearchWorkBehavior.of(
							6L,
							c -> c.collectReference( reference( IndexedEntity.INDEX, "0" ) ),
							c -> c.collectReference( reference( YetAnotherIndexedEntity.INDEX, "1" ) )
					)
			);

			assertThat( query )
					.hasHitsExactOrder(
							new PojoReferenceImpl( IndexedEntity.class, 0 ),
							new PojoReferenceImpl( YetAnotherIndexedEntity.class, 1 )
					)
					.hasHitCount( 6 );
			backendMock.verifyExpectationsMet();
		}
	}

	@Test
	public void search_projection() {
		try ( PojoSearchManager manager = mapping.createSearchManager() ) {
			SearchQuery<List<?>> query = manager.search(
					Arrays.asList( IndexedEntity.class, YetAnotherIndexedEntity.class )
			)
					.query()
					.asProjections(
							"myTextField",
							ProjectionConstants.REFERENCE,
							"myLocalDateField",
							ProjectionConstants.DOCUMENT_REFERENCE,
							"customBridgeOnClass.text"
					)
					.predicate().matchAll().end()
					.build();
			query.setFirstResult( 3L );
			query.setMaxResults( 2L );

			backendMock.expectSearchProjections(
					Arrays.asList( IndexedEntity.INDEX, YetAnotherIndexedEntity.INDEX ),
					b -> b
							.firstResultIndex( 3L )
							.maxResultsCount( 2L ),
					StubSearchWorkBehavior.of(
							2L,
							c -> {
								c.collectProjection( "text1" );
								c.collectReference( reference( IndexedEntity.INDEX, "0" ) );
								c.collectProjection( LocalDate.of( 2017, 11, 1 ) );
								c.collectProjection( reference( IndexedEntity.INDEX, "0" ) );
								c.collectProjection( "text2" );
							},
							c -> {
								c.collectProjection( null );
								c.collectReference( reference( YetAnotherIndexedEntity.INDEX, "1" ) );
								c.collectProjection( LocalDate.of( 2017, 11, 2 ) );
								c.collectProjection( reference( YetAnotherIndexedEntity.INDEX, "1" ) );
								c.collectProjection( null );
							}
					)
			);

			assertThat( query )
					.hasHitsExactOrder(
							Arrays.asList(
									"text1",
									new PojoReferenceImpl( IndexedEntity.class, 0 ),
									LocalDate.of( 2017, 11, 1 ),
									reference( IndexedEntity.INDEX, "0" ),
									"text2"
							),
							Arrays.asList(
									null,
									new PojoReferenceImpl( YetAnotherIndexedEntity.class, 1 ),
									LocalDate.of( 2017, 11, 2 ),
									reference( YetAnotherIndexedEntity.INDEX, "1" ),
									null
							)
					)
					.hasHitCount( 2L );
			backendMock.verifyExpectationsMet();
		}
	}

	public static class ParentIndexedEntity {

		private LocalDate localDate;

		private IndexedEntity embedded;

		public LocalDate getLocalDate() {
			return localDate;
		}

		public void setLocalDate(LocalDate localDate) {
			this.localDate = localDate;
		}

		public IndexedEntity getEmbedded() {
			return embedded;
		}

		public void setEmbedded(IndexedEntity embedded) {
			this.embedded = embedded;
		}

	}

	public static final class IndexedEntity extends ParentIndexedEntity {

		public static final String INDEX = "IndexedEntity";

		// TODO make it work with a primitive int too
		private Integer id;

		private String text;

		private List<ParentIndexedEntity> embeddingAsSingle = new ArrayList<>();

		private List<YetAnotherIndexedEntity> embeddingAsIterable = new ArrayList<>();

		private List<YetAnotherIndexedEntity> embeddingAsList = new ArrayList<>();

		private List<YetAnotherIndexedEntity> embeddingAsArrayList = new ArrayList<>();

		private List<YetAnotherIndexedEntity> embeddingAsMap = new ArrayList<>();

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public List<ParentIndexedEntity> getEmbeddingAsSingle() {
			return embeddingAsSingle;
		}

		public List<YetAnotherIndexedEntity> getEmbeddingAsIterable() {
			return embeddingAsIterable;
		}

		public List<YetAnotherIndexedEntity> getEmbeddingAsList() {
			return embeddingAsList;
		}

		public List<YetAnotherIndexedEntity> getEmbeddingAsArrayList() {
			return embeddingAsArrayList;
		}

		public List<YetAnotherIndexedEntity> getEmbeddingAsMap() {
			return embeddingAsMap;
		}
	}

	public static final class OtherIndexedEntity {

		public static final String INDEX = "OtherIndexedEntity";

		// TODO make it work with a primitive int too
		private Integer id;

		private Integer numeric;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Integer getNumeric() {
			return numeric;
		}

		public void setNumeric(Integer numeric) {
			this.numeric = numeric;
		}

	}

	public static final class YetAnotherIndexedEntity extends ParentIndexedEntity {

		public static final String INDEX = "YetAnotherIndexedEntity";

		private Integer id;

		private Integer numeric;

		private String optionalText;

		private Integer optionalInt;

		private Integer[] numericArray;

		private Iterable<IndexedEntity> embeddedIterable;

		private List<IndexedEntity> embeddedList;

		private ArrayList<IndexedEntity> embeddedArrayList;

		private Map<String, List<IndexedEntity>> embeddedMap;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public Integer getNumeric() {
			return numeric;
		}

		public void setNumeric(Integer numeric) {
			this.numeric = numeric;
		}

		public Optional<String> getOptionalText() {
			return Optional.ofNullable( optionalText );
		}

		public void setOptionalText(Optional<String> text) {
			this.optionalText = text.orElse( null );
		}

		public OptionalInt getOptionalInt() {
			return optionalInt == null ? OptionalInt.empty() : OptionalInt.of( optionalInt );
		}

		public void setOptionalInt(OptionalInt value) {
			this.optionalInt = value.isPresent() ? value.getAsInt() : null;
		}

		public Integer[] getNumericArray() {
			return numericArray;
		}

		public void setNumericArray(Integer[] numericArray) {
			this.numericArray = numericArray;
		}

		public Iterable<IndexedEntity> getEmbeddedIterable() {
			return embeddedIterable;
		}

		public void setEmbeddedIterable(Iterable<IndexedEntity> embeddedIterable) {
			this.embeddedIterable = embeddedIterable;
		}

		public List<IndexedEntity> getEmbeddedList() {
			return embeddedList;
		}

		public void setEmbeddedList(List<IndexedEntity> embeddedList) {
			this.embeddedList = embeddedList;
		}

		public ArrayList<IndexedEntity> getEmbeddedArrayList() {
			return embeddedArrayList;
		}

		public void setEmbeddedArrayList(ArrayList<IndexedEntity> embeddedArrayList) {
			this.embeddedArrayList = embeddedArrayList;
		}

		public Map<String, List<IndexedEntity>> getEmbeddedMap() {
			return embeddedMap;
		}

		public void setEmbeddedMap(Map<String, List<IndexedEntity>> embeddedMap) {
			this.embeddedMap = embeddedMap;
		}
	}

}
