package masters.features;

import java.util.List;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.utils.Helper;

public class VectorFeature implements Feature {

	private static final long serialVersionUID = -3526273600320990373L;
	
  	private List<Double> values;
	private double vectorLength;
	private int featureIndex;
	private boolean useEuclideanDistance = false;
	
	public VectorFeature(List<Double> values, int featureIndex) {
		this.values = values;
		this.featureIndex = featureIndex;
		this.vectorLength = getVectorLength();
		this.useEuclideanDistance = Constants.USE_INIT_IMAGE_FOR_PARIWISE_POTENTIAL;
	}
	
	public List<Double> getValues() {
		return values;
	}

	public Object getValue() {
		return vectorLength;
	}
	public int getFeatureIndex() {
		return featureIndex;
	}
	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		values = (List<Double>) value;
	}
	@Override
	public boolean isOutOfBounds() {
		return false;
	}
	@Override
	public double getDifference(Feature otherFeature) {
		
		return this.useEuclideanDistance ? getEuclideanDifference(otherFeature) : getBinaryDifference(otherFeature);
		
	}
	private double getBinaryDifference(Feature otherFeature) {
		// if vector values are the same - return 0
		// if vector values are different - return 1
		List<Double> otherValues = ((VectorFeature)otherFeature).getValues();
		boolean areValuesTheSame = true;
		for (int i = 0; i < values.size(); i++) {
			if (!Helper.equals(this.values.get(i), otherValues.get(i))) {
				areValuesTheSame = false;
			}
		}
		return areValuesTheSame ? 0 : 1;
	}
	private double getEuclideanDifference(Feature otherFeature) {
		double euclideanDistance = 0.0;
		List<Double> otherValues = ((VectorFeature)otherFeature).getValues();
		for (int i = 0; i < this.values.size(); i++) {
			Double otherValue = otherValues.get(i);
			Double thisValue = this.values.get(i);
			
			double difference = Math.abs(otherValue - thisValue);
			euclideanDistance += Math.pow(difference, 2);
		}
		euclideanDistance = Math.sqrt(euclideanDistance);
		return euclideanDistance;
	}
	private double getVectorLength() {
		double result = 0;
		for (Double value : values) {
			result += value * value;
		}
		return Math.sqrt(result);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Double val : values) {
			sb.append(val + ",");
		}
		return "Vector Feature [" + featureIndex + "->"  + sb.toString() + "]";
	}

	
	@Override
	public int hashCode() {
		int result = 1;
		final int prime = 31;
		if (Constants.USE_NON_LINEAR_MODEL) {
			result = prime * result + featureIndex;
		} else {
			result = prime * result + ((values == null) ? 0 : values.hashCode());
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (Constants.USE_NON_LINEAR_MODEL) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VectorFeature other = (VectorFeature) obj;
			if (featureIndex != other.featureIndex)
				return false;
			return true;
		} else {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VectorFeature other = (VectorFeature) obj;
			if (values == null) {
				if (other.values != null)
					return false;
			} else if (!values.equals(other.values))
				return false;
			return true;
		}
	}
	
	private static transient Logger _log = Logger.getLogger(VectorFeature.class);
}
