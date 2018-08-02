package masters.test2.features;

public interface Feature {

	Object value = null;
	public Object getValue();
	public void setValue(Object value);
	public double getDifference(Feature otherFeature);
}
