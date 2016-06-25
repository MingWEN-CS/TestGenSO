package tasks;

import java.io.File;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import config.ParsingArguments;
import generics.MutantPerClass;
import generics.MutantPerProject;
import utils.FileListUnderDirectory;
import utils.FileToLines;
import utils.Pair;

public class AnalyzeResults {
	
	public static MutantPerClass getMutationScoreOf(String filename) {
		String content = FileToLines.fileToString(filename);
		
		Document doc = Jsoup.parse(content);
		Elements mutants = doc.select("tr");
//		System.out.println(mutants.size());
		MutantPerClass mpc = new MutantPerClass();
		int count = 0;
		for (Element mutant : mutants) {
			Elements tds = mutant.select("td");
//			System.out.println(tds.size());
			if (tds.size() == 3) {
				Element firstTd = tds.get(0);
				if (!firstTd.hasText()) continue;
				int line = Integer.parseInt(firstTd.select("a").text());
				
				Elements ops = tds.get(2).select("p");
//				System.out.println(line + "\t" + ops.size());
				count += ops.size();
				for (Element op : ops) {
					String info = op.text();
					String[] tmp = info.split(":");
					
					if (tmp.length >= 3) {
						int id = Integer.parseInt(tmp[0].substring(0, tmp[0].indexOf(".")));
						info = "";
						for (int i = 2; i < tmp.length; i++)
							info += tmp[i];
//						info = tmp[2].trim();
						tmp = info.trim().split(" ");
						String status = tmp[tmp.length - 1];
						String testCase = "";
						if (status.equals("KILLED")) {
							testCase = tmp[0];
						}
						String mutantOp = tmp[1];
						for (int i = 2; i < tmp.length - 2; i++)
							mutantOp += " " + tmp[i];
						mutantOp = mutantOp.replace("/", ".");
						mutantOp = mutantOp.replace("\\", ".");
//						System.out.println(testCase + "\t" + mutantOp + "\t" + status);
						mpc.updateMutation(line + ":" + id + ":" + mutantOp, status, testCase);
					} else {
						System.out.println("Error\t" + info);
					}
				}
			} 
//			System.out.println(mutant.toString());
		}
//		System.out.println(count);
//		Pair<Integer,Integer> ms = mpc.mutationScore();
//		System.out.println(ms.getKey() + "\t" + ms.getValue() + "\t" + ms.getKey() * 1.0 / ms.getValue());
		return mpc;
	}
	
	public static void compareMutationScore() {
		String targetLibrary = config.Config.targetLib;
		String prefix = config.Config.targetLibraryDir + File.separator + targetLibrary;
		String reportDirPrefix = prefix + File.separator + "evosuite-reports";
		String reportDate = "201606201511";
		String reportDir = reportDirPrefix + File.separator + "report-0";
		
		List<String> reports = FileListUnderDirectory.getFileListUnder(reportDir + File.separator + reportDate, ".html");
		MutantPerProject evosuite = new MutantPerProject();
		System.out.println("Getting Evosuite Results...");
		System.out.println(reportDir);
		for (String report : reports) {
			if (report.endsWith("index.html")) continue;
//			if (report.contains("com.google.common.base")) continue;
//			System.out.println(report + "\t" + report.indexOf(reportDir));
			String classname = report.substring(report.indexOf(reportDate) + reportDate.length() + 1);
//			System.out.println(classname);
			MutantPerClass mpc = getMutationScoreOf(report);
//			System.out.println(classname + "\t" + mpc.getMutationScore());
			classname = classname.replace("\\", ".");
			classname = classname.replace("/", ".");
			evosuite.addClass(classname, mpc);
		}
		System.out.println(evosuite.getMutationScore());
		
		System.out.println("Adding other test cases...");
		File[] files = new File(reportDir).listFiles();
		for (File file : files) {
			if (!file.getName().endsWith("_ESTest")) continue;
			System.out.println(file.getName());
			
			reports = FileListUnderDirectory.getFileListUnder(file.getAbsolutePath(), ".html");
			MutantPerProject testClass = new MutantPerProject();
			
			for (String report : reports) {
				
				if (report.endsWith("index.html")) continue;
				System.out.println(report);
				reportDate = report.substring(report.indexOf("_ESTest") + 8);
				System.out.println(reportDate);
				reportDate = reportDate.substring(0, reportDate.indexOf(config.Config.libToPackage.get(targetLibrary)) - 1);
				System.out.println(reportDate);
//				if (report.contains("com.google.common.base")) continue;
//				System.out.println(report + "\t" + report.indexOf(reportDir));
				String classname = report.substring(report.indexOf(reportDate) + reportDate.length() + 1);
//				System.out.println(classname);
				MutantPerClass mpc = getMutationScoreOf(report);
//				System.out.println(classname + "\t" + mpc.getMutationScore());
				classname = classname.replace("\\", ".");
				classname = classname.replace("/", ".");
				testClass.addClass(classname, mpc);
			}
			
			evosuite.combineTestSuite(testClass);
		}
		System.out.println("After combining...");
		System.out.println(evosuite.getMutationScore());
		
		System.out.println("Getting Randoop Results...");
		reportDirPrefix = prefix + File.separator + "randoop-reports";
		reportDate = "201606151422";
		reportDir = reportDirPrefix + File.separator + "report-2" + File.separator + reportDate;
		reports = FileListUnderDirectory.getFileListUnder(reportDir, ".html");
		MutantPerProject randoop = new MutantPerProject();
		System.out.println(reportDir);
		for (String report : reports) {
			if (report.endsWith("index.html")) continue;
//			if (report.contains("com.google.common.base")) continue;
//			System.out.println(report + "\t" + report.indexOf(reportDir));
			String classname = report.substring(report.indexOf(reportDate) + reportDate.length() + 1);
//			System.out.println(classname);
			MutantPerClass mpc = getMutationScoreOf(report);
//			System.out.println(classname + "\t" + mpc.getMutationScore());
			classname = classname.replace("\\", ".");
			classname = classname.replace("/", ".");
			randoop.addClass(classname, mpc);
		}
		System.out.println(randoop.getMutationScore());
		
		System.out.println("Getting TestSO Results...");
		reportDirPrefix = prefix + File.separator + "testSO-reports";
		reportDate = "201606162039";
		reportDir = reportDirPrefix + File.separator + "report-0" + File.separator + reportDate;
		reports = FileListUnderDirectory.getFileListUnder(reportDir, ".html");
		MutantPerProject testSO = new MutantPerProject();
		System.out.println(reportDir);
		for (String report : reports) {
			if (report.endsWith("index.html")) continue;
//			if (report.contains("com.google.common.base")) continue;
//			System.out.println(report + "\t" + report.indexOf(reportDir));
			String classname = report.substring(report.indexOf(reportDate) + reportDate.length() + 1);
//			System.out.println(classname);
			MutantPerClass mpc = getMutationScoreOf(report);
//			System.out.println(classname + "\t" + mpc.getMutationScore());
			classname = classname.replace("\\", ".");
			classname = classname.replace("/", ".");
			testSO.addClass(classname, mpc);
		}
		System.out.println(testSO.getMutationScore());
		
//		randoop.combineTestSuite(testSO);
		testSO.combineTestSuite(randoop);
		testSO.combineTestSuite(evosuite);
		System.out.println(randoop.getMutationScore());
	}
	
	public static void main(String[] args) {
		ParsingArguments.parsingArguments(args);
		compareMutationScore();
//		String filename = "./report/201606090926/org.apache.commons.math3.analysis/FunctionUtils.java.html";
//		getMutationScoreOf(filename);
	}
}
