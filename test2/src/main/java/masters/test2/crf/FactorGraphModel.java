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

public class FactorGraphModel {
	public static int NUMBER_OF_STATES = 2;
	public static double CONVERGENCE_TOLERANCE = 10e-3;
	
	private ImageDTO image;
	private OutputNode[][] factorisedImage;
	Set<Factor> createdFactors;
	
	private List<List<Double>> pixelFeatureWeights;
	private List<Double> pixelSimilarityWeights;
	
	private Map<FactorEdgeKey, Edge> factorVariableToEdgeMap = new HashMap<FactorEdgeKey, Edge>();
	
	public FactorGraphModel(ImageDTO image) {
		this.image = image;
		PixelDTO[][] pixelData = image.pixelData;
		int width = image.getWidth();
		int height = image.getHeight();
		System.out.println("***************************");
		System.out.println("width" + width + "   height: " + height);
		factorisedImage = new OutputNode[width][height];
		createdFactors = new HashSet<Factor>();
		initFixedModel();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				FeatureNode featureNode = new FeatureNode(pixelData[x][y], pixelFeatureWeights); 
				OutputNode outputNode = new OutputNode(pixelSimilarityWeights, featureNode);
				factorisedImage[x][y] = outputNode;
				//create factor on left, right, top, bottom
				//left
				if (x-1 >= 0) {
					Factor factor = new Factor(x-1, y, x, y);
					setFactorNodeToOutputNode(factor, createdFactors, outputNode);
				}
				//right
				if (x+1 < width) {
					Factor factor = new Factor(x, y, x+1, y);
					setFactorNodeToOutputNode(factor, createdFactors, outputNode);
					
					
				}
				//top
				if (y+1 < height) {
					Factor factor = new Factor(x, y, x, y+1);
					setFactorNodeToOutputNode(factor, createdFactors, outputNode);
				}
				//bottom
				if (y-1 >= 0) {
					Factor factor = new Factor(x, y-1, x, y);
					setFactorNodeToOutputNode(factor, createdFactors, outputNode);
				}
				//create factor connected to featureNode
				Factor factor = new Factor(x, y);
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
		}	
		System.out.println("nr of factors: " + createdFactors.size() );

