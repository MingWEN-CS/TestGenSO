package tasks;

import java.io.File;
import java.util.List;

import utils.FileListUnderDirectory;
import utils.TestCommandHelp;

public class EvaluateTestCases {
	
	public void getRandoopCoverage() {
		String targetLibrary = "commons-math3-3.6.1";
		
		String[] dependancies = {
			"./randoop-tests",
			"./targets/" + targetLibrary
		};
		
		String reportDir = "./report";
		String sourceDir = ".";
		String targetClasses = "org.apache.commons.math3.*";
		String targetTests = "RegressionTest*";
		String workingPath = ".";
		
		TestCommandHelp.generatePiTestMutationTest(dependancies, reportDir, sourceDir, targetClasses, targetTests, workingPath);
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
		EvaluateTestCases etc = new EvaluateTestCases(); 
		etc.getEvosuiteCoverage();
	}
}
