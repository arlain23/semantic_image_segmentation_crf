package masters.features;

import java.io.Serializable;

public interface Feature extends Serializable{

	Object value = null;
	public Object getValue();
	public void setValue(Object value);
	public double getDifference(Feature otherFeature);
	public int getFeatureIndex();
	public boolean isOutOfBounds();
	
}
