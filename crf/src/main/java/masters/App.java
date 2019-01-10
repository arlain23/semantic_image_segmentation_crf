package masters;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import masters.Constants.ImageFolder;
import masters.colors.ColorSpaceException;
import masters.features.DiscreteFeature;
import masters.features.DiscretePositionFeature;
import masters.features.Feature;
import masters.features.FeatureContainer;
import masters.grid.GridHelper;
import masters.grid.GridPoint;
import masters.image.ImageDTO;
import masters.inference.InferenceHelper;
import masters.probabilistic_model.ProbabilityEstimator;
import masters.probabilistic_model.ProbabilityEstimatorHelper;
import masters.superpixel.SuperPixelDTO;
import masters.utils.CRFUtils;
import masters.utils.DataHelper;
import masters.utils.ParametersContainer;
import masters.utils.ResultAnalyser;

public class App { 
	public static boolean PRINT = false;
	public static boolean CLEAR_MAPPING_DATA = false;
	public static boolean CLEAR_TEST_ONLY = false;
	public static boolean CLEAR_FILES = false;
	public static boolean JOIN_MAPPINGS = true;
	public static boolean REGENERATE_SAMPLES = false;
	public static boolean GENERATE_TEST_SAMPLES = false;
	public static boolean ONLY_INFERENCE = true;
	public static boolean MAKE_VISUAL_ANALYSIS = false;
	
	private static Logger _log = Logger.getLogger(App.class);
	
