package tasks;

import java.io.File;

import config.ParsingArguments;
import utils.TestCommandHelp;

public class GenerateTestCases {
	
	public static void generateRandoopTestCases() {
		String libName = config.Config.targetLib;
		String prefix = config.Config.targetLibraryDir + File.separator + libName;
		String libPath = prefix + File.separator + "lib" + File.separator + libName + ".jar";
		String classlist = prefix + File.separator + "classlist.txt";
		File file = new File(classlist);
		if (!file.exists()) {
			System.out.println("== Generating class list ==");
			TestCommandHelp.generateClasslist(libPath, classlist);
			System.out.println("== Done == ");
		} else {
			System.out.println("== Found class list file ==");
		}
		
		String targetLibraryAndDependancy = prefix + File.separator + "lib" + File.separator + "*";
		int timeLimit = 180;
		String workingPath = ".";
		for (int seed = 0; seed < 10; seed++) {
			String outputDir = prefix + File.separator + "randoop-tests-" + timeLimit + "-" + seed;
			file = new File(outputDir);
			if (!file.exists())
				file.mkdir();
			System.out.println("== Generating Randoop test cases with seed:" + seed + " ==");
			TestCommandHelp.generateRandoopTestCases(targetLibraryAndDependancy, classlist, seed, timeLimit, outputDir, workingPath);
		}
	}
	
	public static void main(String[] args) {
		ParsingArguments.parsingArguments(args);
		generateRandoopTestCases();
	}
}
