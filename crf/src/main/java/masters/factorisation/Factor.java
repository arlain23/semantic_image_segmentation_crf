package masters.factorisation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import masters.Constants;

public class Factor implements Serializable {

  private static final long serialVersionUID = -5623003383671897432L;
  
  public int leftX = -1;
	public int leftY = -1;
	
	private int leftSuperPixelIndex = -1;
	
	public int rightX = -1;
	public int rightY = -1;
	
	private int rightSuperPixelIndex = -1;
	
	private List<Double> maxBeliefs;
	
	public boolean isFeatureFactor = false;
	public boolean isSuperPixelFactor = false;
	
	public Factor (int leftSuperPixelIndex, boolean superPixel) {
		this.leftSuperPixelIndex = leftSuperPixelIndex;
		isSuperPixelFactor = true;
		isFeatureFactor = true;
		maxBeliefs = new ArrayList<Double>();
	}
	public Factor (int leftSuperPixelIndex, int rightSuperPixelIndex, boolean superPixel) {
		this.leftSuperPixelIndex = leftSuperPixelIndex;
		this.rightSuperPixelIndex = rightSuperPixelIndex;
		isSuperPixelFactor = true;
		maxBeliefs= new ArrayList<Double>();
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			maxBeliefs.add(Double.POSITIVE_INFINITY);
		}
	}
	public Factor (int x, int y) {
		leftX = x;
		leftY = y;
		rightX = -1;
		rightY = -1;
		isFeatureFactor = true;
		maxBeliefs = new ArrayList<Double>();
		
	}
	public Factor (int leftX, int leftY,int rightX,int rightY ) {
		this.leftX = leftX;
		this.leftY = leftY;
		this.rightX = rightX;
		this.rightY = rightY;
		maxBeliefs= new ArrayList<Double>();
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			maxBeliefs.add(Double.POSITIVE_INFINITY);
		}
		
	}
	
	public boolean isSuperPixelFactor() {
		return isSuperPixelFactor;
	}
	public List<Double> getMaxBeliefs() {
		return maxBeliefs;
	}
	public void setMaxBeliefs(List<Double> maxBeliefs) {
		this.maxBeliefs = maxBeliefs;
	}
	public void setMaxBeliefsValue(int index, double value) {
		System.out.println("MAX BEL " + index + " -> " + value);
		this.maxBeliefs.set(index, value);
	}
	
	public int getLeftSuperPixelIndex() {
		return leftSuperPixelIndex;
	}
	public int getRightSuperPixelIndex() {
		return rightSuperPixelIndex;
	}
	@Override
	public int hashCode() {
		int result = 0;
		result += leftX;
		result += leftY;
		result += rightX;
		result += rightY;
		result += leftSuperPixelIndex;
		result += rightSuperPixelIndex;
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
		Factor other = (Factor) obj;
		if (leftSuperPixelIndex >= 0) {
			if (rightSuperPixelIndex >= 0) {
				boolean theSame = (leftSuperPixelIndex == other.leftSuperPixelIndex && rightSuperPixelIndex == other.rightSuperPixelIndex);
				boolean opposite = (leftSuperPixelIndex == other.rightSuperPixelIndex && rightSuperPixelIndex == other.leftSuperPixelIndex);
				return theSame || opposite;
			} else {
				return leftSuperPixelIndex == other.leftSuperPixelIndex;
			}
		}
		if (leftX != other.leftX)
			return false;
		if (leftY != other.leftY)
			return false;
		if (rightX != other.rightX)
			return false;
		if (rightY != other.rightY)
			return false;
		return true;
	}
	@Override 
	public String toString() {
		return "# (" + leftX + "-" + leftY + ")" + " (" + rightX + "-" + rightY + ") | (" + leftSuperPixelIndex + "->" + rightSuperPixelIndex + ")";
	}
	
}