	public static void main( String[] args ) throws ColorSpaceException, ClassNotFoundException, IOException, URISyntaxException {
		System.out.println("༼ つ ◕_◕ ༽つ");
		
		if (MAKE_VISUAL_ANALYSIS) {
			// constants
			Constants.ADD_COLOUR_LOCAL_FEATURE = true;
			Constants.ADD_NEIGBOUR_FEATURES = true;
			Constants.ADD_COLOUR_LOCAL_FEATURE_WITH_POSITION = true;
			Constants.USE_HISTOGRAMS_3D = false;
			Constants.NUMBER_OF_HISTOGRAM_DIVISIONS =17; // without colour 21, with colour 17
			Constants.GRID_SIZE = 3;
			Constants.NEIGHBOURHOOD_SIZE = 2;
			Constants.IMAGE_FOLDER = ImageFolder.generated_03_01;

			
			Constants.TRAIN_PATH = Constants.IMAGE_FOLDER + File.separator + "train" + File.separator ;
			Constants.TRAIN_RESULT_PATH = Constants.IMAGE_FOLDER + File.separator + "result" + File.separator ;
			Constants.VALIDATION_PATH = Constants.IMAGE_FOLDER + File.separator + "validation" + File.separator ;
			Constants.VALIDATION_RESULT_PATH = Constants.IMAGE_FOLDER + File.separator + "validation_result" + File.separator ;
			Constants.TEST_PATH = Constants.IMAGE_FOLDER + File.separator + "test" + File.separator;
			String basePath = "C:\\Users\\anean\\Desktop\\CRF_TEMP\\";
			FileUtils.cleanDirectory(new File(basePath)); 
			
			List<ImageDTO> validationImageList = DataHelper.getValidationDataSegmented();
			
			ParametersContainer parameterContainer = ParametersContainer.getInstance();
			// prepare probability estimation data
			ImageDTO sampleImage = validationImageList.get(0);
			List<SuperPixelDTO> superPixels = sampleImage.getSuperPixels();
			SuperPixelDTO sampleSuperPixel = superPixels.get(0);
			Feature testFeature = sampleSuperPixel.getLocalFeatureVector().getFeatures().get(0);
					  
			_log.info("TESTING: Probability estimation");
			Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution = new HashMap<>();
			if (Constants.USE_GMM_ESTIMATION || Constants.USE_HISTOGRAM_ESTIMATION) {
				probabilityEstimationDistribution = ProbabilityEstimatorHelper.getProbabilityEstimationDistribution(testFeature, parameterContainer);
			} else {
				probabilityEstimationDistribution = null;
			}
			parameterContainer.setProbabilityEstimationDistribution(probabilityEstimationDistribution);
			Map<ImageDTO, List<List<Double>>> probabilityDistribution  = ResultAnalyser.analyseProbabilityDistribution(parameterContainer, validationImageList, null);
			System.out.println("distribution generated");
			
			int i = 0;
			
			for (ImageDTO validationImage : validationImageList) {
//				DataHelper.viewImageWithSuperPixelsIndex(validationImage, validationImage.getSuperPixels(), basePath + "val_" + (i++) + ".png");
				DataHelper.saveImageWithSuperPixelsIndex(validationImage, validationImage.getSuperPixels(), basePath + "val_" + (i++) + ".png");
			}
			
			Scanner in = new Scanner(System.in);
			// get image to analyse
			System.out.println("Pick image index");
			int imageIndex = in.nextInt();
			System.out.println("chosen image: " + imageIndex);
			ImageDTO imageToAnalyse = validationImageList.get(imageIndex);
			List<SuperPixelDTO> superPixelsToAnalyse = imageToAnalyse.getSuperPixels();
			List<List<Double>> labelProbabilities = probabilityDistribution.get(imageToAnalyse);
			
			
			
			List<Double> probabilitiesFor0 = null;
			List<Double> probabilitiesFor3 = null;
			while (true) {
				// get superpixel index
				System.out.println("Pick superpixel index: ");
				int superPixelIndex = in.nextInt();
				System.out.println("pixel chosen " + superPixelIndex);
				SuperPixelDTO superPixel = superPixelsToAnalyse.get(superPixelIndex);
				List<GridPoint> grid = GridHelper.getGrid(superPixel, GridHelper.getMeanSuperPixelDistance(imageToAnalyse.getSuperPixels()));
				DataHelper.saveImageWithGrid(grid, superPixel, imageToAnalyse, superPixelsToAnalyse,  basePath + "image_grid_sp_" + superPixelIndex + ".png");
				DataHelper.saveImageWithChosenSuperPixel(imageToAnalyse, superPixel, basePath + "image_sp_" + superPixelIndex + ".png");
				for (Integer label = 0; label < Constants.NUMBER_OF_STATES; label++) {
					List<Double> superPixelProbs = labelProbabilities.get(label);
					
					DataHelper.saveImageFi1ProbabilitiesWithMarkedSuperPixel(imageToAnalyse, superPixel,  basePath + "result_sp_" + superPixelIndex + "_", label, superPixelProbs);
					
				}
				Feature feature = superPixel.getLocalFeatureVector().getFeatures().get(0);
				for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
					FeatureContainer featureContainer = (FeatureContainer) feature;
					List<Double> featureOnLabelsProbabilities = new ArrayList<>();
					List<Double> colourProbabilities = new ArrayList<>();
					for (Feature singleFeature : featureContainer.getFeatures()) {
						ProbabilityEstimator currentProbabilityEstimator = null;
						boolean hasAllZeros = false;
						if (probabilityEstimationDistribution.containsKey(singleFeature)) {
							currentProbabilityEstimator = probabilityEstimationDistribution.get(singleFeature).get(label);
							hasAllZeros = currentProbabilityEstimator.getAllZerosOnInput();
						}
						if (!hasAllZeros) {
							if (!(singleFeature instanceof DiscreteFeature || singleFeature instanceof DiscretePositionFeature)) {
								Double colourValue = (Double) singleFeature.getValue();
								colourProbabilities.add(colourValue);
							}
							double probabilityFeatureLabel = CRFUtils.getFeatureOnLabelProbability(null, imageToAnalyse, label, singleFeature, new ArrayList<>(), currentProbabilityEstimator);
							featureOnLabelsProbabilities.add(probabilityFeatureLabel);
						} else {
							featureOnLabelsProbabilities.add(0.0);
						}
					}
					
					//save probabilities
					System.out.println(featureOnLabelsProbabilities);
					int a = featureOnLabelsProbabilities.size() / 3;
					
					if (Constants.USE_HISTOGRAMS_3D) {
						//DataHelper.save3DFeatureProbabilities(featureOnLabelsProbabilities, basePath + "val_" + (i++) + ".png");
					} else {
						DataHelper.save1DFeatureProbabilities(featureOnLabelsProbabilities, colourProbabilities, basePath + "probs_label_" + label + ".png");
						//DataHelper.save1DFeatureProbabilities(featureOnLabelsProbabilities, colourProbabilities, basePath + "probs2_label_" + label + ".png");
					}
					
				}
				
			}
			
			
		}
		

		
		  
