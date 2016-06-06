package utils;

public class TestCommandHelp {
	
	private static void printCommands(String[] commands) {
		for (String command : commands) {
			System.out.print(command + " ");
		}
		System.out.println();
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
				"-classlist",
				classList,
				"--ignore-flaky-tests=true",
				"--junit-output-dir=" + outputDir,
				"--seed=" + seed,
				"timeLimit=" + timeLimit
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
		int seed = 0;
		int timeLimit = 10;
		String outputDir = "./randoop-tests";
		String workingPath = ".";
//		generateEvosuiteTestCasesForAClass(targetLibrary, className, seed, timeLimit, outputDir, workingPath);
		generateRandoopTestCases(targetLibrary,className, seed , timeLimit,outputDir, workingPath);
	}
}
