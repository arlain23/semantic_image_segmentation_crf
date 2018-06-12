package masters.test2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import masters.test2.colors.ColorSpaceException;
import masters.test2.factorisation.FactorGraphModel;
import masters.test2.image.ImageDTO;
import masters.test2.sampler.GibbsSampler;
import masters.test2.sampler.ImageMask;
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
    public static void main( String[] args ) throws ColorSpaceException
    {
        System.out.println(  System.getProperty("user.dir") );
        
        DataHelper dh = new DataHelper();
		List<ImageDTO> imageList = dh.getTrainingDataTestSegmented();
		
		
		Map<ImageDTO, List<SuperPixelDTO>> superPixelMap = new HashMap<ImageDTO, List<SuperPixelDTO>>();
		Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModel>();
		
		/*boolean test = false;
		ImageDTO testImage = dh.readImageToImageDTO("C:\\Users\\anean\\Desktop\\cowhero2.jpg");
		List<SuperPixelDTO> superPixels = SuperPixelHelper.getSuperPixel(testImage, 100, 0.1);
		DataHelper.viewImageSuperpixelBordersOnly(testImage, superPixels);
		if (test) {
			return;
		}*/
		WeightVector randomWeightVector = new WeightVector(FactorGraphModel.NUMBER_OF_STATES, SuperPixelDTO.NUMBER_OF_FEATURES);
		int iterator = 0;
		// factorisation
		for (ImageDTO currentImage : imageList) {
			//dh.viewImage(currentImage);
			//DataHelper.viewImageSegmented(currentImage);
			List<SuperPixelDTO> createdSuperPixels = SuperPixelHelper.getSuperPixel(currentImage, 80, 0.5);
			System.out.println("number of superpixels " + createdSuperPixels.size());
			SuperPixelHelper.updateSuperPixelLabels(createdSuperPixels);
			superPixelMap.put(currentImage, createdSuperPixels);
			FactorGraphModel factorGraph = new FactorGraphModel(currentImage,createdSuperPixels, randomWeightVector);
			imageToFactorGraphMap.put(currentImage, factorGraph);
			System.out.println("number of superpixels " + createdSuperPixels.size());
		//	DataHelper.increaseBlue(createdSuperPixels);
			
			DataHelper.viewImageSuperpixelBordersOnly(currentImage, createdSuperPixels);
			DataHelper.viewImageSuperpixelMeanData(currentImage, createdSuperPixels);
			DataHelper.saveImageSuperpixelBordersOnly(currentImage, createdSuperPixels, "C:\\Users\\anean\\Desktop\\hoho"+ ++iterator + ".png");
			DataHelper.viewImageSegmentedSuperPixels(currentImage, createdSuperPixels);
			DataHelper.saveImageSuperpixelBordersOnly(currentImage, createdSuperPixels, "C:\\Users\\anean\\Desktop\\hoho_a_" + iterator + ".png");
			
		}
		// training
		List<Double> initWeightList = Arrays.asList(new Double[] {
				-1.0, 1.0, 1.0,
				1.0 ,-1.0,1.0 ,
				1.0, 1.0, -1.0,
				-0.10462444633910617, 0.10462444633910617 
		});
		

		WeightVector pretrainedWeights = new WeightVector(initWeightList, 3, 3);

		
		GradientDescentTrainer trainer = new GradientDescentTrainer(imageList, imageToFactorGraphMap);
		//WeightVector weights = pretrainedWeights;
		WeightVector weights = trainer.train(null);
		System.out.println(weights);
		
		
		ImageDTO currentImage = imageList.get(0);
		List<SuperPixelDTO> createdSuperPixels = superPixelMap.get(currentImage);
				
		FactorGraphModel factorGraph = new FactorGraphModel(currentImage,createdSuperPixels,weights);
		
		// inference 
		for (int t = 0; t < 200; t++) {
			factorGraph.computeFactorToVariableMessages();
			factorGraph.computeVariableToFactorMessages();

			factorGraph.updatePixelData();
			factorGraph.computeLabeling();
			
			boolean shown = false;
			if (t%5 == 0) {
				shown = true;
				DataHelper.viewImageSegmentedSuperPixels(factorGraph.getImage(), createdSuperPixels);
				
			}
			
			if (factorGraph.checkIfConverged()) {
				System.out.println("converged");
				factorGraph.updatePixelData();
				if (!shown) {
					DataHelper.viewImageSegmentedSuperPixels(factorGraph.getImage(), createdSuperPixels);
				}
				break;
			}
			
			
		}
		factorGraph.computeLabeling();
		DataHelper.viewImageSegmentedSuperPixels(factorGraph.getImage(), createdSuperPixels);
		dh.saveImage(factorGraph.getImage(), "E:\\Studia\\CSIT\\praca_magisterska\\aneta.png");
		
    }
}
