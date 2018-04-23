package masters.test2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import masters.test2.crf.FactorGraphModelSP;
import masters.test2.image.ImageDTO;
import masters.test2.sampler.GibbsSampler;
import masters.test2.sampler.ImageMask;
import masters.test2.sampler.WeightVector;
import masters.test2.superpixel.SuperPixelDTO;
import masters.test2.superpixel.SuperPixelHelper;
import masters.test2.train.GradientDescentTrainer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println(  System.getProperty("user.dir") );
        
        DataHelper dh = new DataHelper();
		List<ImageDTO> imageList = dh.getTrainingDataTestSegmented();
		Map<ImageDTO, List<SuperPixelDTO>> superPixelMap = new HashMap<ImageDTO, List<SuperPixelDTO>>();
		Map<ImageDTO, FactorGraphModelSP> imageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModelSP>();

		// prepare factorisation
		for (ImageDTO currentImage : imageList) {
			//dh.viewImage(currentImage);
			//DataHelper.viewImageSegmented(currentImage);
			List<SuperPixelDTO> createdSuperPixels = SuperPixelHelper.getSuperPixel(currentImage, 120, 0.05);
			SuperPixelHelper.updateSuperPixelLabels(createdSuperPixels, FactorGraphModelSP.NUMBER_OF_STATES);
			superPixelMap.put(currentImage, createdSuperPixels);
			FactorGraphModelSP factorGraph = new FactorGraphModelSP(currentImage,createdSuperPixels);
			imageToFactorGraphMap.put(currentImage, factorGraph);
			
			DataHelper.viewImageSuperpixelBordersOnly(currentImage, createdSuperPixels);
			
		}
		
		// training
		GradientDescentTrainer trainer = new GradientDescentTrainer(imageList, imageToFactorGraphMap);
		WeightVector weights = trainer.train();
		
		
		
		/*
		// inference 
		for (int t = 0; t < NUMBER_OF_ITERATIONS; t++) {
			factorGraph.computeFactorToVariableMessages();
			factorGraph.computeVariableToFactorMessages();

			factorGraph.updatePixelData();
			factorGraph.computeLabeling();
			
			DataHelper.viewImageSegmentedSuperPixels(factorGraph.getImage(), createdSuperPixels);
			
			
			if (factorGraph.checkIfConverged()) {
				System.out.println("converged");
				factorGraph.updatePixelData();
				break;
			}
			
			
		}
		factorGraph.computeLabeling();
		DataHelper.viewImageSegmentedSuperPixels(factorGraph.getImage(), createdSuperPixels);
		dh.saveImage(factorGraph.getImage(), "E:\\Studia\\CSIT\\praca_magisterska\\aneta.png");
		*/
    }
}
