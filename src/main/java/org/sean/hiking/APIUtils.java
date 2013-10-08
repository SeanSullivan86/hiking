package org.sean.hiking;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public class APIUtils {

	
	public static <T extends Comparable<T>> Range<T> getRangeFromOptionals(Optional<T> lowerEndpoint, Optional<T> upperEndpoint) {
		if (lowerEndpoint.isPresent()) {
			if (upperEndpoint.isPresent()) {
				return Range.closed(lowerEndpoint.get(), upperEndpoint.get());
			} else {
				return Range.atLeast(lowerEndpoint.get());
			}
		} else {
			if (upperEndpoint.isPresent()) {
				return Range.atMost(upperEndpoint.get());
			} else {
				return Range.all();
			}
		}
	}
	
    public static String sha1(String input) {
    	try {
	        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
	        byte[] result = mDigest.digest(input.getBytes());
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < result.length; i++) {
	            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
	        }
	         
	        return sb.toString();
    	} catch (NoSuchAlgorithmException e) {
    		throw new RuntimeException("Cannot hash", e);
    	}
    }
}
