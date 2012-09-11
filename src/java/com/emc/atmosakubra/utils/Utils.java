package com.emc.atmosakubra.utils;

/**
 * Various utility methods
 *
 */
public class Utils {
    
    /**
     * ATMOS name-separator character
     */
    public static final char ATMOS_FILE_SYSTEM_DELIMITER = '/';
    
    /**
     * Join two paths, using <code>ATMOS_FILE_SYSTEM_DELIMITER</code>
     * @param pathA right-hand path
     * @param pathB left-hand path
     * @return <code>pathA</code> + <code>ATMOS_FILE_SYSTEM_DELIMITER</code> + <code>pathA</code>
     */
    public static String joinTwoPaths(String pathA, String pathB) {
	int pathALastChar = pathA.length() - 1;
	while (pathALastChar >= 0 && pathA.charAt(pathALastChar) == ATMOS_FILE_SYSTEM_DELIMITER) {
	    pathALastChar--;
	}
	int pathBFirstChar = 0;
	while (pathBFirstChar < pathB.length() && pathB.charAt(pathBFirstChar) == ATMOS_FILE_SYSTEM_DELIMITER) {
	    pathBFirstChar++;
	}
	StringBuilder result = new StringBuilder(pathA.substring(0, pathALastChar + 1));
	result.append(ATMOS_FILE_SYSTEM_DELIMITER);
	result.append(pathB.substring(pathBFirstChar));
	return result.toString();
    }

}
