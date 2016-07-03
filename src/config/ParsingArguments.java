package config;

public class ParsingArguments {
	
	public static void parsingArguments(String[] args) {
//		System.out.println(args.length);
		String pro = config.Config.targetLib;
		int begin = config.Config.seedBegin;
		int end = config.Config.seedEnd;
		boolean isFix = config.Config.isFix;
		String option = config.Config.option;
		
		if (args.length == 0) {
			showHelp();
		} else {
			int i = 0;
			while (i < args.length - 1) {
				if (args[i].equals("-p")) {
					pro = args[++i];
				}
				if (args[i].equals("-seedBegin")) {
					begin = Integer.parseInt(args[++i]);
				}
				if (args[i].equals("-seedEnd")) {
					end = Integer.parseInt(args[++i]);
				}
				if (args[i].equals("-o")) {
					option = args[++i];
				}
				if (args[i].equals("-fix"))
					isFix = true;
				i++;
			}			
		}
		
		if (pro.length() > 0)
			config.Config.targetLib = pro;
		if (option.length() > 0)
			config.Config.option = option;
		config.Config.seedBegin = begin;
		config.Config.seedEnd = end;
		config.Config.isFix = isFix;
//		if (pro.equals("Ant") || pro.equals("ElasticSearch") || pro.equals("Lucene")) global.Parameters.VERSION = "";
	}
	
	private static void showHelp() {
		System.out.println("Using the default setting..");
	}
}
