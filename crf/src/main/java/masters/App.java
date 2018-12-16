package masters;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.SimpleHistogramBin;

import masters.Constants.ImageFolder;
import masters.Constants.State;
import masters.colors.ColorSpaceException;
import masters.factorisation.FactorGraphModel;
import masters.features.Feature;
import masters.gmm.HistogramModel;
import masters.gmm.ProbabilityEstimator;
import masters.gmm.ProbabilityEstimatorHelper;
import masters.image.ImageDTO;
import masters.inference.InferenceHelper;
import masters.superpixel.SuperPixelDTO;
import masters.train.TrainHelper;
import masters.train.WeightVector;
import masters.utils.DataHelper;
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
	
	private static Logger _log = Logger.getLogger(App.class);
	
	public static void main( String[] args ) throws ColorSpaceException, ClassNotFoundException, IOException, URISyntaxException {
		System.out.println("༼ つ ◕_◕ ༽つ");
		  
		// validate data: variables GRID_SIZE NEIGHBOURHOOD_SIZE NUMBER_OF_DIVISIONS_GMM 
		List<Integer> numberOfHistogramDivisions = Arrays.asList(new Integer [] {17});
		List<Integer> gridSize = Arrays.asList(new Integer [] {3});
		List<Integer> neighborhoodSize = Arrays.asList(new Integer [] {2});
		
		for (int gs : gridSize) {
			Constants.GRID_SIZE = gs;
			for (int ns : neighborhoodSize) {
				Constants.NEIGHBOURHOOD_SIZE = ns;
				for (int hist : numberOfHistogramDivisions) {
					Constants.NUMBER_OF_HISTOGRAM_DIVISIONS = hist;
					
					System.out.println("************************");
					System.out.println("GRID: " + gs + " NEIGHBOURHOOD: " + ns + " HISTOGRAMS: " + hist);
					
					// prepare input
					List<Integer> input = Arrays.asList(new Integer [] {0});
					List<Map<Feature, Map<Integer, ProbabilityEstimator>>> probabilityDistributionList = new ArrayList<>();
					for (int i = 4; i < 20; i++) {
//					for (int i : input) {
						switch (i) {
							case 0:  	Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_0; 	break;
							case 1:		Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_1; 	break;
							case 2:		Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_2; 	break;
							case 3:		Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_3; 	break;
							case 4:		Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_4; 	break;
							case 5:		Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_5; 	break;
							case 6:		Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_6; 	break;
							case 7:		Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_7; 	break;
							case 8:		Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_8; 	break;
							case 9:		Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_9; 	break;
							case 10:	Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_10; 	break;
							case 11:	Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_11; 	break;
							case 12:	Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_12; 	break;
							case 13:	Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_13; 	break;
							case 14:	Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_14; 	break;
							case 15:	Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_15; 	break;
							case 16:	Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_16; 	break;
							case 17:	Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_17; 	break;
							case 18:	Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_18; 	break;
							case 19:	Constants.IMAGE_FOLDER = ImageFolder.generated_equals_noise_19; 	break;
						}
						
						System.out.println("chosen folder " + Constants.IMAGE_FOLDER);
						Constants.TRAIN_PATH = Constants.IMAGE_FOLDER + File.separator + "train" + File.separator ;
						Constants.TRAIN_RESULT_PATH = Constants.IMAGE_FOLDER + File.separator + "result" + File.separator ;
						Constants.VALIDATION_PATH = Constants.IMAGE_FOLDER + File.separator + "validation" + File.separator ;
						Constants.VALIDATION_RESULT_PATH = Constants.IMAGE_FOLDER + File.separator + "validation_result" + File.separator ;
						
						Constants.TEST_PATH = Constants.IMAGE_FOLDER + File.separator + "test" + File.separator;

						
						// prepare data
						List<ImageDTO> trainingImageList = DataHelper.getTrainingDataSegmented();
						List<ImageDTO> validationImageList = DataHelper.getValidationDataSegmented();
//						List<ImageDTO> testImageList = DataHelper.getTestData();

						ParametersContainer parameterContainer = ParametersContainer.getInstance();
						parameterContainer.setParameters(trainingImageList);
					  
						
						// prepare training data
						_log.info("TRAINING: factorisation");
//						Map<ImageDTO, FactorGraphModel> trainingImageToFactorGraphMap = InputHelper.prepareTrainingData(parameterContainer, trainingImageList);
					
						_log.info("VALIDATION: factorisation");
						
//					   List<Double> initWeightList = Arrays.asList(new Double[] {
//							1.1040351538806907, 1.034252374536084E-4, 7.638726392714202E-4
//							1.0,1.0,0.0
//						});
//					   WeightVector weights = new WeightVector(initWeightList);
//					   Map<ImageDTO, FactorGraphModel> validationimageToFactorGraphMap = InputHelper.prepareValidationData(parameterContainer, weights, validationImageList,
//							   trainingImageList);
						   
						   // prepare probability estimation data
						ImageDTO sampleImage = trainingImageList.get(0);
						List<SuperPixelDTO> superPixels = sampleImage.getSuperPixels();
						SuperPixelDTO sampleSuperPixel = superPixels.get(0);
						Feature testFeature = sampleSuperPixel.getLocalFeatureVector().getFeatures().get(0);
					  
						_log.info("TESTING: Probability estimation");
						Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution = new HashMap<>();
						if (Constants.USE_GMM_ESTIMATION || Constants.USE_HISTOGRAM_ESTIMATION) {
							ProbabilityEstimatorHelper.getProbabilityEstimationDistribution(trainingImageList,
									testFeature, probabilityEstimationDistribution, hist);
						} else {
							probabilityEstimationDistribution = null;
						}
					  
						probabilityDistributionList.add(probabilityEstimationDistribution);
						parameterContainer.setProbabilityEstimationDistribution(probabilityEstimationDistribution);
						
						String baseProbabilityImagePath = "C:\\Users\\anean\\Desktop\\CRF\\probabilities_color\\" + Constants.IMAGE_FOLDER + "\\";
						ResultAnalyser.analyseProbabilityDistribution(parameterContainer,
								validationImageList, trainingImageList, baseProbabilityImagePath);
					
					}
//					Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution = probabilityDistributionList.get(0);
//					for (Feature feature : probabilityEstimationDistribution.keySet()) {
//						Map<Integer, ProbabilityEstimator> map = probabilityEstimationDistribution.get(feature);
//						for (Integer label : map.keySet()) {
//							HistogramModel estimator1 = (HistogramModel) probabilityDistributionList.get(0).get(feature).get(label);
////							HistogramModel estimator2 = (HistogramModel) probabilityDistributionList.get(1).get(feature).get(label);
////							HistogramModel estimator13 = (HistogramModel) probabilityDistributionList.get(2).get(feature).get(label);
//							double [] normalisedHistogram1 = estimator1.getNormalisedHistogram();
////							double [] normalisedHistogram2 = estimator2.getNormalisedHistogram();
////							double [] normalisedHistogram13 = estimator13.getNormalisedHistogram();
//							DefaultCategoryDataset dataset1 = new DefaultCategoryDataset( );
//							for (int i = 0; i < normalisedHistogram1.length; i++){
//								dataset1.addValue(normalisedHistogram1[i], "noise level 0" , i + "" );
////								dataset1.addValue(normalisedHistogram2[i], "noise level 2" , i + "" );
////								dataset1.addValue(normalisedHistogram13[i], "noise level 13" , i + "" );
//							}
//							
////							int length = dataArr.length;
////							HistogramDataset dataset = new HistogramDataset();
////							dataset.addSeries("H1", dataArr, 17);
//							
//							
//							
//					        String plotTitle = "Feature " + feature.getFeatureIndex() + " label " + label ;
//					        String xAxis = "";
//					        String yAxis = "";
//					        PlotOrientation orientation = PlotOrientation.VERTICAL;
//
//					        boolean show = true;
//					        boolean toolTips = true;
//					        boolean urls = false;
////					        JFreeChart chart = ChartFactory.createHistogram(plotTitle, xAxis, yAxis,
////					                dataset, orientation, show, toolTips, urls);
//					        
//					        
//					        JFreeChart barChart = ChartFactory.createBarChart(plotTitle, xAxis, yAxis,
//					                dataset1, orientation, show, toolTips, urls);
//					        
//					        BufferedImage objBufferedImage=barChart.createBufferedImage(800,600);
//					        ByteArrayOutputStream bas = new ByteArrayOutputStream();
//					                try {
//					                    ImageIO.write(objBufferedImage, "png", bas);
//					                } catch (IOException e) {
//					                    e.printStackTrace();
//					                }
//
//					                
//					        String baseImagePath = "C:\\Users\\anean\\Desktop\\CRF\\histograms_no_color\\";
//					        String fileName = "feature_" + feature.getFeatureIndex() + "_label_" + label + ".png";
//					                
//					        byte[] byteArray = bas.toByteArray();
//					        InputStream in = new ByteArrayInputStream(byteArray);
//					        BufferedImage image = ImageIO.read(in);
//					        File outputfile = new File(baseImagePath + fileName);
//					        ImageIO.write(image, "png", outputfile);
//						}
//					}
					
//					System.out.println("Gaussian models generated");
					
//					// training
//					_log.info("TRAINING: training");
//					WeightVector weights = null;
//					if (!ONLY_INFERENCE) {
//						List<Double> initWeightList = Arrays.asList(new Double[] {
//								0.14867866229423593, 0.003290306151558794, 0.010959867852824063
//						});
//						weights = TrainHelper.train(initWeightList, imageList, trainingImageToFactorGraphMap, parameterContainer);
//					} else {
//						List<Double> initWeightList = Arrays.asList(new Double[] {
//								//1.1040351538806907, 1.034252374536084E-4, 7.638726392714202E-4
//								1.0,1.0,0.0
//						});
//						weights = new WeightVector(initWeightList);
//					}
//					// prepare validation data
////					_log.info("VALIDATION: factorisation");
//					state = State.VALIDATION;
//					Map<ImageDTO, FactorGraphModel> validationimageToFactorGraphMap = new HashMap<>();
//					InputHelper.prepareValidationData(parameterContainer, state, weights, validationImageList,
//							validationimageToFactorGraphMap, trainingImageToFactorGraphMap);
//					
//					// prepare training data
////					_log.info("TESTING: factorisation");
//					state = State.TEST;
//					Map<ImageDTO, FactorGraphModel> testimageToFactorGraphMap = new HashMap<>();
//					InputHelper.prepareTestData(parameterContainer, state, weights, testImageList,
//						testimageToFactorGraphMap, trainingImageToFactorGraphMap);
//				  
//				  
//					
////					_log.info("VALIDATION: probability analysis");
//				
//					ResultAnalyser.analyseProbabilityDistribution(parameterContainer, validationImageList,
//							trainingImageToFactorGraphMap, validationimageToFactorGraphMap);
////					
////					ResultAnalyser.analyzeEnergyMagnitude(validationImageList, 
////							trainingImageToFactorGraphMap, validationimageToFactorGraphMap, parameterContainer);
//					_log.info("TESTING: inference");
//					String baseImagePath = "C:\\Users\\anean\\Desktop\\CRF\\inference_data\\";
//					String weightString = "";
//					for (double weight : weights.getWeights()) {
//						weightString += "_" + weight; 
//					}
//					InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, baseImagePath, "c_noise_all_10_histogram" + weightString, parameterContainer, weights);
//					
					
					
					
				}
			}
		}
	  
	  
	  
//		_log.info("TESTING: inference");
//		String baseImagePath = "C:\\Users\\anean\\Desktop\\CRF\\inference_data\\";
//	  
//		InferenceHelper.runInference(testImageList, testimageToFactorGraphMap, baseImagePath, "histogram_gmm_hist_" + Constants.NUMBER_OF_DIVISIONS_GMM, parameterContainer, weights);
//	 
	}
}

