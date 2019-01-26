package masters.features;

import org.apache.log4j.Logger;

public class DiscretePositionFeature implements Feature {
	private static final long serialVersionUID = 6620717680231231595L;

	private String value;
	private int featureIndex;
	private int gridIndex;

	public DiscretePositionFeature(String value, int featureIndex, int gridIndex) {
		this.value = value;
		this.featureIndex = featureIndex;
		this.gridIndex = gridIndex;
	}
	
	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		this.value = (String) value;
	}
	@Override
	public double getDifference(Feature otherFeature) {
		return (this.value.equals((String)otherFeature.getValue()) ? 0 : 1);
	}
	@Override
	public int getFeatureIndex() {
		return this.featureIndex;
	}
	@Override
	public boolean isOutOfBounds() {
		return this.value == null;
	}
	public int getGridIndex() {
		return this.gridIndex;
	}
	
	@Override
	public String toString() {
		return "Discrete Feature [" + value +  "(" + featureIndex + ")]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + featureIndex;
		result = prime * result + gridIndex;
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
		DiscretePositionFeature other = (DiscretePositionFeature) obj;
		if (featureIndex != other.featureIndex)
			return false;
		if (gridIndex != other.gridIndex)
			return false;
		return true;
	}


	private static transient Logger _log = Logger.getLogger(DiscretePositionFeature.class);
}
