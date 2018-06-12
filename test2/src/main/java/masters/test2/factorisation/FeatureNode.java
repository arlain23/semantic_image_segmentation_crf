package masters.test2.factorisation;

import java.util.ArrayList;
import java.util.List;

import masters.test2.image.PixelDTO;
import masters.test2.superpixel.SuperPixelDTO;
import masters.test2.train.WeightVector;

public class FeatureNode implements Node{
	private PixelDTO pixel = null;
	private SuperPixelDTO superPixel = null;
	
	private List<Double> maxBeliefs;
	private List<Double> energies;
	
	private Factor edgeFactor;
	
	public FeatureNode (SuperPixelDTO superPixel, WeightVector weightVector) {
		this.superPixel = superPixel;
		maxBeliefs = new ArrayList<Double>();
		energies = new ArrayList<Double>();
		//calculate energy
		for (int label = 0; label < FactorGraphModel.NUMBER_OF_STATES; label++) {
			maxBeliefs.add(Double.POSITIVE_INFINITY);
			double energy = superPixel.getFeatureVector().calculateEnergy(weightVector.getPixelFeatureWeights().get(label)); 
					
			energies.add(energy);
		}
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
		if (superPixel != null) {
			superPixel.setLabel(label);
			return;
		}
		pixel.setLabel(label);
	}
	public int getPixelLabel() {
		if (superPixel != null) return superPixel.getLabel();
		return pixel.getLabel();
	}

	public List<Double> getEnergies() {
		return energies;
	}

	public void setEnergies(List<Double> energies) {
		this.energies = energies;
	}
	public void setEnergiesValue(int index, double value) {
		this.energies.set(index, value);
	}

	public double getEnergy(int label1, int label2) {
		//obsolete
		return energies.get(label1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pixel == null) ? 0 : pixel.hashCode());
		result = prime * result + ((superPixel == null) ? 0 : superPixel.hashCode());
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
		if (superPixel != null && other.superPixel != null) {
			return superPixel.equals(other.superPixel);
		}
		if (pixel == null) {
			if (other.pixel != null)
				return false;
			
		} else if (!pixel.equals(other.pixel))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return ("p: " + pixel + " | sp " + superPixel);
	}
}