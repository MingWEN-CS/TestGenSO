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
		System.out.println(lines.size());
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.startsWith("java -cp ./lib/junit-4.12.jar")) {
				String[] tmp = line.split(" ");
				if (!currentClass.equals("") && !flag)
					filenames.add(currentClass);
				flag = true;
				currentClass = tmp[tmp.length - 1];
			}
			
			if (!line.contains("o.e.r.c.ClassStateSupport"))
				continue;
//			System.out.println(line);
			String status = line.substring(0, line.indexOf("o.e.r.c.ClassStateSupport"));
			if (status.contains("ERROR")) flag = false;
		}
		
		for (String tmp : filenames) {
			System.out.println(tmp);
		}
		return filenames;
	}
	
	
	public static void removeErrorTestCase() {
		String from = "./log/generateEvosuiteTest";
		String to = "./log2";
		TestCommandHelp.move(from, to, ".");
	}
	
	public static void main(String[] args) {
		ParsingArguments.parsingArguments(args);
//		removeErrorTestCase();
		List<String> errorList = getErrorList("./log/evaluateEvosuite.seed.0.commons.lang3");
	}
}
