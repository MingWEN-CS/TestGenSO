package utils;

public class TestCommandHelp {
	
	private static void printCommands(String[] commands) {
		for (String command : commands) {
			System.out.print(command + " ");
		}
		System.out.println();
	} 
	
	
	public static String generatePiTestMutationTest(
			String[] dependancies,
			String reportDir,
			String sourceDir,
			String targetClasses,
			String targetTests,
			String workingPath
			) {
		
		
		// required pitest libraries
		String classPath = "./lib/pitest-command-line-1.1.10.jar:./lib/junit-4.12.jar:./lib/pitest-1.1.10.jar:./lib/hamcrest-all-1.3.jar";
		
		// path for the classes under testing and its dependencies
		for (String dependancy : dependancies) {
			classPath += ":" + dependancy;
		}
		
		String[] commands = {
				"java",
				"-cp",
				classPath,
				"org.pitest.mutationtest.commandline.MutationCoverageReport",
				"--reportDir",
				reportDir,
				"--sourceDirs",
				sourceDir,
				"--targetClasses",
				targetClasses,
				"--mutators",
				"ALL",
				"--targetTests",
				targetTests
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		String result = executor.execOneThread(commands, workingPath);
		System.out.println(result);
		return result;
	}
	
	
	public static String compileJUnitTestCases(
			String targetLibrary,
			String testDir,
			String[] dependancies,
			String testPath,
			String workingPath) {
		
		String requiredFiles = "";
		for (String dependancy : dependancies) {
			requiredFiles += ":" + dependancy;
		}
		
		String[] commands = {
				"javac",
				"-cp",
				"./lib/junit-4.12.jar:./lib/hamcrest-all-1.3.jar" + requiredFiles,
				testPath
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		String result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	public static String runJUnitTestCases(
			String targetLibrary,
			String testDir,
			String testPath,
			String workingPath) {
		
		String[] commands = {
				"java",
				"-cp",
				"./lib/junit-4.12.jar:./lib/hamcrest-all-1.3.jar:./targets/" + targetLibrary + ":" + testDir,
				"org.junit.runner.JUnitCore",
				testPath
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		String result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	public static String generateRandoopTestCases(
			String targetLibrary,
			String classList,
			int seed,
			int timeLimit,
			String outputDir,
			String workingPath) {
		
		String[] commands = {
				"java",
				"-cp",
				"./lib/randoop-2.1.4.jar:./targets/" + targetLibrary,
				"randoop.main.Main",
				"gentests",
				"--classlist=" + classList,
				"--ignore-flaky-tests=true",
				"--junit-output-dir=" + outputDir,
				"--randomseed=" + seed,
				"--timelimit=" + timeLimit
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		String result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	public static String generateEvosuiteTestCasesForAClass(
			String targetLibrary,
			String className,
			int seed,
			int timeLimit,
			String outputDir,
			String workingPath
			) {
		
		String[] commands = {
				"java",
				"-jar",
				"./lib/evosuite-1.0.3.jar",
				"-generateSuite",
				"-class",
				className,
				"-projectCP",
				"./targets/" + targetLibrary,
				"-seed",
				"" + seed,
				"-Dsearch_budget=" + timeLimit,
				"-Dstopping_condition=MaxTime"
		};
		
		ExecCommand executor = new ExecCommand();
		printCommands(commands);
		String result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	public static String generateEvosuiteTestCasesForALibrary(
			String targetLibrary,
			String classList,
			int seed,
			int timeLimit,
			String outputDir,
			String workingPath) {
		
		String[] commands = {
				"java",
				"-jar",
				"./lib/evosuite-1.0.3.jar",
				"-target",
				"./targets/" + targetLibrary,
				"-generateSuite",
				"-seed",
				"" + seed,
				"-Dsearch_budget=" + timeLimit,
				"-Dstopping_condition=MaxTime"
		};
		
		ExecCommand executor = new ExecCommand();
		String result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	
	
	public static void main(String[] args) {
		String targetLibrary = "commons-math3-3.6.1.jar";
		String className = "org.apache.commons.math3.dfp.DfpField";
		String classList = "classnames.txt";
		int seed = 0;
		int timeLimit = 180;
		String outputDir = "./randoop-tests-180";
		String workingPath = ".";
//		generateEvosuiteTestCasesForAClass(targetLibrary, className, seed, timeLimit, outputDir, workingPath);
		generateRandoopTestCases(targetLibrary, classList, seed , timeLimit,outputDir, workingPath);
	}
}
