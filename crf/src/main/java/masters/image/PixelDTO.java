package masters.image;

import java.io.Serializable;
import java.util.List;

public class PixelDTO implements Cloneable, Serializable {

  private static final long serialVersionUID = 221169649944657580L;
  
  private int xIndex;
	private int yIndex;
	private int R;
	private int G;
	private int B;
	private int alpha;
	private Integer label;
	
	private int superPixelIndex;
	private boolean isBorderPixel;
	
	public PixelDTO(int xIndex, int yIndex, int r, int g, int b, int alpha, Integer label) {
		super();
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		R = r;
		G = g;
		B = b;
		this.alpha = alpha;
		this.label = label;
	}

	public double calculateLocalScore(List<Double> featureWeights) {
		return (featureWeights.get(0) * this.R + featureWeights.get(1) * this.G + featureWeights.get(2) * this.B);
	}
	public boolean isNeighbour(PixelDTO otherPixel) {
		if (this.xIndex == otherPixel.xIndex) {
			if (Math.abs(this.yIndex - otherPixel.yIndex) == 1) return true;
		} else if (this.yIndex == otherPixel.yIndex) {
			if (Math.abs(this.xIndex - otherPixel.xIndex) == 1) return true;
		}
		return false;
	}
	
	public boolean isBorderPixel() {
		return isBorderPixel;
	}

	public void setBorderPixel(boolean isBorderPixel) {
		this.isBorderPixel = isBorderPixel;
	}

	public int getSuperPixelIndex() {
		return superPixelIndex;
	}

	public void setSuperPixelIndex(int superPixelIdex) {
		this.superPixelIndex = superPixelIdex;
	}
	public int getXIndex() {
		return xIndex;
	}

	public int getYIndex() {
		return yIndex;
	}

	public int getR() {
		return R;
	}

	public int getG() {
		return G;
	}

	public int getB() {
		return B;
	}

	public Integer getLabel() {
		return label;
	}

	public void setLabel(Integer label) {
		this.label = label;
	}

    @Override
    protected Object clone() throws CloneNotSupportedException {
    	PixelDTO newObject = new PixelDTO(xIndex, yIndex, R, G, B, alpha, label);
    	return newObject;
    }
	
	
	
	

}
