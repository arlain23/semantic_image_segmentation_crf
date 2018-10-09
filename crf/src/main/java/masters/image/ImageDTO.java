package masters.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import masters.utils.DataHelper;

public class ImageDTO implements Cloneable, Serializable {
  private static final long serialVersionUID = 4107635746274497378L;
  
  	private String path;
	private int width = 0;
	private int height = 0;
	private PixelDTO[][] pixelData;
	transient private BufferedImage img;
	
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
	
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	  in.defaultReadObject();
	  this.img = DataHelper.openImage(this.path);
	}
	
	private String getFileName(String path) {
		try {
			return (new File(path)).getName();
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String fileName = getFileName(path);
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageDTO other = (ImageDTO) obj;
		String fileName = getFileName(path);
		String otherFileName = getFileName(other.path);
		
		if (fileName == null) {
			if (otherFileName != null)
				return false;
		} else if (!fileName.equals(otherFileName))
			return false;
		return true;
	}

	
}
