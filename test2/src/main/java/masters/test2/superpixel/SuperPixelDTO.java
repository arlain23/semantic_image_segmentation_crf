package masters.test2.superpixel;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.management.RuntimeErrorException;

import masters.test2.Constants;
import masters.test2.Helper;
import masters.test2.colors.ColorSpaceConverter;
import masters.test2.colors.ColorSpaceException;
import masters.test2.factorisation.FactorGraphModel;
import masters.test2.image.PixelDTO;
import masters.test2.sampler.ImageMask;
import masters.test2.train.FeatureVector;
import masters.test2.train.WeightVector;

public class SuperPixelDTO implements Comparable<SuperPixelDTO> {
	
	private FeatureVector featureVector;
	private double[] meanRGB;	
	public static int NUMBER_OF_FEATURES;
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
	
	private void initFeatureVector(double[] rgb) throws ColorSpaceException {
		List<Double> featureValues = null;
		this.meanRGB = rgb;
		switch (Constants.colorSpace) {
		case RGB:
			featureValues = initRGBFeatures(meanRGB);
			break;
		case CIELAB:
			featureValues = initCIELABFeatures(meanRGB);
			break;
		case HSL:
			featureValues = initHSLFeatures(meanRGB);
			break;
		case HSV:
			featureValues = initHSVFeatures(meanRGB);
			break;
		case HISTOGRAM:
			featureValues = initHistogramFeatures();
			break;
		default:
			throw new ColorSpaceException("Undefined color space");
		}
		NUMBER_OF_FEATURES = featureValues.size();
		this.featureVector = new FeatureVector(featureValues);
	}
	public void initFeatureVector() throws ColorSpaceException {
		this.meanRGB = getMeanRGBValue();
		initFeatureVector(meanRGB);
	}
	private double [] getMeanRGBValue () {
		double RSum = 0;
		double GSum = 0;
		double BSum = 0;
		for (PixelDTO pixel : pixels) {
			RSum += pixel.getR();
			GSum += pixel.getG();
			BSum += pixel.getB();
		}
		int numberOfPixels = pixels.size();
		double meanR = (int) Math.round(RSum / numberOfPixels);
		double meanG = (int) Math.round(GSum / numberOfPixels);
		double meanB = (int) Math.round(BSum / numberOfPixels);
		
		return new double [] {meanR, meanG, meanB};
	}
	private List<Double> initRGBFeatures (double [] rgb) {
		
		//scaled 
		double scaledR = rgb[0] / 255.0;
		double scaledG = rgb[1] / 255.0;
		double scaledB = rgb[2] / 255.0;
		
		return Arrays.asList(new Double[] {scaledR, scaledG, scaledB});
		
	}
	private List<Double> initHSVFeatures (double [] rgb) {
		return Arrays.asList(ColorSpaceConverter.rgb2hsv(rgb));
	}
	private List<Double> initHSLFeatures (double [] rgb) {
		return Arrays.asList(ColorSpaceConverter.rgb2hsl(rgb));
	}
	private List<Double> initCIELABFeatures (double [] rgb) {
		return Arrays.asList(ColorSpaceConverter.rgb2lab(rgb));
	}
	private List<Double> initHistogramFeatures() {
		List<Double> features = Helper.initFixedSizedListDouble(3 * Constants.NUMBER_OF_HISTOGRAM_DIVISIONS);
		int numberOfColorIndexes = 256; 
		
		// get frequencies of each color section
		List<Integer> redFrequencies = Helper.initFixedSizedListInteger(numberOfColorIndexes);
		List<Integer> greenFrequencies = Helper.initFixedSizedListInteger(numberOfColorIndexes);
		List<Integer> blueFrequencies = Helper.initFixedSizedListInteger(numberOfColorIndexes);
		
		for (PixelDTO pixel : pixels) {
			int r = pixel.getR();
			int newValue = redFrequencies.get(r) + 1;
			redFrequencies.set(r, newValue);
			
			int g = pixel.getG();
			newValue = greenFrequencies.get(g) + 1;
			greenFrequencies.set(g, newValue);
			
			int b = pixel.getB();
			newValue = blueFrequencies.get(b) + 1;
			blueFrequencies.set(b, newValue);
		}
		
		if (numberOfColorIndexes % Constants.NUMBER_OF_HISTOGRAM_DIVISIONS != 0) {
			Constants._log.error(numberOfColorIndexes + " has to be divisable by the number of histogram divisions");
			throw new RuntimeErrorException(null);
		}
		
		int blockSize = numberOfColorIndexes / Constants.NUMBER_OF_HISTOGRAM_DIVISIONS;
		for (int section = 0; section < Constants.NUMBER_OF_HISTOGRAM_DIVISIONS; section ++) {
			// red
			int redSum = 0;
			int greenSum = 0;
			int blueSum = 0;
			for (int i = section * blockSize; i < (section + 1) * blockSize; i++) {
				redSum += redFrequencies.get(i);
				greenSum += greenFrequencies.get(i);
				blueSum += blueFrequencies.get(i);
			}
			features.set(0 * section, redSum * 1.0); 	//red 
			features.set(1 * section, greenSum * 1.0); 	//green 
			features.set(2 * section, blueSum * 1.0); 	//blue
		}
		
		// normalise
		int numberOfPixels = pixels.size();
		for (int i = 0; i < features.size(); i++) {
			double scaledValue = features.get(i) / numberOfPixels;
			features.set(i, scaledValue);
		}
		return features;
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
	
	public double getEnergyByWeightVector(WeightVector weightVector) {
		return featureVector.calculateEnergy(weightVector);
	}
	public double getEnergyByWeightVector(WeightVector weightVector, int label) {
		return featureVector.calculateEnergy(weightVector.getFeatureWeightsForLabel(label));
	}
	
	public FeatureVector getLocalImageFi(ImageMask mask){
		FeatureVector imageFi = new FeatureVector(Constants.NUMBER_OF_STATES * NUMBER_OF_FEATURES + 2);
		int featureIndex = 0;
		int objectLabel = this.label;
		if (mask != null) {
			objectLabel = mask.getMask().get(this.superPixelIndex);
		}
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			if (label == objectLabel) {
				for (double featureValue : this.featureVector.getFeatureValues()) {
					imageFi.setFeatureValue(featureIndex++, featureValue);
				}
			} else {
				for (double featureValue : this.featureVector.getFeatureValues()) {
					imageFi.setFeatureValue(featureIndex++, 0.0);
				}
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
		FeatureVector imageFi = new FeatureVector(Constants.NUMBER_OF_STATES * NUMBER_OF_FEATURES + 2);
		boolean labelsEquality = label1 == label2;
		int labelDiff = (labelsEquality) ? 0 : 1;
		int featureIndex = Constants.NUMBER_OF_STATES * 3;
		imageFi.setFeatureValue(featureIndex++, 1 - labelDiff);
		imageFi.setFeatureValue(featureIndex++, labelDiff);
		return imageFi;
	}
	public double getPairSimilarityFeature(int label1, int label2) {
		if (label1 == label2) return 1;
		return 0;
	}
	
	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
		updatePixelLabels();
	}
	public void updatePixelLabels() {
		if (this.label == -1) {
			System.out.println("setting labels " + this.superPixelIndex + " } " + this.label);
		}
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
	
	
	public void setMeanRGB(double[] meanRGB) throws ColorSpaceException {
		initFeatureVector(meanRGB);
	}
	public double[] getMeanRGB() {
		return this.meanRGB;
	}

	public FeatureVector getFeatureVector() {
		return featureVector;
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
