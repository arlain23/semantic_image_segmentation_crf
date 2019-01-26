package masters.cache;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CacheUtils {
	public static void saveStringToFile(String path, String stringToSave) {
		File file = new File(path);
		file.getParentFile().mkdirs();
		
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(path));
			bw.write(stringToSave);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
