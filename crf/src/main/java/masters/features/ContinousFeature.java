package masters.features;

import org.apache.log4j.Logger;

import masters.App;
import masters.Constants;

public class ContinousFeature implements Feature {
  private static final long serialVersionUID = 8860208681108964405L;
  
  private Double value;
	private int featureIndex;
	
	public ContinousFeature(Double value, int featureIndex) {
		this.value = value;
		this.featureIndex = featureIndex;
	}
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = (Double) value;
		
	}
	public double getDifference(Feature otherFeature) {
		return value - (Double)otherFeature.getValue();
	}
	public int getFeatureIndex() {
		return featureIndex;
	}
	
	@Override
	public String toString() {
		return "Feature [" + featureIndex + "->"  + value + "]";
	}

	@Override
	public int hashCode() {
		int result = 1;
		final int prime = 31;
		if (Constants.USE_NON_LINEAR_MODEL) {
			result = prime * result + featureIndex;
		} else {
			result = prime * result + ((value == null) ? 0 : value.hashCode());
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
			ContinousFeature other = (ContinousFeature) obj;
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
			ContinousFeature other = (ContinousFeature) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

	
	private static transient Logger _log = Logger.getLogger(ContinousFeature.class);

}
