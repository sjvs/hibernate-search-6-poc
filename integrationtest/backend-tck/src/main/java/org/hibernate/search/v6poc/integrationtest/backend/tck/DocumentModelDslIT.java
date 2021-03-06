/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.backend.tck;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaFieldContext;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaFieldTypedContext;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.v6poc.integrationtest.backend.tck.util.rule.SearchSetupHelper;
import org.hibernate.search.v6poc.logging.spi.EventContexts;
import org.hibernate.search.v6poc.util.SearchException;
import org.hibernate.search.v6poc.util.impl.common.CollectionHelper;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.FailureReportUtils;
import org.hibernate.search.v6poc.util.impl.test.SubTest;

import org.junit.Rule;
import org.junit.Test;

/**
 * Test the behavior of implementations of the document model definition DSL.
 * <p>
 * This does not check the effects of the model definition on the actual index schema,
 * since this would require backend-specific code to inspect that schema.
 * However, in search and projection tests, we check that defined fields behave correctly at runtime.
 */
public class DocumentModelDslIT {

	private static final String INDEX_NAME = "IndexName";

	@Rule
	public SearchSetupHelper setupHelper = new SearchSetupHelper();

	private static List<Function<IndexSchemaFieldContext, IndexSchemaFieldTypedContext<?>>> NON_ANALYZABLE_TYPES =
			CollectionHelper.toImmutableList( CollectionHelper.asList(
					IndexSchemaFieldContext::asInteger,
					IndexSchemaFieldContext::asLocalDate,
					IndexSchemaFieldContext::asGeoPoint
			) );

	@Test
	public void nullFieldName() {
		SubTest.expectException(
				"Null field name on root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.field( null );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name 'null' is invalid: field names cannot be null or empty" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.indexSchemaRoot()
				) );

