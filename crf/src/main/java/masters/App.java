package masters;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import masters.colors.ColorSpaceException;
import masters.concurency.JCudaUtil;
import masters.factorisation.FactorGraphModel;
import masters.grid.GridHelper;
import masters.image.ImageDTO;
import masters.superpixel.SuperPixelDTO;
import masters.train.GradientDescentTrainer;
import masters.train.WeightVector;
import masters.utils.DataHelper;
import masters.utils.ParametersContainer;
import masters.utils.SerializationUtil;
import masters.utils.SuperPixelHelper;

public class App {
	public static boolean PRINT = false;
	public static boolean CUDA_TEST = false;
	
  @SuppressWarnings("unchecked")
  public static void main( String[] args ) throws ColorSpaceException {
	
	  JCudaUtil.test();
	  
	  
	  if (CUDA_TEST) return;
	  ParametersContainer parameterContainer = new ParametersContainer();
	  
	  List<ImageDTO> imageList = DataHelper.getTrainingDataTestSegmented();
    
    
	  Map<ImageDTO, List<SuperPixelDTO>> superPixelMap = new HashMap<ImageDTO, List<SuperPixelDTO>>();
    
	  Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap;
	  
	  if (Constants.CLEAR_CACHE) {
		  imageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
	  } else {
		  try {
			  imageToFactorGraphMap = (Map<ImageDTO, FactorGraphModel>) SerializationUtil.readObjectFromFile(Constants.IMAGE_FOLDER +File.separator  + Constants.IMAGE_TO_FACTOR_GRAPH_FILE_NAME);
			  System.out.println("read from cache");
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
	  
	  int iterator = 0;

	  int numberOfLocalFeatures = 0;
	  int numberOfParwiseFeatures = 0;
		
	  // factorisation
	  for (ImageDTO currentImage : imageList) {
		  if (!imageToFactorGraphMap.containsKey(currentImage)) {
//			  DataHelper.viewImageSegmented(currentImage);
			  List<SuperPixelDTO> createdSuperPixels = SuperPixelHelper.getSuperPixel(currentImage, Constants.NUMBER_OF_SUPERPIXELS, Constants.RIGIDNESS);
			
			  // set feature vectors
			  int meanDistance = GridHelper.getMeanSuperPixelDistance(createdSuperPixels);
			  for (SuperPixelDTO superPixel : createdSuperPixels) {
				  superPixel.initFeatureVector(meanDistance, createdSuperPixels, currentImage);
			  }
			  
			  numberOfLocalFeatures = createdSuperPixels.get(0).numberOfLocalFeatures;
			  numberOfParwiseFeatures = createdSuperPixels.get(0).numberOfPairwiseFeatures;
			  WeightVector randomWeightVector = new WeightVector(Constants.NUMBER_OF_STATES, numberOfLocalFeatures, numberOfParwiseFeatures);
			
			  SuperPixelHelper.updateSuperPixelLabels(createdSuperPixels);
			  superPixelMap.put(currentImage, createdSuperPixels);
			  FactorGraphModel factorGraph = new FactorGraphModel(currentImage,createdSuperPixels, randomWeightVector, null, numberOfLocalFeatures, numberOfParwiseFeatures);

			  imageToFactorGraphMap.put(currentImage, factorGraph);

//			  DataHelper.viewImageSuperpixelBordersOnly(currentImage, createdSuperPixels, "train " + (iterator) + " borders");
//			  DataHelper.viewImageSuperpixelMeanData(currentImage, createdSuperPixels, "train " + (iterator) + " mean");
//			  DataHelper.viewImageSegmentedSuperPixels(currentImage, createdSuperPixels, "train " + (iterator) + " segmented");
			
			  DataHelper.saveImageSuperpixelBordersOnly(currentImage, createdSuperPixels, "C:\\Users\\anean\\Desktop\\hoho\\hoho_a_" + iterator + ".png");
			  DataHelper.saveImageSuperpixelMeanData(currentImage, createdSuperPixels, "C:\\Users\\anean\\Desktop\\hoho\\hoho_b_" + iterator + ".png");
		  }  else {
			  FactorGraphModel factorGraph = imageToFactorGraphMap.get(currentImage);
//			  DataHelper.viewImageSuperpixelBordersOnly(currentImage, factorGraph.getSuperPixels(), "train " + (iterator) + " borders");
//			  DataHelper.viewImageSuperpixelMeanData(currentImage, factorGraph.getSuperPixels(), "train " + (iterator) + " mean");
//			  DataHelper.viewImageSegmentedSuperPixels(currentImage, factorGraph.getSuperPixels(), "train " + (iterator) + " segmented");
		  }
		  iterator ++;
	  }
		

	  //Data holders
	  parameterContainer.setLabelProbabilities(imageToFactorGraphMap);
	  
	  // training
	  List<Double> initWeightList = Arrays.asList(new Double[] {
			  0.28241759728286414, 0.3508325823707616, 0.6699042630821298 
	  });
	

	  WeightVector pretrainedWeights = new WeightVector(initWeightList);

		
	  GradientDescentTrainer trainer = new GradientDescentTrainer(imageList, imageToFactorGraphMap, parameterContainer, numberOfLocalFeatures, numberOfParwiseFeatures);
//	  WeightVector weights = pretrainedWeights;
	  WeightVector weights = trainer.train(pretrainedWeights);
	  System.out.println("final weights");
	  System.out.println(weights);

	
	  // get test image
	  List<ImageDTO> testImageList = DataHelper.getTestData();
	  System.out.println("");
	  System.out.println("Performing testing");
	  int imageCounter = 0;
	  for (ImageDTO currentImage : testImageList) {
		  List<SuperPixelDTO> createdSuperPixels = SuperPixelHelper.getSuperPixel(currentImage, Constants.NUMBER_OF_SUPERPIXELS, Constants.RIGIDNESS);
		  FactorGraphModel factorGraph = new FactorGraphModel(currentImage, createdSuperPixels, imageToFactorGraphMap, weights, parameterContainer,  numberOfLocalFeatures, numberOfParwiseFeatures);

//		  DataHelper.viewImageSuperpixelBordersOnly(currentImage, createdSuperPixels, ("test " + imageCounter));
//		  DataHelper.viewImageSuperpixelMeanData(currentImage, createdSuperPixels, "test " + (imageCounter) + " mean");
//		  DataHelper.saveImageSuperpixelBordersOnly(currentImage, createdSuperPixels, "C:\\Users\\anean\\Desktop\\hoho_a_" + imageCounter + ".png");
//		  DataHelper.saveImageSuperpixelMeanData(currentImage, createdSuperPixels, "C:\\Users\\anean\\Desktop\\hoho_b_" + imageCounter + ".png");

		
		  // inference 
		  for (int t = 0; t < 20; t++) {
			  factorGraph.computeFactorToVariableMessages();
			  factorGraph.computeVariableToFactorMessages();

			  factorGraph.updatePixelData();
			  factorGraph.computeLabeling();

			  boolean shown = true;
			  if (t%5 == 0) {
				  shown = true;
				  DataHelper.viewImageSegmentedSuperPixels(factorGraph.getImage(), createdSuperPixels, "test " + imageCounter + " inference "+ t);
	
			  }
			  if (factorGraph.checkIfConverged()) {
				  System.out.println("converged");
				  factorGraph.updatePixelData();
				  if (!shown) {
					  DataHelper.viewImageSegmentedSuperPixels(factorGraph.getImage(), createdSuperPixels, "test " + imageCounter + " inference " + t);
				  }
				  //break;
			  }
		  }
		  factorGraph.computeLabeling();
//		  DataHelper.viewImageSegmentedSuperPixels(factorGraph.getImage(), createdSuperPixels, "test " + imageCounter + " final result");
		  String filePath = "E:\\Studia\\CSIT\\praca_magisterska\\output" + (++imageCounter) + ".png";
		  //DataHelper.saveImage(factorGraph.getImage(), filePath);
		  System.out.println("finished for image " + imageCounter);
		  System.out.println();
	  }
		
	  // CACHING
	  try {
		  SerializationUtil.writeObjectToFile(imageToFactorGraphMap, Constants.IMAGE_FOLDER + File.separator + Constants.IMAGE_TO_FACTOR_GRAPH_FILE_NAME);
	  } catch (IOException e) {
		  e.printStackTrace();
	  }
  }
}
