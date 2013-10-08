package org.sean.hiking.track;

import java.sql.Timestamp;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface TrackDao {
	@SqlUpdate("INSERT INTO tracks (upload_time, distance, elevation_gain) VALUES (NOW(), :distance, :elevation_gain)")
	void insert(@Bind("distance") double distance,
			@Bind("elevation_gain") int elevationGain);
	
	@SqlUpdate("INSERT INTO track_points(track_id, time, latitude, longitude, elevation) VALUES (:track_id, :time, :latitude, :longitude, :elevation)")
	void insertTrackPoint(@Bind("track_id") int trackId,
			@Bind("time") Timestamp time,
			@Bind("latitude") double latitude,
			@Bind("longitude") double longitude,
			@Bind("elevation") int elevation);

}