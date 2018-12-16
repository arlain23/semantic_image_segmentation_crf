package masters.gmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import masters.App;
import masters.Constants;
import masters.factorisation.FactorGraphModel;
import masters.features.BinaryMask;
import masters.features.ContinousFeature;
import masters.features.Feature;
import masters.features.FeatureContainer;
import masters.features.ValueMask;
import masters.image.ImageDTO;

public class ProbabilityEstimatorHelper {
	
	
	public static void getProbabilityEstimationDistribution(
			List<ImageDTO> trainImageList,
			Feature testFeature,
			Map<Feature, Map<Integer, ProbabilityEstimator>> probabilityEstimationDistribution,
			int hist) {
		
		
		if (testFeature instanceof FeatureContainer) {
				FeatureContainer featureContainer = (FeatureContainer) testFeature;
				for (Feature singleFeature : featureContainer.getFeatures()) {
					if (singleFeature instanceof ContinousFeature) {
						Map<Integer, ProbabilityEstimator> labelDistribution = new HashMap<>();
						for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
							
							List<Double> data = new ArrayList<>();
							List<ValueMask> featureOnLabelMasks = new ArrayList<ValueMask>(); 
							for (ImageDTO trainingImage : trainImageList) {
								BinaryMask labelMask = new BinaryMask(trainingImage.getImageMask(), label);
								ValueMask featureMask = trainingImage.getContinuousFeatureValueMask(singleFeature);
								ValueMask featureOnLabelMask = new ValueMask(featureMask, labelMask);
								
								featureOnLabelMasks.add(featureOnLabelMask);
							}
							for (ValueMask featureMask : featureOnLabelMasks) {
								for (int i = 0; i < featureMask.getListSize(); i++) {
									Double trainingFeatureValue = featureMask.getValue(i);
									if (trainingFeatureValue != null) {
										data.add(trainingFeatureValue);
									}
								}
							}
							// gaussian mixture
							double[] dataArr = new double[data.size()];
							boolean allzeros = true;
							for (int i = 0; i < data.size(); i++) {
								dataArr[i] = data.get(i);
								if (data.get(i) != 0) {
									allzeros = false;
								}
							}
							if (Constants.USE_GMM_ESTIMATION) {
								if (allzeros) {
									labelDistribution.put(label, new ZeroProbabilityModel());
								} else {
									GaussianMixtureModel gmm = new GaussianMixtureModel(dataArr);
									labelDistribution.put(label, gmm);
								}
							} else if (Constants.USE_HISTOGRAM_ESTIMATION) {
								HistogramModel gmm = new HistogramModel(dataArr, hist);
								labelDistribution.put(label, gmm);
							} else {
								_log.error("Choose estimation mode: histogram or gmm! ");
								throw new RuntimeException();
							}
						}
						probabilityEstimationDistribution.put(singleFeature, labelDistribution);
					}
				}
		  }
	}
	
	private static Logger _log = Logger.getLogger(ProbabilityEstimatorHelper.class);
	
}
