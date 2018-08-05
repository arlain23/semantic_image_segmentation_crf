package masters.test2.features;

import org.apache.log4j.Logger;

import masters.test2.App;
import masters.test2.Constants;

public class ContinousColourFeature implements Feature{

	private Double value;
	private int featureIndex;
	
	public ContinousColourFeature(Double value, int featureIndex) {
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
			ContinousColourFeature other = (ContinousColourFeature) obj;
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
			ContinousColourFeature other = (ContinousColourFeature) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

	
	private static Logger _log = Logger.getLogger(ContinousColourFeature.class);

}
