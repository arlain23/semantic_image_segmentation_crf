package masters.test2.superpixel;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import masters.test2.Helper;
import masters.test2.factorisation.FactorGraphModel;
import masters.test2.image.PixelDTO;
import masters.test2.sampler.ImageMask;
import masters.test2.train.FeatureVector;

public class SuperPixelDTO implements Comparable<SuperPixelDTO> {
	private int meanR;
	private int meanG;
	private int meanB;
	
	private double scaledR;
	private double scaledG;
	private double scaledB;
	
	public static int NUMBER_OF_FEATURES = 3;
	private int label;
	
	private int identifingColorRGB;
	private List<PixelDTO> pixels = new ArrayList<PixelDTO>();
	private List<PixelDTO> borderPixels = new ArrayList<PixelDTO>();
	private int superPixelIndex;
	
	private List<Integer> neighboursIndexes;
	private List<SuperPixelDTO> neigbouringSuperPixels;
	
	public SuperPixelDTO(int superPixelIndex) {
		this.superPixelIndex = superPixelIndex;
		Random rand = new Random();
		
		Color superPixelColor =  new Color(rand.nextInt(256), 
				rand.nextInt(256), rand.nextInt(256)); 		
        this.identifingColorRGB = superPixelColor.getRGB();
	}
	
	public void initMeanColours() {
		double RSum = 0;
		double GSum = 0;
		double BSum = 0;
		for (PixelDTO pixel : pixels) {
			RSum += pixel.getR();
			GSum += pixel.getG();
			BSum += pixel.getB();
		}
		int numberOfPixels = pixels.size();
		this.meanR = (int) Math.round(RSum / numberOfPixels);
		this.meanG = (int) Math.round(GSum / numberOfPixels);
		this.meanB = (int) Math.round(BSum / numberOfPixels);
		
		//scale 
		this.scaledR = meanR / 255.0;
		this.scaledG = meanG / 255.0;
		this.scaledB = meanB / 255.0;
		
	}
	public void initBorderPixels() {
		for (PixelDTO pixel : pixels) {
			if (pixel.isBorderPixel()) {
				borderPixels.add(pixel);
			}
		}
	}
	public void initNeighbours(PixelDTO[][] pixelData, List<SuperPixelDTO> allSuperPixels){
		Set<Integer> neigbouringSuperPixels = new HashSet<Integer>();
		Map<Integer, Integer> numberOfNeighbouringPixels = new HashMap<Integer, Integer>();
		for (PixelDTO borderPixel : borderPixels) {
			int xCoord = borderPixel.getXIndex();
			int yCoord = borderPixel.getYIndex();
			// check pixel on right
			if (xCoord + 1 < pixelData.length) {
				computeNeighbour(xCoord + 1, yCoord, pixelData, neigbouringSuperPixels, numberOfNeighbouringPixels);
			}
			// check pixel on left
			if (xCoord - 1 >= 0) {
				computeNeighbour(xCoord - 1, yCoord, pixelData, neigbouringSuperPixels, numberOfNeighbouringPixels);
			}
			// check pixel on the top
			if (yCoord - 1 >= 0 ) {
				computeNeighbour(xCoord, yCoord - 1, pixelData, neigbouringSuperPixels, numberOfNeighbouringPixels);
			}
			// check pixel on the bottom
			if (yCoord + 1 < pixelData[0].length) {
				computeNeighbour(xCoord, yCoord + 1, pixelData, neigbouringSuperPixels, numberOfNeighbouringPixels);
			}
		}
		int borderLength = borderPixels.size();
		neighboursIndexes = new ArrayList<Integer>();
		for (Integer neighbouringSuperPixelIndex : numberOfNeighbouringPixels.keySet()) {
			int numberValue = numberOfNeighbouringPixels.get(neighbouringSuperPixelIndex);
			this.neighboursIndexes.add(neighbouringSuperPixelIndex);
			
			//TODO: improve neigbours -> if one is neighbour of the other then the other should be neighbour of the first one
			/*if (numberValue > (0.01 * borderLength)){
				this.neighboursIndexes.add(neighbouringSuperPixelIndex);
			}*/
			//System.out.print("# " + neighbouringSuperPixelIndex + " ");
		}
		//System.out.println();
		
		this.neigbouringSuperPixels = new ArrayList<SuperPixelDTO>();
		for (Integer superPixelIndex : this.neighboursIndexes) {
			this.neigbouringSuperPixels.add(allSuperPixels.get(superPixelIndex));
		}
		
	}
	private void computeNeighbour (int xCoord, int yCoord, PixelDTO[][] pixelData, Set<Integer> neigbouringSuperPixels,
			Map<Integer, Integer> numberOfNeighbouringPixels) {
		PixelDTO neigbouringPixel = pixelData[xCoord][yCoord];
		int neigbouringSuperPixelIndex = neigbouringPixel.getSuperPixelIndex();
		if (this.superPixelIndex != neigbouringSuperPixelIndex) {
			neigbouringSuperPixels.add(neigbouringSuperPixelIndex);
			if (numberOfNeighbouringPixels.containsKey(neigbouringSuperPixelIndex)) {
				int tmpValue = numberOfNeighbouringPixels.get(neigbouringSuperPixelIndex) + 1;
				numberOfNeighbouringPixels.replace(neigbouringSuperPixelIndex, tmpValue);
			} else {
				numberOfNeighbouringPixels.put(neigbouringSuperPixelIndex, 1);
			}
		}
	}
	
