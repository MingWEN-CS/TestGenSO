package generics;

import java.util.HashMap;

import utils.Pair;

public class MutantPerProject {
	
	public HashMap<String, MutantPerClass> mutationScore;
	
	
	public MutantPerProject() {
		mutationScore = new HashMap<String,MutantPerClass>();
	}
	
	public void addClass(String classname, MutantPerClass mpc) {
		mutationScore.put(classname, mpc);
	}
	
	public double getMutationScore() {
		int killed = 0;
		int all = 0;
		Pair<Integer,Integer> tmp;
		for (String classname : mutationScore.keySet()) {
			tmp = mutationScore.get(classname).mutationScore();
			killed += tmp.getKey();
			all += tmp.getValue();
//			score += mp.getKey() * 1.0 / tmp.getValue();
		}
		System.out.println(killed + "\t" + all + "\t" + killed * 1.0 / all);
		return killed * 1.0 / all;
	}
	
	public void combineTestSuite(MutantPerProject b) {
		for (String mutant : mutationScore.keySet()) {
			MutantPerClass a = mutationScore.get(mutant);
			if (!b.mutationScore.containsKey(mutant))
				System.err.println("Error:\t do not contain " + mutant);
			MutantPerClass mcp = b.mutationScore.get(mutant);
			a.merge(mcp);
			mutationScore.put(mutant, a);
		}
	}
}
