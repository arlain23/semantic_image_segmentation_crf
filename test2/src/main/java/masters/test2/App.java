package masters.test2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import masters.test2.factorisation.FactorGraphModelSP;
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
    public static void main( String[] args )
    {
        System.out.println(  System.getProperty("user.dir") );
        
        DataHelper dh = new DataHelper();
		List<ImageDTO> imageList = dh.getTrainingDataTestSegmented();
		Map<ImageDTO, List<SuperPixelDTO>> superPixelMap = new HashMap<ImageDTO, List<SuperPixelDTO>>();
		Map<ImageDTO, FactorGraphModelSP> imageToFactorGraphMap = new HashMap<ImageDTO, FactorGraphModelSP>();

		// factorisation
		for (ImageDTO currentImage : imageList) {
			//dh.viewImage(currentImage);
			//DataHelper.viewImageSegmented(currentImage);
			List<SuperPixelDTO> createdSuperPixels = SuperPixelHelper.getSuperPixel(currentImage, 200, 0.05);
			SuperPixelHelper.updateSuperPixelLabels(createdSuperPixels);
			superPixelMap.put(currentImage, createdSuperPixels);
			FactorGraphModelSP factorGraph = new FactorGraphModelSP(currentImage,createdSuperPixels, null);
			imageToFactorGraphMap.put(currentImage, factorGraph);
			
			DataHelper.viewImageSuperpixelBordersOnly(currentImage, createdSuperPixels);
			DataHelper.viewImageSegmentedSuperPixels(currentImage, createdSuperPixels);
			
		}
		
		// training
		GradientDescentTrainer trainer = new GradientDescentTrainer(imageList, imageToFactorGraphMap);
		WeightVector weights = trainer.train();
		System.out.println(weights);
		
		ImageDTO currentImage = imageList.get(0);
		List<SuperPixelDTO> createdSuperPixels = superPixelMap.get(currentImage);
		List<Double> wei = Arrays.asList(new Double[] {1.0, -1.0, 1.0,-1.0,1.0,-1.0,0.0, 0.0});
				
		WeightVector test = new WeightVector(wei, 2, 3);
		FactorGraphModelSP factorGraph = new FactorGraphModelSP(currentImage,createdSuperPixels,weights);
		
		// inference 
		for (int t = 0; t < 20; t++) {
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
		
    }
}
