package masters.test2.image;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImageDTO implements Cloneable{
	private String path;
	private int width = 0;
	private int height = 0;
	public PixelDTO[][] pixelData;
	public PixelDTO[][] segmentedData;
	private BufferedImage img;
	
	public ImageDTO(String path, int width, int height, PixelDTO[][] pixelData, BufferedImage img) {
		super();
		this.path = path;
		this.width = width;
		this.height = height;
		this.pixelData = pixelData;
		this.img = img;
	}
	public ImageDTO(String path, int width, int height, BufferedImage img) {
		super();
		this.path = path;
		this.width = width;
		this.height = height;
		this.img = img;
	}

	@Override
	public String toString() {
		StringBuffer bf = new StringBuffer();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				bf.append(pixelData[x][y] + " ");
			}
			bf.append(System.getProperty("line.separator"));
		}
		return bf.toString();
	}
	public String getPath() {
		return path;
	}
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	public BufferedImage getImage() {
		return img;
	}
	
	public PixelDTO[][] getPixelData() {
		return pixelData;
	}
	public void setPixelData(PixelDTO[][] pixelData) {
		this.pixelData = pixelData;
	}
	public List<PixelDTO> getAdjacentPixels(int x, int y) {
		List<PixelDTO> adjacentPixels = new ArrayList<PixelDTO>();
		// left pixel (y the same, x - 1)
		if (x-1 >= 0) {
			adjacentPixels.add(pixelData[x-1][y]);
		}
		// right pixel (y the same, x + 1)
		if (x+1 < this.width) {
			adjacentPixels.add(pixelData[x+1][y]);
		}
		// top pixel (y + 1, x the same)
		if (y+1 < this.height) {
			adjacentPixels.add(pixelData[x][y+1]);
		}
		// bottom pixel (y - 1, x the same)
		if (y-1 >= 0) {
			adjacentPixels.add(pixelData[x][y-1]);
		}
		return adjacentPixels;
	}
}
