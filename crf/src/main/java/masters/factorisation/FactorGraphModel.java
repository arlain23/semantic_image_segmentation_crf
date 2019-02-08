package masters.factorisation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;

import masters.App;
import masters.Constants;
import masters.features.BinaryMask;
import masters.features.ContinousFeature;
import masters.features.DiscreteFeature;
import masters.features.Feature;
import masters.features.FeatureContainer;
import masters.features.ValueDoubleMask;
import masters.image.ImageDTO;
import masters.image.ImageMask;
import masters.superpixel.SuperPixelDTO;
import masters.train.FeatureVector;
import masters.train.WeightVector;
import masters.utils.ParametersContainer;

public class FactorGraphModel implements Serializable {
  private static final long serialVersionUID = 531050692457565781L;
  
  public static int NUMBER_OF_STATES = Constants.NUMBER_OF_STATES;
	public static double CONVERGENCE_TOLERANCE = Constants.CONVERGENCE_TOLERANCE;
	
	private ImageDTO image;
	private List<OutputNode> factorisedSuperPixels;
	private Set<Factor> createdFactors;
	private WeightVector weightVector;
	private Map<FactorEdgeKey, Edge> factorVariableToEdgeMap = new HashMap<FactorEdgeKey, Edge>();
	
	private List<ImageDTO> trainImageList = null;
	private ParametersContainer parametersContainer = null;
	
	private String uuid;
	
	public FactorGraphModel(ImageDTO currentImage, List<ImageDTO> trainImageList,
			WeightVector weightVector, ParametersContainer parameterContainer) {
		this(currentImage, weightVector,
				parameterContainer);
		this.trainImageList = trainImageList;
		this.uuid = UUID.randomUUID().toString();
	}

