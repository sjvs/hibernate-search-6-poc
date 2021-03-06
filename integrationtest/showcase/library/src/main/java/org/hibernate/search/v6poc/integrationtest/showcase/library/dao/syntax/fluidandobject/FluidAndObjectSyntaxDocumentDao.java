/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.showcase.library.dao.syntax.fluidandobject;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;

import org.hibernate.search.v6poc.entity.orm.hibernate.FullTextSession;
import org.hibernate.search.v6poc.entity.orm.jpa.FullTextQuery;
import org.hibernate.search.v6poc.entity.orm.jpa.FullTextSearchTarget;
import org.hibernate.search.v6poc.integrationtest.showcase.library.dao.DocumentDao;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.Book;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.BookMedium;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.Document;
import org.hibernate.search.v6poc.integrationtest.showcase.library.model.LibraryService;
import org.hibernate.search.v6poc.search.SearchPredicate;
import org.hibernate.search.v6poc.search.dsl.predicate.BooleanJunctionPredicateContext;
import org.hibernate.search.v6poc.spatial.DistanceUnit;
import org.hibernate.search.v6poc.spatial.GeoPoint;

class FluidAndObjectSyntaxDocumentDao extends DocumentDao {
	FluidAndObjectSyntaxDocumentDao(EntityManager entityManager) {
		super( entityManager );
	}

	@Override
	public Optional<Book> getByIsbn(String isbnAsString) {
		if ( isbnAsString == null ) {
			return Optional.empty();
		}

		// Must use Hibernate ORM types (as opposed to JPA types) to benefit from query.uniqueResult()
		FullTextSession fullTextSession = entityManager.unwrap( FullTextSession.class );

		org.hibernate.search.v6poc.entity.orm.hibernate.FullTextQuery<Book> query =
				fullTextSession.search( Book.class ).query()
				.asEntities()
				.predicate().match().onField( "isbn" ).matching( isbnAsString ).end()
				.build();

		return Optional.ofNullable( query.uniqueResult() );
	}

	@Override
	public List<Book> searchByMedium(String terms, BookMedium medium, int offset, int limit) {
		FullTextSearchTarget<Book> target = entityManager.search( Book.class );
		BooleanJunctionPredicateContext<SearchPredicate> booleanBuilder = target.predicate().bool();

		if ( terms != null && !terms.isEmpty() ) {
			booleanBuilder.must().match()
					.onField( "title" ).boostedTo( 2.0f )
					.orField( "summary" )
					.matching( terms );
		}

		booleanBuilder.must().nested().onObjectField( "copies" )
				// Bridged query with value bridge: TODO rely on the bridge to convert to a String
				.match().onField( "copies.medium" ).matching( medium.name() );

		FullTextQuery<Book> query = entityManager.search( Book.class ).query()
				.asEntities()
				.predicate( booleanBuilder.end() )
				.sort().byField( "title_sort" ).end()
				.build();

		query.setFirstResult( offset );
		query.setMaxResults( limit );

		return query.getResultList();
	}

	@Override
	public List<Document<?>> searchAroundMe(String terms, String tags,
			GeoPoint myLocation, Double maxDistanceInKilometers,
			List<LibraryService> libraryServices,
			int offset, int limit) {
		FullTextSearchTarget<Document<?>> target = entityManager.search( DOCUMENT_CLASS );
		BooleanJunctionPredicateContext<SearchPredicate> booleanBuilder = target.predicate().bool();

		// Match query
		if ( terms != null && !terms.isEmpty() ) {
			booleanBuilder.must().match()
					.onField( "title" ).boostedTo( 2.0f )
					.orField( "summary" )
					.matching( terms );
		}

		// Bridged query with complex bridge: TODO rely on the bridge to split the String
		String[] splitTags = tags == null ? null : tags.split( "," );
		if ( splitTags != null && splitTags.length > 0 ) {
			for ( String tag : splitTags ) {
				booleanBuilder.must().match()
						.onField( "tags" )
						.matching( tag );
			}
		}

		// Spatial query

		if ( myLocation != null && maxDistanceInKilometers != null ) {
			booleanBuilder.must().nested().onObjectField( "copies" ).spatial()
					.within()
					.onField( "copies.library.location" )
					.circle( myLocation, maxDistanceInKilometers, DistanceUnit.KILOMETERS );
		}

		// Nested query + must loop
		if ( libraryServices != null && !libraryServices.isEmpty() ) {
			BooleanJunctionPredicateContext<?> nestedBoolean =
					booleanBuilder.must().nested().onObjectField( "copies" ).bool();
			for ( LibraryService service : libraryServices ) {
				nestedBoolean.must().match()
						.onField( "copies.library.services" )
						// Bridged query with value bridge: TODO rely on the bridge to convert to a String
						.matching( service.name() );
			}
		}

		FullTextQuery<Document<?>> query = target.query()
				.asEntities()
				.predicate( booleanBuilder.end() )
				// TODO facets (tag, medium, library in particular)
				.sort().byScore().end()
				.build();

		query.setFirstResult( offset );
		query.setMaxResults( limit );

		return query.getResultList();
	}
}
