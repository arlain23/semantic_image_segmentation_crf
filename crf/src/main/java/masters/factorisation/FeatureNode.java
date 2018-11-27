package masters.factorisation;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;

import masters.image.PixelDTO;
import masters.superpixel.SuperPixelDTO;
import masters.train.WeightVector;

public class FeatureNode implements Node {
  private static final long serialVersionUID = -8681526956284931559L;
  
	private SuperPixelDTO superPixel = null;
	
	private List<Double> maxBeliefs;
	
	private Factor edgeFactor;
	
	public FeatureNode (SuperPixelDTO superPixel, WeightVector weightVector) {
		this.superPixel = superPixel;
		maxBeliefs = new ArrayList<Double>();
	}
	
	public Factor getEdgeFactor() {
		return edgeFactor;
	}

	public void setEdgeFactor(Factor edgeFactor) {
		this.edgeFactor = edgeFactor;
	}
	
	public List<Double> getMaxBeliefs() {
		return maxBeliefs;
	}

	public void setMaxBeliefs(List<Double> maxBeliefs) {
		this.maxBeliefs = maxBeliefs;
	}
	public void setMaxBeliefsValue(int index, double value) {
		this.maxBeliefs.set(index, value);
	}
	public void setPixelLabel(int label) {
		superPixel.setLabel(label);
		if (label == -1) {
			_log.error("set pixel label -1" );
			throw new RuntimeErrorException(null);
		}
	}
	public int getPixelLabel() {
		return superPixel.getLabel();
	}

	public SuperPixelDTO getSuperPixel() {
		return superPixel;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((superPixel == null) ? 0 : superPixel.hashCode());
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
		FeatureNode other = (FeatureNode) obj;
		if (superPixel == null) {
			if (other.superPixel != null)
				return false;
		} else if (!superPixel.equals(other.superPixel))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return (" sp " + superPixel);
	}
	
	private static transient Logger _log = Logger.getLogger(FeatureNode.class);

}