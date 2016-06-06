package tasks;

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
	
	public static void main(String[] args) {
		EvaluateTestCases etc = new EvaluateTestCases(); 
		etc.getRandoopCoverage();
	}
}
