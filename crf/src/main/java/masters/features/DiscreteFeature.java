package masters.features;

import org.apache.log4j.Logger;

public class DiscreteFeature implements Feature {
  private static final long serialVersionUID = 6620717680231231595L;
  
  private String value;
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = (String) value;
	}
	
	public DiscreteFeature(String value) {
		this.value = value;
	}
	
	public double getDifference(Feature otherFeature) {
		return (this.value.equals((String)otherFeature.getValue()) ? 0 : 1);
	}
	public int getFeatureIndex() {
		_log.error("returning feature index for discrete feature");
		return 0;
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
    DiscreteFeature other = (DiscreteFeature) obj;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }


  private static transient Logger _log = Logger.getLogger(DiscreteFeature.class);
}
