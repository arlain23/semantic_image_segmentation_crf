package masters.features;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Continous3DFeature implements Feature {
  private static final long serialVersionUID = 8860208681108964405L;
  
	private List<Feature> features;
	private Double[] values;
	private int featureIndex;
	
	public Continous3DFeature(Double[] values, int featureIndex) {
		this.values = values;
		this.features = new ArrayList<Feature>();
		for (Double value : values) {
			this.features.add(new ContinousFeature(value, featureIndex++));
		}
		
		this.featureIndex = featureIndex;
	}
	public Object getValue() {
		return values;
	}

	public void setValue(Object value) {
		this.values = (Double[]) value;
		
	}
	@Override
	public boolean isOutOfBounds() {
		for (int i = 0; i < this.values.length; i++) {
			if (this.values[i] == null) return true;
		}
		return false;
	}
	public List<Feature> getFeatures() {
		return features;
	}
	public double getDifference(Feature otherFeature) {
		Double[] otherValues = (Double[])otherFeature.getValue();
		double thisLength = 0;
		for (int i = 0; i < this.values.length; i++) {
			thisLength += values[i] * values[i];
		}
		thisLength = Math.sqrt(thisLength);
		
		double otherLength = 0;
		for (int i = 0; i < otherValues.length; i++) {
			otherLength += otherValues[i] * otherValues[i];
		}
		otherLength = Math.sqrt(otherLength);
		
		return Math.abs(thisLength - otherLength);
	}
	public int getFeatureIndex() {
		return featureIndex;
	}
	
	@Override
	public String toString() {
		return "3D Continous feature [" + featureIndex + "->"  + value + "]";
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + featureIndex;
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
		Continous3DFeature other = (Continous3DFeature) obj;
		if (featureIndex != other.featureIndex)
			return false;
		return true;
	}



	private static transient Logger _log = Logger.getLogger(Continous3DFeature.class);

}
