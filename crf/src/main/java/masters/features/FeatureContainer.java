package masters.features;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;

public class FeatureContainer implements Feature {
	private static final long serialVersionUID = -2607718850680822924L;

	private List<Feature> features;
	private Set<Feature> featureSet;

	private int featureIndex;

	public FeatureContainer(List<Feature> features, int featureIndex) {
		this.featureIndex = featureIndex;
		this.features = features;
		this.featureSet = new HashSet<>(features);
	}

	@Override
	public Object getValue() {
		_log.error("get value not implemented for FeatureContainer");
		throw new RuntimeErrorException(null);
		//return 0;
	}

	@Override
	public void setValue(Object value) {
		_log.error("set value not implemented for FeatureContainer");
	}

	@Override
	public double getDifference(Feature otherFeature) {
		_log.error("get difference not implemented for FeatureContainer");
		return 0;
	}

	@Override
	public int getFeatureIndex() {
		return featureIndex;
	}
	@Override
	public boolean isOutOfBounds() {
		return false;
	}
	public List<Feature> getFeatures() {
		return this.features;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + featureIndex;
		result = prime * result
				+ ((featureSet == null) ? 0 : featureSet.hashCode());
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
		FeatureContainer other = (FeatureContainer) obj;
		if (featureIndex != other.featureIndex)
			return false;
		if (featureSet == null) {
			if (other.featureSet != null)
				return false;
		} else if (!featureSet.equals(other.featureSet))
			return false;
		return true;
	}



	private static transient Logger _log = Logger.getLogger(FeatureContainer.class);

}
