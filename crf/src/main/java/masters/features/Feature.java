package masters.features;

public interface Feature {

	Object value = null;
	public Object getValue();
	public void setValue(Object value);
	public double getDifference(Feature otherFeature);
	public int getFeatureIndex();
	
}
