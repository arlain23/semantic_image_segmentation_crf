package masters.grid;

import java.util.ArrayList;
import java.util.List;

import masters.Constants;
import masters.image.PixelDTO;
import masters.superpixel.SuperPixelDTO;

public class GridHelper {
	
	public static Integer getMeanSuperPixelDistance(List<SuperPixelDTO> superPixels) {
		double distance = 0.0;
		int counter = 0;
		for (SuperPixelDTO superPixel : superPixels) {
			GridPoint middlePoint = superPixel.getSamplePixel();
			List<SuperPixelDTO> neighbours = superPixel.getNeigbouringSuperPixels();
			for (SuperPixelDTO neighbour : neighbours) {
				GridPoint neighbourMiddlePoint = neighbour.getSamplePixel();
				distance += middlePoint.getDistance(neighbourMiddlePoint);
				counter++;
			}
		}
		double meanSuperPixelDistance = distance / counter;
		meanSuperPixelDistance  *= Constants.MEAN_SUPERPIXEL_DISTANCE_MULTIPLIER;
		return (int)Math.round(meanSuperPixelDistance);
		
	}
	
	public static int getGridPointSuperPixelIndex(GridPoint gridPoint, PixelDTO[][] pixelData) throws GridOutOfBoundsException {
		if (gridPoint.x < 0 || gridPoint.x >= pixelData.length || gridPoint.y < 0 || gridPoint.y >= pixelData[0].length) {
			throw new GridOutOfBoundsException("Point (" + gridPoint.x + " " + gridPoint.y + ") does not lie on an image");
		}
		return pixelData[gridPoint.x][gridPoint.y].getSuperPixelIndex();
	}
	public static List<GridPoint> getGrid (SuperPixelDTO superPixel, int meanSuperPixelDistance) {
		int gridSize = Constants.GRID_SIZE;
		List<GridPoint> pointList = new ArrayList<GridPoint> ();
		GridPoint middlePoint = superPixel.getSamplePixel();
		int startingX = middlePoint.x - gridSize * meanSuperPixelDistance;
		int startingY = middlePoint.y - gridSize * meanSuperPixelDistance;
		for (int i = 0; i < (gridSize * 2 + 1); i++) {
			for (int j = 0; j < (gridSize * 2 + 1); j++) {
				int x = startingX + j * meanSuperPixelDistance;
				int y = startingY + i * meanSuperPixelDistance;
				pointList.add(new GridPoint(x, y));
			}
		}
		return pointList;
	}
}