		// validate data: variables GRID_SIZE NEIGHBOURHOOD_SIZE NUMBER_OF_DIVISIONS_GMM 
//		System.out.println("************************");
//		System.out.println("GRID: " + Constants.GRID_SIZE + " NEIGHBOURHOOD: " + Constants.NEIGHBOURHOOD_SIZE + " HISTOGRAMS: " + Constants.NUMBER_OF_HISTOGRAM_DIVISIONS + " ADD COLOUR FEATURE: " + Constants.ADD_COLOUR_LOCAL_FEATURE);
//					
//		 prepare input
		for (int i = 1; i < 20; i+=3) {
			switch (i) {
				case 1:		Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_1; 	break;
				case 2:		Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_2; 	break;
				case 3:		Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_3; 	break;
				case 4:		Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_4; 	break;
				case 5:		Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_5; 	break;
				case 6:		Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_6; 	break;
				case 7:		Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_7; 	break;
				case 8:		Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_8; 	break;
				case 9:		Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_9; 	break;
				case 10:	Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_10; 	break;
				case 11:	Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_11; 	break;
				case 12:	Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_12; 	break;
				case 13:	Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_13; 	break;
				case 14:	Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_14; 	break;
				case 15:	Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_15; 	break;
				case 16:	Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_16; 	break;
				case 17:	Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_17; 	break;
				case 18:	Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_18; 	break;
				case 19:	Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_19; 	break;
			}
			Constants.TRAIN_PATH = Constants.IMAGE_FOLDER + File.separator + "train" + File.separator ;
			Constants.TRAIN_RESULT_PATH = Constants.IMAGE_FOLDER + File.separator + "result" + File.separator ;
			
			Constants.VALIDATION_PATH = Constants.IMAGE_FOLDER + File.separator + "validation" + File.separator ;
			Constants.VALIDATION_RESULT_PATH = Constants.IMAGE_FOLDER + File.separator + "validation_result" + File.separator ;
			
			Constants.TEST_PATH = Constants.IMAGE_FOLDER + File.separator + "test" + File.separator;
			
			System.out.println("********** chosen folder : " + Constants.IMAGE_FOLDER + " **********");
			
			List<Boolean> includeColour = Arrays.asList(new Boolean[] {false,true});
			List<Integer> gridSize = Arrays.asList(new Integer[] {3});
			List<Integer> histogramDivisions = Arrays.asList(new Integer[] {17,21});
			for (Boolean b : includeColour) {
				Constants.ADD_COLOUR_LOCAL_FEATURE = b;
				Constants.ADD_COLOUR_LOCAL_FEATURE_WITH_POSITION = b;
				for (Integer h : histogramDivisions) {
					Constants.NUMBER_OF_HISTOGRAM_DIVISIONS = h;
					for (Integer gs : gridSize) {
						Constants.GRID_SIZE = gs;
					
					
				System.out.println("************************");
				System.out.println("GRID: " + Constants.GRID_SIZE + " NEIGHBOURHOOD: " + Constants.NEIGHBOURHOOD_SIZE + " HISTOGRAMS: " + Constants.NUMBER_OF_HISTOGRAM_DIVISIONS + " ADD COLOUR FEATURE: " + Constants.ADD_COLOUR_LOCAL_FEATURE);
							
				// prepare data
	//			List<ImageDTO> trainingImageList = DataHelper.getTrainingDataSegmented();
				List<ImageDTO> validationImageList = DataHelper.getValidationDataSegmented();
	//			List<ImageDTO> testImageList = DataHelper.getTestData();
	
				ParametersContainer parameterContainer = ParametersContainer.getInstance();
				;
				// prepare probability estimation data
				ImageDTO sampleImage = validationImageList.get(0);
				List<SuperPixelDTO> superPixels = sampleImage.getSuperPixels();
				SuperPixelDTO sampleSuperPixel = superPixels.get(0);
				Feature testFeature = sampleSuperPixel.getLocalFeatureVector().getFeatures().get(0);
						  
				_log.info("TESTING: Probability estimation");
				Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution = new HashMap<>();
				if (Constants.USE_GMM_ESTIMATION || Constants.USE_HISTOGRAM_ESTIMATION) {
					probabilityEstimationDistribution = ProbabilityEstimatorHelper.getProbabilityEstimationDistribution(testFeature, parameterContainer);
				} else {
					probabilityEstimationDistribution = null;
				}
				System.out.println("distribution generated");
				parameterContainer.setProbabilityEstimationDistribution(probabilityEstimationDistribution);
				
				String baseProbabilityImagePath = "C:\\Users\\anean\\Desktop\\CRF\\08.01.2019\\probabilities_1D_c_" + Constants.ADD_COLOUR_LOCAL_FEATURE 
						+ "_" + Constants.NUMBER_OF_HISTOGRAM_DIVISIONS +  "_gs_" + Constants.GRID_SIZE +   "_ns_" + Constants.NEIGHBOURHOOD_SIZE + "\\" + Constants.IMAGE_FOLDER + "\\";
				ResultAnalyser.analyseProbabilityDistribution(parameterContainer, validationImageList, baseProbabilityImagePath);
							
	//			ResultAnalyser.analyzeEnergyMagnitude(validationImageList, trainingImageList, parameterContainer);
						
			
			}
			// prepare training data
//			_log.info("TRAINING: factorisation");
//			Map<ImageDTO, FactorGraphModel> trainingImageToFactorGraphMap = InputHelper.prepareTrainingData(parameterContainer, trainingImageList);
		
			// training
//			_log.info("TRAINING: training");
//			WeightVector weights = null;
//			if (!ONLY_INFERENCE) {
//				List<Double> initWeightList = Arrays.asList(new Double[] {
//						0.14867866229423593, 0.003290306151558794, 0.010959867852824063
//				});
//				weights = TrainHelper.train(initWeightList, trainingImageList, trainingImageToFactorGraphMap, parameterContainer);
//			} else {
//				List<Double> initWeightList = Arrays.asList(new Double[] {
//						//1.1040351538806907, 1.034252374536084E-4, 7.638726392714202E-4
//						1.0,1.0,0.0
//				});
//				weights = new WeightVector(initWeightList);
//			}
//			// prepare validation data
//			_log.info("VALIDATION: factorisation");
//			Map<ImageDTO, FactorGraphModel> validationimageToFactorGraphMap = InputHelper.prepareValidationData(parameterContainer, weights,
//					validationImageList, trainingImageList);
//			   
//			_log.info("TESTING: factorisation");
//			Map<ImageDTO, FactorGraphModel> testimageToFactorGraphMap = InputHelper.prepareTestData(parameterContainer, weights, 
//					testImageList, trainingImageList);
//			
//			_log.info("TESTING: inference");
//			String baseImagePath = "C:\\Users\\anean\\Desktop\\CRF\\inference_data\\";
//			String weightString = "";
//			for (double weight : weights.getWeights()) {
//				weightString += "_" + weight; 
//			}
//			InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, baseImagePath, "c_noise_all_10_histogram" + weightString, parameterContainer, weights);
//		}
				}
			}
		}
	}
}

