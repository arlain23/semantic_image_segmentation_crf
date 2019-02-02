package masters;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.nativelibs4java.opencl.util.ParallelMath;

import masters.Constants.ColorAverageMethod;
import masters.Constants.ImageFolder;
import masters.Constants.State;
import masters.colors.ColorSpaceException;
import masters.factorisation.FactorGraphModel;
import masters.features.BinaryMask;
import masters.features.Continous3DFeature;
import masters.features.ContinousFeature;
import masters.features.DiscreteFeature;
import masters.features.DiscretePositionFeature;
import masters.features.Feature;
import masters.features.FeatureContainer;
import masters.features.FeatureSelector;
import masters.features.ValueDoubleMask;
import masters.features.ValueStringMask;
import masters.grid.GridHelper;
import masters.grid.GridOutOfBoundsException;
import masters.grid.GridPoint;
import masters.image.ImageDTO;
import masters.inference.InferenceHelper;
import masters.probabilistic_model.DiscreteFeatureModel;
import masters.probabilistic_model.DiscreteFeaturePositionModel;
import masters.probabilistic_model.Histogram3DModel;
import masters.probabilistic_model.HistogramModel;
import masters.probabilistic_model.ProbabilityEstimator;
import masters.probabilistic_model.ProbabilityEstimatorHelper;
import masters.superpixel.SuperPixelDTO;
import masters.train.GradientDescentTrainer;
import masters.train.TrainHelper;
import masters.train.WeightVector;
import masters.utils.CRFUtils;
import masters.utils.DataHelper;
import masters.utils.Helper;
import masters.utils.InputHelper;
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
	public static boolean MAKE_VISUAL_ANALYSIS = true;
	public static boolean RUN_INFERENCE = true;
	public static boolean PREPARE_INPUT = false;
	
	
	
	public static boolean IS_LINEAR = true;
	
	private static Logger _log = Logger.getLogger(App.class);
	
	public static void main( String[] args ) throws ColorSpaceException, ClassNotFoundException, IOException, URISyntaxException {
		System.out.println("༼ つ ◕_◕ ༽つ");
		
		
		if (IS_LINEAR) {
			Constants.USE_NON_LINEAR_MODEL = false;
			Constants.IMAGE_FOLDER = ImageFolder.generated_linear_coloured;
			Constants.SUPERPIXEL_IMAGE_FOLDER = ImageFolder.generated_linear;
			Constants.COLOR_AVERAGE_METHOD = ColorAverageMethod.MEAN;
			Constants.NUMBER_OF_STATES = 3;
			Constants.TRAINING_STEP = 0.0001;
			Constants.REGULARIZATION_FACTOR = 1000;
			
			Constants.TRAIN_PATH = Constants.IMAGE_FOLDER + File.separator + "train" + File.separator ;
			Constants.TRAIN_RESULT_PATH = Constants.IMAGE_FOLDER + File.separator + "result" + File.separator ;
			Constants.VALIDATION_PATH = Constants.IMAGE_FOLDER + File.separator + "validation" + File.separator ;
			Constants.VALIDATION_RESULT_PATH = Constants.IMAGE_FOLDER + File.separator + "validation_result" + File.separator ;
			Constants.TEST_PATH = Constants.IMAGE_FOLDER + File.separator + "test" + File.separator;
			
			ParametersContainer parameterContainer = ParametersContainer.getInstance();
			parameterContainer.setNumberOfLocalFeatures(3);
			
			// testing 
			// linear basic colour
//			List<Double> initWeightList = Arrays.asList(new Double[] {
//					-0.04148262363505202, 0.02088709381110257, 0.012204837263693908, 
//					0.02053515535187271, -0.04250326661084058, 0.013991170837388876, 
//					0.020947468283179317, 0.02161617279973801, -0.026196008101082784, 
//					-0.2191765937200767, 0.2191765937200767
//			});
			
			// linear coloured CIELAB
//			List<Double> initWeightList = Arrays.asList(new Double[] {
//					-4.3541902265591256E-4, -0.008407929539318907, -0.00680094864001077,
//					-0.0010723486101746709, 0.01483647596323914, -0.010258986068260709,
//					0.0015077676328305695, -0.006428546423920232, 0.0170599347082715,
//					-0.23759526574339257, 0.23759526574339257 
//			});
			
		
			// linear coloured RGB
			List<Double> initWeightList = Arrays.asList(new Double[] {
					-0.01929696880558088, 0.00967165632403644, 0.014061761254553109,
					0.013121748946249215, -0.013694388602578954, 0.01513050683624383,
					0.00617521985933168, 0.0040227322785425495, -0.02919226809079693,
					-0.23531971785110614, 0.23531971785110614
			});
						
			
			
					
			WeightVector weights = new WeightVector(initWeightList);
//			WeightVector weights = TrainHelper.train(null, parameterContainer);
			List<ImageDTO> trainImageList = DataHelper.getTrainingDataSegmented(parameterContainer);
			List<ImageDTO> testImageList = DataHelper.getTestData(parameterContainer);

			
			String baseImagePath = "C:\\Users\\anean\\Desktop\\CRF\\thesis_inference_data\\";
			InferenceHelper.runInference(testImageList, new ArrayList<>(), baseImagePath, "linear_coloured_rgb", parameterContainer, weights);
			
			
			return;
		}
		
		if (MAKE_VISUAL_ANALYSIS) {
			// constants
			Constants.ADD_COLOUR_LOCAL_FEATURE = true;
			Constants.ADD_NEIGBOUR_FEATURES = true;
			Constants.ADD_COLOUR_LOCAL_FEATURE_WITH_POSITION = true;
			Constants.USE_HISTOGRAMS_3D = false;
			Constants.NUMBER_OF_HISTOGRAM_DIVISIONS = 17; // without colour 21, with colour 17
			Constants.GRID_SIZE = 3;
			Constants.NEIGHBOURHOOD_SIZE = 2;
//			List<Double> meanDistance = Arrays.asList(new Double[] {1.5, 2.0, 2.5, 3.0});
			Constants.MEAN_SUPERPIXEL_DISTANCE_MULTIPLIER = 1.0;
			Constants.IMAGE_FOLDER = ImageFolder.generated_03_01_noise_19;
			
			
			Constants.TRAIN_PATH = Constants.IMAGE_FOLDER + File.separator + "train" + File.separator ;
			Constants.TRAIN_RESULT_PATH = Constants.IMAGE_FOLDER + File.separator + "result" + File.separator ;
			Constants.VALIDATION_PATH = Constants.IMAGE_FOLDER + File.separator + "validation" + File.separator ;
			Constants.VALIDATION_RESULT_PATH = Constants.IMAGE_FOLDER + File.separator + "validation_result" + File.separator ;
			Constants.TEST_PATH = Constants.IMAGE_FOLDER + File.separator + "test" + File.separator;
			String basePath = "C:\\Users\\anean\\Desktop\\CRF_TEMP\\";
			DataHelper.clearDirectory(basePath); 
			
			ParametersContainer parameterContainer = ParametersContainer.getInstance();
			List<ImageDTO> validationImageList = DataHelper.getValidationDataSegmented(parameterContainer);
			
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
			
			_log.info("TESTING: feature selection");
			// choose appropriate features 
			FeatureSelector featureSelector = new FeatureSelector(5, 3, testFeature, parameterContainer, validationImageList);
			List<Integer> selectedFeatureIds = featureSelector.selectFeatureIds();
			parameterContainer.setSelectedFeatureIds(selectedFeatureIds);
			for (ImageDTO validationImage : validationImageList) {
				validationImage.updateSelectedFeatures(selectedFeatureIds);
			}

			
			Map<ImageDTO, List<List<Double>>> probabilityDistribution  = ResultAnalyser.analyseProbabilityDistribution(parameterContainer, validationImageList, basePath + "dont_know//");
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
			boolean stop = false;
			while (!stop) {
				// get superpixel index
				System.out.println("Pick superpixel index: ");
				int superPixelIndex = in.nextInt();
				System.out.println("pixel chosen " + superPixelIndex);
				SuperPixelDTO superPixel = superPixelsToAnalyse.get(superPixelIndex);
				List<GridPoint> grid = GridHelper.getGrid(superPixel, GridHelper.getMeanSuperPixelDistance(imageToAnalyse.getSuperPixels()));
				DataHelper.saveImageWithGrid(grid, superPixel, imageToAnalyse, superPixelsToAnalyse,  basePath + "image_grid_sp_" + superPixelIndex + ".png");
				//DataHelper.saveImageWithChosenSuperPixel(imageToAnalyse, superPixel, basePath + "image_sp_" + superPixelIndex + ".png");
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
							if (singleFeature.getValue() != null) {
								double probabilityFeatureLabel = CRFUtils.getFeatureOnLabelProbability(null, imageToAnalyse, label, singleFeature, new ArrayList<>(), currentProbabilityEstimator);
								featureOnLabelsProbabilities.add(probabilityFeatureLabel);
							} else {
								featureOnLabelsProbabilities.add(null);
							}
								
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
						//DataHelper.save1DFeatureProbabilities(featureOnLabelsProbabilities, colourProbabilities, basePath + "probs_label_" + label + ".png");
						//DataHelper.save1DFeatureProbabilities(featureOnLabelsProbabilities, colourProbabilities, basePath + "probs2_label_" + label + ".png");
					}
					
				}
				if (RUN_INFERENCE) stop = true;
			}
			
			_log.info("Starting training");
			
