package masters.test2.image;

import java.util.List;

public class PixelDTO implements Cloneable {

	public int xIndex;
	public int yIndex;
	public int R;
	public int G;
	public int B;
	public int alpha;
	public Boolean isForeground = null;
	public Integer label;
	
	private int superPixelIdex;
	private boolean isBorderPixel;
	
	public PixelDTO(int xIndex, int yIndex, int r, int g, int b, int alpha, Boolean isForeground, Integer label) {
		super();
		this.xIndex = xIndex;
		this.yIndex = yIndex;
		R = r;
		G = g;
		B = b;
		this.alpha = alpha;
		this.isForeground = isForeground;
		this.label = label;
	}

	public double calculateLocalScore(List<Double> featureWeights) {
		return (featureWeights.get(0) * this.R + featureWeights.get(1) * this.G + featureWeights.get(2) * this.B);
	}
	public boolean hasTheSameForegroundProperty(PixelDTO otherPixel) {
		return (isForeground == otherPixel.isForeground);
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
		return superPixelIdex;
	}

	public void setSuperPixelIdex(int superPixelIdex) {
		this.superPixelIdex = superPixelIdex;
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
	public String toString() {
		return (isForeground ? "o" : " ");
	}
	
    @Override
    protected Object clone() throws CloneNotSupportedException {
    	PixelDTO newObject = new PixelDTO(xIndex, yIndex, R, G, B, alpha, isForeground, label);
    	return newObject;
    }
	
	
	
	

}
