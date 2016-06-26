
package config;

import java.util.HashMap;

public class Config {
	
	public static String targetLib = "google-collections-1.0";
	public static String option = "TestSO";
	public static int randoopTimeLimit = 180;
	public static int evosuiteTimeLimit = 30;
	public static int seedBegin = 0;
	public static int seedEnd = 0;
	public static final String targetLibraryDir = "../TargetLibraries";
	
	public static HashMap<String,String> libToPackage = new HashMap<String,String>();
	
	static {
		libToPackage.put("google-collections-1.0", "com.google.common.");
		libToPackage.put("commons-lang3-3.4", "org.apache.commons.lang3.");
	}
	
}
