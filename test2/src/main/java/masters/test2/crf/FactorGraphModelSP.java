package masters.test2.crf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import masters.test2.image.ImageDTO;
import masters.test2.image.PixelDTO;
import masters.test2.superpixel.SuperPixelDTO;

public class FactorGraphModelSP {
	public static int NUMBER_OF_STATES = 2;
	public static double CONVERGENCE_TOLERANCE = 10e-3;
	
	private ImageDTO image;
	private List<SuperPixelDTO> superPixels;
	private List<OutputNode> factorisedSuperPixels;
	private OutputNode[][] factorisedImage;
	Set<Factor> createdFactors;
	
	private List<List<Double>> pixelFeatureWeights;
	private List<Double> pixelSimilarityWeights;
	
	private Map<FactorEdgeKey, Edge> factorVariableToEdgeMap = new HashMap<FactorEdgeKey, Edge>();
	
	public FactorGraphModelSP(ImageDTO image, List<SuperPixelDTO> superPixels) {
		this.image = image;
		this.superPixels = superPixels;
		this.factorisedSuperPixels = new ArrayList<OutputNode>();
		
		createdFactors = new HashSet<Factor>();
		initFixedModelGreenTest();
		//create factors and edges
		for (SuperPixelDTO superPixel : superPixels) {
			FeatureNode featureNode = new FeatureNode(superPixel, pixelFeatureWeights);
			OutputNode outputNode = new OutputNode(pixelSimilarityWeights, featureNode);
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
		System.out.println("nr of factors: " + createdFactors.size() );

		System.out.println("nr of edges: " + factorVariableToEdgeMap.keySet().size());
	}
	
	private void initFixedModelGreenTest() {
		pixelFeatureWeights = new ArrayList<List<Double>>();
		pixelSimilarityWeights = new ArrayList<Double>();
		
		// state 0
		pixelSimilarityWeights.add(0.0);
		List<Double> featureWeightSingleList = new ArrayList<Double>();
		featureWeightSingleList.add(1.0); //R
		featureWeightSingleList.add(-1.0); //G
		featureWeightSingleList.add(1.0); //B

		pixelFeatureWeights.add(featureWeightSingleList);
		
		
		// state 1
		pixelSimilarityWeights.add(0.0);
		featureWeightSingleList = new ArrayList<Double>();
		featureWeightSingleList.add(-1.0); //R
		featureWeightSingleList.add(1.0); //G
		featureWeightSingleList.add(-1.0); //B
	
		
		pixelFeatureWeights.add(featureWeightSingleList);
	}
	private void initFixedModel() {
		pixelFeatureWeights = new ArrayList<List<Double>>();
		Random random = new Random();

		
		pixelSimilarityWeights = new ArrayList<Double>();
		for (int i = 0; i < FactorGraphModel.NUMBER_OF_STATES; i++) {
			pixelSimilarityWeights.add((random.nextDouble() * 2) - 1);
			List<Double> featureWeightSingleList = new ArrayList<Double>();
			featureWeightSingleList.add((random.nextDouble() * 2) - 1); //R
			featureWeightSingleList.add((random.nextDouble() * 2) - 1); //G
			featureWeightSingleList.add((random.nextDouble() * 2) - 1); //B
			pixelFeatureWeights.add(featureWeightSingleList);
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
						double energy = leftNode.getEnergy(label, variableLabel);
						double rightNodeVariableToFactorMsg = factorToRightNodeEdge.getVariableToFactorMsg().get(variableLabel);
						//double rightNodeVariableToFactorMsg = factorToRightNodeEdge.getVariableToFactorMsg().get(label);
						double differenceValue = rightNodeVariableToFactorMsg - energy;
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
					
					// transfering message to right node 
					double msgToRightNode = 0;
					
					innerSumValues = new ArrayList<Double>();
					// inner sum
					for (int variableLabel = 0; variableLabel < NUMBER_OF_STATES; variableLabel++) {
						double energy = rightNode.getEnergy(label, variableLabel);
						double leftNodeVariableToFactorMsg = factorToLeftNodeEdge.getVariableToFactorMsg().get(variableLabel);
						//double leftNodeVariableToFactorMsg = factorToLeftNodeEdge.getVariableToFactorMsg().get(label);
						double differenceValue = leftNodeVariableToFactorMsg - energy;
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
					rightNode = leftNode.getFeatureNode();
					
					// get edge between left node and factor
					FactorEdgeKey factorToLeftNodeKey = new FactorEdgeKey(factor, leftNode);
					Edge factorToLeftNodeEdge = factorVariableToEdgeMap.get(factorToLeftNodeKey);
					
					double featureEnergy = rightNode.getEnergy(label, label);
					factorToLeftNodeEdge.setFactorToVariableMsgValue(label, featureEnergy);
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
				//normalise msgs
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
		int maxIndex = 0;
		double maxValue = Double.NEGATIVE_INFINITY;
		//System.out.print("# ");
		for (int i = 0; i < list.size(); i++ ){
			if (list.get(i) > maxValue) {
				maxValue = list.get(i);
				maxIndex = i;
			}
		//	System.out.print(" " + i + " " + list.get(i));
		}
		//System.out.println( "  ->" + maxIndex);
		return maxIndex;
	}
	
	private List<Double> calculateFactorBeliefs(Factor factor) {
		if (factor.isFeatureFactor) {
			// Energy E1 - local model
			// factor domain 0 1
			OutputNode currentNode = factorisedSuperPixels.get(factor.getLeftSuperPixelIndex());
			FeatureNode featureNode =  currentNode.getFeatureNode();
			
			FactorEdgeKey factorToNodeKey = new FactorEdgeKey(factor, currentNode);
			Edge factorToNodeEdge = factorVariableToEdgeMap.get(factorToNodeKey);
			
			List<Double> newFactorBeliefs = new ArrayList<Double>();
			
			for (int label = 0; label < NUMBER_OF_STATES; label++) {
				double energy = featureNode.getEnergy(label, label);
				double variableToFactorMsg = factorToNodeEdge.getVariableToFactorMsg().get(label);
				
				newFactorBeliefs.add(variableToFactorMsg - energy);
			}
			
			//normalisation
			
			double normalisationFactor = 0;
			for (int label = 0; label < NUMBER_OF_STATES; label++) {
				normalisationFactor += Math.exp(newFactorBeliefs.get(label));
			}
			normalisationFactor = Math.log(normalisationFactor);
			
			for (int label = 0; label < NUMBER_OF_STATES; label++) {
				double normalisedBelief = Math.exp(newFactorBeliefs.get(label) - normalisationFactor);
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
			for (int label1 = 0; label1 < NUMBER_OF_STATES; label1++) {
				for (int label2 = 0; label2 < NUMBER_OF_STATES; label2++) {
					double energy = leftNode.getEnergy(label1, label2);
					double variableToFactorMsg = leftMsgs.get(label1) + rightMsgs.get(label2);
					newFactorBeliefs.add(variableToFactorMsg - energy);
				}
			}
			
			//normalisation
			
			double normalisationFactor = 0;
			int iterator = 0;
			for (int label1 = 0; label1 < NUMBER_OF_STATES; label1++) {
				for (int label2 = 0; label2 < NUMBER_OF_STATES; label2++) {
					normalisationFactor += Math.exp(newFactorBeliefs.get(iterator));
					iterator++;
				}
			}
			
			for (int i = 0; i < newFactorBeliefs.size(); i++) {
				double normalisedBelief = Math.exp(newFactorBeliefs.get(i) - normalisationFactor);
				newFactorBeliefs.set(i, normalisedBelief);
			}
			return newFactorBeliefs;
		}
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
			List<Double> newMaxBeliefs = calculateFactorBeliefs(factor);
			// check maxBeliefChange
			for (int i = 0; i < previousMaxBeliefs.size(); i++) {
				Double beliefChange = Math.abs(newMaxBeliefs.get(i) - previousMaxBeliefs.get(i));
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
	private List<Double> computeVariableBeliefs(OutputNode currentNode) {
		List<Double> variableBeliefs = new ArrayList<Double>();
		for (int label = 0; label < NUMBER_OF_STATES; label++) {
			double belief = 0;
			List<Factor> adjacentFactors = currentNode.getAdjacentFactors();
			for (Factor factor : adjacentFactors) {
				
				// get edge between node and factor
				FactorEdgeKey factorToNodeKey = new FactorEdgeKey(factor, currentNode);
				Edge factorToNodeEdge = factorVariableToEdgeMap.get(factorToNodeKey);
				
				belief += factorToNodeEdge.getFactorToVariableMsg().get(label);
			}
			variableBeliefs.add(belief);
		}
		// normalise beliefs
		double normalisingFactor = 0;
		for (Double belief : variableBeliefs) {
			normalisingFactor += Math.exp(belief);
		}
		normalisingFactor = Math.log(normalisingFactor);
		
		List<Double> outputBeliefs = new ArrayList<Double>();
		for (Double belief : variableBeliefs) {
			outputBeliefs.add(Math.exp(belief - normalisingFactor));
		}
		
		return outputBeliefs;
	}
	public double computeLogZ() {
		double logZ = 0;
		
		double variableZPart = 0;
		for (OutputNode currentNode : factorisedSuperPixels) {
			int numberOfAdjacentFactors = currentNode.getAdjacentFactors().size();
			
			double beliefSumProduct = 0;
			List<Double> maxBeliefs = currentNode.getMaxBeliefs();
			for (Double maxBelief : maxBeliefs) {
				beliefSumProduct += (maxBelief * Math.log(maxBelief));
			}
			
			variableZPart += (numberOfAdjacentFactors - 1) * beliefSumProduct;
		}
		
		double factorZPart = 0;
		for (Factor factor : createdFactors) {
			List<Double> maxBeliefs = factor.getMaxBeliefs();
			List<Double> factorEnergies;
			if (factor.isFeatureFactor) {
				// Energy E1 - local model
				// domain 0 1
				OutputNode currentNode = factorisedSuperPixels.get(factor.getLeftSuperPixelIndex());
				FeatureNode featureNode =  currentNode.getFeatureNode();
				
				for (int label = 0; label < NUMBER_OF_STATES; label++) {
					double energy = featureNode.getEnergy(label, label);
					double maxBelief = maxBeliefs.get(label);
					factorZPart += maxBelief * (energy + Math.log(maxBelief));
				}
			} else {
				// Energy E2 - pairwise model
				// domain 00 01 10 11 
				
				OutputNode leftNode = factorisedSuperPixels.get(factor.getLeftSuperPixelIndex());
				OutputNode rightNode = factorisedSuperPixels.get(factor.getRightSuperPixelIndex());
				
				int iterator = 0;
				for (int label1 = 0; label1 < NUMBER_OF_STATES; label1++) {
					for (int label2 = 0; label2 < NUMBER_OF_STATES; label2++) {
						double energy = leftNode.getEnergy(label1, label2);
						double maxBelief = maxBeliefs.get(iterator);
						factorZPart += maxBelief * (energy + Math.log(maxBelief));
						iterator++;
					}
				}
			}
		}
		logZ = variableZPart - factorZPart;
		return logZ;
	}
	
	
	public void computeLabeling () {
		int label1 = 0;
		int label0 = 0;
		for (OutputNode currentNode : factorisedSuperPixels) {
			FeatureNode featureNode = currentNode.getFeatureNode();
			int label = getMaximumIndexOfAList(currentNode.getMaxBeliefs());
			featureNode.setPixelLabel(label);
			if (label == 1 ) {
				label1++;
			} else if (label == 0){
				label0++;
			}
		}
		System.out.println("Label 1: " + label1 + " Label 0: " + label0);
	}
	
	
	public ImageDTO getImage() {
		return image;
	}

	public List<SuperPixelDTO> getSuperPixels() {
		return superPixels;
	}

	public List<OutputNode> getFactorisedSuperPixels() {
		return factorisedSuperPixels;
	}
	
	

}
