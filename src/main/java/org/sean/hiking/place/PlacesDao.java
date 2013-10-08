package org.sean.hiking.place;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

@RegisterMapper(PlaceMapper.class)
@UseStringTemplate3StatementLocator
public interface PlacesDao {
	@SqlUpdate("INSERT INTO places (name, latitude, longitude, elevation, type, tile, is_public, created_by, creation_time, last_updated_by, last_updated_time) VALUES (:name, :latitude, :longitude, :elevation, :type, :tile, :isPublic, :createdBy, :creationTime, :lastUpdatedBy, :lastUpdatedTime)")
	void insert(@Bind("name") String name,
			@Bind("latitude") double latitude,
			@Bind("longitude") double longitude,
			@Bind("elevation") int elevation,
			@Bind("type") String type,
			@Bind("tile") String tile,
			@Bind("isPublic") int isPublic,
			@Bind("createdBy") int createdBy,
			@Bind("creationTime") String creationTime,
			@Bind("lastUpdatedBy") int lastUpdatedBy,
			@Bind("lastUpdatedTime") String lsatUpdatedTime
			);
	
	@SqlUpdate("UPDATE places SET last_updated_by = :lastUpdatedBy, last_updated_date = :lastUpdatedDate WHERE id = :id")
	void updateAuditColumns(@Bind("id") int id,
			@Bind("lastUpdatedBy") int lastUpdatedBy,
			@Bind("lastUpdatedDate") String lastUpdatedDate);
	
	@SqlUpdate("DELETE FROM places WHERE id = :id")
	void delete(@Bind("id") int id);
			
	@SqlQuery("select * from places")
	List<Place> findAll();
	
	@SqlUpdate("UPDATE places set latitude = :lat, longitude = :lng WHERE id = :id")
	void updateLocation(@Bind("id") int id, @Bind("lat") double lat, @Bind("lng") double lng);
	
	@SqlUpdate("UPDATE places set elevation = :elevation WHERE id = :id")
	void updateElevation(@Bind("id") int id, @Bind("elevation") int elevation);
	
	@SqlUpdate("UPDATE places set name = :name WHERE id = :id")
	void updateName(@Bind("id") int id, @Bind("name") String name);
	
	@SqlQuery("select * from places where id in ( <ids> )")
	List<Place> findPlacesByIds(@BindIn("ids") List<Integer> ids);
	
	@SqlUpdate("UPDATE places set type = :type WHERE id = :id")
	void updateType(@Bind("id") int it, @Bind("type") String type);
	
	@SqlUpdate("UPDATE places set tile = concat(floor((longitude+200)*10),'_', floor((latitude+100)*10)) WHERE id = :id")
	void updateTile(@Bind("id") int id);
	
	@SqlQuery("select * from places where tile = :tile")
	List<Place> findPlacesForTile(@Bind("tile") String tile);
	
	@SqlQuery("select * from places where tile in ( <tiles> )")
	List<Place> findPlacesForTiles(@BindIn("tiles") List<String> tiles);
	
	@SqlQuery("select * from places WHERE id = :id")
	Place findPlaceForId(@Bind("id") int id);
	
	@SqlQuery("SELECT LAST_INSERT_ID()")
	int lastInsertId();
}