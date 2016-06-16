package utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ExecCommand {
	
	public String execOneThread(String command, String workingpath) {
		final StringBuffer result = new StringBuffer("");
		try {
			File dir = new File(workingpath);
			Process process = Runtime.getRuntime().exec(command, null, dir);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line = null;
			while ((line = stdInput.readLine()) != null) {
				result.append(line + "\n");
			}
			
			while ((line = stdError.readLine()) != null) {
				System.out.println(line);
			}
			
			stdInput.close();
			stdError.close();
		} catch (Exception e) {
			System.err.println("Error:" + command);
			return null;
		}
		return result.toString();
	}
	
	public Pair<String,String> execOneThread(String[] commands, String workingpath) {
		final StringBuffer result = new StringBuffer("");
		final StringBuffer errors = new StringBuffer("");
		try {
			File dir = new File(workingpath);
			Process process = Runtime.getRuntime().exec(commands, null, dir);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line = null;
			process.waitFor();
			while ((line = stdInput.readLine()) != null) {
				System.out.println(line);
				result.append(line + "\n");
			}
			
			while ((line = stdError.readLine()) != null) {
				System.err.println(line);
				errors.append(line + "\n");
			}
			
			stdInput.close();
			stdError.close();
		} catch (Exception e) {
			System.err.println("Error:" + commands);
			return null;
		}
		return new Pair<String,String>(result.toString(), errors.toString());
	}
}