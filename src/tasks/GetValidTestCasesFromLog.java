package tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import config.ParsingArguments;
import utils.FileToLines;
import utils.TestCommandHelp;

public class GetValidTestCasesFromLog {
	
	public static List<String> getErrorList(String file) {
		List<String> filenames = new ArrayList<String>();
		List<String> lines = FileToLines.fileToLines(file);
		String currentClass = "";
		boolean flag = true;
		for (String line : lines) {
			if (line.startsWith("java -cp ./lib/junit-4.12.jar")) {
				String[] tmp = line.split(" ");
				if (!currentClass.equals("") && !flag)
					filenames.add(currentClass);
				flag = true;
				currentClass = tmp[tmp.length - 1];
			}
			if (!line.contains("org.evosuite.runtime"))
				continue;
			String status = line.substring(0, line.indexOf("org.evosuite.runtime"));
			if (status.contains("ERROR")) flag = false;
		}
		
		for (String tmp : filenames) {
			System.out.println(tmp);
		}
		return filenames;
	}
	
	
	public static void removeErrorTestCase() {
		List<String> errorList = getErrorList("./log/evaluateEvosuite.seed.0");
		String targetLibrary = config.Config.targetLib;
		String prefix = config.Config.targetLibraryDir + File.separator + targetLibrary;
		String testCasePrefix = prefix + File.separator + "evosuite-tests";
		String testCaseDir = testCasePrefix + File.separator + "evosuite-tests-" + 30 + "-" + 0;
		String invalid = testCaseDir + File.separator + "invaid";
		File file = new File(invalid);
		if (!file.exists()) file.mkdir();
		for (String error : errorList) {
			String path = testCaseDir + File.separator + error.replace(".", "/") + "*";
			String target = testCaseDir + File.separator + error;
			TestCommandHelp.move(path, target, ",");
		}
	}
	
	public static void main(String[] args) {
		ParsingArguments.parsingArguments(args);
		removeErrorTestCase();
	}
}
