package tasks;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import utils.FileToLines;

public class AnalyzeResults {
	
	public static void getMutationScoreOf(String filename) {
		String content = FileToLines.fileToString(filename);
		
		Document doc = Jsoup.parse(content);
		Elements mutants = doc.select("p");
		System.out.println(mutants.size());
	}
	
	public static void main(String[] args) {
		String filename = "./report/201606090926/org.apache.commons.math3.analysis/FunctionUtils.java.html";
		getMutationScoreOf(filename);
	}
}
