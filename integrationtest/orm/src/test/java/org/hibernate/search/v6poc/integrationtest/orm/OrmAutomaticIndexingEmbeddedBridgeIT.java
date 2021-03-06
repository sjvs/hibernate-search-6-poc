/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.IndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.v6poc.entity.model.SearchModel;
import org.hibernate.search.v6poc.entity.orm.cfg.SearchOrmSettings;
import org.hibernate.search.v6poc.entity.pojo.bridge.PropertyBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.TypeBridge;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.TypeBridgeMapping;
import org.hibernate.search.v6poc.entity.pojo.bridge.declaration.TypeBridgeReference;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.v6poc.entity.pojo.model.PojoElement;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelType;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.rule.BackendMock;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.index.impl.StubBackendFactory;
import org.hibernate.search.v6poc.util.impl.integrationtest.orm.OrmUtils;
import org.hibernate.service.ServiceRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test automatic indexing based on Hibernate ORM entity events when
 * {@link TypeBridge}s or {@link PropertyBridge}s are embedded in an {@link IndexedEmbedded}.
 */
public class OrmAutomaticIndexingEmbeddedBridgeIT {
	private static final String PREFIX = SearchOrmSettings.PREFIX;

	@Rule
	public BackendMock backendMock = new BackendMock( "stubBackend" );

	private SessionFactory sessionFactory;

	@Before
	public void setup() {
		StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
				.applySetting( PREFIX + "backend.stubBackend.type", StubBackendFactory.class.getName() )
				.applySetting( PREFIX + "index.default.backend", "stubBackend" );

		ServiceRegistry serviceRegistry = registryBuilder.build();

		MetadataSources ms = new MetadataSources( serviceRegistry )
				.addAnnotatedClass( IndexedEntity.class )
				.addAnnotatedClass( ContainingEntity.class )
				.addAnnotatedClass( ContainedEntity.class );

		Metadata metadata = ms.buildMetadata();

		final SessionFactoryBuilder sfb = metadata.getSessionFactoryBuilder();

		backendMock.expectSchema( IndexedEntity.INDEX, b -> b
				.objectField( "child", b2 -> b2
						.objectField( "firstBridge", b3 -> b3
								.field( "value", String.class )
						)
				)
		);

		sessionFactory = sfb.build();
		backendMock.verifyExpectationsMet();
	}

	@After
	public void cleanup() {
		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
	}

	@Test
	public void indirectValueUpdate_embeddedBridge() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity entity1 = new IndexedEntity();
			entity1.setId( 1 );

			ContainingEntity containingEntity1 = new ContainingEntity();
			containingEntity1.setId( 2 );
			entity1.setChild( containingEntity1 );
			containingEntity1.setParent( entity1 );

			ContainedEntity containedEntity1 = new ContainedEntity();
			containedEntity1.setId( 3 );
			containedEntity1.setIncludedInFirstBridge( "initialValue" );
			containingEntity1.setFirstContained( containedEntity1 );
			containedEntity1.getContainingAsFirstContained().add( containingEntity1 );

			ContainedEntity containedEntity2 = new ContainedEntity();
			containedEntity2.setId( 4 );
			containedEntity2.setIncludedInSecondBridge( "initialValue" );
			containingEntity1.setSecondContained( containedEntity2 );
			containedEntity2.getContainingAsSecondContained().add( containingEntity1 );

