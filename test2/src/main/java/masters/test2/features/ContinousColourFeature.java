package masters.test2.features;

public class ContinousColourFeature implements Feature{

	private Double value;
	
	public ContinousColourFeature(Double value) {
		this.value = value;
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
		return "Feature [" + value + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		ContinousColourFeature other = (ContinousColourFeature) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}


}
