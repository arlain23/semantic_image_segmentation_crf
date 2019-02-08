package masters.inference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.Constants.State;
import masters.factorisation.FactorGraphModel;
import masters.features.BinaryMask;
import masters.image.ImageDTO;
import masters.superpixel.SuperPixelDTO;
import masters.train.WeightVector;
import masters.utils.DataHelper;
import masters.utils.ParametersContainer;
import masters.utils.ResultAnalyser;

public class InferenceHelper {
	public static void runInference(String baseImagePath, String typeFolderName, 
			ParametersContainer parameterContainer, WeightVector weights) {
		
		
		Map<String, File> testFiles = DataHelper.getFilesFromDirectory(Constants.TEST_PATH);
		Map<String, File> initFiles = DataHelper.getFilesFromDirectory(Constants.TEST_INIT_PATH);
		Map<String, File> resultFiles = DataHelper.getFilesFromDirectory(Constants.TEST_RESULT_PATH);
		
		Map<Integer,List<Double>> imageIoUs = new HashMap<>();
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			imageIoUs.put(label, new ArrayList<Double>());
		}
		
		int imageCounter = 0;
		for (String fileName : testFiles.keySet()) {
			imageCounter++;
			File testFile = testFiles.get(fileName);
			File resultFile = resultFiles.get(fileName + Constants.RESULT_IMAGE_SUFFIX);
			File initFile = null;
			if (Constants.USE_INIT_IMAGE_FOR_PARIWISE_POTENTIAL) {
				initFile = initFiles.get(fileName);
			}
			
			ImageDTO currentImage = DataHelper.getSingleImage(testFile, initFile, State.TEST, parameterContainer);
			ImageDTO referenceImage = DataHelper.getSingleImageSegmented(testFile, resultFile, initFile, State.TEST_IOU, parameterContainer);
			
			
			
			  System.out.println("inference image " + imageCounter);
			  
			  FactorGraphModel factorGraph = new FactorGraphModel(currentImage, new ArrayList<>(), weights, parameterContainer);
			  
			  
			  
			  DataHelper.saveImageSuperpixelBordersOnlyOriginalData(currentImage, currentImage.getSuperPixels(), baseImagePath + "images\\"+imageCounter + ".png");
			  List<SuperPixelDTO> superPixels = currentImage.getSuperPixels();
			  
			  String saveProgressPath = baseImagePath + typeFolderName + "\\progress\\" + imageCounter + "\\";
			  String saveFinalPath = baseImagePath + typeFolderName + "\\final\\" + imageCounter + ".png";
			  
			  
			  // inference 
			  for (int t = 0; t < 20; t++) {
				  System.out.println("iteration " + t);
				  factorGraph.computeFactorToVariableMessages();
				  factorGraph.computeVariableToFactorMessages();

				  factorGraph.updatePixelData();
				  factorGraph.computeLabeling();

				  boolean shown = false;
//				  if (t%5 == 0) {
				  shown = true;
				  DataHelper.saveImageSegmentedSuperPixels(currentImage, superPixels, saveProgressPath + t + ".png");
						
		
//				  }
				  if (factorGraph.checkIfConverged()) {
					  System.out.println("converged");
					  factorGraph.updatePixelData();
					  if (!shown) {
						  DataHelper.saveImageSegmentedSuperPixels(currentImage, superPixels, saveProgressPath + t + ".png");
					  }
					  break;
				  }
			  }
			  factorGraph.computeLabeling();
			  
			  
			  String initPath = saveFinalPath.replace(".png", "_I.png");
			  DataHelper.saveImageSegmentedSuperPixels(referenceImage, referenceImage.getSuperPixels(), initPath);
			  DataHelper.saveImageSegmentedSuperPixels(currentImage, superPixels, saveFinalPath);
			  Map<Integer, BinaryMask> resultingLabelMap = currentImage.getResultingLabelMasks();
			  Map<Integer, BinaryMask> referenceLabelMap = referenceImage.getLabelMap();
			  
			  Map<Integer, Double> labelsIoUMap = ResultAnalyser.getIoUPrecision(referenceLabelMap, resultingLabelMap);
			  for (Integer label : labelsIoUMap.keySet()) {
				  List<Double> precisionList = imageIoUs.get(label);
				  precisionList.add(labelsIoUMap.get(label));
				  imageIoUs.put(label, precisionList);
			  }
			  
			  
			  System.out.println("finished for image " + imageCounter);
		  }
		
		System.out.println("**************** IoU analysis ******************");
		for (Integer label : imageIoUs.keySet()) {
			double precisionSum = 0.0;
			List<Double> precisionList = imageIoUs.get(label);
			double precisionSumLength = precisionList.size();
			System.out.println("label: " + label);
			for (Double IoU : precisionList) {
				System.out.print(IoU + " ");
				if (IoU != -1) {
					precisionSum += IoU;
				} else {
					precisionSumLength--;
				}
			}
			System.out.println();
			System.out.println("AVG: " + (precisionSum / precisionSumLength));
		}
		
		
	}
	
	private static Logger _log = Logger.getLogger(InferenceHelper.class);
	
}