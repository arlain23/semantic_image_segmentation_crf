package masters.features;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import masters.cache.CacheUtils;
import masters.cache.FeatureSelectionCacheHelper;
import masters.image.ImageDTO;
import masters.utils.ParametersContainer;
import masters.utils.ResultAnalyser;

public class FeatureSelector {
	private int numberOfStepsForward;
	private int numberOfStepsBackward;
	private List<Feature> allFeatures;
	
	private ParametersContainer parametersContainer;
	private List<ImageDTO> validationImageList;
	private int testFeatureId;
	private Map<FeatureContainer, Double> precisionHistoryMap;
	
	private double CORRECTNESS_TOLERANCE = 0.05;
	
	public FeatureSelector(int numberOfStepsForward, int numberOfStepsBackward, Feature testFeature,
			ParametersContainer parametersContainer, List<ImageDTO> validationImageList) {

		this.numberOfStepsForward = numberOfStepsForward;
		this.numberOfStepsBackward = numberOfStepsBackward;
		this.allFeatures = ((FeatureContainer)testFeature).getFeatures();
		
		this.parametersContainer = parametersContainer;
		this.validationImageList = validationImageList;
	
		this.testFeatureId = testFeature.getFeatureIndex();
		this.precisionHistoryMap = new HashMap<>();
	}

	public List<Integer> selectFeatureIds() {
		if (FeatureSelectionCacheHelper.isFeatureSelectionCached(this.numberOfStepsForward, this.numberOfStepsBackward)) {
			List<Integer> cachedFeatureIds = FeatureSelectionCacheHelper.getCachedSelectedFeatureIds(this.numberOfStepsForward, this.numberOfStepsBackward);
			List<Feature> tmpList = new ArrayList<>();
			Set<Integer> cachedFeatureIdSet = new HashSet<>(cachedFeatureIds);
			for (Feature feature : this.allFeatures) {
				if (cachedFeatureIdSet.contains(feature.getFeatureIndex())) {
					tmpList.add(feature);
				}
			}
			
			FeatureContainer featureContainer = new FeatureContainer(tmpList, testFeatureId);
			double currentCorrectness = getFeatureListPredictionCorrectness(featureContainer);
			System.out.println("cached correctness " + currentCorrectness);
			
			return cachedFeatureIds;
		} else {
			List<Feature> availableFeatures = new ArrayList<>(this.allFeatures);
			
			int counter = 0;
			boolean isCorectnessIncreasing = true;
			double previousCorectness = 0.0;
			double previousMilestoneCoretness = 0.0;
			
			double maximalCorrectness = 0.0;
			FeatureContainer maximalFeatureContainer = new FeatureContainer(new ArrayList<Feature>(), this.testFeatureId);;
			
			FeatureContainer currentFeatureContainer = new FeatureContainer(new ArrayList<Feature>(), this.testFeatureId);
			while(!availableFeatures.isEmpty() && isCorectnessIncreasing) {
				
				System.out.println("#" + counter + " " + availableFeatures.size() + " " + currentFeatureContainer.getFeatures().size()
						+ " " + previousCorectness);
				FeatureContainer tmpFeatureContainer;
				if (counter < this.numberOfStepsForward) {
					tmpFeatureContainer = doStepForward(availableFeatures, currentFeatureContainer);
					counter++;
				} else {
					if (counter < (this.numberOfStepsForward + this.numberOfStepsBackward)) {
						tmpFeatureContainer = doStepBackward(availableFeatures, currentFeatureContainer);
						counter++;
					} else {
						counter = 0;
						if (previousCorectness <= previousMilestoneCoretness) {
							isCorectnessIncreasing = false;
						} else {
							previousMilestoneCoretness = previousCorectness;
						}
						continue;
					}
				}
				
				double currentCorrectness = this.precisionHistoryMap.get(tmpFeatureContainer);
				currentFeatureContainer = tmpFeatureContainer;
				previousCorectness = currentCorrectness;
				if (currentCorrectness > maximalCorrectness) {
					maximalCorrectness = currentCorrectness;
					maximalFeatureContainer = currentFeatureContainer;
				}
					
			}
			
			System.out.println("#" + counter + " " + availableFeatures.size() + " " + currentFeatureContainer.getFeatures().size()
					+ " " + previousCorectness);
			List<Integer> selectedFeatureIds = new ArrayList<>();
			maximalFeatureContainer.getFeatures().forEach(feature -> selectedFeatureIds.add(feature.getFeatureIndex()));
			
			FeatureSelectionCacheHelper.cacheSelectedFeatureIds(this.numberOfStepsForward, this.numberOfStepsBackward, selectedFeatureIds);
			return selectedFeatureIds;
		}
	}
	
	
	private FeatureContainer doStepForward(List<Feature> availableFeatures, FeatureContainer currentFeatureContainer) {
		Comparator<FeatureContainer> comparator = getComparator();
		Queue<FeatureContainer> featureQueue = new PriorityQueue<FeatureContainer>(comparator); 
		
		HashMap<FeatureContainer, Feature> addedFeatures = new HashMap<>();
		for (Feature potentialFeature : availableFeatures) {
			List<Feature> tmpList = new ArrayList<>(currentFeatureContainer.getFeatures());
			tmpList.add(potentialFeature);
			
			FeatureContainer featureContainer = new FeatureContainer(tmpList, testFeatureId);
			addedFeatures.put(featureContainer, potentialFeature);
			featureQueue.add(featureContainer);
		}
		
		FeatureContainer chosenFeatureContainer = featureQueue.peek();
		availableFeatures.remove(addedFeatures.get(chosenFeatureContainer));
		return chosenFeatureContainer;
	}
	
