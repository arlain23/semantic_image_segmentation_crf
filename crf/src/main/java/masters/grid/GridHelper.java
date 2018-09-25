package masters.grid;

import java.util.ArrayList;
import java.util.List;

import masters.Constants;
import masters.image.PixelDTO;
import masters.superpixel.SuperPixelDTO;

public class GridHelper {
	private static final int GRID_SIZE = Constants.GRID_SIZE;
	
	
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
		return (int)Math.round(distance / counter);
		
	}
	
	public static int getGridPointSuperPixelIndex(GridPoint gridPoint, PixelDTO[][] pixelData) throws GridOutOfBoundsException {
		if (gridPoint.x < 0 || gridPoint.x >= pixelData.length || gridPoint.y < 0 || gridPoint.y >= pixelData[0].length) {
			throw new GridOutOfBoundsException("Point (" + gridPoint.x + " " + gridPoint.y + ") does not lie on an image");
		}
		return pixelData[gridPoint.x][gridPoint.y].getSuperPixelIndex();
	}
	public static List<GridPoint> getGrid (SuperPixelDTO superPixel, int meanSuperPixelDistance) {
		
		List<GridPoint> pointList = new ArrayList<GridPoint> ();
		GridPoint middlePoint = superPixel.getSamplePixel();
		int startingX = middlePoint.x - GRID_SIZE * meanSuperPixelDistance;
		int startingY = middlePoint.y - GRID_SIZE * meanSuperPixelDistance;
		for (int i = 0; i < (GRID_SIZE * 2 + 1); i++) {
			for (int j = 0; j < (GRID_SIZE * 2 + 1); j++) {
				int x = startingX + j * meanSuperPixelDistance;
				int y = startingY + i * meanSuperPixelDistance;
				pointList.add(new GridPoint(x, y));
			}
		}
		return pointList;
	}
}
