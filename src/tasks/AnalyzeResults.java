package tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
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
	
	public String targetLibrary = config.Config.targetLib;
	public String prefix = config.Config.targetLibraryDir + File.separator + targetLibrary;
	
	public String evosuiteDirPrefix = prefix + File.separator + "evosuite-reports";
	public String evosuiteDate = "201606201511";
	public String evosuiteDir = evosuiteDirPrefix + File.separator + "report-0";
	
	public String randoopDirPrefix = prefix + File.separator + "randoop-reports";
	public String randoopDate = "201606162039";
	public String randoopDir = randoopDirPrefix + File.separator + "report-0";
	
	public String testSODirPrefix = prefix + File.separator + "testSO-reports";
	public String testSODate = "201606162039";
	public String testSODir = testSODirPrefix + File.separator + testSODate;

	
	public MutantPerClass getMutationScoreOf(String filename) {
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
	
	public HashSet<String> getNoCoveragePackage(String filename) {
		HashSet<String> classnames = new HashSet<String>();
		String content = FileToLines.fileToString(filename);
		Document doc = Jsoup.parse(content);
		Elements tables = doc.select("table");
		
		if (tables.size() != 2)
			System.err.println("Error:\t Table size do not match!" );
		Element table = tables.get(1);
		String classname = "";
		int coverage = 0;
		
		Elements tbody = table.select("tbody");
		Elements tds = tbody.get(0).select("td");
		for (int i = 0; i < tds.size(); i++) {
			if (i % 3 == 0) {
				classname = tds.get(i).text().trim();
			} else if (i % 3 == 1) {
				coverage = Integer.parseInt(tds.get(i).text().split("%")[0]);
				if (coverage == 0)
					classnames.add(classname);
			}
		}
			
		
		
		return classnames;
	}
	
	public HashSet<String> getNoCoverageClass() {
		HashSet<String> classnames = new HashSet<String>();
		
		HashSet<String> evosuite = new HashSet<String>();
		List<String> reports = FileListUnderDirectory.getFileListUnder(evosuiteDir + File.separator + evosuiteDate, "index.html");
		for (String report : reports) {
			String packagename = report.replace("\\", ".");
			packagename = packagename.replace("/", ".");
			if (!packagename.contains(config.Config.libToPackage.get(targetLibrary))) continue;
			packagename = packagename.substring(packagename.indexOf(config.Config.libToPackage.get(targetLibrary)), packagename.length() - 10);
			HashSet<String> tmp = getNoCoveragePackage(report);
//			System.out.println(packagename);
			for (String tmp1 : tmp) {
				evosuite.add(packagename + tmp1); 
			}
		}
		
		System.out.println("Evosuite Size:\t" + evosuite.size());
		System.out.println("Adding other test cases...");
		File[] files = new File(evosuiteDir).listFiles();
		System.out.println("Invalid Test Cases Length\t" + files.length);
		
		HashSet<String> nonEmpty = new HashSet<String>();
		for (File file : files) {
			if (!file.getName().endsWith("_ESTest")) continue;
//			System.out.println(file.getName());
			reports = FileListUnderDirectory.getFileListUnder(file.getAbsolutePath(), "index.html");
			HashSet<String> fullnames = new HashSet<String>();
			for (String report : reports) {
				
//				System.out.println(report);
				String reportDate = report.substring(report.indexOf("_ESTest") + 8);
				reportDate = reportDate.replace("\\", ".");
				reportDate = reportDate.replace("/", ".");
				if (!reportDate.contains(config.Config.libToPackage.get(targetLibrary))) continue;
				reportDate = reportDate.substring(0, reportDate.indexOf(config.Config.libToPackage.get(targetLibrary)) - 1);
//				System.out.println(reportDate);
//				if (report.contains("com.google.common.base")) continue;
//				System.out.println(report + "\t" + report.indexOf(reportDir));
//				String classname = report.substring(report.indexOf(reportDate) + reportDate.length() + 1);
				String packagename = report.replace("\\", ".");
				packagename = packagename.replace("/", ".");
				packagename = packagename.substring(packagename.indexOf(reportDate) + reportDate.length());
//				System.out.println(packagename);
				if (!packagename.contains(config.Config.libToPackage.get(targetLibrary))) continue;
				packagename = packagename.substring(packagename.indexOf(config.Config.libToPackage.get(targetLibrary)), packagename.length() - 10);
//				System.out.println(packagename);
				HashSet<String> names = getNoCoveragePackage(report);
				for (String name : names) fullnames.add(packagename + name); 
			}
			
//			System.out.println(fullnames.toString());
			for (String empty : evosuite) {
				if (!fullnames.contains(empty))
					nonEmpty.add(empty);
			}
		}
		
		evosuite.removeAll(nonEmpty);
		
		System.out.println("Evosuite Size:\t" + evosuite.size());
		
		HashSet<String> randoop = new HashSet<String>();
		reports = FileListUnderDirectory.getFileListUnder(randoopDir + File.separator + randoopDate, "index.html");
		for (String report : reports) {
			String packagename = report.replace("\\", ".");
			packagename = packagename.replace("/", ".");
			if (!packagename.contains(config.Config.libToPackage.get(targetLibrary))) continue;
			packagename = packagename.substring(packagename.indexOf(config.Config.libToPackage.get(targetLibrary)), packagename.length() - 10);
			HashSet<String> tmp = getNoCoveragePackage(report);
//			System.out.println(packagename);
			for (String tmp1 : tmp) {
				randoop.add(packagename + tmp1); 
			}
		}

		System.out.println("Randoop Size:\t" + randoop.size());
		
		HashSet<String> testSO = new HashSet<String>();
		reports = FileListUnderDirectory.getFileListUnder(testSODir, "index.html");
		for (String report : reports) {
			String packagename = report.replace("\\", ".");
			packagename = packagename.replace("/", ".");
			if (!packagename.contains(config.Config.libToPackage.get(targetLibrary))) continue;
			packagename = packagename.substring(packagename.indexOf(config.Config.libToPackage.get(targetLibrary)), packagename.length() - 10);
			HashSet<String> tmp = getNoCoveragePackage(report);
//			System.out.println(packagename);
			for (String tmp1 : tmp) {
				testSO.add(packagename + tmp1); 
			}
		}
		
		System.out.println("TestSO size:\t" + testSO.size());
		
		System.out.println(evosuite.toString());
		System.out.println(randoop.toString());
		System.out.println(testSO.toString());
		classnames.addAll(evosuite);
		classnames.addAll(randoop);
		classnames.addAll(testSO);
		System.out.println("All Size:\t" + classnames.size());
		return classnames;
	}
	
	public void compareMutationScore() {		
		System.out.println(evosuiteDir + File.separator + evosuiteDate);
		List<String> reports = FileListUnderDirectory.getFileListUnder(evosuiteDir + File.separator + evosuiteDate, ".html");
		MutantPerProject evosuite = new MutantPerProject();
		System.out.println("Getting Evosuite Results...");
//		System.out.println(reportDir);
		HashSet<String> noCoverageClasses = getNoCoverageClass();
		for (String report : reports) {
			if (report.endsWith("index.html")) continue;
//			if (report.contains("com.google.common.base")) continue;
//			System.out.println(report + "\t" + report.indexOf(reportDir));
			String classname = report.substring(report.indexOf(evosuiteDate) + evosuiteDate.length() + 1);
//			System.out.println(classname);
			MutantPerClass mpc = getMutationScoreOf(report);
//			System.out.println(classname + "\t" + mpc.getMutationScore());
			classname = classname.replace("\\", ".");
			classname = classname.replace("/", ".");
			classname = classname.substring(0, classname.length() - 5);
			evosuite.addClass(classname, mpc);
		}
		System.out.println(evosuite.getMutationScore(noCoverageClasses));
		
		System.out.println("Adding other test cases...");
		File[] files = new File(evosuiteDir).listFiles();
		System.out.println("Invalid Test Cases Length\t" + files.length);
		for (File file : files) {
			if (!file.getName().endsWith("_ESTest")) continue;
//			System.out.println(file.getName());
			reports = FileListUnderDirectory.getFileListUnder(file.getAbsolutePath(), ".html");
			MutantPerProject testClass = new MutantPerProject();
//			System.out.println("report size:\t" + reports.size());
			if (reports.size() == 0) continue;
			for (String report : reports) {
//				System.out.println(report);
				if (report.endsWith("index.html")) continue;
//				System.out.println(report);
				String reportDate = report.substring(report.indexOf("_ESTest") + 8);
//				System.out.println(reportDate);
				reportDate = reportDate.replace("\\", ".");
				reportDate = reportDate.replace("/", ".");
				reportDate = reportDate.substring(0, reportDate.indexOf(config.Config.libToPackage.get(targetLibrary)) - 1);
//				System.out.println(reportDate);
//				if (report.contains("com.google.common.base")) continue;
//				System.out.println(report + "\t" + report.indexOf(reportDir));
				String classname = report.substring(report.indexOf(reportDate) + reportDate.length() + 1);
//				System.out.println(classname);
				MutantPerClass mpc = getMutationScoreOf(report);
//				System.out.println(classname + "\t" + mpc.getMutationScore());
				classname = classname.replace("\\", ".");
				classname = classname.replace("/", ".");
				classname = classname.substring(0, classname.length() - 5);
				testClass.addClass(classname, mpc);
			}
			
			evosuite.combineTestSuite(testClass);
		}
		System.out.println("After combining...");
		System.out.println(evosuite.getMutationScore(noCoverageClasses));
		
		System.out.println("Getting Randoop Results...");
		System.out.println(randoopDir + File.separator + randoopDate);
		reports = FileListUnderDirectory.getFileListUnder(randoopDir + File.separator + randoopDate, ".html");
		MutantPerProject randoop = new MutantPerProject();
		
		for (String report : reports) {
			if (report.endsWith("index.html")) continue;
//			if (report.contains("com.google.common.base")) continue;
//			System.out.println(report + "\t" + report.indexOf(reportDir));
			String classname = report.substring(report.indexOf(randoopDate) + randoopDate.length() + 1);
//			System.out.println(classname);
			MutantPerClass mpc = getMutationScoreOf(report);
//			System.out.println(classname + "\t" + mpc.getMutationScore());
			classname = classname.replace("\\", ".");
			classname = classname.replace("/", ".");
			classname = classname.substring(0, classname.length() - 5);
			randoop.addClass(classname, mpc);
		}
		System.out.println(randoop.getMutationScore(noCoverageClasses));
		
		System.out.println("Getting TestSO Results...");
		reports = FileListUnderDirectory.getFileListUnder(testSODir, ".html");
		MutantPerProject testSO = new MutantPerProject();
		System.out.println(testSO);
		for (String report : reports) {
			if (report.endsWith("index.html")) continue;
//			if (report.contains("com.google.common.base")) continue;
//			System.out.println(report + "\t" + report.indexOf(reportDir));
			String classname = report.substring(report.indexOf(testSODate) + testSODate.length() + 1);
//			System.out.println(classname);
			MutantPerClass mpc = getMutationScoreOf(report);
//			System.out.println(classname + "\t" + mpc.getMutationScore());
			classname = classname.replace("\\", ".");
			classname = classname.replace("/", ".");
			classname = classname.substring(0, classname.length() - 5);
			testSO.addClass(classname, mpc);
		}
		System.out.println(testSO.getMutationScore(noCoverageClasses));
		
		System.out.println("Randoop + Evosuite");
		
		MutantPerProject tmp1 = new MutantPerProject();
		tmp1.combineTestSuite(evosuite);
		tmp1.combineTestSuite(randoop);
		System.out.println(tmp1.getMutationScore(noCoverageClasses));
		
		System.out.println("Randoop + TestSO");
		MutantPerProject tmp2 = new MutantPerProject();
		tmp2.combineTestSuite(testSO);
		tmp2.combineTestSuite(randoop);
		System.out.println(tmp2.getMutationScore(noCoverageClasses));
		
		System.out.println("Evosuite + TestSO");
		MutantPerProject tmp3 = new MutantPerProject();
		tmp3.combineTestSuite(testSO);
		tmp3.combineTestSuite(evosuite);
		System.out.println(tmp3.getMutationScore(noCoverageClasses));
		
//		randoop.combineTestSuite(testSO);
		System.out.println("Randoop + Evosuite + TestSO");
		
		MutantPerProject tmp4 = new MutantPerProject();
		tmp4.combineTestSuite(testSO);
		tmp4.combineTestSuite(randoop);
		tmp4.combineTestSuite(evosuite);
		System.out.println(tmp4.getMutationScore(noCoverageClasses));
	}
	
	public static void main(String[] args) {
		ParsingArguments.parsingArguments(args);
		AnalyzeResults ar = new AnalyzeResults();
		ar.compareMutationScore();
//		getNoCoverageClass();
//		String filename = "./report/201606090926/org.apache.commons.math3.analysis/FunctionUtils.java.html";
//		getMutationScoreOf(filename);
	}
}
