package masters.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import masters.App;
import masters.Constants;
import masters.colors.ColorSpaceException;
import masters.factorisation.FactorGraphModel;
import masters.grid.GridHelper;
import masters.image.ImageDTO;
import masters.superpixel.SuperPixelDTO;
import masters.train.WeightVector;

public class DataFixer {
	
	public static boolean CLEAR_MAPPING_DATA = App.CLEAR_MAPPING_DATA;
	public static boolean CLEAR_FILES = App.CLEAR_FILES;
	public static boolean JOIN_MAPPINGS = App.JOIN_MAPPINGS;
	public static boolean REGENERATE_SAMPLES = App.REGENERATE_SAMPLES;
	private static Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap;
	
	
	public static void clearUnusedTestImages () throws URISyntaxException, IOException, ClassNotFoundException {
		 // clearning unused images
		  Set<String> UUIDToKeep = new HashSet<String>();
		  for (int i = 1; i < 6; i++) {
			  System.out.println("PROCESSING " + i);
			  Map<ImageDTO, FactorGraphModel> serializedMap = (Map<ImageDTO, FactorGraphModel>) SerializationUtil.readObjectFromFile(Constants.IMAGE_FOLDER +File.separator  + Constants.IMAGE_TO_FACTOR_GRAPH_TEST_FILE_NAME + i);
			  Map<ImageDTO, FactorGraphModel> tmpMap = new HashMap<ImageDTO, FactorGraphModel>();
			  
			  Map<String, File> chosenFiles = DataHelper.getFilesFromDirectory("C:\\Users\\anean\\Desktop\\crf_data_test\\" + i + "\\", true); 
			 
			  System.out.println("keeping ");
			  for (String uuid : chosenFiles.keySet()) {
				  UUIDToKeep.add(uuid);
				  System.out.println(uuid);
			  }
			  System.out.println();
			  System.out.println(chosenFiles.keySet().size());
			  System.out.println();
			  int iter = 0;
			  for (ImageDTO img : serializedMap.keySet()) {
				  FactorGraphModel model = serializedMap.get(img);
				  System.out.println(model.getUuid());
				  if (!UUIDToKeep.contains(model.getUuid())) {
					  String testPath = img.getPath();
					  File testFile = new File(testPath);
					  System.out.println("DELETE " + testFile.getAbsolutePath() + " " + (++iter));
					  testFile.delete();
				  } else {
					  tmpMap.put(img, model);
					  System.out.println("keep");
				  }
			  }
			  System.out.println("*************");
		  
			 SerializationUtil.writeObjectToFile(tmpMap, Constants.IMAGE_FOLDER + File.separator + Constants.IMAGE_TO_FACTOR_GRAPH_TEST_FILE_NAME + i);
		  }
		}
	
	
	@SuppressWarnings("unchecked")
	public static void clearUnusedImages () throws URISyntaxException, IOException, ClassNotFoundException {
	 // clearning unused images
	  Set<String> UUIDToKeep = new HashSet<String>();
	  for (int i = 1; i < 11; i++) {
		  System.out.println("PROCESSING " + i);
		  Map<ImageDTO, FactorGraphModel> serializedMap = (Map<ImageDTO, FactorGraphModel>) SerializationUtil.readObjectFromFile(Constants.IMAGE_FOLDER +File.separator  + Constants.IMAGE_TO_FACTOR_GRAPH_TRAIN_FILE_NAME + i);
		  Map<ImageDTO, FactorGraphModel> tmpMap = new HashMap<ImageDTO, FactorGraphModel>();
		  
		  Map<String, File> chosenFiles = DataHelper.getFilesFromDirectory("C:\\Users\\anean\\Desktop\\crf_data\\" + i + "\\", true); 
		 
		  System.out.println("keeping ");
		  for (String uuid : chosenFiles.keySet()) {
			  UUIDToKeep.add(uuid);
			  System.out.println(uuid);
		  }
		  System.out.println();
		  System.out.println(chosenFiles.keySet().size());
		  System.out.println();
		  int iter = 0;
		  for (ImageDTO img : serializedMap.keySet()) {
			  FactorGraphModel model = serializedMap.get(img);
			  System.out.println(model.getUuid());
			  if (!UUIDToKeep.contains(model.getUuid())) {
				  try {
					  String trainPath = img.getPath();
					  File trainFile = new File(trainPath);
					  System.out.println("DELETE " + trainFile.getAbsolutePath() + " " + (++iter));
					  trainFile.delete();
					  
					  String[] fileName = trainFile.getName().split(".png");
					  String resultPath = Constants.RESULT_PATH + fileName[0] + "_N." + Constants.IMAGE_EXTENSION;
					  File resultFile =  new File(DataHelper.class.getClassLoader().getResource(resultPath).toURI());
					  System.out.println("DELETE " + resultFile.getAbsolutePath() + " " + iter);
					  resultFile.delete();
				  } catch (NullPointerException e) {
					  System.out.println(e.getMessage());
				  }
			  } else {
				  tmpMap.put(img, model);
				  System.out.println("keep");
			  }
		  }
		  System.out.println("*************");
	  
		 SerializationUtil.writeObjectToFile(tmpMap, Constants.IMAGE_FOLDER + File.separator + Constants.IMAGE_TO_FACTOR_GRAPH_TRAIN_FILE_NAME + i);
	  }
	}
	
