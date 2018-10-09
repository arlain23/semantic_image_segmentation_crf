package masters;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.jndi.url.corbaname.corbanameURLContextFactory;

import masters.colors.ColorSpaceException;
import masters.concurency.JCudaUtil;
import masters.factorisation.FactorGraphModel;
import masters.grid.GridHelper;
import masters.image.ImageDTO;
import masters.image.PixelDTO;
import masters.inference.InferenceHelper;
import masters.superpixel.SuperPixelDTO;
import masters.train.GradientDescentTrainer;
import masters.train.WeightVector;
import masters.utils.CacheUtils;
import masters.utils.DataFixer;
import masters.utils.DataHelper;
import masters.utils.ParametersContainer;
import masters.utils.SerializationUtil;
import masters.utils.SuperPixelHelper;

public class App {
	public static boolean PRINT = false;
	public static boolean CLEAR_MAPPING_DATA = false;
	public static boolean CLEAR_TEST_ONLY = false;
	public static boolean CLEAR_FILES = false;
	public static boolean JOIN_MAPPINGS = true;
	public static boolean REGENERATE_SAMPLES = false;
	public static boolean GENERATE_TEST_SAMPLES = false;
	public static boolean ONLY_INFERENCE = true;
	
  @SuppressWarnings("unchecked")
  public static void main( String[] args ) throws ColorSpaceException, ClassNotFoundException, IOException, URISyntaxException {
	 
	  ParametersContainer parameterContainer = new ParametersContainer();
	  
	  List<ImageDTO> imageList = DataHelper.getTrainingDataTestSegmented();
    
	  Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
	  
	  if (GENERATE_TEST_SAMPLES) {
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
	  if (CLEAR_MAPPING_DATA) {
		  // clearning unused images
		  DataFixer.clearUnusedTestImages();
		  if (!CLEAR_TEST_ONLY) {
			  DataFixer.clearUnusedImages();
		  } else {
			  return;
		  }
		  return;
	  }
	  
	  // factorisation
	  if (Constants.CLEAR_CACHE) {
		  imageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
	  } else {
		  try {
			  if (JOIN_MAPPINGS) {
				  DataFixer.getCachedFactorisationModels(imageToFactorGraphMap);
			  } else {
				  imageToFactorGraphMap = (Map<ImageDTO, FactorGraphModel>) SerializationUtil.readObjectFromFile(Constants.IMAGE_FOLDER +File.separator  + Constants.IMAGE_TO_FACTOR_GRAPH_TRAIN_FILE_NAME);
			  }
			  System.out.println("read from cache");
			  System.out.println("TOTAL number of files " + imageToFactorGraphMap.keySet().size());
		  } catch (ClassNotFoundException e) {
			  e.printStackTrace();
			  imageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
		  } catch (EOFException exception) {
			  imageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
		  } catch (IOException e) {
			  e.printStackTrace();
			  imageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
		  }
	  } 
	  
	  
	  /*
	   * NEW CACHING
	   */
	  
	  
	  for (ImageDTO image : imageToFactorGraphMap.keySet()) {
		  CacheUtils.saveSuperPixelDivision(image, "train");
	  }
	  
	  int iterator = 0;
	  int total = imageList.size();
	  
	  
	  int mappingIndex = 1;
	  int numberOfLocalFeatures = 0;
	  int numberOfParwiseFeatures = 0;
	  
	  Map<ImageDTO, FactorGraphModel> tmpMap = new HashMap<ImageDTO, FactorGraphModel>();
	  for (ImageDTO currentImage : imageList) {
		  iterator++;
		  if (imageToFactorGraphMap.containsKey(currentImage)) {
			  FactorGraphModel factorGraph = imageToFactorGraphMap.get(currentImage);
			  numberOfLocalFeatures = factorGraph.getSuperPixels().get(0).numberOfLocalFeatures;
			  numberOfParwiseFeatures = factorGraph.getSuperPixels().get(0).numberOfPairwiseFeatures;
			  
			  String uuid = factorGraph.getUuid();
			  System.out.println(iterator + "/" + total + " factorisation read from cache " + currentImage.getPath());
		  } else {
//			  DataHelper.viewImageSegmented(currentImage);
			  List<SuperPixelDTO> createdSuperPixels = SuperPixelHelper.getSuperPixel(currentImage, Constants.NUMBER_OF_SUPERPIXELS, Constants.RIGIDNESS);
			
			  // set feature vectors
			  int meanDistance = GridHelper.getMeanSuperPixelDistance(createdSuperPixels);
			  for (SuperPixelDTO superPixel : createdSuperPixels) {
				  superPixel.initFeatureVector(meanDistance, createdSuperPixels, currentImage);
			  }
			  
			  WeightVector randomWeightVector = new WeightVector(Constants.NUMBER_OF_STATES, numberOfLocalFeatures, numberOfParwiseFeatures);
			
			  SuperPixelHelper.updateSuperPixelLabels(createdSuperPixels);
			  FactorGraphModel factorGraph = new FactorGraphModel(currentImage,createdSuperPixels, randomWeightVector, null, numberOfLocalFeatures, numberOfParwiseFeatures);

			  imageToFactorGraphMap.put(currentImage, factorGraph);
			  tmpMap.put(currentImage, factorGraph);

//			  DataHelper.viewImageSuperpixelBordersOnly(currentImage, createdSuperPixels, "train " + (iterator) + " borders");
//			  DataHelper.viewImageSuperpixelMeanData(currentImage, createdSuperPixels, "train " + (iterator) + " mean");
//			  DataHelper.viewImageSegmentedSuperPixels(currentImage, createdSuperPixels, "train " + (iterator) + " segmented");
			
			 // DataHelper.saveImageSegmented(currentImage, createdSuperPixels, "C:\\Users\\anean\\Desktop\\crf_data\\" + mappingIndex + "\\" +  factorGraph.getUuid()  + ".png");
//			  DataHelper.saveImageSuperpixelMeanData(currentImage, createdSuperPixels, "C:\\Users\\anean\\Desktop\\hoho\\hoho_b_" + iterator + ".png");
			  System.out.println(iterator + "/" + total + " factorisation generated");
		  }  
	  }
	  
	  //Data holders
	  parameterContainer.setLabelProbabilities(imageToFactorGraphMap);
	  parameterContainer.setNumberOfLocalFeatures(numberOfLocalFeatures);
	  parameterContainer.setNumberOfParwiseFeatures(numberOfParwiseFeatures);
	  parameterContainer.setCurrentDate();
		  
	  
	  
	  WeightVector weights;
	  WeightVector weightsUniform = null;
	  WeightVector weightsTest1 = null;
	  WeightVector weightsTest2 = null;
	  WeightVector weightsTest3 = null;
	  WeightVector weightsTest4 = null;
	  WeightVector weightsTest5 = null;
	  if (!ONLY_INFERENCE) {
		  // training
		  List<Double> initWeightList = Arrays.asList(new Double[] {
				  0.28241759728286414, 0.3508325823707616, 0.6699042630821298 
		  });
		
		  WeightVector pretrainedWeights = new WeightVector(initWeightList);

		  GradientDescentTrainer trainer = new GradientDescentTrainer(imageList, imageToFactorGraphMap, parameterContainer);
//		  WeightVector weights = pretrainedWeights;
		  weights = trainer.train(null);
		  System.out.println("final weights");
		  System.out.println(weights);
	  } else {
		  List<Double> initWeightList = Arrays.asList(new Double[] {
				  0.0240453880751775, 3.544197728746141E-19, 7.100818535250691E-19
		  });
		
		  weights = new WeightVector(initWeightList);
		  
		  initWeightList = Arrays.asList(new Double[] {
				  0.8, 0.8, 0.002
		  });
		  
		  weightsUniform = new WeightVector(initWeightList);
		  
		  // TESTING VALUES
		  initWeightList = Arrays.asList(new Double[] {
				  0.98, 0.00002, 0.00002
		  });
		  
		  weightsTest1 = new WeightVector(initWeightList);
		
		  initWeightList = Arrays.asList(new Double[] {
				  0.0, 1.0,0.0
		  });
		  weightsTest2 = new WeightVector(initWeightList);
			
		  initWeightList = Arrays.asList(new Double[] {
				  0.0002, 0.98, 0.98
		  });
		  weightsTest3 = new WeightVector(initWeightList);
		  initWeightList = Arrays.asList(new Double[] {
				  0.0002, 0.002, 0.98
		  });
		  weightsTest4 = new WeightVector(initWeightList);
		  initWeightList = Arrays.asList(new Double[] {
				  0.98, 0.002, 0.98
		  });
		  weightsTest5 = new WeightVector(initWeightList);
	  }
	  
	  
	  // TESTING
	  System.out.println("Performing testing");
	  List<ImageDTO> testImageList = DataHelper.getTestData();
	  
	  Map<ImageDTO, FactorGraphModel> testimageToFactorGraphMap;
	  if (Constants.CLEAR_CACHE) {
		  testimageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
	  } else {
		  try {
			  if (JOIN_MAPPINGS) {
				  testimageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
				  DataFixer.getCachedTestFactorisationModels(testimageToFactorGraphMap);
			  } else {
				  testimageToFactorGraphMap = (Map<ImageDTO, FactorGraphModel>) SerializationUtil.readObjectFromFile(Constants.IMAGE_FOLDER +File.separator  + Constants.IMAGE_TO_FACTOR_GRAPH_TEST_FILE_NAME);
			  }
			  System.out.println("read from cache");
			  System.out.println("TOTAL number of files " + testimageToFactorGraphMap.keySet().size());
		  } catch (ClassNotFoundException e) {
			  e.printStackTrace();
			  testimageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
		  } catch (EOFException exception) {
			  testimageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
		  } catch (IOException e) {
			  e.printStackTrace();
			  testimageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
		  }
	  }
	  
	  
	  for (ImageDTO image : testimageToFactorGraphMap.keySet()) {
		  CacheUtils.saveSuperPixelDivision(image, "test");
	  }
	  
	  // update parameters 
	  
	  int imageCounter = 0;

	  String baseImagePath = "C:\\Users\\anean\\Desktop\\inference_data\\";
	  /*
	  System.out.println("");
	  System.out.println("INFERENCE");
	  System.out.println();
	  for (ImageDTO currentImage : testImageList) {
		  if (!testimageToFactorGraphMap.containsKey(currentImage)){
			  System.out.println("IMAGE " + currentImage.getPath() + " not mapped");
			  return;
		  }
		  System.out.println("inference image " + imageCounter);
		  imageCounter++;
		  FactorGraphModel templateGraph = testimageToFactorGraphMap.get(currentImage);
		  FactorGraphModel factorGraph = new FactorGraphModel(currentImage, templateGraph.getSuperPixels(), imageToFactorGraphMap, weights, parameterContainer, parameterContainer.getNumberOfLocalFeatures(), parameterContainer.getNumberOfParwiseFeatures());
		  
		  List<SuperPixelDTO> superPixels = factorGraph.getSuperPixels();
		  
		  String saveProgressPath = baseImagePath + "normal\\progress\\" + imageCounter + "\\";
		  String saveFinalPath = baseImagePath + "normal\\final\\" + imageCounter + ".png";
		  // inference 
		  ImageDTO processedImage = factorGraph.getImage();
		  for (int t = 0; t < 20; t++) {
			  System.out.println("iteration " + t);
			  factorGraph.computeFactorToVariableMessages();
			  factorGraph.computeVariableToFactorMessages();

			  factorGraph.updatePixelData();
			  factorGraph.computeLabeling();

			  boolean shown = false;
			  if (t%5 == 0) {
				  shown = true;
				  DataHelper.saveImageSegmentedSuperPixels(processedImage, superPixels, saveProgressPath + t + ".png");
					
	
			  }
			  if (factorGraph.checkIfConverged()) {
				  System.out.println("converged");
				  factorGraph.updatePixelData();
				  if (!shown) {
					  DataHelper.saveImageSegmentedSuperPixels(processedImage, superPixels, saveProgressPath + t + ".png");
				  }
			  }
		  }
		  factorGraph.computeLabeling();
		  DataHelper.saveImageSegmentedSuperPixels(processedImage, superPixels, saveFinalPath);
		  System.out.println("finished for image " + imageCounter);
	  }
	  */
	  
	  System.out.println();
	  System.out.println("INFERENCE");
	  System.out.println();
	  
//	  InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, imageToFactorGraphMap, baseImagePath, "normal2", parameterContainer, weights);
//	  InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, imageToFactorGraphMap, baseImagePath, "uniform2", parameterContainer, weightsUniform);
	  InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, imageToFactorGraphMap, baseImagePath, "test1", parameterContainer, weightsTest1);
	  InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, imageToFactorGraphMap, baseImagePath, "test2", parameterContainer, weightsTest2);
	  InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, imageToFactorGraphMap, baseImagePath, "test3", parameterContainer, weightsTest3);
	  InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, imageToFactorGraphMap, baseImagePath, "test4", parameterContainer, weightsTest4);
	  InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, imageToFactorGraphMap, baseImagePath, "test5", parameterContainer, weightsTest5);
	  
  }
}
