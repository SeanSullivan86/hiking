package org.sean.hiking.gpx;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.sean.hiking.coordinates.EarthPosition3D;
import org.sean.hiking.coordinates.TrackLogEntry;
import org.sean.hiking.track.TrackLog;

public class GPXReader {
	
	public static TrackLog readFromFile(String filename) {
		return parseGPX(readFile(filename));
	}

	private static String readFile(String filename) {
		try {
		    byte[] buffer = new byte[(int) new File(filename).length()];
		    BufferedInputStream f = new BufferedInputStream(new FileInputStream(filename));
		    f.read(buffer);
		    return new String(buffer);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	private static String getTag(String str, String tag) {
		return str.substring(str.indexOf("<"+tag)+1+tag.length(),str.indexOf("</"+tag+">")+3+tag.length());
	}
	
	private static String getInnerTag(String str, String tag) {
		String x = str.substring(str.indexOf("<"+tag)+1+tag.length(),str.indexOf("</"+tag+">"));
		return x.substring(x.indexOf(">")+1);
	}
	
	private static List<String> getTags(String str, String tag) {
		ArrayList<String> ret = new ArrayList<String>();
		while (str.indexOf(tag) != -1) {
			ret.add(getTag(str,tag));
			str = str.substring(str.indexOf("</"+tag+">")+tag.length()+3);
		}
		return ret;
	}
	
	private static long parseDate(String date) {
		try {
			SimpleDateFormat frm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			return frm.parse(date).getTime()/1000;
		}
		catch (Exception e) {
			return 0;
		}
	}
	
	private static TrackLog parseGPX(String gpx) {
		List<TrackLogEntry> entries = new ArrayList<TrackLogEntry>();
		String name = getInnerTag(gpx, "name");
		List<String> trkpts = getTags(gpx,"trkpt");

		for (String trkpt: trkpts) {

			double elev = Double.parseDouble(getInnerTag(trkpt,"ele"))*3.2808399;
			String lat1 = trkpt.substring(trkpt.indexOf("lat=")+5);
		    double lat = Double.parseDouble(lat1.substring(0,lat1.indexOf('"')));
			String lon1 = trkpt.substring(trkpt.indexOf("lon=")+5);
			double lon = Double.parseDouble(lon1.substring(0,lon1.indexOf('"')));
			
			entries.add(new TrackLogEntry(
					new EarthPosition3D(lat, lon, elev),
					new DateTime(parseDate(getInnerTag(trkpt,"time")))));
		}
		
		return new TrackLog(name, entries);

	}
}
