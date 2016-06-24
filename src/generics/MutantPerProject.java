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
		double score = 0;
		Pair<Integer,Integer> tmp;
		for (String classname : mutationScore.keySet()) {
			tmp = mutationScore.get(classname).mutationScore();
			score += tmp.getKey() * 1.0 / tmp.getValue();
		}
		
		return score / mutationScore.keySet().size();
	}
}