	public double getEnergyByWeights(List<Double> pixelFeatureWeights) {
		return scaledR * pixelFeatureWeights.get(0) + 
			scaledG * pixelFeatureWeights.get(1) +
			scaledB * pixelFeatureWeights.get(2);
	}
	
	public FeatureVector getLocalImageFi(ImageMask mask){
		FeatureVector imageFi = new FeatureVector(FactorGraphModel.NUMBER_OF_STATES * NUMBER_OF_FEATURES + 2);
		int featureIndex = 0;
		int objectLabel = this.label;
		if (mask != null) {
			objectLabel = mask.getMask().get(this.superPixelIndex);
		}
		for (int label = 0; label < FactorGraphModel.NUMBER_OF_STATES; label++) {
			if (label == objectLabel) {
				imageFi.setFeatureValue(featureIndex++, scaledR);
				imageFi.setFeatureValue(featureIndex++, scaledG);
				imageFi.setFeatureValue(featureIndex++, scaledB);
			} else {
				imageFi.setFeatureValue(featureIndex++, 0.0);
				imageFi.setFeatureValue(featureIndex++, 0.0);
				imageFi.setFeatureValue(featureIndex++, 0.0);
			}
		}
		return imageFi;
	}
	public FeatureVector getPairwiseImageFi(SuperPixelDTO superPixel, ImageMask mask){
		int label1 = this.label;
		if (mask != null) {
			label1 = mask.getMask().get(this.superPixelIndex);
		}
		int label2 = superPixel.getLabel();
		FeatureVector imageFi = new FeatureVector(FactorGraphModel.NUMBER_OF_STATES * NUMBER_OF_FEATURES + 2);
		int labelDiff = Math.abs(label1 - label2);
		int featureIndex = FactorGraphModel.NUMBER_OF_STATES * 3;
		imageFi.setFeatureValue(featureIndex++, 1 - labelDiff);
		imageFi.setFeatureValue(featureIndex++, labelDiff);
		return imageFi;
	}
	public double getPairSimilarityFeature(int label1, int label2) {
		if (label1 == label2) return 1;
		return 0;
	}
	
	public int getMeanR() {
		return meanR;
	}

	public int getMeanG() {
		return meanG;
	}

	public int getMeanB() {
		return meanB;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
		updatePixelLabels();
	}
	public void updatePixelLabels() {
		for (PixelDTO pixel : pixels) {
			pixel.setLabel(this.label);
		}
	}
	public void updatePixelLabels(List<Integer> mask) {
		for (PixelDTO pixel : pixels) {
			int label = mask.get(this.superPixelIndex);
			pixel.setLabel(label);
		}
	}
	public int getIdentifingColorRGB() {
		return this.identifingColorRGB;
	}
	public List<PixelDTO> getPixels() {
		return pixels;
	}

	public List<SuperPixelDTO> getNeigbouringSuperPixels() {
		return neigbouringSuperPixels;
	}

	public int getSuperPixelIndex() {
		return superPixelIndex;
	}
	public void addPixel(PixelDTO pixel) {
		this.pixels.add(pixel);
	}

	public List<PixelDTO> getBorderPixels() {
		return borderPixels;
	}

	public List<Integer> getNeighboursIndexes() {
		return neighboursIndexes;
	}
	public Point getSamplePixel() {
		int x = 0;
		int y = 0;
		int maxX = 0;
		int minX = Integer.MAX_VALUE;
		int maxY = 0;
		int minY = Integer.MAX_VALUE;
		for (PixelDTO bp : borderPixels) {
			if (bp.getXIndex() > maxX) maxX = bp.getXIndex();
			if (bp.getXIndex() < minX) minX = bp.getXIndex();
			if (bp.getYIndex() > maxY) maxY = bp.getYIndex();
			if (bp.getYIndex() < minY) minY = bp.getYIndex();
		}
		y = (maxY - minY) / 2;
		x = (maxX - minX) / 2;
		y += minY;
		x += minX;
		return new Point(x,y);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + superPixelIndex;
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
		SuperPixelDTO other = (SuperPixelDTO) obj;
		if (superPixelIndex != other.superPixelIndex)
			return false;
		return true;
	}

	public int compareTo(SuperPixelDTO otherObject) {
		if(this.superPixelIndex == otherObject.superPixelIndex)
            return 0;
        return this.superPixelIndex < otherObject.superPixelIndex ? -1 : 1;
	}
	@Override
	public String toString() {
		return ("superpixel index " + superPixelIndex);
	}
	
}
