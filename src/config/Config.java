
package config;

import java.util.HashMap;

public class Config {
	
	public static String targetLib = "commons-lang3-3.4";
	public static String option = "TestSO";
	public static int randoopTimeLimit = 180;
	public static int evosuiteTimeLimit = 30;
	public static int seedBegin = 0;
	public static int seedEnd = 1;
	public static final String targetLibraryDir = "../TargetLibraries";
	
	public static HashMap<String,String> libToPackage = new HashMap<String,String>();
	
	static {
		libToPackage.put("google-collections-1.0", "com.google.common.");
		libToPackage.put("commons-lang3-3.4", "org.apache.commons.lang3.");
		libToPackage.put("joda-time-2.9.4", "org.joda.time.");
		libToPackage.put("gson-2.6.2", "com.google.gson.");
		libToPackage.put("gson-2.6.2", "com.google.gson.");
		libToPackage.put("stanford-corenlp-3.6.0", "edu.stanford.nlp.");
	}
	
}
