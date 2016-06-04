package utils;

public class TestCommandHelp {
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
		
		ExecCommand executor = new ExecCommand();
		String result = executor.execOneThread(commands, workingPath);
		return result;
	}
}
