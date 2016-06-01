package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileClassesGen {
	public static void main(String[] args) {
		final List<String> classNames = new ArrayList<String>();

        ZipInputStream zip;
        try {
            zip = new ZipInputStream(new FileInputStream("./targets/commons-math3-3.6.1.jar"));
            
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry())
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    // This ZipEntry represents a class. Now, what class
                    // does it
                    // represent?
                    final String fullclassName = entry.getName().replace('/', '.');//.replaceAll("\\$",""); // including
                                                                                  // //
                    if (!entry.getName().contains("$"))                                                            // ".class"
                    	classNames.add(fullclassName.substring(0, fullclassName.length() - 6));
                    
                }
            zip.close();
            
        } catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        String filename = "classnames.txt";
        WriteLinesToFile.writeLinesToFile(classNames, filename);
        
	}
}
