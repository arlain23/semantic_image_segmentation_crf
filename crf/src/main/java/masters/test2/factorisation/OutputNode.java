package masters.test2.factorisation;

import java.util.ArrayList;
import java.util.List;

import masters.test2.Constants;
import masters.test2.train.WeightVector;

public class OutputNode implements Node{
	
	private List<Double> maxBeliefs;
	
	private FeatureNode featureNode;
	private List<Factor> adjacentFactors = new ArrayList<Factor>();
	
	public OutputNode (WeightVector weightVector, FeatureNode featureNode) {
		this.featureNode = featureNode;
		maxBeliefs= new ArrayList<Double>();
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			maxBeliefs.add(Double.POSITIVE_INFINITY);
		}
		
	}
	
	public List<Factor> getAdjacentFactors() {
		return adjacentFactors;
	}

	public void addAdjacentFactors(Factor adjacentFactor) {
		this.adjacentFactors.add(adjacentFactor);
	}
	public void setAdjacentFactors(List<Factor> adjacentFactors) {
		this.adjacentFactors = adjacentFactors;
	}



	public FeatureNode getFeatureNode() {
		return featureNode;
	}

	public void setFeatureNode(FeatureNode featureNode) {
		this.featureNode = featureNode;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((featureNode == null) ? 0 : featureNode.hashCode());
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
		OutputNode other = (OutputNode) obj;
		if (featureNode == null) {
			if (other.featureNode != null)
				return false;
		} else if (!featureNode.equals(other.featureNode))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "OutputNode for ( " + featureNode.toString() + ")";
	}
}
