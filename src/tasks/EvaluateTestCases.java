package tasks;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import config.ParsingArguments;
import utils.FileListUnderDirectory;
import utils.FileToLines;
import utils.Pair;
import utils.TestCommandHelp;
import utils.WriteLinesToFile;

public class EvaluateTestCases {
	
	private List<Integer> getCompilingErrors(String results, String relativePath) {
		List<Integer> lineNumbers = new ArrayList<Integer>();
		System.out.println("== get compiling errors == ");
		System.out.println(results);
		results = results.trim();
		if (results.endsWith("errors") || results.endsWith("error")) {
			int index = results.indexOf(relativePath);
			while (index >= 0) {
				String line = results.substring(index + relativePath.length() + 1, results.indexOf(":",index + relativePath.length() + 1));
				if (line.matches("[0-9]+"))
					lineNumbers.add(Integer.parseInt(line));
				index = results.indexOf(relativePath, index + 1);
			}
		}
		System.out.println("== end compiling errors == ");
		System.out.println(lineNumbers.toString());
		return lineNumbers;
	}
	
	private int withInRange(List<Pair<Integer,Integer>> ranges, int line) {
		for (int i = 0; i < ranges.size(); i++) {
			Pair<Integer,Integer> range = ranges.get(i);			
			if (range.getKey() <= line && range.getValue() >= line) return i;
		}
		return -1;
	}
	
	private void removeInvalidTestCases(String relativePath, List<Integer> nums) {
		List<String> lines = FileToLines.fileToLines(relativePath);
		List<Pair<Integer,Integer>> testRange = new ArrayList<Pair<Integer,Integer>>();
		for (int num : nums) {
			int begin = num;
			int end = num;
			while (!lines.get(begin).contains("@Test")) begin--;
			while (end < lines.size() && !lines.get(end).contains("@Test")) end++;
			testRange.add(new Pair<Integer,Integer>(begin, end - 1));
			System.out.println("== Test Case Range: " + begin + " " + (end - 1));
		}
		
		List<String> after = new ArrayList<String>();
		HashSet<Integer> mark = new HashSet<Integer>();
		for (int i = 0; i < lines.size(); i++) {
			int index = withInRange(testRange, i);
			if (index >= 0) {
				after.add("// " + lines.get(i));
				mark.add(index);
			}
			else after.add(lines.get(i));
		}
		for (int i : mark) {
			System.out.println("== Removed test cases:" + testRange.get(i).toString() + " ==");
		}
		WriteLinesToFile.writeLinesToFile(after, relativePath);
			
	}
	
	private void removeInvalidAndCompileJUnitTestCases(String testCasePrefix, String targetLibrary, String dependancy, int timeLimit) {
		for (int seed = 1; seed < 10; seed++) {
			String testCaseDir = testCasePrefix + File.separator + "randoop-tests-" + timeLimit + "-" + seed;
			List<String> files = FileListUnderDirectory.getFileListUnder(testCaseDir, ".java");
			
			System.out.println("== Compiling test cases... ==");
			String[] dependancies = {dependancy, testCaseDir};
			String entryRegressionTest = "";
			String entryErrorTest = "";
			for (String testFile : files) {
				String relativePath = testFile.substring(testFile.indexOf(testCaseDir));
				if (testFile.endsWith("ErrorTest.java"))
					entryErrorTest = testFile;
				else if (testFile.endsWith("RegressionTest.java"))
					entryRegressionTest = testFile;
				else {
					Pair<String,String> result = TestCommandHelp.compileJUnitTestCases("javac",targetLibrary, testCaseDir, dependancies, relativePath, ".");
					List<Integer> nums = getCompilingErrors(result.getValue(), relativePath);
					if (nums.size() > 0)
						removeInvalidTestCases(relativePath, nums);
//					while (nums.size() > 0) {
//						removeInvalidTestCases(relativePath, nums);
//						result = TestCommandHelp.compileJUnitTestCases(targetLibrary, testCaseDir, dependancies, relativePath, ".");
//						nums = getCompilingErrors(result.getValue(), relativePath);
//					}
				}
			}
			
			// Compiling the entry point test cases
			TestCommandHelp.compileJUnitTestCases("javac",targetLibrary, testCaseDir, dependancies, entryRegressionTest, ".");
			TestCommandHelp.compileJUnitTestCases("javac",targetLibrary, testCaseDir, dependancies, entryErrorTest, ".");
			System.out.println("== Compiling the test suite:" + seed + " successfully ==");
		}	
	}
		
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
		
		
		ExecutorService executor = Executors.newFixedThreadPool(10);
	
