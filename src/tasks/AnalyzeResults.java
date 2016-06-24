package tasks;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import generics.MutantPerClass;
import utils.FileToLines;
import utils.Pair;

public class AnalyzeResults {
	
	public static void getMutationScoreOf(String filename) {
		String content = FileToLines.fileToString(filename);
		
		Document doc = Jsoup.parse(content);
		Elements mutants = doc.select("tr");
		System.out.println(mutants.size());
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
						tmp = info.split(" ");
						String status = tmp[tmp.length - 1];
						String testCase = "";
						if (status.equals("KILLED")) {
							testCase = tmp[0];
						}
						String mutantOp = tmp[1];
						for (int i = 2; i < tmp.length - 2; i++)
							mutantOp += " " + tmp[i];
//						System.out.println(testCase + "\t" + mutantOp + "\t" + status);
						mpc.updateMutation(line + ":" + id + ":" + mutantOp, status, testCase);
					} else {
						System.out.println("Error\t" + info);
					}
				}
			} 
//			System.out.println(mutant.toString());
		}
		System.out.println(count);
		Pair<Integer,Integer> ms = mpc.mutationScore();
		System.out.println(ms.getKey() + "\t" + ms.getValue() + "\t" + ms.getKey() * 1.0 / ms.getValue());
	}
	
	public static void main(String[] args) {
		String filename = "./report/201606090926/org.apache.commons.math3.analysis/FunctionUtils.java.html";
		getMutationScoreOf(filename);
	}
}