//			// testing 
			List<Double> initWeightList = Arrays.asList(new Double[] {
					0.9035850594426786, 0.97218963318741401246, 1.942003813595220153
//					0.0 , 1.0 , 0.0
//					1.0, 0.0, 0.0
//					0.0000005, 3.0, 4.0
			});
//			WeightVector weights = new WeightVector(initWeightList);
			WeightVector weights = TrainHelper.train(initWeightList, parameterContainer);
			List<ImageDTO> trainImageList = new ArrayList<>();
			List<ImageDTO> testImageList = DataHelper.getTestData(parameterContainer);
			
			
			if (!Constants.USE_NON_LINEAR_MODEL) {
				trainImageList = DataHelper.getTrainingDataSegmented(parameterContainer);
				parameterContainer.setNumberOfLocalFeatures(3);
			}

			
			
			String baseImagePath = "C:\\Users\\anean\\Desktop\\CRF\\inference_data\\";
			InferenceHelper.runInference(testImageList, new ArrayList<>(), baseImagePath, "test_31_01", parameterContainer, weights);
			
			
		}
		if (PREPARE_INPUT) {
			
			List<Integer> il= Arrays.asList(new Integer []{0, 1, 7, 19});
	//		for (int i = 1; i < 20; i+=3) {
			for (int i : il) {
				switch (i) {
					case 0:		Constants.IMAGE_FOLDER = ImageFolder.generated_03_01; 			break;
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
				
				List<Boolean> includeColour = Arrays.asList(new Boolean[] {true});
				List<Integer> gridSize = Arrays.asList(new Integer[] {1,2,3,4,5,6,7});
				List<Integer> histogramDivisions = Arrays.asList(new Integer[] {17});
				List<Double> meanDistance = Arrays.asList(new Double[] {1.0});
				for (Boolean b : includeColour) {
					Constants.ADD_COLOUR_LOCAL_FEATURE = b;
					Constants.ADD_COLOUR_LOCAL_FEATURE_WITH_POSITION = b;
					for (Integer h : histogramDivisions) {
						Constants.NUMBER_OF_HISTOGRAM_DIVISIONS = h;
						for (Integer gs : gridSize) {
							Constants.GRID_SIZE = gs;
							for (Double md : meanDistance) {
								Constants.MEAN_SUPERPIXEL_DISTANCE_MULTIPLIER = md;
						
								System.out.println("************************");
								System.out.println("GRID: " + Constants.GRID_SIZE + " NEIGHBOURHOOD: " + Constants.NEIGHBOURHOOD_SIZE +
										" HISTOGRAMS: " + Constants.NUMBER_OF_HISTOGRAM_DIVISIONS + " MEAN DISTANCE " + Constants.MEAN_SUPERPIXEL_DISTANCE_MULTIPLIER + 
										" ADD COLOUR FEATURE: " + Constants.ADD_COLOUR_LOCAL_FEATURE);
											
								// prepare data
								ParametersContainer parameterContainer = ParametersContainer.getInstance();
					//			List<ImageDTO> trainingImageList = DataHelper.getTrainingDataSegmented();
								List<ImageDTO> validationImageList = DataHelper.getValidationDataSegmented(parameterContainer);
					//			List<ImageDTO> testImageList = DataHelper.getTestData();
					
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
				//				ResultAnalyser.analyseProbabilityDistribution(parameterContainer, validationImageList, baseProbabilityImagePath);
											
					//			ResultAnalyser.analyzeEnergyMagnitude(validationImageList, trainingImageList, parameterContainer);
							}
						}
					}
				}
			}
			return;
		}
	}
	
	
	private static void viewSegmentation() {
		Map<String, File> trainingFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_PATH);
		Map<String, File> resultFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_RESULT_PATH);
		
		String baseProbabilityImagePath = "C:\\Users\\anean\\Desktop\\CRF\\segmentation\\";
		ParametersContainer parameterContainer = ParametersContainer.getInstance();
		
		List<String> fileNames = Arrays.asList(new String [] {"138","199","872"});
		List<Integer> pixels = Arrays.asList(new Integer [] {398, 160, 192});
		int i = 0;
		for (String fileName : fileNames) {
					int superPixelIndex = pixels.get(i++);
			File trainFile = trainingFiles.get(fileName);
			File segmentedFile = resultFiles.get(fileName + Constants.RESULT_IMAGE_SUFFIX);
			
			ImageDTO trainingImage = DataHelper.getSingleImageSegmented(trainFile, segmentedFile, State.TRAIN, parameterContainer);
			
			DataHelper.saveImageSuperpixelBordersOnly(trainingImage, trainingImage.getSuperPixels(), baseProbabilityImagePath + fileName + ".png" );
			DataHelper.saveImageSegmentedSuperPixels(trainingImage, trainingImage.getSuperPixels(), baseProbabilityImagePath + fileName + "_S.png" );
			DataHelper.saveImageWithChosenSuperPixel(trainingImage, trainingImage.getSuperPixel(superPixelIndex), baseProbabilityImagePath + fileName + "_M.png");
				
			SuperPixelDTO superPixel = trainingImage.getSuperPixel(superPixelIndex);
			FeatureContainer f = (FeatureContainer) superPixel.getLocalFeatureVector().getFeatures().get(0);
			List<Feature> features = f.getFeatures();
			
			int meanDistance = GridHelper.getMeanSuperPixelDistance(trainingImage.getSuperPixels());
			List<GridPoint> grid = GridHelper.getGrid(superPixel, meanDistance);
			try {
				int index = GridHelper.getGridPointSuperPixelIndex(grid.get(24), trainingImage.getPixelData());
			} catch (GridOutOfBoundsException e) {
				e.printStackTrace();
			}
			DataHelper.saveImageWithGrid(grid, superPixel, trainingImage, trainingImage.getSuperPixels(), baseProbabilityImagePath + fileName + "_MM.png");
		}
		
	}
	
	private static void getNewProbabilityEstimationDistribution(Feature testFeature, ParametersContainer parameterContainer) {
		System.out.println("Getting new estimation");
		
		Map<String, File> trainingFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_PATH);
		Map<String, File> resultFiles = DataHelper.getFilesFromDirectory(Constants.TRAIN_RESULT_PATH);
		
		FeatureContainer featureContainer = (FeatureContainer) testFeature;
		Set<Feature> features = prepareFeatures(featureContainer);
		List<String> fileNames = Arrays.asList(new String [] {"138","199","872"});
		for (String fileName : fileNames) {
			
			File trainFile = trainingFiles.get(fileName);
			File segmentedFile = resultFiles.get(fileName + Constants.RESULT_IMAGE_SUFFIX);
			
			ImageDTO trainingImage = DataHelper.getSingleImageSegmented(trainFile, segmentedFile, State.TRAIN, parameterContainer);
			
			for (Feature singleFeature : features) {
				 if (singleFeature instanceof DiscretePositionFeature) {
					DiscretePositionFeature discretePositionFeature = (DiscretePositionFeature) singleFeature;
					for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
						BinaryMask labelMask = new BinaryMask(trainingImage.getImageMask(), label);
						ValueStringMask featureMask = trainingImage.getDiscretePositionFeatureValueMask(discretePositionFeature);
						ValueStringMask featureOnLabelMask = new ValueStringMask(featureMask, labelMask);
						
						// feature data for each superpixel
						List<String> singleData = new ArrayList<>();
						for (int i = 0; i < featureOnLabelMask.getListSize(); i++) {
							String trainingFeatureValue = featureOnLabelMask.getValue(i);
							if (trainingFeatureValue != null) {
								singleData.add(trainingFeatureValue);
								if (singleFeature.getFeatureIndex() == 171) {
									if (label == 1) {
										if (trainingFeatureValue.equals("#0000ff") || trainingFeatureValue.equals("#ff0000")) {
											System.out.println("NOOOOOOOOOOOO " + trainFile.getPath() + " label 1 and " + trainingFeatureValue + "   " + i);
											System.out.println(labelMask);
											System.out.println(featureMask);
											System.out.println(featureOnLabelMask);
											System.out.println(features.size());
										}
									}
									if (label == 3) {
										if (trainingFeatureValue.equals("#00ff00")) {
											System.out.println("NOOOOOOOOOOOO " + trainFile.getPath() + " label 3 and " + trainingFeatureValue + "   " + i);
											System.out.println(featureOnLabelMask);
											System.out.println(features.size());
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static Set<Feature> prepareFeatures(
			FeatureContainer featureContainer) {
		List<Feature> featureList = featureContainer.getFeatures();
		Set<Feature> features = new HashSet<>(featureList);
		int featureIndex = featureList.get(featureList.size() - 1).getFeatureIndex();
		if (Constants.ADD_COLOUR_LOCAL_FEATURE && !Constants.ADD_COLOUR_LOCAL_FEATURE_WITH_POSITION) {
			// init all descrete values
			for (Color color : Constants.AVAILABLE_COLOURS_SET){
				String baseHex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
				features.add(new DiscreteFeature(baseHex, ++featureIndex));
			}
		}
		return features;
	}
}