		System.out.println("nr of edges: " + factorVariableToEdgeMap.keySet().size());
	}
	private void setFactorNodeToOutputNode(Factor factor, Set<Factor> createdFactors, OutputNode outputNode) {
		if (!createdFactors.contains(factor)) {
			createdFactors.add(factor);
			outputNode.addAdjacentFactors(factor);
		} else {
			for (Factor f : createdFactors) {
				if (factor.equals(f)) {
					outputNode.addAdjacentFactors(f);
					break;
				}
			}
		}
		
		// create edge between factors
		FactorEdgeKey key = new FactorEdgeKey(factor, outputNode);
		Edge edge = new Edge(factor, outputNode);
		factorVariableToEdgeMap.put(key, edge);
		
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
				OutputNode leftNode = factorisedImage[factor.leftX][factor.leftY];
				
				Node rightNode;
				boolean isOutputNode = (factor.rightX >= 0);
				
				// pairwise model
				if (isOutputNode) {
					rightNode = factorisedImage[factor.rightX][factor.rightY];
					
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
					
					// get edge between right node and factor
					FactorEdgeKey factorToLeftNodeKey = new FactorEdgeKey(factor, leftNode);
					Edge factorToLeftNodeEdge = factorVariableToEdgeMap.get(factorToLeftNodeKey);
					
					double featureEnergy = rightNode.getEnergy(label, label);
					factorToLeftNodeEdge.setFactorToVariableMsgValue(label, featureEnergy);
					if (print)
					System.out.println("L2: " + featureEnergy);
				}
			}
			print = false;
		}
	}
	public void computeVariableToFactorMessages() {
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				OutputNode currentNode = factorisedImage[x][y];
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
	}
	private int getMaximumIndexOfAList(List<Double> list){
		int maxIndex = 0;
		double maxValue = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < list.size(); i++ ){
			if (list.get(i) > maxValue) {
				maxValue = list.get(i);
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	private List<Double> calculateFactorBeliefs(Factor factor) {
		List<Double> newFactorBeliefs = new ArrayList<Double>();
		
		List<Double> factorEnergies;
		List<Double> variableToFactorMsgs = new ArrayList<Double>();
		if (factor.isFeatureFactor) {
			// Energy E1 - local model
			OutputNode currentNode = factorisedImage[factor.leftX][factor.leftY];
			FeatureNode featureNode =  currentNode.getFeatureNode();
			factorEnergies = featureNode.getEnergies();
			
			FactorEdgeKey factorToNodeKey = new FactorEdgeKey(factor, currentNode);
			Edge factorToNodeEdge = factorVariableToEdgeMap.get(factorToNodeKey);
			variableToFactorMsgs = factorToNodeEdge.getVariableToFactorMsg();
			
		} else {
			// Energy E2 - pairwise model
			OutputNode leftNode = factorisedImage[factor.leftX][factor.leftY];
			OutputNode rightNode = factorisedImage[factor.rightX][factor.rightY];
			factorEnergies = leftNode.getEnergies();
			
			FactorEdgeKey factorToLeftNodeKey = new FactorEdgeKey(factor, leftNode);
			Edge factorToLeftNodeEdge = factorVariableToEdgeMap.get(factorToLeftNodeKey);
			List<Double> leftMsgs = factorToLeftNodeEdge.getVariableToFactorMsg();
			
			FactorEdgeKey factorToRightNodeKey = new FactorEdgeKey(factor, rightNode);
			Edge factorToRightNodeEdge = factorVariableToEdgeMap.get(factorToRightNodeKey);
			List<Double> rightMsgs = factorToRightNodeEdge.getVariableToFactorMsg();
			
			for (int i = 0; i < rightMsgs.size(); i++) {
				variableToFactorMsgs.add(leftMsgs.get(i) + rightMsgs.get(i));
			}
		}
		
		double normalisationFactor = 0;
		for (int label = 0; label < NUMBER_OF_STATES; label++) {
			double newBelief = variableToFactorMsgs.get(label) - factorEnergies.get(label);
			newFactorBeliefs.add(newBelief);
			normalisationFactor += Math.exp(normalisationFactor);
		}
		normalisationFactor = Math.log(normalisationFactor);
		for (int label = 0; label < NUMBER_OF_STATES; label++) {
			double normalisedBelief = Math.exp(newFactorBeliefs.get(label) - normalisationFactor);
			newFactorBeliefs.set(label, normalisedBelief);
		}
		
		return newFactorBeliefs;
		
	}
	
	public void updatePixelData() {
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				OutputNode currentNode = factorisedImage[x][y];
				List<Double> maxBeliefs = currentNode.getMaxBeliefs();
				double winnerLabelValue = -1;
				int winnerLabelIndex = -1;
				for (int i = 0; i < maxBeliefs.size(); i++) {
					if (maxBeliefs.get(i) > winnerLabelValue) {
						winnerLabelValue = maxBeliefs.get(i);
						winnerLabelIndex = i;
						
					}
					System.out.println("#: " + i + " " + maxBeliefs.get(i));
				}
				
				//update label
				//System.out.println("# " + winnerLabelIndex);
				FeatureNode featureNode = currentNode.getFeatureNode();
				featureNode.setPixelLabel(winnerLabelIndex);
			}
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
		
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				OutputNode currentNode = factorisedImage[x][y];
				List<Double> previousMaxBeliefs = currentNode.getMaxBeliefs();
				List<Double> newMaxBeliefs = computeVariableBeliefs(x, y);
				
				// check maxBeliefChange
				for (int i = 0; i < previousMaxBeliefs.size(); i++) {
					Double beliefChange = Math.abs(newMaxBeliefs.get(i) - previousMaxBeliefs.get(i));
					if (beliefChange > maxVariableBefiefChange) {
						maxVariableBefiefChange = beliefChange;
					}
				}
				currentNode.setMaxBeliefs(newMaxBeliefs);
				
			}
		}
		System.out.println(maxFactorBefiefChange + " " + maxVariableBefiefChange);
		return maxFactorBefiefChange <= CONVERGENCE_TOLERANCE && maxVariableBefiefChange <= CONVERGENCE_TOLERANCE; 
	}
	private List<Double> computeVariableBeliefs(int x, int y) {
		OutputNode currentNode = factorisedImage[x][y];
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
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				OutputNode currentNode = factorisedImage[x][y];
				int numberOfAdjacentFactors = currentNode.getAdjacentFactors().size();
				
				double beliefSumProduct = 0;
				List<Double> maxBeliefs = currentNode.getMaxBeliefs();
				for (Double maxBelief : maxBeliefs) {
					beliefSumProduct += (maxBelief * Math.log(maxBelief));
				}
				
				variableZPart += numberOfAdjacentFactors - 1 * beliefSumProduct;
			}
		}
		
		double factorZPart = 0;
		for (Factor factor : createdFactors) {
			List<Double> maxBeliefs = factor.getMaxBeliefs();
			List<Double> factorEnergies;
			if (factor.isFeatureFactor) {
				// Energy E1 - local model
				OutputNode currentNode = factorisedImage[factor.leftX][factor.leftY];
				FeatureNode featureNode =  currentNode.getFeatureNode();
				factorEnergies = featureNode.getEnergies();
				
			} else {
				// Energy E2 - pairwise model
				OutputNode leftNode = factorisedImage[factor.leftX][factor.leftY];
				OutputNode rightNode = factorisedImage[factor.rightX][factor.rightY];
				factorEnergies = leftNode.getEnergies();
			}
			for (int label = 0; label < NUMBER_OF_STATES; label++) {
				factorZPart += maxBeliefs.get(label) * (factorEnergies.get(label) + Math.log(maxBeliefs.get(label)));
			}
			
		}
		logZ = variableZPart - factorZPart;
		return logZ;
	}
	
	
	public void computeLabeling () {
		System.out.println("labeling");	
		PixelDTO[][] pixelData = image.pixelData;
		int a = 0;
		int b = 0;
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				OutputNode currentNode = factorisedImage[x][y];
				FeatureNode featureNode = currentNode.getFeatureNode();
				int label = getMaximumIndexOfAList(currentNode.getMaxBeliefs());
				featureNode.setPixelLabel(label);
				pixelData[x][y].label = label;
				if (label == 1 ) {
					a++;
				} else if (label == 0){
					b++;
				}
			}
		}
		System.out.println("a: " + a + "b: " + b);
	}
	
	
	public ImageDTO getImage() {
		return image;
	}
	

}
