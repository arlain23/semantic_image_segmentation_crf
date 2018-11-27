package masters.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import masters.image.ImageDTO;

public class XLSHelper {
	
	public static void saveProbabilityInformation(String path, Map<ImageDTO, List<List<Double>>> imageProbabilityMap) 
			throws FileNotFoundException, IOException {
		Workbook wb = new HSSFWorkbook();

		for (ImageDTO image : imageProbabilityMap.keySet()) {
			List<List<Double>> labelProbabilities = imageProbabilityMap.get(image);
			File file = new File(image.getPath());
			Sheet sheet = wb.createSheet("Image " + file.getName());
			Row titleRow = sheet.createRow(0);
			titleRow.createCell(0).setCellValue("LABEL");
			titleRow.createCell(1).setCellValue("SUPERPIXELS");
			
			for (int label = 0; label < labelProbabilities.size(); label++) {
				List<Double> superPixelProbabilities = labelProbabilities.get(label);
				Row imageRow = sheet.createRow(label+1);
				imageRow.createCell(0).setCellValue("label " + label);
				for (int j = 0; j < superPixelProbabilities.size(); j++) {
					imageRow.createCell(j+1).setCellValue(superPixelProbabilities.get(j));
				}
			}

		}
		
		try  (OutputStream fileOut = new FileOutputStream(path)) {
		    wb.write(fileOut);
		    wb.close();
		}
	}
}