			session.persist( containedEntity1 );
			session.persist( containedEntity2 );
			session.persist( containingEntity1 );
			session.persist( entity1 );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "firstBridge", b3 -> b3
											.field( "value", "initialValue" )
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating a value used in an included bridge
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = session.get( ContainedEntity.class, 3 );
			containedEntity.setIncludedInFirstBridge( "updatedValue" );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "firstBridge", b3 -> b3
											.field( "value", "updatedValue" )
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		/*
		 * Test updating a value used in an excluded bridge
		 * (every index field filtered out by the IndexedEmbedded filter)
		 */
		OrmUtils.withinTransaction( sessionFactory, session -> {
			ContainedEntity containedEntity = session.get( ContainedEntity.class, 4 );
			containedEntity.setIncludedInSecondBridge( "updatedValue" );

			// Do not expect any work
		} );
		backendMock.verifyExpectationsMet();
	}

	@Entity(name = "containing")
	@FirstTypeBridgeAnnotation
	@SecondTypeBridgeAnnotation
	public static class ContainingEntity {

		@Id
		private Integer id;

		@OneToOne
		private IndexedEntity parent;

		@ManyToOne
		private ContainedEntity firstContained;

		@ManyToOne
		private ContainedEntity secondContained;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public IndexedEntity getParent() {
			return parent;
		}

		public void setParent(IndexedEntity parent) {
			this.parent = parent;
		}

		public ContainedEntity getFirstContained() {
			return firstContained;
		}

		public void setFirstContained(ContainedEntity firstContained) {
			this.firstContained = firstContained;
		}

		public ContainedEntity getSecondContained() {
			return secondContained;
		}

		public void setSecondContained(ContainedEntity secondContained) {
			this.secondContained = secondContained;
		}
	}

	@Entity(name = "indexed")
	@Indexed(index = IndexedEntity.INDEX)
	public static class IndexedEntity {
		static final String INDEX = "IndexedEntity";

		@Id
		private Integer id;

		@OneToOne(mappedBy = "parent")
		@IndexedEmbedded(includePaths = "firstBridge.value")
		private ContainingEntity child;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public ContainingEntity getChild() {
			return child;
		}

		public void setChild(ContainingEntity child) {
			this.child = child;
		}
	}

	@Entity(name = "contained")
	public static class ContainedEntity {

		@Id
		private Integer id;

		@OneToMany(mappedBy = "firstContained")
		@OrderBy("id asc") // Make sure the iteration order is predictable
		private List<ContainingEntity> containingAsFirstContained = new ArrayList<>();

		@OneToMany(mappedBy = "secondContained")
		@OrderBy("id asc") // Make sure the iteration order is predictable
		private List<ContainingEntity> containingAsSecondContained = new ArrayList<>();

		@Basic
		private String includedInFirstBridge;

		@Basic
		private String includedInSecondBridge;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public List<ContainingEntity> getContainingAsFirstContained() {
			return containingAsFirstContained;
		}

		public List<ContainingEntity> getContainingAsSecondContained() {
			return containingAsSecondContained;
		}

		public String getIncludedInFirstBridge() {
			return includedInFirstBridge;
		}

		public void setIncludedInFirstBridge(String includedInFirstBridge) {
			this.includedInFirstBridge = includedInFirstBridge;
		}

		public String getIncludedInSecondBridge() {
			return includedInSecondBridge;
		}

		public void setIncludedInSecondBridge(String includedInSecondBridge) {
			this.includedInSecondBridge = includedInSecondBridge;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	@TypeBridgeMapping(bridge = @TypeBridgeReference(type = FirstTypeBridge.class))
	public @interface FirstTypeBridgeAnnotation {
	}

	public static class FirstTypeBridge implements TypeBridge {

		private PojoModelElementAccessor<String> valueSourceAccessor;
		private IndexObjectFieldAccessor objectFieldAccessor;
		private IndexFieldAccessor<String> valueTargetAccessor;

		@Override
		public void bind(IndexSchemaElement indexSchemaElement, PojoModelType bridgedPojoModelType,
				SearchModel searchModel) {
			valueSourceAccessor = bridgedPojoModelType.property( "firstContained" )
					.property( "includedInFirstBridge" )
					.createAccessor( String.class );
			IndexSchemaObjectField objectField = indexSchemaElement.objectField( "firstBridge" );
			objectFieldAccessor = objectField.createAccessor();
			valueTargetAccessor = objectField.field( "value" ).asString().createAccessor();
		}

		@Override
		public void write(DocumentElement target, PojoElement source) {
			DocumentElement objectField = objectFieldAccessor.add( target );
			valueTargetAccessor.write( objectField, valueSourceAccessor.read( source ) );
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	@TypeBridgeMapping(bridge = @TypeBridgeReference(type = SecondTypeBridge.class))
	public @interface SecondTypeBridgeAnnotation {
	}

	public static class SecondTypeBridge implements TypeBridge {

		private PojoModelElementAccessor<String> valueSourceAccessor;
		private IndexObjectFieldAccessor objectFieldAccessor;
		private IndexFieldAccessor<String> valueTargetAccessor;

		@Override
		public void bind(IndexSchemaElement indexSchemaElement, PojoModelType bridgedPojoModelType,
				SearchModel searchModel) {
			valueSourceAccessor = bridgedPojoModelType.property( "secondContained" )
					.property( "includedInSecondBridge" )
					.createAccessor( String.class );
			IndexSchemaObjectField objectField = indexSchemaElement.objectField( "secondBridge" );
			objectFieldAccessor = objectField.createAccessor();
			valueTargetAccessor = objectField.field( "value" ).asString().createAccessor();
		}

		@Override
		public void write(DocumentElement target, PojoElement source) {
			DocumentElement objectField = objectFieldAccessor.add( target );
			valueTargetAccessor.write( objectField, valueSourceAccessor.read( source ) );
		}
	}
}
