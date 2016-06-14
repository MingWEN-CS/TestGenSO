package tasks;


import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import config.ParsingArguments;
import utils.FileListUnderDirectory;
import utils.TestCommandHelp;

public class EvaluateTestCases {
	
	public void getRandoopCoverage() {
		String targetLibrary = config.Config.targetLib;
		String prefix = config.Config.targetLibraryDir + File.separator + targetLibrary;
		
		String testCasePrefix = prefix + File.separator + "randoop-tests";
		String reportDirPrefix = prefix + File.separator + "randoop-reports";
		
		File file = new File(reportDirPrefix);
		if (!file.exists()) file.mkdir();
	
		String targetLibraryAndDependancy = prefix + File.separator + "lib" + File.separator + "*";
		int timeLimit = 180;
		String workingPath = ".";
		String[] dependancies = {targetLibraryAndDependancy};
		
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int seed = 0; seed < 10; seed++) {
			String testCaseDir = testCasePrefix + File.separator + "randoop-tests-" + timeLimit + "-" + seed;
			List<String> files = FileListUnderDirectory.getFileListUnder(testCaseDir, ".java");
			
			System.out.println("== Compiling test cases... ==");
			
			for (String testFile : files) {
				String relativePath = testFile.substring(testFile.indexOf(testCaseDir));
				System.out.println(relativePath);
				TestCommandHelp.compileJUnitTestCases(targetLibrary, testCaseDir, dependancies, relativePath, ".");
			}
			
			String reportDir = reportDirPrefix + "-report-" + seed;
			String sourceDir = ".";
			String targetClasses = config.Config.libToPackage.get(targetLibrary) + "*";
			String targetTests = "RegressionTest*";
			
			Runnable work = new runPiTestRandoopPerSeed(dependancies, reportDir, sourceDir, targetClasses, targetTests, workingPath, seed);
			executor.execute(work);
		}
		
		executor.shutdown();
	}
	
	
	
	public void getEvosuiteCoverage() {
		String targetLibrary = "commons-math3-3.6.1";
		
		/*
		 * Compile JUnit Test Cases
		 * */
		System.out.println("Compiling JUnit Test Cases...");
		
		String[] dependancies = {
			"./targets/" + targetLibrary,
			"./lib/evosuite-standalone-runtime-1.0.3.jar",
			"./evosuite-tests/"
		};
		
		List<String> files = FileListUnderDirectory.getFileListUnder("./evosuite-tests/","ESTest.java");
		for (String testFile : files) {
			System.out.println(testFile);
			String relativePath = testFile.substring(testFile.indexOf("evosuite-tests"));
			TestCommandHelp.compileJUnitTestCases(targetLibrary, "./evosuite-tests/", dependancies, relativePath, ".");
		}
		
		/*
		 * Evaluate the test cases using PiTest
		 * */
		
		System.out.println("Runinng PiTest on Evosuite Test Cases...");
		
		String reportDir = "./report";
		String sourceDir = ".";
		String targetClasses = "org.apache.commons.math3.*";
		String targetTests = "org.apache.commons.math3.*ESTest";
		String workingPath = ".";
		
		TestCommandHelp.generatePiTestMutationTest(dependancies, reportDir, sourceDir, targetClasses, targetTests, workingPath);
	}
	
	public static void main(String[] args) {
		ParsingArguments.parsingArguments(args);
		EvaluateTestCases etc = new EvaluateTestCases(); 
		etc.getRandoopCoverage();
	}
}

class runPiTestRandoopPerSeed implements Runnable {
	String[] dependancies;
	String reportDir;
	String sourceDir; 
	String targetClasses; 
	String targetTests; 
	String workingPath;
	int seed;
	
	public runPiTestRandoopPerSeed(String[] dependancies, String reportDir, String sourceDir, String targetClasses, String targetTests, String workingPath, int seed) {
		this.dependancies = dependancies;
		this.reportDir = reportDir; 
		this.sourceDir = sourceDir;
		this.targetClasses = targetClasses;
		this.targetTests = targetTests;
		this.workingPath = workingPath;
		this.seed = seed;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		long threadId = Thread.currentThread().getId();
		System.out.println("== Thread: " + threadId + "Generating Randoop test cases with seed:" + seed + " ==");
		TestCommandHelp.generatePiTestMutationTest(dependancies, reportDir, sourceDir, targetClasses, targetTests, workingPath);
	}
	
}