	private FeatureContainer doStepBackward(List<Feature> availableFeatures, FeatureContainer currentFeatureContainer) {
		Comparator<FeatureContainer> comparator = getComparator();
		Queue<FeatureContainer> featureQueue = new PriorityQueue<FeatureContainer>(comparator); 
		
		HashMap<FeatureContainer, Feature> addedFeatures = new HashMap<>();
		
		List<Feature> selectedFeatures = currentFeatureContainer.getFeatures();
		for (Feature potentialFeature : selectedFeatures) {
			List<Feature> tmpList = new ArrayList<>(selectedFeatures);
			tmpList.remove(potentialFeature);
			
			FeatureContainer featureContainer = new FeatureContainer(tmpList, testFeatureId);
			
			addedFeatures.put(featureContainer, potentialFeature);
			featureQueue.add(featureContainer);
		}
		
		FeatureContainer chosenFeatureContainer = featureQueue.peek();
		availableFeatures.add(addedFeatures.get(chosenFeatureContainer));
		return chosenFeatureContainer;
		
	}
	
	private Comparator<FeatureContainer> getComparator() {
		Comparator<FeatureContainer> featureListComparator = new Comparator<FeatureContainer>() {
            @Override
            public int compare(FeatureContainer featureContainer1, FeatureContainer featureContainer2) {
            	
            	double predictionError1 = getFeatureListPredictionCorrectness(featureContainer1);
            	double predictionError2 = getFeatureListPredictionCorrectness(featureContainer2);
            	
            	
            	if(predictionError1 < predictionError2) {
                    return 1;
                } else if (predictionError1 > predictionError2) {
                    return -1;
                } else {
                    return 0;
                }
            	
            }
        };
        
        return featureListComparator;
	}
	
	private double getFeatureListPredictionCorrectness(FeatureContainer featureContainer) {
		if (precisionHistoryMap.containsKey(featureContainer)) {
			return precisionHistoryMap.get(featureContainer);
		} else {
			List<Integer> featureIdList = new ArrayList<>();
			featureContainer.getFeatures().forEach(feature -> featureIdList.add(feature.getFeatureIndex()));
			
			
			for (ImageDTO validationImage : this.validationImageList) {
				validationImage.updateSelectedFeatures(featureIdList);
				
			}
			Map<ImageDTO, List<List<Double>>> probabilityDistribution = ResultAnalyser.analyseProbabilityDistribution(parametersContainer, validationImageList, null);
			double correctness = ResultAnalyser.assessLabelisationCorrectness(probabilityDistribution);
			precisionHistoryMap.put(featureContainer, correctness);
			return correctness;
		}
	}
		
	
}
