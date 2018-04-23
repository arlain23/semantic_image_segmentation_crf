package masters.test2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import masters.test2.image.ImageDTO;
import masters.test2.image.PixelDTO;

public class LabelHelper {
	public static List<List<Integer>> possibleVariations = new ArrayList<List<Integer>>();
	static int totalIterations = 0;
	
	public static List<ImageDTO> generateAllPossibleLabelizations(ImageDTO image) {
		PixelDTO[][] pixelData = image.pixelData;
		int numberOfPixels = pixelData.length * pixelData[0].length;
		possibleVariations.clear();
		List<Integer> output = new ArrayList<Integer>();
		generateVariations(Arrays.asList(new Integer [] {0,1}), numberOfPixels, output);
		
		List<ImageDTO> allLabelisations = new ArrayList<ImageDTO>();
		for (List<Integer> labelData : possibleVariations) {
			ImageDTO currentLabelization = transformLabelListToImageDTO(image, labelData);
			allLabelisations.add(currentLabelization);
		}
		return allLabelisations;
		
	}
	
	private static ImageDTO transformLabelListToImageDTO(ImageDTO image, List<Integer> labelData) {
		int width = image.getWidth();
		int height = image.getHeight();
		ImageDTO currentLabelization = new ImageDTO(image.getPath(), width, height, image.getImage());
		PixelDTO [][] pixelData = image.pixelData;
		int rowIterator = 0;
		int colIterator = 0;
		for (int i = 0; i < labelData.size(); i++) {
			pixelData[rowIterator][colIterator].isForeground = (labelData.get(i) == 1);
			if (colIterator >= height) {
				colIterator = 0;
				rowIterator++;
			}
		}
		System.out.println();
		currentLabelization.pixelData = pixelData;
		return currentLabelization;
	}
	
	public static void generateVariations(List<Integer> input, int depth, List<Integer> output) {
        if (depth == 0) {
			List<Integer> tmp = new ArrayList<Integer>();
			for (int i = 0; i < output.size(); i++) {
				tmp.add(output.get(i));
			}
			totalIterations++;
			possibleVariations.add(tmp);
        } else {
            for (int i = 0; i < input.size(); i++) {
                output.add(input.get(i));
                generateVariations(input, depth - 1, output);
                output.remove(output.size() - 1);
            }
        }
    }
}
