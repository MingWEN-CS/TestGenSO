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
		ReadStream s1,s2;
		Pair<String,String> result = null;
		try {
			File dir = new File(workingpath);
			Process process = Runtime.getRuntime().exec(commands, null, dir);
			
			s1 = new ReadStream("stdin",process.getInputStream());
			s2 = new ReadStream("stderr", process.getErrorStream());
			s1.start();
			s2.start();
			process.waitFor();
			
//			String line = null;
//			while ((line = stdInput.readLine()) != null) {
//				System.out.println(line);
//				result.append(line + "\n");
//			}
//			
//			while ((line = stdError.readLine()) != null) {
//				System.err.println(line);ls
//				errors.append(line + "\n");
//			}
//			
//			stdInput.close();
//			stdError.close();
			result = new Pair<String,String>(s1.output,s2.output);
		} catch (Exception e) {
			System.err.println("Error:" + e.getClass());
			return result;
		}
		return result;
	}
}

