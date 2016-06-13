package config;

public class ParsingArguments {
	
	public static void parsingArguments(String[] args) {
//		System.out.println(args.length);
		String pro = config.Config.targetLib;
		if (args.length == 0) {
			showHelp();
		} else {
			int i = 0;
			while (i < args.length - 1) {
				if (args[i].equals("-p")) {
					pro = args[++i];
				}
				i++;
			}			
		}
		
		if (pro.length() > 0)
			config.Config.targetLib = pro;
//		if (pro.equals("Ant") || pro.equals("ElasticSearch") || pro.equals("Lucene")) global.Parameters.VERSION = "";
	}
	
	private static void showHelp() {
		System.out.println("Using the default setting..");
	}
}
