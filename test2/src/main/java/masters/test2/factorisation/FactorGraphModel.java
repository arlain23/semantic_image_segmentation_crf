package masters.test2.factorisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

import masters.test2.Constants;
import masters.test2.features.BinaryMask;
import masters.test2.features.Feature;
import masters.test2.image.ImageDTO;
import masters.test2.image.ImageMask;
import masters.test2.superpixel.SuperPixelDTO;
import masters.test2.train.FeatureVector;
import masters.test2.train.WeightVector;

public class FactorGraphModel {
	public static int NUMBER_OF_STATES = Constants.NUMBER_OF_STATES;
	public static double CONVERGENCE_TOLERANCE = Constants.CONVERGENCE_TOLERANCE;
	
	private ImageDTO image;
	private List<SuperPixelDTO> superPixels;
	private List<OutputNode> factorisedSuperPixels;
	private Set<Factor> createdFactors;
	private WeightVector weightVector;
	private Map<FactorEdgeKey, Edge> factorVariableToEdgeMap = new HashMap<FactorEdgeKey, Edge>();
	
	private int numberOfSuperPixels;
	// TODO remove for continuous? 
	private Map<Feature, BinaryMask> featureMap;
	private Map<Integer, BinaryMask> labelMap;
	
	private Map<ImageDTO, FactorGraphModel> trainingDataimageToFactorGraphMap = null;
	
	
	public FactorGraphModel(ImageDTO currentImage, List<SuperPixelDTO> createdSuperPixels, WeightVector weightVector,
			Map<ImageDTO, FactorGraphModel> imageToFactorGraphMap) {
		this(currentImage, createdSuperPixels, weightVector);
		this.trainingDataimageToFactorGraphMap = imageToFactorGraphMap;
		// TODO Auto-generated constructor stub
	}
	

	public FactorGraphModel(ImageDTO image, List<SuperPixelDTO> superPixels, WeightVector weightVector) {
		System.out.println("################ calling constructor ######################");
		this.image = image;
		this.superPixels = superPixels;
		this.numberOfSuperPixels = superPixels.size();
		this.factorisedSuperPixels = new ArrayList<OutputNode>();
		this.createdFactors = new HashSet<Factor>();
		this.weightVector = weightVector;
		// prepare binary masks for features and labels
		this.featureMap = new HashMap<Feature, BinaryMask>();
		this.labelMap = initLabelToBinaryMaskMap(numberOfSuperPixels);
		
		
		//create factors and edges
		for (SuperPixelDTO superPixel : superPixels) {
			// prepare label map
			BinaryMask labelMask = labelMap.get(superPixel.getLabel());
			labelMask.switchOnByte(superPixel.getSuperPixelIndex());
			
			//prepare feature masks
			List<Feature> features = superPixel.getFeatureVector().getFeatures();
			for (Feature feature : features) {
				if (!featureMap.containsKey(feature)) {
					featureMap.put(feature, new BinaryMask(numberOfSuperPixels));
				}
				BinaryMask featureMask = featureMap.get(feature);
				featureMask.switchOnByte(superPixel.getSuperPixelIndex());
			}
			
			FeatureNode featureNode = new FeatureNode(superPixel, weightVector);
			OutputNode outputNode = new OutputNode(weightVector, featureNode);
			factorisedSuperPixels.add(outputNode);
		
			//create factor for each neighbour
			List<SuperPixelDTO> neigbouringSuperPixels = superPixel.getNeigbouringSuperPixels();
			
			for (SuperPixelDTO neigbouringSuperPixel : neigbouringSuperPixels) {
				Factor factor = new Factor(superPixel.getSuperPixelIndex(), neigbouringSuperPixel.getSuperPixelIndex(),true);
				if (!createdFactors.contains(factor)) {
					createdFactors.add(factor);
					outputNode.addAdjacentFactors(factor);
				} else {
					for (Factor f : createdFactors) {
						if (factor.equals(f)) {
							outputNode.addAdjacentFactors(f);
							factor = f;
							//break;
						}
					}
				}
				
				// create edge between factors
				FactorEdgeKey key = new FactorEdgeKey(factor, outputNode);
				Edge edge = new Edge(factor, outputNode);
				factorVariableToEdgeMap.put(key, edge);
			}
			
			//create factor connected to featureNode
			Factor factor = new Factor(superPixel.getSuperPixelIndex(), true);
			outputNode.addAdjacentFactors(factor);
			createdFactors.add(factor);
			
			// create edge between factors
			FactorEdgeKey factorToOutputNodeKey = new FactorEdgeKey(factor, outputNode);
			Edge factorToOutputNodeEdge = new Edge(factor, outputNode);
			factorVariableToEdgeMap.put(factorToOutputNodeKey, factorToOutputNodeEdge);
			
			FactorEdgeKey factorToFeatureNodeKey = new FactorEdgeKey(factor, featureNode);
			Edge factorToFeatureNodeEdge = new Edge(factor, featureNode);
			factorVariableToEdgeMap.put(factorToFeatureNodeKey, factorToFeatureNodeEdge);
		}
		System.out.println("printing binary masks");
		System.out.println("**************************************");
		for (Integer label : labelMap.keySet()) {
			System.out.println("label " + label);
			BinaryMask bm = labelMap.get(label);
			System.out.println(bm);
		}
		System.out.println("**************************************");
		for (Feature feature : featureMap.keySet()) {
			System.out.println(feature);
			BinaryMask bm = featureMap.get(feature);
			System.out.println(bm);
		}
	}
	