		removeInvalidAndCompileJUnitTestCases(testCasePrefix, targetLibrary, targetLibraryAndDependancy, timeLimit);
		
		System.out.println("== Extracting Jar Class Files == ");
		String classDir = prefix + File.separator + targetLibrary;
		file = new File(classDir);
		if (!file.exists()) {
			TestCommandHelp.extractJarClassFiles(prefix + File.separator + "lib" + File.separator + targetLibrary + ".jar", classDir);
			System.out.println("== Finish Extracting Jar Class Files == ");
		} else {
			System.out.println("== Jar Class Files Exists ==");
		}
		
		for (int seed = 1; seed < 10; seed++) {
			String testCaseDir = testCasePrefix + File.separator + "randoop-tests-" + timeLimit + "-" + seed;
			String reportDir = reportDirPrefix + File.separator + "report-" + seed;
			String sourceDir = ".";
			String targetClasses = config.Config.libToPackage.get(targetLibrary) + "*";
			String targetTests = "RegressionTest";
			String[] dependancies = {targetLibraryAndDependancy, testCaseDir, classDir};
			Runnable work = new runPiTestRandoopPerSeed(dependancies, reportDir, sourceDir, targetClasses, targetTests, workingPath, seed);
			executor.execute(work);
		}
		executor.shutdown();
	}
	
	
	
	public void getEvosuiteCoverage() {
		String targetLibrary = config.Config.targetLib;
		String prefix = config.Config.targetLibraryDir + File.separator + targetLibrary;
		String testCasePrefix = prefix + File.separator + "evosuite-tests";
		String reportDirPrefix = prefix + File.separator + "evosuite-reports";
		File file = new File(reportDirPrefix);
		if (!file.exists()) file.mkdir();
		/*
		 * Compile JUnit Test Cases
		 * */
		
		String targetLibraryAndDependancy = prefix + File.separator + "lib" + File.separator + targetLibrary + ".jar";
		int timeLimit = 30;
		String workingPath = ".";
		int seedNum = 2;
		boolean updateData = true;
		
		System.out.println("Compiling JUnit Test Cases...");
		
		for (int seed = 1; seed < seedNum; seed++) {
			String testCaseDir = testCasePrefix + File.separator + "evosuite-tests-" + timeLimit + "-" + seed;
			String reportDir = reportDirPrefix + File.separator + "report-" + seed;
			
			String[] dependancies = {
				targetLibraryAndDependancy,
				"./lib/evosuite-standalone-runtime-1.0.2.jar",
				"./lib/slf4j-simple-1.6.1.jar",
				testCaseDir
			};
			
			List<String> files = FileListUnderDirectory.getFileListUnder(testCaseDir,"ESTest.java");
			for (String testFile : files) {
				String relativePath = testFile.substring(testFile.indexOf(testCaseDir));
				System.out.println(relativePath);
			}
			
			for (String testFile : files) {
				String relativePath = testFile.substring(testFile.indexOf(testCaseDir));
				file = new File(relativePath.substring(0, relativePath.length() - 5) + ".class");
				if (!file.exists() || updateData) {
					TestCommandHelp.compileJUnitTestCases("../jdk1.8.0_91/bin/javac", targetLibrary, testCaseDir, dependancies, relativePath, ".");
				} else {
					System.out.println("Already Compiled:" + relativePath);
				}
				
				
				String classname = relativePath.substring(relativePath.indexOf(config.Config.libToPackage.get(targetLibrary).replace(".", "/")));
				classname = classname.replace("/", ".");
				classname = classname.substring(0, classname.length() - 5);
//				System.out.println("Running JUnit Test Cases on " + classname);
//				Pair<String,String> results = TestCommandHelp.runJUnitTestCases("../jdk1.8.0_91/bin/java",dependancies, testCaseDir, classname, workingPath);
//				System.out.println(results.getValue());
			}
			
			System.out.println("Compiling JUnit test with seed :" + seed + " successfully");
			System.out.println("Runinng PiTest on Evosuite Test Cases...");
		
			String sourceDir = ".";
			String targetClasses = config.Config.libToPackage.get(targetLibrary) + "*";
			String targetTests = config.Config.libToPackage.get(targetLibrary) + "*ESTest";
			String excludedClasses = config.Config.libToPackage.get(targetLibrary) + "*_ESTest_scaffolding";
			workingPath = ".";
			
			TestCommandHelp.generatePiTestMutationTest("../jdk1.8.0_91/bin/java", dependancies, reportDir, sourceDir, excludedClasses, targetClasses, targetTests, workingPath);
		}
		/*
		 * Evaluate the test cases using PiTest
		 * */
	
	}
	
	public void getTestSOCoverage() {
		String targetLibrary = config.Config.targetLib;
		String prefix = config.Config.targetLibraryDir + File.separator + targetLibrary;
		String reportDir = prefix + File.separator + "testSO-reports";
		File file = new File(reportDir);
		if (!file.exists()) file.mkdir();
		
		String classname = "test.TestSuiteSO";
		String workingPath = prefix;
		String sourceDir = ".";
		
		String[] dependancy1 = {
				"./lib/*",
				"./bin"
		};
		
		TestCommandHelp.runJUnitTestCasesLocally(dependancy1, classname, workingPath);
		String[] dependancy2 = {
				"./lib2/*",
				"./bin",
				targetLibrary
		};
		
		TestCommandHelp.runJUnitTestCasesLocally(dependancy2, classname, workingPath);
		String targetClasses = config.Config.libToPackage.get(targetLibrary) + "*";
		
		System.out.println("== Running PiTest ==");
		TestCommandHelp.generatePiTestMutationTestLocally(dependancy2, "testSO-reports", sourceDir, "", targetClasses, classname, workingPath);
		
		
	}
	
	public static void main(String[] args) {
		ParsingArguments.parsingArguments(args);
		EvaluateTestCases etc = new EvaluateTestCases(); 
		etc.getEvosuiteCoverage();
//		test();
//		List<Integer> tmp = new ArrayList<Integer>();
//		tmp.add(61);
//		tmp.add(37);
//		etc.removeInvalidTestCases("RegressionTest1.java", tmp);
//		String filename = "RegressionTest1.java";
//		List<String> lines = FileToLines.fileToLines(filename);
//		int target = 61;
//		int current = target;
//		while (!lines.get(current).trim().equals("@Test")) {
//			System.out.println(lines.get(current));
//			current--;
//		}
//		System.out.println(current);
//		List<String> after = new ArrayList<String>();
//		for (int i = 0; i < lines.size(); i++)
//			if (i == current) 
//				after.add("// " + lines.get(i));
//			else after.add(lines.get(i));
//		
//		WriteLinesToFile.writeLinesToFile(after, filename);
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
		System.out.println("== Thread: " + threadId + "Evaluating Randoop test cases with seed:" + seed + " ==");
		TestCommandHelp.generatePiTestMutationTest("java",dependancies, reportDir, sourceDir, "", targetClasses, targetTests, workingPath);
	}
	
}
