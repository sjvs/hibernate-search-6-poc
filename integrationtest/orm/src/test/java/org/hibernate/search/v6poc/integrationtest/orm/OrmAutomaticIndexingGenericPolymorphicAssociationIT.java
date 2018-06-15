/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.orm;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.search.v6poc.entity.orm.cfg.SearchOrmSettings;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Field;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.rule.BackendMock;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.index.impl.StubBackendFactory;
import org.hibernate.search.v6poc.util.impl.integrationtest.orm.OrmUtils;
import org.hibernate.service.ServiceRegistry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test automatic indexing based on Hibernate ORM entity events when polymorphic associations using generics are involved.
 */
public class OrmAutomaticIndexingGenericPolymorphicAssociationIT {

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
				.addAnnotatedClass( MiddleContainingEntity.class )
				.addAnnotatedClass( UnrelatedContainingEntity.class )
				.addAnnotatedClass( ContainedEntity.class );

		Metadata metadata = ms.buildMetadata();

		final SessionFactoryBuilder sfb = metadata.getSessionFactoryBuilder();

		backendMock.expectSchema( IndexedEntity.INDEX, b -> b
				.objectField( "child", b3 -> b3
						.objectField( "containedSingle", b2 -> b2
								.field( "includedInSingle", String.class )
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
	public void inversePathHandlesGenericTypes() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			IndexedEntity indexedEntity = new IndexedEntity();
			indexedEntity.setId( 1 );

			MiddleContainingEntity middleContainingEntity = new MiddleContainingEntity();
			middleContainingEntity.setId( 2 );
			indexedEntity.setChild( middleContainingEntity );
			middleContainingEntity.setParent( indexedEntity );

			/*
			 * The automatic reindexing process should detect that the containing entity
			 * is a MiddleContainingEntity and thus has a parent property that can
			 * be used to get back to the indexed entity.
			 */
			ContainedEntity<MiddleContainingEntity> containedEntity1 = new ContainedEntity<>();
			containedEntity1.setId( 3 );
			containedEntity1.setIncludedInSingle( "initialValue" );
			middleContainingEntity.setContainedSingle( containedEntity1 );
			containedEntity1.getContainingAsSingle().add( middleContainingEntity );

			session.persist( containedEntity1 );
			session.persist( middleContainingEntity );
			session.persist( indexedEntity );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.add( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedSingle", b3 -> b3
											.field( "includedInSingle", "initialValue" )
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();

		// Test updating the value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			@SuppressWarnings("unchecked")
			ContainedEntity<MiddleContainingEntity> containedEntity = session.get( ContainedEntity.class, 3 );
			containedEntity.setIncludedInSingle( "updatedValue" );

			backendMock.expectWorks( IndexedEntity.INDEX )
					.update( "1", b -> b
							.objectField( "child", b2 -> b2
									.objectField( "containedSingle", b3 -> b3
											.field( "includedInSingle", "updatedValue" )
									)
							)
					)
					.preparedThenExecuted();
		} );
		backendMock.verifyExpectationsMet();
	}

	@Test
	public void inversePathIgnoresUnrelatedTypes() {
		OrmUtils.withinTransaction( sessionFactory, session -> {
			UnrelatedContainingEntity unrelatedContainingEntity = new UnrelatedContainingEntity();
			unrelatedContainingEntity.setId( 1 );

			/*
			 * The automatic reindexing process should detect that the containing entity
			 * is a UnrelatedContainingEntity and thus doesn't have any parent property that can
			 * be used to get back to the indexed entity.
			 */
			ContainedEntity<UnrelatedContainingEntity> containedEntity1 = new ContainedEntity<>();
			containedEntity1.setId( 2 );
			containedEntity1.setIncludedInSingle( "initialValue" );
			unrelatedContainingEntity.setContainedSingle( containedEntity1 );
			containedEntity1.getContainingAsSingle().add( unrelatedContainingEntity );

			session.persist( containedEntity1 );
			session.persist( unrelatedContainingEntity );

			// Do not expect any work
		} );
		backendMock.verifyExpectationsMet();

		// Test updating the value
		OrmUtils.withinTransaction( sessionFactory, session -> {
			@SuppressWarnings("unchecked")
			ContainedEntity<UnrelatedContainingEntity> containedEntity = session.get( ContainedEntity.class, 2 );
			containedEntity.setIncludedInSingle( "updatedValue" );

			// Do not expect any work
		} );
		backendMock.verifyExpectationsMet();
	}

	@Entity(name = "indexed")
	@Indexed(index = IndexedEntity.INDEX)
	public static class IndexedEntity {
		static final String INDEX = "IndexedEntity";

		@Id
		private Integer id;

		@OneToOne(mappedBy = "parent")
		@IndexedEmbedded
		private MiddleContainingEntity child;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public MiddleContainingEntity getChild() {
			return child;
		}

		public void setChild(MiddleContainingEntity child) {
			this.child = child;
		}
	}

	@Entity(name = "containing")
	public static class ContainingEntity<S extends ContainingEntity<S>> {
		@Id
		private Integer id;

		@ManyToOne(targetEntity = ContainedEntity.class)
		@IndexedEmbedded
		private ContainedEntity<S> containedSingle;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public ContainedEntity<S> getContainedSingle() {
			return containedSingle;
		}

		public void setContainedSingle(ContainedEntity<S> containedSingle) {
			this.containedSingle = containedSingle;
		}
	}

	@Entity(name = "middle")
	public static class MiddleContainingEntity extends ContainingEntity<MiddleContainingEntity> {
		@OneToOne
		private IndexedEntity parent;

		public IndexedEntity getParent() {
			return parent;
		}

		public void setParent(IndexedEntity parent) {
			this.parent = parent;
		}
	}

	@Entity(name = "unrelated")
	public static class UnrelatedContainingEntity extends ContainingEntity<UnrelatedContainingEntity> {
		@Transient
		public IndexedEntity getParent() {
			Assert.fail( "This method should never have been called" );
			return null; // Dead code
		}
	}

	@Entity(name = "contained")
	public static class ContainedEntity<C extends ContainingEntity<C>> {

		@Id
		private Integer id;

		@OneToMany(mappedBy = "containedSingle", targetEntity = ContainingEntity.class)
		@OrderBy("id asc") // Make sure the iteration order is predictable
		private List<C> containingAsSingle = new ArrayList<>();

		@Basic
		@Field
		private String includedInSingle;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public List<C> getContainingAsSingle() {
			return containingAsSingle;
		}

		public String getIncludedInSingle() {
			return includedInSingle;
		}

		public void setIncludedInSingle(String includedInSingle) {
			this.includedInSingle = includedInSingle;
		}
	}

}