	public void computeFactorToVariableMessages() {
		boolean print = false;
		//for every factor
		for (Factor factor : createdFactors) {
			//for every label
			for (int label = 0; label < NUMBER_OF_STATES; label++) {
				//OutputNode leftNode = factorisedImage[factor.leftX][factor.leftY];
				OutputNode leftNode = factorisedSuperPixels.get(factor.getLeftSuperPixelIndex());
				Node rightNode;
				boolean isOutputNode = (factor.getRightSuperPixelIndex() >= 0);
				
				// pairwise model
				if (isOutputNode) {
					//rightNode = factorisedImage[factor.rightX][factor.rightY];
					rightNode = factorisedSuperPixels.get(factor.getRightSuperPixelIndex());
					OutputNode rightNodeCast = (OutputNode) rightNode;
					
					// get edge between right node and factor
					FactorEdgeKey factorToRightNodeKey = new FactorEdgeKey(factor, rightNode);
					Edge factorToRightNodeEdge = factorVariableToEdgeMap.get(factorToRightNodeKey);
					FactorEdgeKey factorToLeftNodeKey = new FactorEdgeKey(factor, leftNode);
					Edge factorToLeftNodeEdge = factorVariableToEdgeMap.get(factorToLeftNodeKey);
					
					//transfering message to left node
					double msgToLeftNode = 0;
					List<Double> innerSumValues = new ArrayList<Double>();
					// inner sum
					for (int variableLabel = 0; variableLabel < NUMBER_OF_STATES; variableLabel++) {
						
						FeatureVector pairWiseImageFi = leftNode.getFeatureNode().getSuperPixel().getPairwiseImageFi(rightNodeCast.getFeatureNode().getSuperPixel(), 
								this.getImageMask(), label, variableLabel);
						double featureEnergy = pairWiseImageFi.calculateEnergy(this.weightVector);
						
						double rightNodeVariableToFactorMsg = factorToRightNodeEdge.getVariableToFactorMsg().get(variableLabel);
						//double rightNodeVariableToFactorMsg = factorToRightNodeEdge.getVariableToFactorMsg().get(label);
						double differenceValue = rightNodeVariableToFactorMsg - featureEnergy;
						innerSumValues.add(differenceValue);
					}
					// logarithm
					
					// log( exp(v)) = α + log(exp(v − α))
					double maxInnerSum = Collections.max(innerSumValues);
					for (Double innerSumValue : innerSumValues) {
						msgToLeftNode += Math.exp(innerSumValue - maxInnerSum);
					}
					
					msgToLeftNode = maxInnerSum + Math.log(msgToLeftNode);
					if (print)
					System.out.println("L" + label + ": " + factorToLeftNodeEdge.getFactorToVariableMsg().get(label) + "->" + msgToLeftNode);
					
					factorToLeftNodeEdge.setFactorToVariableMsgValue(label, msgToLeftNode);
					
					// Transferring message to right node 
					double msgToRightNode = 0;
					
					innerSumValues = new ArrayList<Double>();
					// inner sum
					for (int variableLabel = 0; variableLabel < NUMBER_OF_STATES; variableLabel++) {
						
						FeatureVector pairWiseImageFi = rightNodeCast.getFeatureNode().getSuperPixel().getPairwiseImageFi(leftNode.getFeatureNode().getSuperPixel(), 
								this.getImageMask(), label, variableLabel);
						double featureEnergy = pairWiseImageFi.calculateEnergy(this.weightVector);
						
						double leftNodeVariableToFactorMsg = factorToLeftNodeEdge.getVariableToFactorMsg().get(variableLabel);
						//double leftNodeVariableToFactorMsg = factorToLeftNodeEdge.getVariableToFactorMsg().get(label);
						double differenceValue = leftNodeVariableToFactorMsg - featureEnergy;
						innerSumValues.add(differenceValue);
					}
					
					maxInnerSum = Collections.max(innerSumValues);
					for (Double innerSumValue : innerSumValues) {
						msgToRightNode += Math.exp(innerSumValue - maxInnerSum);
					}
					
						
					msgToRightNode = maxInnerSum + Math.log(msgToRightNode);
					if (print)
						System.out.println("R" + label + ": " + factorToRightNodeEdge.getFactorToVariableMsg().get(label) + "->" + msgToRightNode);
						
					factorToRightNodeEdge.setFactorToVariableMsgValue(label, msgToRightNode);
					
				} else {		// local model
					//feature node
					FeatureNode rightNodeFeatureNode = leftNode.getFeatureNode();
					
					
					// get edge between left node and factor
					FactorEdgeKey factorToLeftNodeKey = new FactorEdgeKey(factor, leftNode);
					Edge factorToLeftNodeEdge = factorVariableToEdgeMap.get(factorToLeftNodeKey);
					
					FeatureVector localImageFi = rightNodeFeatureNode.getSuperPixel().getLocalImageFi(label, this.getImageMask(), this, this.trainingDataimageToFactorGraphMap);
					double featureEnergy = localImageFi.calculateEnergy(this.weightVector);
					
					/*
					 * TODO
					 * ZDRADZIECKI MINUS
					 */
					factorToLeftNodeEdge.setFactorToVariableMsgValue(label, -featureEnergy);
					if (print)
					System.out.println("feature node: " + featureEnergy);
				}
			}
			print = false;
		}
	}
	public void computeVariableToFactorMessages() {
		for (OutputNode currentNode : factorisedSuperPixels) {
			List<Factor> adjacentFactors = currentNode.getAdjacentFactors();
			for (Factor factor : adjacentFactors) {
				
				// get edge between node and factor
				FactorEdgeKey factorToNodeKey = new FactorEdgeKey(factor, currentNode);
				Edge factorToNodeEdge = factorVariableToEdgeMap.get(factorToNodeKey);
				
				// for every state
				List<Double> msgSums = new ArrayList<Double>();
				for (int label = 0; label < NUMBER_OF_STATES; label++) {
					double factorToVariableMsgSum = 0;
					for (Factor innerLoopFactor : adjacentFactors) {
						if (!innerLoopFactor.equals(factor)) {
							FactorEdgeKey factorToInnerNodeKey = new FactorEdgeKey(innerLoopFactor, currentNode);
							Edge factorToInnerNodeEdge = factorVariableToEdgeMap.get(factorToInnerNodeKey);
							
							factorToVariableMsgSum += factorToInnerNodeEdge.getFactorToVariableMsg().get(label);
						}
					}
					msgSums.add(factorToVariableMsgSum);
				}
				//normalise messages
				for (int label = 0; label < NUMBER_OF_STATES; label++) {
					double normalisationFactor = 0;
					for (Double singleMsg : msgSums) {
						normalisationFactor += Math.exp(singleMsg);
					}
					normalisationFactor = Math.log(normalisationFactor);
					double normalisedMsgValue = msgSums.get(label) - normalisationFactor;
					//System.out.println("L" + label + ": " + factorToNodeEdge.getVariableToFactorMsg().get(label) + "->" + normalisedMsgValue);
					
					factorToNodeEdge.setVariableToFactorMsgValue(label, normalisedMsgValue);
				}
			}
		}
	}
	private int getMaximumIndexOfAList(List<Double> list){
		int maxIndex = -1;
		double maxValue = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < list.size(); i++ ){
			if (list.get(i) > maxValue) {
				maxValue = list.get(i);
				maxIndex = i;
			}
		}
		if (maxIndex == -1) {
			Constants._log.error("getMaximumIndexOfAList - 1");
			throw new RuntimeErrorException(null);
		}
		return maxIndex;
	}
	
	
	public void updatePixelData() {
		for (OutputNode currentNode : factorisedSuperPixels) {
			List<Double> maxBeliefs = currentNode.getMaxBeliefs();
			double winnerLabelValue = -1;
			int winnerLabelIndex = -1;
			for (int i = 0; i < maxBeliefs.size(); i++) {
				if (maxBeliefs.get(i) > winnerLabelValue) {
					winnerLabelValue = maxBeliefs.get(i);
					winnerLabelIndex = i;
				}
			}
			if (winnerLabelIndex == -1) {
				Constants._log.error("updatePixelData  -1");
			}
			//update label
			FeatureNode featureNode = currentNode.getFeatureNode();
			featureNode.setPixelLabel(winnerLabelIndex);
			
		}
	}
	public boolean checkIfConverged() {
		
		//calculate belief change for factors
		double maxFactorBefiefChange = 0;
		
		for (Factor factor : createdFactors) {
			List<Double> previousMaxBeliefs = factor.getMaxBeliefs();
			List<Double> newMaxBeliefs = computeFactorBeliefs(factor);
			// check maxBeliefChange
			for (int i = 0; i < previousMaxBeliefs.size(); i++) {
				Double beliefChange = Math.abs(newMaxBeliefs.get(i) - previousMaxBeliefs.get(i));
				//System.out.print((factor.isFeatureFactor ? "feature " : "output") + "  ->  ");
				//System.out.println("belief change " + beliefChange);
				if (beliefChange > maxFactorBefiefChange) {
					maxFactorBefiefChange = beliefChange;
				}
			}
			factor.setMaxBeliefs(newMaxBeliefs);
		}
		//calculate belief change for variables
		
		double maxVariableBefiefChange = 0;
		for (OutputNode currentNode : factorisedSuperPixels) {
		
			List<Double> previousMaxBeliefs = currentNode.getMaxBeliefs();
			List<Double> newMaxBeliefs = computeVariableBeliefs(currentNode);
			
			// check maxBeliefChange
			for (int i = 0; i < previousMaxBeliefs.size(); i++) {
				Double beliefChange = Math.abs(newMaxBeliefs.get(i) - previousMaxBeliefs.get(i));
				if (beliefChange > maxVariableBefiefChange) {
					maxVariableBefiefChange = beliefChange;
				}
			}
			currentNode.setMaxBeliefs(newMaxBeliefs);
		}
		return maxFactorBefiefChange <= CONVERGENCE_TOLERANCE && maxVariableBefiefChange <= CONVERGENCE_TOLERANCE; 
	}
	private List<Double> computeFactorBeliefs(Factor factor) {
		if (factor.isFeatureFactor) {
			// Energy E1 - local model
			// factor domain 0 1
			OutputNode currentNode = factorisedSuperPixels.get(factor.getLeftSuperPixelIndex());
			FeatureNode featureNode =  currentNode.getFeatureNode();
			
			FactorEdgeKey factorToNodeKey = new FactorEdgeKey(factor, currentNode);
			Edge factorToNodeEdge = factorVariableToEdgeMap.get(factorToNodeKey);
			
			List<Double> newFactorBeliefs = new ArrayList<Double>();
			
			double maxFactorBeliefValue = 0;
			for (int label = 0; label < NUMBER_OF_STATES; label++) {
				FeatureVector localImageFi = featureNode.getSuperPixel().getLocalImageFi(label, this.getImageMask(), this, this.trainingDataimageToFactorGraphMap);
				double energy = localImageFi.calculateEnergy(this.weightVector);
				
				double variableToFactorMsg = factorToNodeEdge.getVariableToFactorMsg().get(label);
				
				double newFactorBelief = variableToFactorMsg - energy;
				if (newFactorBelief > maxFactorBeliefValue) maxFactorBeliefValue = newFactorBelief;
				newFactorBeliefs.add(newFactorBelief);
			}
			
			//normalisation
			
			double normalisationFactor = 0;
			for (int label = 0; label < NUMBER_OF_STATES; label++) {
				normalisationFactor += Math.exp(newFactorBeliefs.get(label) - maxFactorBeliefValue);
			}
			normalisationFactor = Math.log(normalisationFactor) + maxFactorBeliefValue;
			
			for (int label = 0; label < NUMBER_OF_STATES; label++) {
				double normalisedBelief = Math.exp(newFactorBeliefs.get(label) - normalisationFactor);
				//System.out.println(newFactorBeliefs.get(label) + "->" + normalisedBelief);
				newFactorBeliefs.set(label, normalisedBelief);
			}
			return newFactorBeliefs;
		} else {
			// Energy E2 - pairwise model
			// factor domain 00 01 10 11
			OutputNode leftNode = factorisedSuperPixels.get(factor.getLeftSuperPixelIndex());
			OutputNode rightNode = factorisedSuperPixels.get(factor.getRightSuperPixelIndex());
			
			FactorEdgeKey factorToLeftNodeKey = new FactorEdgeKey(factor, leftNode);
			Edge factorToLeftNodeEdge = factorVariableToEdgeMap.get(factorToLeftNodeKey);
			List<Double> leftMsgs = factorToLeftNodeEdge.getVariableToFactorMsg();
			
			FactorEdgeKey factorToRightNodeKey = new FactorEdgeKey(factor, rightNode);
			Edge factorToRightNodeEdge = factorVariableToEdgeMap.get(factorToRightNodeKey);
			List<Double> rightMsgs = factorToRightNodeEdge.getVariableToFactorMsg();
			
			List<Double> newFactorBeliefs = new ArrayList<Double>();
			double maxFactorBelief = 0;
			for (int label1 = 0; label1 < NUMBER_OF_STATES; label1++) {
				for (int label2 = 0; label2 < NUMBER_OF_STATES; label2++) {
					FeatureVector pairWiseImageFi = leftNode.getFeatureNode().getSuperPixel().getPairwiseImageFi(rightNode.getFeatureNode().getSuperPixel(), 
							this.getImageMask(), label1, label2);
					double energy = pairWiseImageFi.calculateEnergy(this.weightVector);
					
					double variableToFactorMsg = leftMsgs.get(label1) + rightMsgs.get(label2);
					double newFactorBelief = variableToFactorMsg - energy;
					if (newFactorBelief > maxFactorBelief) maxFactorBelief = newFactorBelief;
					newFactorBeliefs.add(newFactorBelief);
				}
			}
			
			//normalisation
			
			double normalisationFactor = 0;
			int iterator = 0;
			for (int label1 = 0; label1 < NUMBER_OF_STATES; label1++) {
				for (int label2 = 0; label2 < NUMBER_OF_STATES; label2++) {
					normalisationFactor += Math.exp(newFactorBeliefs.get(iterator) - maxFactorBelief);
					iterator++;
				}
			}
			normalisationFactor = Math.log(normalisationFactor) + maxFactorBelief;
			
			for (int i = 0; i < newFactorBeliefs.size(); i++) {
				double normalisedBelief = Math.exp(newFactorBeliefs.get(i) - normalisationFactor);
				//System.out.println("#" + newFactorBeliefs.get(i) + "->" + normalisedBelief);
				newFactorBeliefs.set(i, normalisedBelief);
			}
			return newFactorBeliefs;
		}
	}
	private List<Double> computeVariableBeliefs(OutputNode currentNode) {
		List<Double> variableBeliefs = new ArrayList<Double>();
		double maxBeliefValue = 0;
		for (int label = 0; label < NUMBER_OF_STATES; label++) {
			double belief = 0;
			List<Factor> adjacentFactors = currentNode.getAdjacentFactors();
			for (Factor factor : adjacentFactors) {
				
				// get edge between node and factor
				FactorEdgeKey factorToNodeKey = new FactorEdgeKey(factor, currentNode);
				Edge factorToNodeEdge = factorVariableToEdgeMap.get(factorToNodeKey);
				
				belief += factorToNodeEdge.getFactorToVariableMsg().get(label);
				if (belief > maxBeliefValue) maxBeliefValue = belief;
			}
			variableBeliefs.add(belief);
		}
		// normalise beliefs
		double normalisingFactorPlus = 0;
		for (Double belief : variableBeliefs) {
				normalisingFactorPlus +=  Math.exp(belief - maxBeliefValue);
		}
		normalisingFactorPlus = Math.log(normalisingFactorPlus) + maxBeliefValue;
		
		List<Double> outputBeliefs = new ArrayList<Double>();
		for (Double belief : variableBeliefs) {
				double outputBelief = Math.exp(belief - normalisingFactorPlus);
				outputBeliefs.add(outputBelief);
				//System.out.println("$" + outputBelief);
		}
		
		return outputBeliefs;
	}
	
	public void computeLabeling () {
		HashMap<Integer, Integer> labelCount = new HashMap<Integer,Integer>();
		for (int i = 0; i < FactorGraphModel.NUMBER_OF_STATES; i++) {
			labelCount.put(i,0);
		}
		
		for (OutputNode currentNode : factorisedSuperPixels) {
			FeatureNode featureNode = currentNode.getFeatureNode();
			int label = getMaximumIndexOfAList(currentNode.getMaxBeliefs());
			featureNode.setPixelLabel(label);
			int count = labelCount.get(label);
			labelCount.put(label, ++count);
			
		}
		for (int i = 0; i < FactorGraphModel.NUMBER_OF_STATES; i++) {
			System.out.print("Label " + i + ": " + labelCount.get(i) + " ");
		}
		System.out.println();
		System.out.println("***************");
	}
	
	
	public ImageDTO getImage() {
		return image;
	}

	public List<SuperPixelDTO> getSuperPixels() {
		return superPixels;
	}

	public int getNumberOfSuperPixels() {
		return numberOfSuperPixels;
	}

	public List<OutputNode> getFactorisedSuperPixels() {
		return factorisedSuperPixels;
	}

	public Set<Factor> getCreatedFactors() {
		return createdFactors;
	}
	public ImageMask getImageMask() {
		List<Integer> mask = new ArrayList<Integer>();
		for (SuperPixelDTO superPixel : superPixels) {
			mask.add(superPixel.getLabel());
		}
		return new ImageMask(mask);
	}
	
	public Map<Integer, BinaryMask> initLabelToBinaryMaskMap(int numberOfSuperPixels) {
		Map<Integer, BinaryMask> map = new HashMap<Integer, BinaryMask> ();
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			map.put(label, new BinaryMask(numberOfSuperPixels));
		}
		return map;
	}

	public Map<Feature, BinaryMask> getFeatureMap() {
		return featureMap;
	}

	public Map<Integer, BinaryMask> getLabelMap() {
		return labelMap;
	}
	public BinaryMask getLabelBinaryMask(Integer label) {
		return this.labelMap.get(label);
	}
	public BinaryMask getFeatureBinaryMask(Feature feature) {
		return this.featureMap.get(feature);
	}
	

}