	public FactorGraphModel(ImageDTO image, WeightVector weightVector, ParametersContainer parameterContainer) {
		this.uuid = UUID.randomUUID().toString();
		
		this.image = image;
		this.factorisedSuperPixels = new ArrayList<OutputNode>();
		this.createdFactors = new HashSet<Factor>();
		this.weightVector = weightVector;
		
		List<SuperPixelDTO> superPixels = this.image.getSuperPixels();

		this.parametersContainer = parameterContainer;
		
		//create factors and edges
		for (SuperPixelDTO superPixel : superPixels) {
			
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
//							break;
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
	}
	

	public void computeFactorToVariableMessages() {
		
		
		//label 0
		Map<Integer, Map<SuperPixelDTO, FeatureVector>> labelToLocalImageFi = new HashMap<>();
		for (int label = 0; label < NUMBER_OF_STATES; label++) {
			labelToLocalImageFi.put(label, new HashMap<SuperPixelDTO, FeatureVector>());
		}
		
		
		//for every factor
		for (Factor factor : createdFactors) {
			//for every label
			for (int label = 0; label < NUMBER_OF_STATES; label++) {
				OutputNode leftNode = factorisedSuperPixels.get(factor.getLeftSuperPixelIndex());
				Node rightNode;
				boolean isOutputNode = (factor.getRightSuperPixelIndex() >= 0);
				// pairwise model
				if (isOutputNode) {
					rightNode = factorisedSuperPixels.get(factor.getRightSuperPixelIndex());
					OutputNode rightNodeCast = (OutputNode) rightNode;
					
					// get edge between right node and factor
					FactorEdgeKey factorToRightNodeKey = new FactorEdgeKey(factor, rightNode);
					Edge factorToRightNodeEdge = factorVariableToEdgeMap.get(factorToRightNodeKey);
					FactorEdgeKey factorToLeftNodeKey = new FactorEdgeKey(factor, leftNode);
					Edge factorToLeftNodeEdge = factorVariableToEdgeMap.get(factorToLeftNodeKey);
					
					//Transferring message to left node
					double msgToLeftNode = 0;
					List<Double> innerSumValues = new ArrayList<Double>();
					// inner sum
					for (int variableLabel = 0; variableLabel < NUMBER_OF_STATES; variableLabel++) {
						
						FeatureVector pairWiseImageFi = leftNode.getFeatureNode().getSuperPixel().getPairwiseImageFi(rightNodeCast.getFeatureNode().getSuperPixel(), 
								this.image.getImageMask(), label, variableLabel, this.image, this.parametersContainer);
						double featureEnergy = pairWiseImageFi.calculateEnergy(this.weightVector);
						
//						_log.info("PAIRWISE FI " + pairWiseImageFi.toString() );
						
						double rightNodeVariableToFactorMsg = factorToRightNodeEdge.getVariableToFactorMsg()
								.get(variableLabel);
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
					
					factorToLeftNodeEdge.setFactorToVariableMsgValue(label, msgToLeftNode);
//					System.out.println("pair " + label + "  " + msgToLeftNode);
					
					// Transferring message to right node 
					double msgToRightNode = 0;
					
					innerSumValues = new ArrayList<Double>();
					// inner sum
					for (int variableLabel = 0; variableLabel < NUMBER_OF_STATES; variableLabel++) {
						
						FeatureVector pairWiseImageFi = rightNodeCast.getFeatureNode().getSuperPixel().getPairwiseImageFi(leftNode.getFeatureNode().getSuperPixel(), 
								this.image.getImageMask(), label, variableLabel, this.image, this.parametersContainer);
						double featureEnergy = pairWiseImageFi.calculateEnergy(this.weightVector);
						
//						_log.info("PAIRWISE FI " + pairWiseImageFi.toString() );
						
						double leftNodeVariableToFactorMsg = factorToLeftNodeEdge.getVariableToFactorMsg().get(variableLabel);
						double differenceValue = leftNodeVariableToFactorMsg - featureEnergy;
						innerSumValues.add(differenceValue);
					}
					
					maxInnerSum = Collections.max(innerSumValues);
					for (Double innerSumValue : innerSumValues) {
						msgToRightNode += Math.exp(innerSumValue - maxInnerSum);
					}
					
						
					msgToRightNode = maxInnerSum + Math.log(msgToRightNode);
//					System.out.println("pair " + label + "  " + msgToRightNode);
					factorToRightNodeEdge.setFactorToVariableMsgValue(label, msgToRightNode);
					
				} else {		// local model
					//feature node
					FeatureNode rightNodeFeatureNode = leftNode.getFeatureNode();
					
					
					// get edge between left node and factor
					FactorEdgeKey factorToLeftNodeKey = new FactorEdgeKey(factor, leftNode);
					Edge factorToLeftNodeEdge = factorVariableToEdgeMap.get(factorToLeftNodeKey);
					
					SuperPixelDTO superPixel = rightNodeFeatureNode.getSuperPixel();
					FeatureVector localImageFi = superPixel.getLocalImageFi(label, this.image.getImageMask(), this.image, this.trainImageList, 
							this.parametersContainer);
					
//					_log.info("LOCAL FI " + localImageFi.toString() );
					
					
					Map<SuperPixelDTO, FeatureVector> superpixelToFeatureVectorMap = labelToLocalImageFi.get(label);
					superpixelToFeatureVectorMap.put(superPixel, localImageFi);
					
					double featureEnergy = localImageFi.calculateEnergy(this.weightVector);
					
					/*
					 * 			ENERGIA
					 */
					
					/*
					 * ZDRADZIECKI MINUS
					 */
//					System.out.println("local " + label + "  " + (-featureEnergy));
					factorToLeftNodeEdge.setFactorToVariableMsgValue(label, -featureEnergy);
				}
			}
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
			_log.error("getMaximumIndexOfAList - 1");
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
				_log.error("updatePixelData  -1 ");
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
			OutputNode currentNode = factorisedSuperPixels.get(factor.getLeftSuperPixelIndex());
			FeatureNode featureNode =  currentNode.getFeatureNode();
			
			FactorEdgeKey factorToNodeKey = new FactorEdgeKey(factor, currentNode);
			Edge factorToNodeEdge = factorVariableToEdgeMap.get(factorToNodeKey);
			
			List<Double> newFactorBeliefs = new ArrayList<Double>();
			
			double maxFactorBeliefValue = 0;
			for (int label = 0; label < NUMBER_OF_STATES; label++) {
				FeatureVector localImageFi = featureNode.getSuperPixel().getLocalImageFi(label, this.image.getImageMask(), this.image, this.trainImageList, parametersContainer);
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
				newFactorBeliefs.set(label, normalisedBelief);
			}
			return newFactorBeliefs;
		} else {
			// Energy E2 - pairwise model
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
							this.image.getImageMask(), label1, label2, this.image, this.parametersContainer);
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
	
	
	public void setWeightVector(WeightVector weightVector) {
		this.weightVector = weightVector;
	}

	public ImageDTO getImage() {
		return image;
	}

	public List<OutputNode> getFactorisedSuperPixels() {
		return factorisedSuperPixels;
	}

	public Set<Factor> getCreatedFactors() {
		return createdFactors;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	private static transient Logger _log = Logger.getLogger(FactorGraphModel.class);
}
