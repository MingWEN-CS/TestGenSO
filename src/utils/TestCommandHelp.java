package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TestCommandHelp {
	
	private static void printCommands(String[] commands) {
		for (String command : commands) {
			System.out.print(command + " ");
		}
		System.out.println();
	} 
	
	public static void move(String from, String to, String workingPath) {
		String[] commands = {
				"mv",
				from,
				to
		};
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		Pair<String,String> result = executor.execOneThread(commands, workingPath);	
	}
	
	public static Pair<String,String> generatePiTestMutationTestLocally(
			String[] dependancies,
			String reportDir,
			String sourceDir,
			String excludeClasses,
			String targetClasses,
			String targetTests,
			String workingPath
			) {
		
		
		// required pitest libraries
		String classPath = "../../TestGenSO/lib/pitest-command-line-1.1.10.jar:../../TestGenSO/lib/junit-4.12.jar:../../TestGenSO/lib/pitest-1.1.10.jar:../../TestGenSO/lib/hamcrest-all-1.3.jar";
		
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
				"--threads",
				"10",
				"--timeoutFactor",
				"2",
				"--timeoutConst",
				"10000",
				"--excludedClasses",
				excludeClasses.equals("") ? "CIVI_UNMATCH_FORMAT" : excludeClasses, 
				"--targetClasses",
				targetClasses,
				"--mutators",
				"ALL",
				"--targetTests",
 				targetTests
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		Pair<String,String> result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	public static Pair<String,String> generatePiTestMutationTest(
			String command,
			String[] dependancies,
			String reportDir,
			String sourceDir,
			String excludeClasses,
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
				command,
				"-cp",
				classPath,
				"org.pitest.mutationtest.commandline.MutationCoverageReport",
				"--reportDir",
				reportDir,
				"--sourceDirs",
				sourceDir,
				"--excludedClasses",
				excludeClasses.equals("") ? "CIVI_UNMATCH_FORMAT" : excludeClasses, 
				"--targetClasses",
				targetClasses,
				"--mutators",
				"ALL",
				"--targetTests",
				targetTests
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		Pair<String,String> result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	
	public static Pair<String,String> compileJUnitTestCases(
			String command,
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
				command,
				"-cp",
				"./lib/junit-4.12.jar:./lib/hamcrest-all-1.3.jar" + requiredFiles,
				testPath
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		Pair<String,String> result = executor.execOneThread(commands, ".");
		return result;
	}
	
	public static Pair<String,String> compileJUnitTestCasesLocally(
			String command,
			String[] dependancies,
			String testPath,
			String workingPath) {
		
		String requiredFiles = "";
		for (String dependancy : dependancies) {
			requiredFiles += ":" + dependancy;
		}
		
		String[] commands = {
				command,
				"-cp",
				"../../TestGenSO/lib/junit-4.12.jar:../../TestGenSO/lib/hamcrest-all-1.3.jar" + requiredFiles,
				testPath
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		Pair<String,String> result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	public static Pair<String,String> runJUnitTestCases(
			String command,
			String[] dependancies,
			String testDir,
			String testPath,
			String workingPath) {
		
		String requiredFiles = "";
		for (String dependancy : dependancies) {
			requiredFiles += ":" + dependancy;
		}
		
		String[] commands = {
				command,
				"-cp",
				"./lib/junit-4.12.jar:./lib/hamcrest-all-1.3.jar" + requiredFiles,
				"org.junit.runner.JUnitCore",
				testPath
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		Pair<String,String> result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	public static Pair<String,String> runJUnitTestCasesLocally(
			String[] dependancies,
			String testPath,
			String workingPath) {
		
		String requiredFiles = dependancies[0];
		for (int i = 1; i < dependancies.length; i++) {
			requiredFiles += ":" + dependancies[i];
		}
		
		String[] commands = {
				"java",
				"-cp",
				requiredFiles,
				"org.junit.runner.JUnitCore",
				testPath
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		Pair<String,String> result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	public static Pair<String,String> generateRandoopTestCases(
			String targetLibraryAndDependancy,
			String classList,
			int seed,
			int timeLimit,
			String outputDir,
			String workingPath) {
		
		String[] commands = {
				"java",
				"-cp",
				"./lib/randoop-2.1.4.jar:" + targetLibraryAndDependancy,
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
		Pair<String,String> result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	public static void extractJarClassFiles(String file, String workingPath) {
		String[] commands =  {
				"jar",
				"-xvf",
				file
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		executor.execOneThread(commands, workingPath);
		
	}
	
	public static Pair<String,String> generateEvosuiteTestCasesForAClass(
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
				"./lib/evosuite-1.0.2.jar",
				"-generateSuite",
				"-class",
				className,
				"-projectCP",
				targetLibrary,
				"-seed",
				"" + seed,
				"-Dtest_dir=" + outputDir,
				"-Dsearch_budget=" + timeLimit,
				"-Dstopping_condition=MaxTime"
		};
		
		ExecCommand executor = new ExecCommand();
		printCommands(commands);
		Pair<String,String> result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	public static Pair<String,String> generateEvosuiteTestCasesForALibrary(
			String targetLibrary,
			int seed,
			int timeLimit,
			String dependancies,
			String outputDir,
			String workingPath) {
		
		String[] commands = {
				"java",
				"-jar",
				"./lib/evosuite-1.0.2.jar",
				"-target",
				targetLibrary,
				"-generateSuite",
				"-projectCP",
				dependancies,
				"-seed",
				"" + seed,
				"-Dtest_dir=" + outputDir,
				"-Dsearch_budget=" + timeLimit,
				"-Dstopping_condition=MaxTime"
		};
		
		printCommands(commands);
		ExecCommand executor = new ExecCommand();
		Pair<String,String> result = executor.execOneThread(commands, workingPath);
		return result;
	}
	
	public static void generateClasslist(String targetlib, String saveFile) {
		final List<String> classNames = new ArrayList<String>();

        ZipInputStream zip;
        try {
            zip = new ZipInputStream(new FileInputStream(targetlib));
            
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry())
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    // This ZipEntry represents a class. Now, what class
                    // does it
                    // represent?
                    final String fullclassName = entry.getName().replace('/', '.');//.replaceAll("\\$",""); // including
                                                                                  // //
                    if (!entry.getName().contains("$"))                                                            // ".class"
                    	classNames.add(fullclassName.substring(0, fullclassName.length() - 6));
                    
                }
            zip.close();
            
        } catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        WriteLinesToFile.writeLinesToFile(classNames, saveFile);
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
