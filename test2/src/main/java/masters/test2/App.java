package masters.test2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import masters.test2.colors.ColorSpaceException;
import masters.test2.factorisation.FactorGraphModel;
import masters.test2.image.ImageDTO;
import masters.test2.superpixel.SuperPixelDTO;
import masters.test2.superpixel.SuperPixelHelper;
import masters.test2.train.GradientDescentTrainer;
import masters.test2.train.WeightVector;

/**
 * Hello world!
 *
 */
public class App 
{
	public static boolean PRINT = false;
    public static void main( String[] args ) throws ColorSpaceException
    {
        System.out.println(  System.getProperty("user.dir") );
		List<ImageDTO> imageList = DataHelper.getTrainingDataTestSegmented();
		
		
		Map<ImageDTO, List<SuperPixelDTO>> superPixelMap = new HashMap<ImageDTO, List<SuperPixelDTO>>();
		Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
		
		WeightVector randomWeightVector = new WeightVector(Constants.NUMBER_OF_STATES, SuperPixelDTO.NUMBER_OF_FEATURES);
		int iterator = 0;
		// factorisation
		for (ImageDTO currentImage : imageList) {
			//dh.viewImage(currentImage);
			//DataHelper.viewImageSegmented(currentImage);
			List<SuperPixelDTO> createdSuperPixels = SuperPixelHelper.getSuperPixel(currentImage, Constants.NUMBER_OF_SUPERPIXELS, Constants.RIGIDNESS);
			SuperPixelHelper.updateSuperPixelLabels(createdSuperPixels);
			superPixelMap.put(currentImage, createdSuperPixels);
			FactorGraphModel factorGraph = new FactorGraphModel(currentImage,createdSuperPixels, randomWeightVector);
			imageToFactorGraphMap.put(currentImage, factorGraph);
		//	DataHelper.increaseBlue(createdSuperPixels);
			
			DataHelper.viewImageSuperpixelBordersOnly(currentImage, createdSuperPixels, "train " + (iterator) + " borders");
			DataHelper.viewImageSuperpixelMeanData(currentImage, createdSuperPixels, "train " + (iterator) + " mean");
			DataHelper.viewImageSegmentedSuperPixels(currentImage, createdSuperPixels, "train " + (iterator) + " segmented");
//			DataHelper.saveImageSuperpixelBordersOnly(currentImage, createdSuperPixels, "C:\\Users\\anean\\Desktop\\hoho_a_" + iterator + ".png");
			iterator ++;
		}
		// training
		List<Double> initWeightList = Arrays.asList(new Double[] {
				0.08292468359694471, 0.06052600318127024, 0.12554237532561843,
				0.0898448781054524, 0.07695783160281422, 0.024718181360882035,
				0.08879491811743553, 0.1100196824625974, 0.0945666684977111,
				0.08930024167642975, 0.09067010498884343, 0.10900655504883139,
				0.07482432828437814, 0.13957085957423876 
 

 
		});
		

		WeightVector pretrainedWeights = new WeightVector(initWeightList, 3, 3);

		
		GradientDescentTrainer trainer = new GradientDescentTrainer(imageList, imageToFactorGraphMap);
		//WeightVector weights = pretrainedWeights;
		WeightVector weights = trainer.train(null);
		System.out.println(weights);
		
		
		// get test image
		List<ImageDTO> testImageList = DataHelper.getTestData();
		App.PRINT = false;
		System.out.println("");
		System.out.println("Performing testing");
		int imageCounter = 0;
		for (ImageDTO currentImage : testImageList) {
			List<SuperPixelDTO> createdSuperPixels = SuperPixelHelper.getSuperPixel(currentImage, Constants.NUMBER_OF_SUPERPIXELS, Constants.RIGIDNESS);
			FactorGraphModel factorGraph = new FactorGraphModel(currentImage,createdSuperPixels, weights, imageToFactorGraphMap);
			
			DataHelper.viewImageSuperpixelBordersOnly(currentImage, createdSuperPixels, ("test " + imageCounter));
			
			// inference 
			for (int t = 0; t < 200; t++) {
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
					break;
				}
			}
			factorGraph.computeLabeling();
			DataHelper.viewImageSegmentedSuperPixels(factorGraph.getImage(), createdSuperPixels, "test " + imageCounter + " final result");
			String filePath = "E:\\Studia\\CSIT\\praca_magisterska\\output" + (++imageCounter) + ".png";
			DataHelper.saveImage(factorGraph.getImage(), filePath);
			System.out.println("finished for image " + imageCounter);
			System.out.println();
		}
    }
}
