package tasks;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import config.ParsingArguments;
import utils.FileListUnderDirectory;
import utils.FileToLines;
import utils.Pair;
import utils.TestCommandHelp;
import utils.WriteLinesToFile;

public class EvaluateTestCases {
	
	public static List<Integer> getCompilingErrors(String results, String relativePath) {
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
	
	public static List<Integer> getRunningErrors(String results, String className) {
		List<Integer> lineNumbers = new ArrayList<Integer>();
		String regex = className + "\\." + "test" + "(\\d)+\\(";
		System.out.println(className);
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(results);
		while (matcher.find()) {
			String subString = results.substring(matcher.end());
			String num = subString.substring(subString.indexOf(":") + 1, subString.indexOf(")"));
			int line = Integer.parseInt(num);
			System.out.println(num);
			lineNumbers.add(line);
		}
		return lineNumbers;
	}
	
	
	public static int withInRange(List<Pair<Integer,Integer>> ranges, int line) {
		for (int i = 0; i < ranges.size(); i++) {
			Pair<Integer,Integer> range = ranges.get(i);			
			if (range.getKey() <= line && range.getValue() >= line) return i;
		}
		return -1;
	}
	
	public static void removeInvalidTestCases(String relativePath, List<Integer> nums) {
		List<String> lines = FileToLines.fileToLines(relativePath);
		List<Pair<Integer,Integer>> testRange = new ArrayList<Pair<Integer,Integer>>();
		for (int num : nums) {
			int begin = num;
			int end = num;
			while (!lines.get(begin).contains("@Test")) begin--;
			while (end < lines.size() - 1 && !lines.get(end).contains("@Test")) end++;
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
	
	private static void removeInvalidAndCompileJUnitTestCases(String testCasePrefix, String targetLibrary, String dependancy, int timeLimit) {
		int seedBegin = config.Config.seedBegin;
		int seedEnd = config.Config.seedEnd;
		for (int seed = seedBegin; seed <= seedEnd; seed++) {
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
					System.out.println("Test Case Path...");
					System.out.println(relativePath);
					String classname = relativePath.substring(testCaseDir.length() + 1);
					classname = classname.substring(0, classname.length() - 5);
					
					try {
					
						Pair<String, String> results = TestCommandHelp.runJUnitTestCases("java", dependancies, testCaseDir, classname, ".");
						nums = getRunningErrors(results.getKey(), classname);
						
						while (nums.size() > 0) {
							System.out.println("Failures at :" + nums.toString());
							removeInvalidTestCases(relativePath, nums);
							TestCommandHelp.compileJUnitTestCases("javac", targetLibrary, testCaseDir, dependancies, relativePath, ".");
							results = TestCommandHelp.runJUnitTestCases("java", dependancies, testCaseDir, classname, ".");
							
							System.out.println(results.getKey());
							nums = getRunningErrors(results.getKey(), classname);
						}
						
	//					while (nums.size() > 0) {
	//						removeInvalidTestCases(relativePath, nums);
	//						result = TestCommandHelp.compileJUnitTestCases(targetLibrary, testCaseDir, dependancies, relativePath, ".");
	//						nums = getCompilingErrors(result.getValue(), relativePath);
	//					}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
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
		
		int seedBegin = config.Config.seedBegin;
		int seedEnd = config.Config.seedEnd;
		
		for (int seed = seedBegin; seed <= seedEnd; seed++) {
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
	
	public boolean containsError(String output) {
		String[] lines = output.split("\t");
		boolean flag = false;
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (!(line.contains("org.evosuite.runtime.classhandling.ClassStateSupport") || line.contains("o.e.r.c.ClassStateSupport")))
				continue;
//			System.out.println(line);
			String status = "";
			if (line.contains("o.e.r.c.ClassStateSupport"))
				status = line.substring(0, line.indexOf("o.e.r.c.ClassStateSupport"));
			else status	= line.substring(0, line.indexOf("org.evosuite.runtime.classhandling.ClassStateSupport"));
			if (status.contains("ERROR")) flag = true;
		}
		return flag;
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
		int timeLimit = config.Config.evosuiteTimeLimit;
		String workingPath = ".";
		int seedBegin = config.Config.seedBegin;
		int seedEnd = config.Config.seedEnd;
		boolean updateData = false;
		
		System.out.println("Compiling JUnit Test Cases...");
		
		for (int seed = seedBegin; seed <= seedEnd; seed++) {
			String testCaseDir = testCasePrefix + File.separator + "evosuite-tests-" + timeLimit + "-" + seed;
			String reportDir = reportDirPrefix + File.separator + "report-" + seed;
			String invalidFolder = testCasePrefix + File.separator + "invalid-" + timeLimit + "-" + seed;
			file = new File(invalidFolder);
			if (!file.exists())
				file.mkdir();
			
			String[] dependancies = {
				targetLibraryAndDependancy,
				"./lib/evosuite-1.0.2.jar",
//				"./lib/slf4j-simple-1.6.1.jar",
				testCaseDir
			};
			
			List<String> files = FileListUnderDirectory.getFileListUnder(testCaseDir,"ESTest.java");
			for (String testFile : files) {
				String relativePath = testFile.substring(testFile.indexOf(testCaseDir));
				System.out.println(relativePath);
				file = new File(relativePath.substring(0, relativePath.length() - 5) + ".class");
				if (!file.exists() || updateData) {
					Pair<String,String> result = TestCommandHelp.compileJUnitTestCases("javac", targetLibrary, testCaseDir, dependancies, relativePath, ".");
					List<Integer> nums = getCompilingErrors(result.getValue(), relativePath);
					
					while (nums.size() > 0) {
						removeInvalidTestCases(relativePath, nums);
						result = TestCommandHelp.compileJUnitTestCases("javac", targetLibrary, testCaseDir, dependancies, relativePath, ".");
						nums = getCompilingErrors(result.getValue(), relativePath);
					}
					
				} else {
					System.out.println("Already Compiled:" + relativePath);
				}
			}
			
			
			for (String testFile : files) {
				String relativePath = testFile.substring(testFile.indexOf(testCaseDir));
				String classname = relativePath.substring(relativePath.indexOf(config.Config.libToPackage.get(targetLibrary).replace(".", "/")));
				classname = classname.replace("/", ".");
				classname = classname.substring(0, classname.length() - 5);
//				if (!classname.equals("org.apache.commons.lang3.concurrent.BasicThreadFactory_ESTest"))
//					continue;
				System.out.println("Running JUnit Test Cases on " + classname);
				
				try {
					Pair<String,String> results = TestCommandHelp.runJUnitTestCases("java", dependancies, testCaseDir, classname, workingPath);
//					System.out.println("==Running standard output...==");
//					System.out.println(results.getKey());
//					System.out.println("==Running error output...==");
//					System.out.println(results.getValue());
					List<Integer> nums = getRunningErrors(results.getKey(), classname);
					
					while (nums.size() > 0) {
						System.out.println("Failures at :" + nums.toString());
						removeInvalidTestCases(relativePath, nums);
						TestCommandHelp.compileJUnitTestCases("javac", targetLibrary, testCaseDir, dependancies, relativePath, ".");
						results = TestCommandHelp.runJUnitTestCases("java", dependancies, testCaseDir, classname, workingPath);
						System.out.println(results.getKey());
						nums = getRunningErrors(results.getKey(), classname);
					}
					
					// Check whether if it contains error or not!
					String pathPrefix = relativePath.substring(0, relativePath.length() - 5);
					if (containsError(results.getKey())) {
						System.out.println("== Contains Error, Remove This Test Cases ===");
						TestCommandHelp.move(pathPrefix + ".java", invalidFolder + File.separator + classname + ".java", workingPath);
						TestCommandHelp.move(pathPrefix + ".class", invalidFolder + File.separator + classname + ".class", workingPath);
						TestCommandHelp.move(pathPrefix + "_scaffolding.java", invalidFolder + File.separator + classname + "_scaffolding.java", workingPath);
						TestCommandHelp.move(pathPrefix + "_scaffolding.class", invalidFolder + File.separator + classname + "_scaffolding.class", workingPath);
						// Move to invalid test folder
					}
					
				} catch (Exception e) {
					System.out.println("Exception:\t" + e.getClass());
				}
				
				
			}
			
			System.out.println("Compiling JUnit test with seed :" + seed + " successfully");
			System.out.println("Runinng PiTest on Evosuite Test Cases...");
			
			String sourceDir = ".";
			String targetClasses = config.Config.libToPackage.get(targetLibrary) + "*";
			String targetTests = config.Config.libToPackage.get(targetLibrary) + "*ESTest";
			String excludedClasses = config.Config.libToPackage.get(targetLibrary) + "*_ESTest_scaffolding";
			workingPath = ".";
			
			String[] dependancies2 = {
					prefix + File.separator + targetLibrary,
					"./lib/evosuite-1.0.2.jar",
//					"./lib/slf4j-simple-1.6.1.jar",
					testCaseDir
				};
			
			TestCommandHelp.generatePiTestMutationTest("java", dependancies2, reportDir, sourceDir, excludedClasses, targetClasses, targetTests, workingPath);
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
		
		String classname = "sotestgen.TestSuiteSO";
		String workingPath = prefix;
		String sourceDir = ".";
		
		String[] dependancy1 = {
				"./lib/*",
				"./test/",
		};
		
		String filePath = "." + File.separator + "test" + File.separator + "sotestgen" + File.separator + "TestSuiteSO.java";
		TestCommandHelp.compileJUnitTestCasesLocally("javac", dependancy1, filePath, workingPath);
		System.out.println("workingPath:\t" + filePath);
		TestCommandHelp.runJUnitTestCasesLocally(dependancy1, classname, workingPath);
		String[] dependancy2 = {
				"./lib2/*",
				"./test/",
				targetLibrary
		};
		
		Pair<String, String> results = TestCommandHelp.runJUnitTestCasesLocally(dependancy2, classname, workingPath);
		List<Integer> nums = getRunningErrors(results.getKey(), classname);
		
		while (nums.size() > 0) {
			System.out.println("Failures at :" + nums.toString());
			removeInvalidTestCases(workingPath + File.separator + filePath, nums);
			TestCommandHelp.compileJUnitTestCasesLocally("javac", dependancy1, filePath, workingPath);
			results = TestCommandHelp.runJUnitTestCasesLocally(dependancy2, classname, workingPath);
			System.out.println(results.getKey());
			nums = getRunningErrors(results.getKey(), classname);
		}
		
		String targetClasses = config.Config.libToPackage.get(targetLibrary) + "*";
		
		System.out.println("== Running PiTest ==");
		TestCommandHelp.generatePiTestMutationTestLocally(dependancy2, "testSO-reports", sourceDir, "", targetClasses, classname, workingPath);
		
		
	}
	
	public void fixEvosuiteInvalidTestCases() {

		ExecutorService executor = Executors.newFixedThreadPool(10);
		String targetLibrary = config.Config.targetLib;
		String prefix = config.Config.targetLibraryDir + File.separator + targetLibrary;
		String testCasePrefix = prefix + File.separator + "evosuite-tests";
		int timeLimit = config.Config.evosuiteTimeLimit;
		int seedBegin = config.Config.seedBegin;
		int seedEnd = config.Config.seedEnd;
		
		for (int seed = seedBegin; seed <= seedEnd; seed++) {
			String invalidFolder = testCasePrefix + File.separator + "invalid-" + timeLimit + "-" + seed;
			File[] files = new File(invalidFolder).listFiles();
			HashSet<String> buggyClasses = new HashSet<String>();
			for (File file : files) {
				buggyClasses.add(file.getName().substring(0, file.getName().indexOf("_")));
			}
			System.out.println(buggyClasses.toString());
			for (String buggyClass : buggyClasses) {
				Runnable work = new fixEvosuiteInvalidTestCase(buggyClass, 0);
				executor.execute(work);
			}
		}
		executor.shutdown();
	}
	
	public static void main(String[] args) {
		ParsingArguments.parsingArguments(args);
		EvaluateTestCases etc = new EvaluateTestCases(); 
//		String content = FileToLines.fileToString("./runResults.txt");
//		etc.getRunningErrors(content, "com.google.common.base.Joiner_ESTest");
		String option = config.Config.option;
		boolean isFix = config.Config.isFix;
		System.out.println("Option:" + option + "\t" + "isFix:" + isFix);
		if (option.equals("Evosuite")) {
//			etc.getEvosuiteCoverage();
			if (isFix)
				etc.fixEvosuiteInvalidTestCases();
			else
				etc.getEvosuiteCoverage();
		} else if (option.equals("Randoop"))
			etc.getRandoopCoverage();
		else if (option.equals("TestSO"))
			etc.getTestSOCoverage();
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
class fixEvosuiteInvalidTestCase implements Runnable {
	String buggyClass;
	int seed;
	
	public fixEvosuiteInvalidTestCase(String buggyClass, int seed) {
		this.buggyClass = buggyClass;
		this.seed = seed;
	}
	
	@Override
	public void run() {

		
		String libName = config.Config.targetLib;
		String prefix = config.Config.targetLibraryDir + File.separator + libName;
		String libPath = prefix + File.separator + "lib" + File.separator + libName + ".jar";
//		String targetLibraryAndDependancy = combineAllFileNameWithSemiColon(prefix + File.separator + "lib");
		String targetLibraryAndDependancy = libPath;
		String reportDirPrefix = prefix + File.separator + "evosuite-reports";
		String targetClasses = config.Config.libToPackage.get(libName) + "*";
		String excludedClasses = config.Config.libToPackage.get(libName) + "*_ESTest_scaffolding";
		
		int timeLimit = 30;
		String workingPath = ".";
		String testCaseDir = prefix + File.separator + "evosuite-tests";
		String outputDir = testCaseDir + File.separator + "evosuite-tests-" + timeLimit + "-" + seed;
		String[] dependancies = {
				targetLibraryAndDependancy,
				"./lib/evosuite-1.0.2.jar",
//				"./lib/slf4j-simple-1.6.1.jar",
				outputDir
			};
		
		String[] dependancies2 = {
				prefix + File.separator + libName,
				"./lib/evosuite-1.0.2.jar",
//				"./lib/slf4j-simple-1.6.1.jar",
				outputDir
			};
		
		
		
		System.out.println("Handling class :" + buggyClass);
		System.out.println("=== Generating Test Case ===");
		String classname = buggyClass;
		TestCommandHelp.generateEvosuiteTestCasesForAClass(libPath, classname, seed, timeLimit, outputDir, workingPath);
		
		System.out.println("=== Compiling JUnit Test Case ===");
		String relativePath = outputDir + File.separator + buggyClass.replace(".", File.separator) + ".java";
		Pair<String,String> result = TestCommandHelp.compileJUnitTestCases("javac", libName, outputDir, dependancies, relativePath, ".");
		
		List<Integer> nums = EvaluateTestCases.getCompilingErrors(result.getValue(), relativePath);
		
		while (nums.size() > 0) {
			EvaluateTestCases.removeInvalidTestCases(relativePath, nums);
			result = TestCommandHelp.compileJUnitTestCases("javac", libName, outputDir, dependancies, relativePath, ".");
			nums = EvaluateTestCases.getCompilingErrors(result.getValue(), relativePath);
		}
		
		System.out.println("=== Running JUnit Test Case ===");
		result = TestCommandHelp.runJUnitTestCases("java", dependancies, "", buggyClass, workingPath);
		
		nums = EvaluateTestCases.getRunningErrors(result.getKey(), classname);
		
		while (nums.size() > 0) {
			System.out.println("Failures at :" + nums.toString());
			EvaluateTestCases.removeInvalidTestCases(relativePath, nums);
			TestCommandHelp.compileJUnitTestCases("javac", libName, outputDir, dependancies, relativePath, ".");
			result = TestCommandHelp.runJUnitTestCases("java", dependancies, "", buggyClass, workingPath);
			System.out.println(result.getKey());
			nums = EvaluateTestCases.getRunningErrors(result.getKey(), classname);
		}
		
		String reportDir = reportDirPrefix + File.separator + "report-" + seed + File.separator + buggyClass;
		TestCommandHelp.generatePiTestMutationTest("java", dependancies2, reportDir, "", excludedClasses, targetClasses, buggyClass, workingPath);
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
