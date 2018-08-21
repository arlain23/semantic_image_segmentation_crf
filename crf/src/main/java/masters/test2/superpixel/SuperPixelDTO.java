package masters.test2.superpixel;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.management.DescriptorRead;
import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;

import masters.test2.App;
import masters.test2.Constants;
import masters.test2.colors.ColorSpaceConverter;
import masters.test2.colors.ColorSpaceException;
import masters.test2.factorisation.Factor;
import masters.test2.factorisation.FactorGraphModel;
import masters.test2.features.BinaryMask;
import masters.test2.features.ContinousFeature;
import masters.test2.features.DiscreteFeature;
import masters.test2.features.Feature;
import masters.test2.features.ValueMask;
import masters.test2.features.VectorFeature;
import masters.test2.image.ImageDTO;
import masters.test2.image.ImageMask;
import masters.test2.image.PixelDTO;
import masters.test2.train.FeatureVector;
import masters.test2.train.WeightVector;
import masters.test2.utils.CRFUtils;
import masters.test2.utils.Helper;
import masters.test2.utils.ProbabilityContainer;

public class SuperPixelDTO implements Comparable<SuperPixelDTO> {
	
	private FeatureVector localFeatureVector;
	private FeatureVector pairwiseFeatureVector;
	private double[] meanRGB;	
	public int numberOfLocalFeatures;
	public int numberOfPairwiseFeatures;
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
	
	
	/*
	 * 		SUPERPIXELS FEATURES
	 * 
	 */
	public void initFeatureVector() throws ColorSpaceException {
		List<Feature> localFeatures = new ArrayList<Feature>();
		List<Feature> pairwiseFeatures = new ArrayList<Feature>();
		
		if (Constants.USE_NON_LINEAR_MODEL) {
			if (Constants.INCLUDE_DESCRETE_FEATURES) {
				localFeatures.addAll(initBayesFeatures());
				pairwiseFeatures.addAll(localFeatures);
			}
			if (Constants.INCLUDE_CONTINUOUS_FEATURES) {
				localFeatures.addAll(initLocalContinuousFeatures(this.meanRGB));
				pairwiseFeatures.addAll(initPairwiseContinuousFeatures(this.meanRGB));
			}
		} else {
			localFeatures.addAll(getColourFeatures(this.meanRGB, 0));
		}
		numberOfLocalFeatures = localFeatures.size();
		numberOfPairwiseFeatures = pairwiseFeatures.size();
		this.localFeatureVector = new FeatureVector(localFeatures, 1);
		this.pairwiseFeatureVector = new FeatureVector(pairwiseFeatures, 1);
		
	}
	public void initMeanRGB() throws ColorSpaceException {
		switch (Constants.colorAverageMethod) {
		case MEAN:
			this.meanRGB = getMeanRGBValue();
			break;
		case POPULARITY:
			this.meanRGB = getMostPopularRGBValue();
			break;
		default:
			throw new ColorSpaceException("Undefined averaging method");
		}
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
	private double [] getMostPopularRGBValue () {
		Map<Integer, Integer> redCounter = new HashMap<Integer, Integer>();
		Map<Integer, Integer> greenCounter = new HashMap<Integer, Integer>();
		Map<Integer, Integer> blueCounter = new HashMap<Integer, Integer>();
		
		int previousValue;
		for (PixelDTO pixel : pixels) {
			int r = pixel.getR();
			int g = pixel.getG();
			int b = pixel.getB();
			if (!redCounter.containsKey(r)) {
				redCounter.put(r, 0);
			}
			previousValue = redCounter.get(r);
			redCounter.put(r, (++previousValue));
			
			if (!greenCounter.containsKey(g)) {
				greenCounter.put(g, 0);
			}
			previousValue = greenCounter.get(g);
			greenCounter.put(g, (++previousValue));
			
			if (!blueCounter.containsKey(b)) {
				blueCounter.put(b, 0);
			}
			previousValue = blueCounter.get(b);
			blueCounter.put(b, (++previousValue));
			
		}
		int maxKey = 0;
		int maxValue = 0;
		for (Integer key : redCounter.keySet()) {
			if (redCounter.get(key) > maxValue) {
				maxKey = key;
				maxValue = redCounter.get(key);
			}
		}
		double maxR = maxKey;
		
		maxKey = 0;
		maxValue = 0;
		for (Integer key : greenCounter.keySet()) {
			if (greenCounter.get(key) > maxValue) {
				maxKey = key;
				maxValue = greenCounter.get(key);
			}
		}
		double maxG = maxKey;
		
		maxKey = 0;
		maxValue = 0;
		for (Integer key : blueCounter.keySet()) {
			if (blueCounter.get(key) > maxValue) {
				maxKey = key;
				maxValue = blueCounter.get(key);
			}
		}
		double maxB = maxKey;
		
		return new double[] {maxR, maxG, maxB};
	}
	
	private List<Feature> initBayesFeatures() {
		List<Feature> features = new ArrayList<Feature> ();
		
		String hex = String.format("#%02x%02x%02x", (int)meanRGB[0], (int)meanRGB[1], (int)meanRGB[2]);
		Feature colorFeature = new DiscreteFeature(hex);
		
		features.add(colorFeature);
		return features;
	}
	private List<Feature> initLocalContinuousFeatures(double [] rgb) throws ColorSpaceException {
		List<Feature> features = new ArrayList<Feature> ();
		features.addAll(getColourFeatures(rgb, features.size()));
		//features.addAll(getNeighbourColourFeatures(features.size()));
		
		return features;
	}
	private List<Feature> initPairwiseContinuousFeatures(double [] rgb) throws ColorSpaceException {
		List<Feature> features = new ArrayList<Feature> ();
		features.add(new VectorFeature(getColour(rgb, features.size()), features.size()));
		return features;
	}
	private List<Feature> getNeighbourColourFeatures(int startingIndex) {
		List<Feature> features = new ArrayList<Feature> ();
		List<Double> featureValues = getNeighboursMeanRGB();
		for (int i = 0; i < featureValues.size(); i++) {
			features.add(new ContinousFeature(featureValues.get(i), i + startingIndex));
		}
		return features;
	}
	private List<Double> getNeighboursMeanRGB() {
		Double[] meanRGB = new Double[] {0.0, 0.0, 0.0};
		int numberOfNeighbours = this.neigbouringSuperPixels.size();
		for (SuperPixelDTO neighbour : this.neigbouringSuperPixels) {
			double[] rgb = neighbour.getMeanRGB();
			meanRGB[0] = meanRGB[0] + rgb[0];
			meanRGB[1] = meanRGB[1] + rgb[1];
			meanRGB[2] = meanRGB[2] + rgb[2];
		}
		meanRGB[0] = scaleColour(meanRGB[0] / numberOfNeighbours);
		meanRGB[1] = scaleColour(meanRGB[1] / numberOfNeighbours);
		meanRGB[2] = scaleColour(meanRGB[2] /numberOfNeighbours);
		
		return Arrays.asList(meanRGB);
	}
	
	private List<Feature> getColourFeatures(double [] rgb, int startingIndex) throws ColorSpaceException {
		List<Feature> features = new ArrayList<Feature> ();
		List<Double> featureValues = getColour(rgb, startingIndex);
		for (int i = 0; i < featureValues.size(); i++) {
			features.add(new ContinousFeature(featureValues.get(i), i + startingIndex));
		}
		return features;
		
	}
	private List<Double> getColour(double [] rgb, int startingIndex) throws ColorSpaceException {
		switch (Constants.colorSpace) {
		case RGB:
			return initRGBFeatures(rgb, startingIndex);
		case CIELAB:
			return initCIELABFeatures(rgb, startingIndex);
		case HSL:
			return initHSLFeatures(rgb, startingIndex);
		case HSV:
			return initHSVFeatures(rgb, startingIndex);
		case HISTOGRAM:
			return initHistogramFeatures(startingIndex);
		default:
			throw new ColorSpaceException("Undefined color space");
		}
	}
	
	private double scaleColour(double value) {
		return value / 255.0;
	}
	private List<Double> scaleColour(List<Double> values) {
		List<Double> result = new ArrayList<Double>();
		for (double val : values) {
			result.add(scaleColour(val));
		}
		return result;
	}
	private List<Double> initRGBFeatures (double [] rgb, int startingIndex) {
		return Arrays.asList(new Double[] {scaleColour(rgb[0]), scaleColour(rgb[1]), scaleColour(rgb[2])});
	}
	private List<Double> initHSVFeatures (double [] rgb, int startingIndex) {
		return scaleColour(Arrays.asList(ColorSpaceConverter.rgb2hsv(rgb)));
	}
	private List<Double> initHSLFeatures (double [] rgb, int startingIndex) {
		return scaleColour(Arrays.asList(ColorSpaceConverter.rgb2hsl(rgb)));
	}
	private List<Double> initCIELABFeatures (double [] rgb, int startingIndex) {
		return scaleColour(Arrays.asList(ColorSpaceConverter.rgb2lab(rgb)));
	}
	private List<Double> initHistogramFeatures(int startingIndex) {
		List<Double> featureValues = Helper.initFixedSizedListDouble(3 * Constants.NUMBER_OF_HISTOGRAM_DIVISIONS);
		int numberOfColorIndexes = 256; 
		
		// get frequencies of each colour section
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
			_log.error(numberOfColorIndexes + " has to be divisable by the number of histogram divisions");
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
			featureValues.set(0 * section, redSum * 1.0); 	//red 
			featureValues.set(1 * section, greenSum * 1.0); 	//green 
			featureValues.set(2 * section, blueSum * 1.0); 	//blue
		}
		
		// normalise
		int numberOfPixels = pixels.size();
		for (int i = 0; i < featureValues.size(); i++) {
			double scaledValue = featureValues.get(i) / numberOfPixels;
			featureValues.set(i, scaledValue);
		}
		
		return featureValues;
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
		neighboursIndexes = new ArrayList<Integer>();
		for (Integer neighbouringSuperPixelIndex : numberOfNeighbouringPixels.keySet()) {
			this.neighboursIndexes.add(neighbouringSuperPixelIndex);
		}
		
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
	
	
	/*
	 * 	
	 * 			COMPUTING OF FEATURE VECTORS
	 * 
	 */
		
	public FeatureVector getLocalImageFi(Integer objectLabel, ImageMask imageMask, FactorGraphModel factorGraphModel,
			Map<ImageDTO, FactorGraphModel> trainingDataimageToFactorGraphMap,
			ProbabilityContainer probabiltyContainer) {
		
		return CRFUtils.getLocalImageFi(this.superPixelIndex, objectLabel, imageMask, factorGraphModel, trainingDataimageToFactorGraphMap,
				probabiltyContainer, this.localFeatureVector.getFeatures(), this.numberOfLocalFeatures, this.numberOfPairwiseFeatures);
	}
	
	public FeatureVector getPairwiseImageFi(SuperPixelDTO superPixel, ImageMask mask, Integer label, Integer variableLabel, FactorGraphModel factorGraph){
		return CRFUtils.getPairwiseImageFi(this.superPixelIndex, superPixel, mask, label, variableLabel, factorGraph, this.pairwiseFeatureVector.getFeatures(), this.numberOfLocalFeatures, this.numberOfPairwiseFeatures);
			
	}
	
	/*
	 * 		GETTERS & SETTERS
	 * 
	 */
	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
		updatePixelLabels();
	}
	public void updatePixelLabels() {
		if (this.label == -1) {
			_log.error("updating super pixel labels to -1 " + this.superPixelIndex);
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
		this.meanRGB = meanRGB;
		initFeatureVector();
	}
	public double[] getMeanRGB() {
		return this.meanRGB;
	}

	public FeatureVector getLocalFeatureVector() {
		return localFeatureVector;
	}
	public FeatureVector getPairwiseFeatureVector() {
		return pairwiseFeatureVector;
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
	
	private static Logger _log = Logger.getLogger(SuperPixelDTO.class);

}
