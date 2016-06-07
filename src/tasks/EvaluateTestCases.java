package tasks;

import java.io.File;

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
		
		String[] dependancies = {
			"./lib/evosuite-standalone-runtime-1.0.3.jar",
			"./evosuite-tests/"
		};
		
		File file = new File("./evosuite-tests/");
		File[] testFiles = file.listFiles();
		for (File testFile : testFiles) {
			System.out.println(testFile);
			if (!testFile.getName().endsWith("ESTest.java")) continue;
			String absolutePath = testFile.getAbsolutePath();
			String relativePath = absolutePath.substring(absolutePath.indexOf("evosuite-tests") + 15);
			TestCommandHelp.compileJUnitTestCases(targetLibrary, "./evosuite-tests/", dependancies, relativePath, ".");
		}
		
	}
	
	public static void main(String[] args) {
		EvaluateTestCases etc = new EvaluateTestCases(); 
		etc.getEvosuiteCoverage();
	}
}
