package masters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import masters.colors.ColorSpaceException;
import masters.factorisation.FactorGraphModel;
import masters.features.Feature;
import masters.features.FeatureContainer;
import masters.gmm.ProbabilityEstimator;
import masters.gmm.ProbabilityEstimatorHelper;
import masters.image.ImageDTO;
import masters.superpixel.SuperPixelDTO;
import masters.train.TrainHelper;
import masters.train.WeightVector;
import masters.utils.CRFUtils;
import masters.utils.DataHelper;
import masters.utils.InputHelper;
import masters.utils.ParametersContainer;

public class App {
	public static boolean PRINT = false;
	public static boolean CLEAR_MAPPING_DATA = false;
	public static boolean CLEAR_TEST_ONLY = false;
	public static boolean CLEAR_FILES = false;
	public static boolean JOIN_MAPPINGS = true;
	public static boolean REGENERATE_SAMPLES = false;
	public static boolean GENERATE_TEST_SAMPLES = false;
	public static boolean ONLY_INFERENCE = true;
	
	private static Logger _log = Logger.getLogger(App.class);
	
  public static void main( String[] args ) throws ColorSpaceException, ClassNotFoundException, IOException, URISyntaxException {
	  System.out.println("༼ つ ◕_◕ ༽つ");
	  
	  // prepare input
	  ParametersContainer parameterContainer = new ParametersContainer();
	  List<ImageDTO> imageList = DataHelper.getTrainingDataTestSegmented();
	  List<ImageDTO> testImageList = DataHelper.getTestData();
	  
	  // prepare training data
	  _log.info("TRAINING: factorisation");
	  Constants.State state = Constants.State.TRAIN;
	  Map<ImageDTO, FactorGraphModel> trainingImageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
	  InputHelper.prepareTrainingData(parameterContainer, imageList,
				trainingImageToFactorGraphMap, state);
	
	  // training
	  _log.info("TRAINING: training");
	  WeightVector weights = null;
	  if (!ONLY_INFERENCE) {
		  weights = TrainHelper.train(null, imageList, trainingImageToFactorGraphMap, parameterContainer);
	  } else {
		  List<Double> initWeightList = Arrays.asList(new Double[] {
				  1.0, 1.0, 1.0 
		  });
		  weights = new WeightVector(initWeightList);
	  }
	  
	  
	  // prepare training data
	  _log.info("TESTING: factorisation");
	  state = Constants.State.TEST;
	  Map<ImageDTO, FactorGraphModel> testimageToFactorGraphMap = new HashMap<>();
	  InputHelper.prepareTestData(parameterContainer, state, weights, testImageList,
			testimageToFactorGraphMap);
	  
	  
//	  _log.info("TESTING: inference");
//	  String baseImagePath = "C:\\Users\\anean\\Desktop\\CRF\\inference_data\\";
//	  
//	  Constants.KERNEL_BANDWIDTH = 0.1;
//	  InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, trainingImageToFactorGraphMap, baseImagePath, "kernel0_1", parameterContainer, weights);
//	  Constants.KERNEL_BANDWIDTH = 0.2;
//	  InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, trainingImageToFactorGraphMap, baseImagePath, "kernel0_2", parameterContainer, weights);
//	  Constants.KERNEL_BANDWIDTH = 0.15;
//	  InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, trainingImageToFactorGraphMap, baseImagePath, "kernel0_15", parameterContainer, weights);
//	  Constants.KERNEL_BANDWIDTH = 15;
//	  InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, trainingImageToFactorGraphMap, baseImagePath, "kernel15", parameterContainer, weights);
//	 
	 
	  
	  _log.info("TESTING: Probability estimation");
	  
	  // prepare probability estimation data
	  ImageDTO testImage = testImageList.get(0);
	  FactorGraphModel testGraph = testimageToFactorGraphMap.get(testImage);
	  List<SuperPixelDTO> superPixels = testGraph.getSuperPixels();
	  SuperPixelDTO testSuperPixel = superPixels.get(0);
	  Feature testFeature = testSuperPixel.getLocalFeatureVector().getFeatures().get(0);
	  
	  Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution = new HashMap<>();
	  if (Constants.USE_GMM_ESTIMATION || Constants.USE_HISTOGRAM_ESTIMATION) {
		  ProbabilityEstimatorHelper.getProbabilityEstimationDistribution(trainingImageToFactorGraphMap,
				testFeature, probabilityEstimationDistribution);
	  } else {
		  probabilityEstimationDistribution = null;
	  }
	  
	  System.out.println("Gaussian models generated");
	  
	  analyseProbabilityDistribution(parameterContainer, testImageList,
			trainingImageToFactorGraphMap, testimageToFactorGraphMap,
			probabilityEstimationDistribution);
  }

private static void analyseProbabilityDistribution(
		ParametersContainer parameterContainer, List<ImageDTO> testImageList,
		Map<ImageDTO, FactorGraphModel> trainingImageToFactorGraphMap,
		Map<ImageDTO, FactorGraphModel> testimageToFactorGraphMap,
		Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution) {
	
	
	  List<Double> kernelBandwidth = Arrays.asList(new Double[] {0.005});
	  String baseProbabilityImagePath = "C:\\Users\\anean\\Desktop\\CRF\\probability_data_grid_large_hist_c_" + Constants.GRID_SIZE + "_ns_" + Constants.NEIGHBOURHOOD_SIZE + "\\";
	  for (Double h : kernelBandwidth) {
		  Constants.KERNEL_BANDWIDTH = h;
		  Map<ImageDTO, List<List<Double>>> imageProbabilityMap = new HashMap<>();
		  
		  for (ImageDTO currentImage : testImageList) {
			  List<List<Double>> labelProbablity = new ArrayList<List<Double>>();
			  imageProbabilityMap.put(currentImage, labelProbablity);
		  }
		  
		  
		  for (ImageDTO currentImage : testImageList) {
			  FactorGraphModel factorGraph = testimageToFactorGraphMap.get(currentImage);
			  List<SuperPixelDTO> superpixels = factorGraph.getSuperPixels();
			  
			  List<List<Double>> labelProbabilities = new ArrayList<>();
			  for (int objectLabel = 0; objectLabel < Constants.NUMBER_OF_STATES; objectLabel++) {
				  List<Double> superPixelProbs = new ArrayList<Double>();
				  int i = 0;
				  for (SuperPixelDTO superPixel : superpixels) {
					  i++;
					  	Feature feature = superPixel.getLocalFeatureVector().getFeatures().get(0);
						//  p(f1|l)* p(f2|l) * .... * p(fn|l)
						double featureOnLabelConditionalProbability = 1;
						
						// log p(l)
						double labelProbability = 0;
						
						//log p(f)
						double featureProbability = 0;
						
						boolean isCurrentProbabilityZero = false;
						for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
							// p(f1|l)* p(f2|l) * .... * p(fn|l)
							boolean isProbabilityZero = true;
							double currentFeatureOnLabelConditionalProbability = 1;
							
							if (feature instanceof FeatureContainer) {
								FeatureContainer featureContainer = (FeatureContainer) feature;
								for (Feature singleFeature : featureContainer.getFeatures()) {
									ProbabilityEstimator currentProbabilityEstimator = null;
									boolean hasAllZeros = false;
									if (probabilityEstimationDistribution.containsKey(singleFeature)) {
										currentProbabilityEstimator = probabilityEstimationDistribution.get(singleFeature).get(label);
										hasAllZeros = currentProbabilityEstimator.getAllZerosOnInput();
									}
									if (!hasAllZeros) {
										double probabilityFeatureLabel = CRFUtils.getFeatureOnLabelProbability(null, factorGraph, label, singleFeature, trainingImageToFactorGraphMap, currentProbabilityEstimator);
										currentFeatureOnLabelConditionalProbability *= probabilityFeatureLabel;
										isProbabilityZero = false;
									} else {
										isProbabilityZero = true;
									}
								}
							} else {
								ProbabilityEstimator currentGMM = probabilityEstimationDistribution.get(feature).get(label);
								if (currentGMM != null) {
									double probabilityFeatureLabel = CRFUtils.getFeatureOnLabelProbability(null, factorGraph, label, feature, trainingImageToFactorGraphMap, currentGMM);
									currentFeatureOnLabelConditionalProbability *= probabilityFeatureLabel;
									isProbabilityZero = false;
								}
							}
								
							// log p(l)
							double currentLabelProbability = parameterContainer.getLabelProbability(objectLabel);
								
							featureProbability += currentFeatureOnLabelConditionalProbability * currentLabelProbability;
								
							if (label == objectLabel) {
								featureOnLabelConditionalProbability = currentFeatureOnLabelConditionalProbability;
								labelProbability = currentLabelProbability;
								isCurrentProbabilityZero = isProbabilityZero;
							}
						}
						double finalProbability;
						if (isCurrentProbabilityZero) {
							finalProbability = 0;
						} else {
							finalProbability = featureOnLabelConditionalProbability * labelProbability / featureProbability;
							if (featureOnLabelConditionalProbability == 0) {
								finalProbability = 1.0 / Constants.NUMBER_OF_STATES;
							}
						}
						
						superPixelProbs.add(finalProbability);
				  }
				  labelProbabilities.add(superPixelProbs);
				  
				  // save image probabilities
				  String imageName = DataHelper.getFileNameFromImageDTO(currentImage);
				  String basePath = baseProbabilityImagePath + imageName + "\\" + "h_" + Constants.KERNEL_BANDWIDTH + "\\";
						  
				  DataHelper.saveImageFi1Probabilities(currentImage, superpixels,  basePath, objectLabel, superPixelProbs);
					
				  DataHelper.saveImageSuperpixelBordersOnly(currentImage, superpixels, basePath +  "image.png");
				  
			  }
			  imageProbabilityMap.put(currentImage, labelProbabilities);
		  }
//		  XLSHelper.saveProbabilityInformation(baseImagePath + "probabilities.xls", imageProbabilityMap);
	  }
}


}