	public static void getCachedFactorisationModels(Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap) throws URISyntaxException, IOException, ClassNotFoundException {
		for (int i = 1; i < 11; i++) {
			  System.out.println("READING MAPPING  " + i);
			  Map<ImageDTO, FactorGraphModel> serializedMap = (Map<ImageDTO, FactorGraphModel>) SerializationUtil.readObjectFromFile(Constants.IMAGE_FOLDER +File.separator  + Constants.IMAGE_TO_FACTOR_GRAPH_TRAIN_FILE_NAME + i);
			  imageToFactorGraphMap.putAll(serializedMap);
			  if (CLEAR_FILES) {
				 clearTrainFiles(i, imageToFactorGraphMap);
			  }
			  System.out.println("TOTAL FILES " + serializedMap.keySet().size() );
			  if (REGENERATE_SAMPLES) {
				  regenerateTrainSamples(serializedMap);
			  }
			  
		  }
	}
	public static void getCachedTestFactorisationModels(Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap) throws URISyntaxException, IOException, ClassNotFoundException {
		for (int i = 1; i < 6; i++) {
			  System.out.println("READING MAPPING  " + i);
			  Map<ImageDTO, FactorGraphModel> serializedMap = (Map<ImageDTO, FactorGraphModel>) SerializationUtil.readObjectFromFile(Constants.IMAGE_FOLDER +File.separator  + Constants.IMAGE_TO_FACTOR_GRAPH_TEST_FILE_NAME + i);
			  imageToFactorGraphMap.putAll(serializedMap);
			  System.out.println("TOTAL FILES " + serializedMap.keySet().size() );
		  }
	}
	public static void regenerateTrainSamples(Map<ImageDTO, FactorGraphModel> serializedMap) throws URISyntaxException, IOException {
		int iter = 0;
		int total = serializedMap.keySet().size();
		for (ImageDTO img : serializedMap.keySet()) {
			DataHelper.saveTrainAndResult(img);
			System.out.println("SAVED " + (++iter) + "/" + total + "  "+ img.getPath());
		}
	}
	public static void clearTrainFiles(int i, Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap) {
		 Map<String, File> chosenFiles = DataHelper.getFilesFromDirectory("C:\\Users\\anean\\Desktop\\crf_data\\" + i + "\\", true); 
		 
		  Set<String> UUIDToKeep = new HashSet<String>();
		  for (String uuid : chosenFiles.keySet()) {
			  UUIDToKeep.add(uuid);
		  }
		  System.out.println("UUID to keep " + UUIDToKeep.size());
		  int iter = 0;
		  for (ImageDTO img : imageToFactorGraphMap.keySet()) {
			  FactorGraphModel model = imageToFactorGraphMap.get(img);
			  if (!UUIDToKeep.contains(model.getUuid())) {
				  try {
					  String trainPath = img.getPath();
					  File trainFile = new File(trainPath);
					  System.out.println("DELETE " + trainFile.getAbsolutePath() + " " + (++iter));
					  trainFile.delete();
					  
					  String[] fileName = trainFile.getName().split(".png");
					  String resultPath = Constants.RESULT_PATH + fileName[0] + "_N." + Constants.IMAGE_EXTENSION;
					  File resultFile =  new File(DataHelper.class.getClassLoader().getResource(resultPath).toURI());
					  System.out.println("DELETE " + resultFile.getAbsolutePath() + " " + iter);
					  resultFile.delete();
				  } catch (Exception e) {
					  
				  }
			  }
		  }
	}
	
	public static void generateTestSamples(ParametersContainer parameterContainer) throws ColorSpaceException, IOException {
		  Map<ImageDTO, FactorGraphModel> testMap = new HashMap<ImageDTO, FactorGraphModel>();
		  
		  List<Double> initWeightList = Arrays.asList(new Double[] {
				  0.0240453880751775, 3.544197728746141E-19, 7.100818535250691E-19 
		  });
		
		  WeightVector weightsw = new WeightVector(initWeightList);
		  
		  // get test image
		  List<ImageDTO> testImageList = DataHelper.getTestData();
		  int imageCounter = 0;
		  int testDataSize = testImageList.size();
		  int fileCounter = 1;
		  for (ImageDTO currentImage : testImageList) {
			  System.out.println("IMAGE " + (++imageCounter) + "/" + testDataSize);
			  List<SuperPixelDTO> createdSuperPixels = SuperPixelHelper.getSuperPixel(currentImage, Constants.NUMBER_OF_SUPERPIXELS, Constants.RIGIDNESS);
			  // set feature vectors
			  int meanDistance = GridHelper.getMeanSuperPixelDistance(createdSuperPixels);
			  for (SuperPixelDTO superPixel : createdSuperPixels) {
				  superPixel.initFeatureVector(meanDistance, createdSuperPixels, currentImage);
			  }
			  
			  FactorGraphModel factorGraph = new FactorGraphModel(currentImage, createdSuperPixels, imageToFactorGraphMap, weightsw, parameterContainer, parameterContainer.getNumberOfLocalFeatures(), parameterContainer.getNumberOfParwiseFeatures());
			  testMap.put(currentImage, factorGraph);
			  
			  DataHelper.saveImageSuperPixelIdentifingColor(currentImage, createdSuperPixels, "C:\\Users\\anean\\Desktop\\crf_data_test\\" + fileCounter + "\\" +  factorGraph.getUuid()  + ".png");

			  if (imageCounter % 20 == 0) {
				  SerializationUtil.writeObjectToFile(testMap, Constants.IMAGE_FOLDER + File.separator + Constants.IMAGE_TO_FACTOR_GRAPH_TEST_FILE_NAME + fileCounter);
				  testMap = new HashMap<>();
				  fileCounter++;
			  }
			  
		  }
		  if (testMap.keySet().size() != 0) {
			  SerializationUtil.writeObjectToFile(testMap, Constants.IMAGE_FOLDER + File.separator + Constants.IMAGE_TO_FACTOR_GRAPH_TEST_FILE_NAME + fileCounter);
		  }
		
	}

}
