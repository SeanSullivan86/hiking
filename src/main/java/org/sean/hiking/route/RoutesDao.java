package org.sean.hiking.route;

import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

@UseStringTemplate3StatementLocator
public interface RoutesDao {
	@SqlUpdate("INSERT INTO routes (start, end, distance, elevation_gain, reverse_gain, type, subtype, name, is_public, created_by, creation_time, last_updated_by, last_updated_time) VALUES (:start, :end, :distance, :elevation_gain, :reverse_gain, :type, :subtype, :name, :isPublic, :createdBy, :creationTime, :lastUpdatedBy, :lastUpdatedTime)")
	void insertRoute(@Bind("start") int start,
			@Bind("end") int end,
			@Bind("distance") int distance,
			@Bind("elevation_gain") int elevationGain,
			@Bind("reverse_gain") int reverseGain,
			@Bind("type") String type,
			@Bind("subtype") String subtype,
			@Bind("name") String name,
			@Bind("isPublic") int isPublic,
			@Bind("createdBy") int createdBy,
			@Bind("creationTime") String creationTime,
			@Bind("lastUpdatedBy") int lastUpdatedBy,
			@Bind("lastUpdatedTime") String lastUpdatedTime
			);
	
	@SqlUpdate("UPDATE routes SET start = :start, end = :end, distance = :distance, elevation_gain = :elevation_gain, reverse_gain = :reverse_gain, type = :type, subtype = :subtype, name = :name, is_public = :isPublic, last_updated_by = :lastUpdatedBy, last_updated_time = :lastUpdatedTime WHERE id = :id")
	void updateRoute(@Bind("id") int id,
			@Bind("start") int start,
			@Bind("end") int end,
			@Bind("distance") int distance,
			@Bind("elevation_gain") int elevationGain,
			@Bind("reverse_gain") int reverseGain,
			@Bind("type") String type,
			@Bind("subtype") String subtype,
			@Bind("name") String name,
			@Bind("isPublic") int isPublic,
			@Bind("lastUpdatedBy") int lastUpdatedBy,
			@Bind("lastUpdatedTime") String lastUpdatedTime);
	
	@SqlUpdate("UPDATE routes SET last_updated_by = :lastUpdatedBy, last_updated_time = :lastUpdatedTime WHERE id = :id")
	void updateAuditColumns(@Bind("id") int id,
			@Bind("lastUpdatedBy") int lastUpdatedBy,
			@Bind("lastUpdatedTime") String lastUpdatedTime);
			
	@SqlUpdate("delete from routes where id = :id")
	void deleteRoute(@Bind("id") int id);
	
	@SqlUpdate("insert into route_tiles (route, tile) VALUES (:route, :tile)")
	void insertRouteTile(@Bind("route") int route,
			@Bind("tile") String tile);
	
	@SqlUpdate("insert into route_points (route, sequence, latitude, longitude) VALUES (:route, :sequence, :latitude, :longitude)")
	void insertRoutePoint(@Bind("route") int route,
			@Bind("sequence") int sequence,
			@Bind("latitude") double latitude,
			@Bind("longitude") double longitude);
	
	@SqlUpdate("UPDATE route_points set latitude = :latitude, longitude = :longitude WHERE route = :route AND sequence = :sequence")
	void updateRoutePoint(@Bind("route") int route,
			@Bind("sequence") int sequence,
			@Bind("latitude") double latitude,
			@Bind("longitude") double longitude);
		
	@SqlUpdate ("UPDATE routes set elevation_gain = :elevation_gain, reverse_gain = :reverse_gain WHERE id = :id")
	void updateElevationGains(@Bind("id") int id, @Bind("elevation_gain") int elevationGain, @Bind("reverse_gain") int reverseGain);
	
	@RegisterMapper(RouteMapper.class)
	@SqlQuery("select * from routes")
	List<Route> findAllRoutes();
	
	@RegisterMapper(RouteMapper.class)
	@SqlQuery("select * from routes where start = :id or end = :id")
	List<Route> findRoutesByEndpoint(@Bind("id") int endpointId);
	
	@RegisterMapper(RouteMapper.class)
	@SqlQuery("select * from routes where id in (select route from route_tiles where tile = :tile)")
	List<Route> findRoutesForTile(@Bind("tile") String tile);
	
	@RegisterMapper(RouteMapper.class)
	@SqlQuery("select * from routes where id in (select route from route_tiles where tile in ( <tiles> ))")
	List<Route> findRoutesForTiles(@BindIn("tiles") List<String> tiles);
	
	@RegisterMapper(RouteMapper.class)
	@SqlQuery("select * from routes WHERE id = :id")
	Route findRouteById(@Bind("id") int id);
	
	@RegisterMapper(RouteMapper.class)
	@SqlQuery("select * from routes where id in ( <ids> )")
	List<Route> findRoutesByIds(@BindIn("ids") List<Integer> ids);
	
	@RegisterMapper(RoutePointMapper.class)
	@SqlQuery("select * from route_points where route = :route order by sequence")
	List<RoutePoint> findPathForRoute(@Bind("route") int route);
	
	@RegisterMapper(RoutePointMapper.class)
	@SqlQuery("select * from route_points where route in ( <routes> ) order by route, sequence")
	List<RoutePoint> findPathsForRoutes(@BindIn("routes") List<Integer> routes);
	
	@RegisterMapper(RouteTileMapper.class)
	@SqlQuery("select * from route_tiles where route = :route")
	List<RouteTilePair> findTilesForRoute(@Bind("route") int route);
	
	@SqlUpdate("delete from route_tiles where route = :route")
	void deleteTilesForRoute(@Bind("route") int route);
	
	@SqlUpdate("delete from route_points where route = :route")
	void deletePathsForRoute(@Bind("route") int route);
	
	@RegisterMapper(RouteTileMapper.class)
	@SqlQuery("select * from route_tiles where route in ( <routes> ) order by route")
	List<RouteTilePair> findTilesForRoutes(@BindIn("routes") List<Integer> routes);
	
	@SqlQuery("SELECT LAST_INSERT_ID()")
	int lastInsertId();
	
	@SqlUpdate("INSERT INTO split_routes (old_route, new_starting_route, new_ending_route) VALUES (:old, :start, :end)")
	void insertSplitRoute(@Bind("old") int oldRoute,
			@Bind("start") int newStartingRoute,
			@Bind("end") int newEndingRoute);
}