		SubTest.expectException(
				"Null field name on non-root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.objectField( "nonRoot" ).field( null );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name 'null' is invalid: field names cannot be null or empty" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.fromIndexFieldAbsolutePath( "nonRoot" )
				) );

		SubTest.expectException(
				"Null object field name on root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.objectField( null );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name 'null' is invalid: field names cannot be null or empty" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.indexSchemaRoot()
				) );

		SubTest.expectException(
				"Null object field name on non-root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.objectField( "nonRoot" ).objectField( null );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name 'null' is invalid: field names cannot be null or empty" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.fromIndexFieldAbsolutePath( "nonRoot" )
				) );
	}

	@Test
	public void emptyFieldName() {
		SubTest.expectException(
				"empty field name on root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.field( "" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name '' is invalid: field names cannot be null or empty" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.indexSchemaRoot()
				) );

		SubTest.expectException(
				"empty field name on non-root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.objectField( "nonRoot" ).field( "" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name '' is invalid: field names cannot be null or empty" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.fromIndexFieldAbsolutePath( "nonRoot" )
				) );

		SubTest.expectException(
				"empty object field name on root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.objectField( "" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name '' is invalid: field names cannot be null or empty" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.indexSchemaRoot()
				) );

		SubTest.expectException(
				"empty object field name on non-root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.objectField( "nonRoot" ).objectField( "" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name '' is invalid: field names cannot be null or empty" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.fromIndexFieldAbsolutePath( "nonRoot" )
				) );
	}

	@Test
	public void dotInFieldName() {
		SubTest.expectException(
				"field name containing a dot on root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.field( "foo.bar" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name 'foo.bar' is invalid: field names cannot contain a dot ('.')." )
				.hasMessageContaining( " Remove the dot from your field name" )
				.hasMessageContaining( "if you are declaring the field in a bridge and want a tree of fields,"
						+ " declare an object field using the objectField() method." )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.indexSchemaRoot()
				) );

		SubTest.expectException(
				"field name containing a dot on non-root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.objectField( "nonRoot" ).field( "foo.bar" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name 'foo.bar' is invalid: field names cannot contain a dot ('.')." )
				.hasMessageContaining( " Remove the dot from your field name" )
				.hasMessageContaining( "if you are declaring the field in a bridge and want a tree of fields,"
						+ " declare an object field using the objectField() method." )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.fromIndexFieldAbsolutePath( "nonRoot" )
				) );

		SubTest.expectException(
				"object field name containing a dot on root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.objectField( "foo.bar" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name 'foo.bar' is invalid: field names cannot contain a dot ('.')." )
				.hasMessageContaining( " Remove the dot from your field name" )
				.hasMessageContaining( "if you are declaring the field in a bridge and want a tree of fields,"
						+ " declare an object field using the objectField() method." )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.indexSchemaRoot()
				) );

		SubTest.expectException(
				"object field name containing a dot on non-root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.objectField( "nonRoot" ).objectField( "foo.bar" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "Field name 'foo.bar' is invalid: field names cannot contain a dot ('.')." )
				.hasMessageContaining( " Remove the dot from your field name" )
				.hasMessageContaining( "if you are declaring the field in a bridge and want a tree of fields,"
						+ " declare an object field using the objectField() method." )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.fromIndexFieldAbsolutePath( "nonRoot" )
				) );
	}

	@Test
	public void nameCollision_fields() {
		SubTest.expectException(
				"Name collision between two fields on root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.field( "field1" );
					root.field( "field1" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "schema node 'field1' was added twice" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.indexSchemaRoot()
				) );

		SubTest.expectException(
				"Name collision between two fields on non-root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					IndexSchemaObjectField objectField1 = root.objectField( "object1" );
					IndexSchemaObjectField objectField2 = objectField1.objectField( "object2" );
					objectField2.field( "field1" );
					objectField2.field( "field1" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "schema node 'field1' was added twice" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.fromIndexFieldAbsolutePath( "object1.object2" )
				) );
	}

	@Test
	public void nameCollision_objectFields() {
		SubTest.expectException(
				"Name collision between two object fields on root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.objectField( "field1" );
					root.objectField( "field1" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "schema node 'field1' was added twice" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.indexSchemaRoot()
				) );

		SubTest.expectException(
				"Name collision between two object fields on non-root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					IndexSchemaObjectField objectField1 = root.objectField( "object1" );
					IndexSchemaObjectField objectField2 = objectField1.objectField( "object2" );
					objectField2.objectField( "field1" );
					objectField2.objectField( "field1" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "schema node 'field1' was added twice" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.fromIndexFieldAbsolutePath( "object1.object2" )
				) );
	}

	@Test
	public void nameCollision_fieldAndObjectField() {
		SubTest.expectException(
				"Name collision between two fields on root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					root.field( "field1" );
					root.objectField( "field1" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "schema node 'field1' was added twice" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.indexSchemaRoot()
				) );

		SubTest.expectException(
				"Name collision between two fields (object and non-object) on non-root",
				() -> setup( ctx -> {
					IndexSchemaElement root = ctx.getSchemaElement();
					IndexSchemaObjectField objectField1 = root.objectField( "object1" );
					IndexSchemaObjectField objectField2 = objectField1.objectField( "object2" );
					objectField2.field( "field1" );
					objectField2.objectField( "field1" );
				} )
		)
				.assertThrown()
				.isInstanceOf( SearchException.class )
				.hasMessageContaining( "schema node 'field1' was added twice" )
				.satisfies( FailureReportUtils.hasContext(
						EventContexts.fromIndexName( INDEX_NAME ),
						EventContexts.fromIndexFieldAbsolutePath( "object1.object2" )
				) );
	}

	@Test
	public void analyzerOnNonAnalyzableType() {
		for ( Function<IndexSchemaFieldContext, IndexSchemaFieldTypedContext<?>> typedContextFunction
				: NON_ANALYZABLE_TYPES ) {
			SubTest.expectException(
					"Setting an analyzer after " + typedContextFunction + " on schema root",
					() -> setup( ctx -> {
						IndexSchemaElement root = ctx.getSchemaElement();
						typedContextFunction.apply(
								root.field( "myField" )
						)
								.analyzer( "someAnalyzer" );
					} )
			)
					.assertThrown()
					.isInstanceOf( SearchException.class )
					.hasMessageContaining( "An analyzer was set on field 'myField'" )
					.hasMessageContaining( "fields of this type cannot be analyzed" )
					.satisfies( FailureReportUtils.hasContext(
							EventContexts.fromIndexName( INDEX_NAME ),
							EventContexts.fromIndexFieldAbsolutePath( "myField" )
					) );
			SubTest.expectException(
					"Setting an analyzer after " + typedContextFunction + " on non-root",
					() -> setup( ctx -> {
						IndexSchemaElement root = ctx.getSchemaElement();
						typedContextFunction.apply(
								root.objectField( "nonRoot" ).field( "myField" )
						)
								.analyzer( "someAnalyzer" );
					} )
			)
					.assertThrown()
					.isInstanceOf( SearchException.class )
					.hasMessageContaining( "An analyzer was set on field 'myField'" )
					.hasMessageContaining( "fields of this type cannot be analyzed" )
					.satisfies( FailureReportUtils.hasContext(
							EventContexts.fromIndexName( INDEX_NAME ),
							EventContexts.fromIndexFieldAbsolutePath( "nonRoot.myField" )
					) );
		}
	}

	@Test
	public void normalizerOnNonAnalyzableType() {
		for ( Function<IndexSchemaFieldContext, IndexSchemaFieldTypedContext<?>> typedContextFunction
				: NON_ANALYZABLE_TYPES ) {
			SubTest.expectException(
					"Setting a normalizer after " + typedContextFunction + " on schema root",
					() -> setup( ctx -> {
						IndexSchemaElement root = ctx.getSchemaElement();
						typedContextFunction.apply(
								root.field( "myField" )
						)
								.normalizer( "someNormalizer" );
					} )
			)
					.assertThrown()
					.isInstanceOf( SearchException.class )
					.hasMessageContaining( "A normalizer was set on field 'myField'" )
					.hasMessageContaining( "fields of this type cannot be analyzed" )
					.satisfies( FailureReportUtils.hasContext(
							EventContexts.fromIndexName( INDEX_NAME ),
							EventContexts.fromIndexFieldAbsolutePath( "myField" )
					) );
			SubTest.expectException(
					"Setting a normalizer after " + typedContextFunction + " on non-root",
					() -> setup( ctx -> {
						IndexSchemaElement root = ctx.getSchemaElement();
						typedContextFunction.apply(
								root.objectField( "nonRoot" ).field( "myField" )
						)
								.normalizer( "someNormalizer" );
					} )
			)
					.assertThrown()
					.isInstanceOf( SearchException.class )
					.hasMessageContaining( "A normalizer was set on field 'myField'" )
					.hasMessageContaining( "fields of this type cannot be analyzed" )
					.satisfies( FailureReportUtils.hasContext(
							EventContexts.fromIndexName( INDEX_NAME ),
							EventContexts.fromIndexFieldAbsolutePath( "nonRoot.myField" )
					) );
		}
	}

	private void setup(Consumer<IndexModelBindingContext> mappingContributor) {
		setupHelper.withDefaultConfiguration()
				.withIndex(
						"MappedType", INDEX_NAME,
						mappingContributor,
						indexManager -> { }
				)
				.setup();
	}

}
