package masters.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import masters.Constants;

public class SerializationUtil {
  
  private static String workPath = Constants.WORK_PATH;
  
  public static void writeObjectToFile(Object object, String fileName) throws IOException {
    String path = workPath + File.separator + fileName;
    
    File outputFile = new File(path);
    outputFile.getParentFile().mkdirs();
    
    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

    // Write objects to file
    objectOutputStream.writeObject(object);
    
    objectOutputStream.close();
  }
  public static Object readObjectFromFile(String fileName) throws IOException, ClassNotFoundException {
	  try {
	    String path = workPath + File.separator + fileName;
	    FileInputStream  fileInputStream = new FileInputStream (new File(path));
	    ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	    Object obj = objectInputStream.readObject();
	    objectInputStream.close();
	    return obj;
	  } catch(FileNotFoundException e) {
		  return new HashMap<>();
	  }
	    
  }
  
}
