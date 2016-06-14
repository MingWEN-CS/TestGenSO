package tasks;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		
		String testCaseDir = prefix + File.separator + "randoop-tests";
		file = new File(testCaseDir);
		if (!file.exists())
			file.mkdir();
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int seed = 0; seed < 10; seed++) {
			String outputDir = testCaseDir + File.separator + "randoop-tests-" + timeLimit + "-" + seed;
			file = new File(outputDir);
			if (!file.exists())
				file.mkdir();
			Runnable work = new runRandoopTestCasesPerSeed(targetLibraryAndDependancy, classlist, seed, timeLimit, outputDir, workingPath);
			executor.execute(work);
		}
		executor.shutdown();
	}
	
	
	public static void main(String[] args) {
		ParsingArguments.parsingArguments(args);
		generateRandoopTestCases();
	}
}

class runRandoopTestCasesPerSeed implements Runnable {
	
	String targetLibraryAndDependancy;
	String classlist;
	int seed;
	int timeLimit;
	String outputDir;
	String workingPath;
	
	public runRandoopTestCasesPerSeed(String targetLibraryAndDependancy, String classlist, int seed, int timeLimit, String outputDir, String workingPath) {
		this.targetLibraryAndDependancy = targetLibraryAndDependancy;
		this.classlist = classlist;
		this.seed = seed;
		this.timeLimit = timeLimit;
		this.outputDir = outputDir;
		this.workingPath = workingPath;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		long threadId = Thread.currentThread().getId();
		System.out.println("== Thread: " + threadId + "Generating Randoop test cases with seed:" + seed + " ==");
		TestCommandHelp.generateRandoopTestCases(targetLibraryAndDependancy, classlist, seed, timeLimit, outputDir, workingPath);
	}
	
}
