/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.bridge.builtin.spatial.impl;

import java.util.function.Function;
import java.util.stream.Collector;

import org.hibernate.search.v6poc.backend.document.model.spi.IndexSchemaElement;
import org.hibernate.search.v6poc.backend.document.spi.DocumentState;
import org.hibernate.search.v6poc.backend.document.spi.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.spatial.GeoPoint;
import org.hibernate.search.v6poc.entity.model.spi.SearchModel;
import org.hibernate.search.v6poc.entity.pojo.bridge.builtin.spatial.GeoPointBridge;
import org.hibernate.search.v6poc.backend.spatial.ImmutableGeoPoint;
import org.hibernate.search.v6poc.entity.pojo.bridge.spi.Bridge;
import org.hibernate.search.v6poc.engine.spi.BuildContext;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoState;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoModelElement;
import org.hibernate.search.v6poc.util.SearchException;
import org.hibernate.search.v6poc.util.StreamHelper;

/**
 * @author Yoann Rodiere
 */
public class GeoPointBridgeImpl implements Bridge<GeoPointBridge> {

	private GeoPointBridge parameters;

	private IndexFieldAccessor<GeoPoint> fieldAccessor;
	private Function<PojoState, GeoPoint> coordinatesExtractor;

	@Override
	public void initialize(BuildContext buildContext, GeoPointBridge parameters) {
		this.parameters = parameters;
	}

	@Override
	public void contribute(IndexSchemaElement indexSchemaElement, PojoModelElement bridgedPojoModelElement,
			SearchModel searchModel) {
		String fieldName = parameters.fieldName();

		if ( fieldName.isEmpty() ) {
			// TODO retrieve the default name somehow when parameters.name() is empty
			throw new UnsupportedOperationException( "Default field name not implemented yet" );
		}

		fieldAccessor = indexSchemaElement.field( fieldName ).asGeoPoint().createAccessor();

		if ( bridgedPojoModelElement.isAssignableTo( GeoPoint.class ) ) {
			PojoModelElementAccessor<GeoPoint> sourceAccessor = bridgedPojoModelElement.createAccessor( GeoPoint.class );
			coordinatesExtractor = sourceAccessor::read;
		}
		else {
			String markerSet = parameters.markerSet();

			PojoModelElementAccessor<Double> latitudeAccessor = bridgedPojoModelElement.properties()
					.filter( model -> model.markers( GeoPointBridge.Latitude.class )
							.anyMatch( m -> markerSet.equals( m.markerSet() ) ) )
					.collect( singleMarkedProperty( "latitude", fieldName, markerSet ) )
					.createAccessor( Double.class );
			PojoModelElementAccessor<Double> longitudeAccessor = bridgedPojoModelElement.properties()
					.filter( model -> model.markers( GeoPointBridge.Longitude.class )
							.anyMatch( m -> markerSet.equals( m.markerSet() ) ) )
					.collect( singleMarkedProperty( "longitude", fieldName, markerSet ) )
					.createAccessor( Double.class );

			coordinatesExtractor = bridgedElement -> {
				Double latitude = latitudeAccessor.read( bridgedElement );
				Double longitude = longitudeAccessor.read( bridgedElement );

				if ( latitude == null || longitude == null ) {
					return null;
				}

				return new ImmutableGeoPoint( latitude, longitude );
			};
		}
	}

	private static Collector<PojoModelElement, ?, PojoModelElement> singleMarkedProperty(
			String markerName, String fieldName, String markerSet) {
		return StreamHelper.singleElement(
				() -> new SearchException( "Could not find a property with the " + markerName
						+ " marker for field '" + fieldName + "' (marker set: '" + markerSet + "')" ),
				() -> new SearchException( "Found multiple properties with the " + markerName
						+ " marker for field '" + fieldName + "' (marker set: '" + markerSet + "')" )
				);
	}

	@Override
	public void write(DocumentState target, PojoState source) {
		GeoPoint coordinates = coordinatesExtractor.apply( source );
		fieldAccessor.write( target, coordinates );
	}

}