package masters.test2.train;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import masters.test2.features.ContinousFeature;
import masters.test2.features.Feature;
import masters.test2.utils.Helper;

public class FeatureVector {
	private List<Feature> features;
	private Map<Feature, Object> featureToValueMap= null;
	public FeatureVector (int featureSize) {
		features = Helper.initFixedSizedListContinuousFeature(featureSize);
	}
	
	public FeatureVector (List<Feature> features, boolean flag) {
		this.features = features;
	}
	
	public FeatureVector (List<Double> featureValues) {
		this.features = new ArrayList<Feature>();
		for (Double d : featureValues) {
			this.features.add(new ContinousFeature(d, -1));
		}
		initFeatureToValueMapping();
	}
	public void initFeatureToValueMapping() {
		this.featureToValueMap = new HashMap<Feature, Object>();
		for (Feature feature : this.features) {
			featureToValueMap.put(feature, feature.getValue());
		}
	}
	public List<Feature> getFeatures() {
		return this.features;
	}
	public void setFeatures(List<Feature> features) {
		this.features = features;
	}
	public void setFeatureValue(int index, double value) {
		this.features.get(index).setValue(value);
	}
	public void add(FeatureVector otherVector) {
		for (int i = 0; i < features.size(); i++) {
			double sum = (Double)features.get(i).getValue() + (Double)otherVector.features.get(i).getValue();
			features.get(i).setValue(sum);
		}
	}
	public double calculateEnergy(WeightVector weightVector) {
		List<Double> weights = weightVector.getWeights();
		double energy = 0;
		for (int i = 0; i < weights.size(); i++) {
			energy += weights.get(i) * (Double)this.features.get(i).getValue();
		}
		return energy;
	}
	@Override
	public int hashCode() {
		int result = 1;
		final int prime = 31;
		Iterator<Feature> i = features.iterator();
		while (i.hasNext()) {
			Feature obj = i.next();
			result = prime * result + (obj == null ? 0 : obj.hashCode());
		}
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
		FeatureVector other = (FeatureVector) obj;
		if (other.features.size() != this.features.size()) {
			return false;
		}
		for (int i = 0; i < features.size(); i++) {
			if (other.features.get(i) != this.features.get(i)) {
				return false;
			}
		}
		return true;
	}

	public Object getFeatureValue(Feature feature) {
		if (this.featureToValueMap == null) {
			initFeatureToValueMapping();
		}
		return this.featureToValueMap.get(feature);
	}
	
	
}
