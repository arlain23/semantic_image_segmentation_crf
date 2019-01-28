package masters.inference;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import masters.factorisation.FactorGraphModel;
import masters.image.ImageDTO;
import masters.superpixel.SuperPixelDTO;
import masters.train.WeightVector;
import masters.utils.CRFUtils;
import masters.utils.DataHelper;
import masters.utils.ParametersContainer;

public class InferenceHelper {
	public static void runInference(List<ImageDTO>  testImageList, List<ImageDTO>  trainImageList,
			String baseImagePath, String typeFolderName, ParametersContainer parameterContainer, WeightVector weights) {
		
		int imageCounter = 0;
		for (ImageDTO currentImage : testImageList) {
			  
			  System.out.println("inference image " + imageCounter);
			  imageCounter++;
			  
			  FactorGraphModel factorGraph = new FactorGraphModel(currentImage, trainImageList, weights, parameterContainer);
			  
			  DataHelper.saveImageWithSuperPixelsIndex(currentImage, currentImage.getSuperPixels(), baseImagePath + "images\\"+imageCounter + ".png");
			  List<SuperPixelDTO> superPixels = currentImage.getSuperPixels();
			  
			  String saveProgressPath = baseImagePath + typeFolderName + "\\progress\\" + imageCounter + "\\";
			  String saveFinalPath = baseImagePath + typeFolderName + "\\final\\" + imageCounter + ".png";
			  
			  
			  // inference 
			  for (int t = 0; t < 20; t++) {
				  System.out.println("iteration " + t);
				  factorGraph.computeFactorToVariableMessages();
				  factorGraph.computeVariableToFactorMessages();

				  factorGraph.updatePixelData();
				  factorGraph.computeLabeling();

				  boolean shown = false;
//				  if (t%5 == 0) {
				  shown = true;
				  DataHelper.saveImageSegmentedSuperPixels(currentImage, superPixels, saveProgressPath + t + ".png");
						
		
//				  }
				  if (factorGraph.checkIfConverged()) {
					  System.out.println("converged");
					  factorGraph.updatePixelData();
					  if (!shown) {
						  DataHelper.saveImageSegmentedSuperPixels(currentImage, superPixels, saveProgressPath + t + ".png");
					  }
					  break;
				  }
			  }
			  factorGraph.computeLabeling();
			  DataHelper.saveImageSegmentedSuperPixels(currentImage, superPixels, saveFinalPath);
			  System.out.println("finished for image " + imageCounter);
		  }
	}
	
	private static Logger _log = Logger.getLogger(InferenceHelper.class);
	
}