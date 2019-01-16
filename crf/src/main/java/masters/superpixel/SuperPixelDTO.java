package masters.superpixel;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.colors.ColorSpaceConverter;
import masters.colors.ColorSpaceException;
import masters.factorisation.FactorGraphModel;
import masters.features.Continous3DFeature;
import masters.features.ContinousFeature;
import masters.features.DiscreteFeature;
import masters.features.DiscretePositionFeature;
import masters.features.Feature;
import masters.features.FeatureContainer;
import masters.features.VectorFeature;
import masters.grid.GridHelper;
import masters.grid.GridOutOfBoundsException;
import masters.grid.GridPoint;
import masters.image.ImageDTO;
import masters.image.ImageMask;
import masters.image.PixelDTO;
import masters.train.FeatureVector;
import masters.utils.CRFUtils;
import masters.utils.Helper;
import masters.utils.ParametersContainer;

public class SuperPixelDTO implements Comparable<SuperPixelDTO>, Serializable {
  private static final long serialVersionUID = -7965602668333570635L;
  
  	private FeatureVector localFeatureVector = null;
	private FeatureVector pairwiseFeatureVector = null;
	private double[] meanRGB;	
	private int label;
	
	private int identifingColorRGB;
	private List<PixelDTO> pixels = new ArrayList<PixelDTO>();
	private List<PixelDTO> borderPixels = new ArrayList<PixelDTO>();
	private int superPixelIndex;
	
	private List<Integer> neighboursIndexes;
	private List<SuperPixelDTO> neigbouringSuperPixels;
	
	private GridPoint samplePixel = null;
	
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
	
	public boolean wereFeaturesInitiated () {
		if (this.localFeatureVector == null && this.pairwiseFeatureVector == null) {
			return false;
		}
		return true;
	}
	public void initFeatureVector(int meanSuperPixelDistance, List<SuperPixelDTO> superPixels, ImageDTO image)  {
		List<Feature> localFeatures = new ArrayList<Feature>();
		List<Feature> pairwiseFeatures = new ArrayList<Feature>();
		
		if (Constants.USE_NON_LINEAR_MODEL) {
		  List<Feature> tmpLocalFeatures = new ArrayList<Feature>();
		  tmpLocalFeatures.addAll(initLocalFeatures(this.meanRGB,meanSuperPixelDistance, superPixels, image));
		  Feature featureContainer = new FeatureContainer(tmpLocalFeatures, 0);
		  localFeatures.add(featureContainer);

		  pairwiseFeatures.addAll(initPairwiseFeatures(this.meanRGB));
			
		} else {
			localFeatures.addAll(getColourFeatures(this.meanRGB, 0));
			
		}
		this.localFeatureVector = new FeatureVector(localFeatures, true);
		this.pairwiseFeatureVector = new FeatureVector(pairwiseFeatures, true);
		
	}
	public void initMeanRGB() {
		try {
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
		} catch (ColorSpaceException e) {
			throw new RuntimeException(e);
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
		
		if (Constants.USE_GRID_MODEL) {
			Map<Color, Integer> colourCounter = new HashMap<Color, Integer>();
			int previousValue;
			for (PixelDTO pixel : pixels) {
				int r = pixel.getR();
				int g = pixel.getG();
				int b = pixel.getB();
				
				Color colour = new Color(r, g, b);
				if (!colourCounter.containsKey(colour)) {
					colourCounter.put(colour, 0);
				}
				previousValue = colourCounter.get(colour);
				colourCounter.put(colour, (++previousValue));
			}
			Color maxKey = new Color(0,0,0);
			int maxValue = 0;
			for (Color key : colourCounter.keySet()) {
				if (colourCounter.get(key) > maxValue) {
					maxKey = key;
					maxValue = colourCounter.get(key);
				}
			}
			return new double [] {maxKey.getRed(), maxKey.getGreen(), maxKey.getBlue()};
		}
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
	
	private List<Feature> initBayesFeatures(double rgb[], int startingIndex) {
		List<Feature> features = new ArrayList<Feature> ();
		
		String hex = Helper.getColorHex(rgb);
		Feature colorFeature = new DiscreteFeature(hex, startingIndex++);
		
		features.add(colorFeature);
		return features;
	}
	private List<Feature> initLocalFeatures(double [] rgb, int meanSuperPixelDistance, List<SuperPixelDTO> superPixels, ImageDTO image) {
		List<Feature> features = new ArrayList<Feature> ();
		if (Constants.USE_GRID_MODEL) {
			int pivotPosition = 0;
			List<Feature> neighbourPercentageFeatures = getNeighbourPercentageFeatures(rgb, features.size(), meanSuperPixelDistance, superPixels, image);
			if (Constants.ADD_NEIGBOUR_FEATURES) {
				features.addAll(neighbourPercentageFeatures);
			} else {
				pivotPosition = neighbourPercentageFeatures.get(neighbourPercentageFeatures.size() - 1).getFeatureIndex() + 1;
			}
			if (Constants.ADD_COLOUR_LOCAL_FEATURE) {
				if (Constants.ADD_COLOUR_LOCAL_FEATURE_WITH_POSITION) {
					List<GridPoint> grid = GridHelper.getGrid(this, meanSuperPixelDistance);
					PixelDTO[][] pixelData = image.getPixelData();
					int iter = 0;
					for (GridPoint point : grid) {
						try {
							int gridIndex = GridHelper.getGridPointSuperPixelIndex(point, pixelData);
							SuperPixelDTO gridSuperPixel = superPixels.get(gridIndex);
							String baseHex = String.format("#%02x%02x%02x", (int)gridSuperPixel.meanRGB[0], (int)gridSuperPixel.meanRGB[1], (int)gridSuperPixel.meanRGB[2]);
							features.add(new DiscretePositionFeature(baseHex, pivotPosition + features.size(), iter));
						} catch (GridOutOfBoundsException e) {
							// assign null
							features.add(new DiscretePositionFeature(null, pivotPosition + features.size(), iter));
						}
						iter++;
					}
				} else {
					pivotPosition += GridHelper.getGrid(this, meanSuperPixelDistance).size();
					features.add(getDiscreteColourFeature(pivotPosition));
				}
				
			}
		} else {
			features.addAll(getColourFeatures(rgb, features.size()));
			features.add(getNeighbourBayesColourFeature(features.size()));
		}

		
		return features;
	}
	private List<Feature> initPairwiseFeatures(double [] rgb) {
		List<Feature> features = new ArrayList<Feature> ();
		features.add(new VectorFeature(getColour(rgb, features.size()), features.size()));
		return features;
	}
	
	private Feature getDiscreteColourFeature(int startingIndex) { 
		String baseHex = String.format("#%02x%02x%02x", (int)this.meanRGB[0], (int)this.meanRGB[1], (int)this.meanRGB[2]);
		for (Color color : Constants.AVAILABLE_COLOURS_SET){
			String colorHex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
			if (baseHex.equals(colorHex)) {
				return new DiscreteFeature(baseHex, startingIndex);
			}
			startingIndex++;
		}
		_log.error("Colour " + baseHex + " not available.");
		throw new RuntimeException();
		

	}
	
	private Feature getDiscreteColourFeature(int startingIndex, SuperPixelDTO superpixel) { 
		String baseHex = String.format("#%02x%02x%02x", (int)superpixel.meanRGB[0], (int)superpixel.meanRGB[1], (int)superpixel.meanRGB[2]);
		for (Color color : Constants.AVAILABLE_COLOURS_SET){
			String colorHex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
			if (baseHex.equals(colorHex)) {
				return new DiscreteFeature(baseHex, startingIndex);
			}
			startingIndex++;
		}
		_log.error("Colour " + baseHex + " not available.");
		throw new RuntimeException();
		

	}
	private Feature getNeighbourBayesColourFeature(int startingIndex) { 
		//most popular colour of neigbours
		String baseHex = String.format("#%02x%02x%02x", (int)this.meanRGB[0], (int)this.meanRGB[1], (int)this.meanRGB[2]);

		Map<String, Integer> hexToCounterMap = new HashMap<String, Integer>();
		hexToCounterMap = getNeigbourBayesColourCount(5, hexToCounterMap, this, baseHex);
	  
		String mostPopularHex = "";
		int maxCounter = 0;
		for (String hex : hexToCounterMap.keySet()) {
			Integer counter = hexToCounterMap.get(hex);
			if (counter > maxCounter) {
				mostPopularHex = hex;
				maxCounter = counter;
			}
		}
		Feature colorFeature = new DiscreteFeature(mostPopularHex, startingIndex++);
	  
		return colorFeature;
	}
	
	private Map<String, Integer> getNeigbourBayesColourCount(int level, Map<String, Integer> hexToCounterMap, SuperPixelDTO superPixel, String baseHex) {
		if (level < 1) return hexToCounterMap;
	  
		for (SuperPixelDTO neighbour : superPixel.neigbouringSuperPixels) {
			double[] rgb = neighbour.getMeanRGB();
			String hex = Helper.getColorHex(rgb);
			if (hex.equals(baseHex)) {
				getNeigbourBayesColourCount((level-1), hexToCounterMap, neighbour, baseHex);
			} else {
				if (hexToCounterMap.containsKey(hex)) {
					int counter = hexToCounterMap.get(hex) + level;
					hexToCounterMap.put(hex, counter);
				} else {
					hexToCounterMap.put(hex, level);
				}
			}
		}
		return hexToCounterMap;
	}



	private Double[] getNeighboursMeanRGB() {
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
		
		return meanRGB;
	}
	
	private List<Feature> getColourFeatures(double [] rgb, int startingIndex) {
		List<Feature> features = new ArrayList<Feature> ();
		List<Double> featureValues = getColour(rgb, startingIndex);
		for (int i = 0; i < featureValues.size(); i++) {
			features.add(new ContinousFeature(featureValues.get(i), i + startingIndex));
		}
		return features;
		
	}

	private List<Feature> getNeighbourPercentageFeatures(double [] rgb, int startingIndex, int meanSuperPixelDistance, List<SuperPixelDTO> superPixelList, ImageDTO image) {
		List<Feature> features = new ArrayList<Feature> ();
		
		PixelDTO[][] pixelData = image.getPixelData();
		
		List<GridPoint> grid = GridHelper.getGrid(this, meanSuperPixelDistance);
		
		for (GridPoint point : grid) {
			Map<Color, Integer> colorMap = getColourCounterMap();
			
			try {
				int gridIndex = GridHelper.getGridPointSuperPixelIndex(point, pixelData); 
				List<SuperPixelDTO> superPixelNeighbours = Arrays.asList(new SuperPixelDTO[]{superPixelList.get(gridIndex)});
				
				for (int level = 0; level < Constants.NEIGHBOURHOOD_SIZE; level++) {
					superPixelNeighbours = SuperPixelHelper.getListNeighbours(superPixelNeighbours);
				}
				Set<SuperPixelDTO> neigbouringPixelSet = new HashSet<SuperPixelDTO>();
				neigbouringPixelSet.addAll(superPixelNeighbours);
				int numberOfNeighbours = neigbouringPixelSet.size();
				
				for (SuperPixelDTO neighbour : neigbouringPixelSet) {
					double[] meanRGB = neighbour.getMeanRGB();
					Color color = new Color((int)meanRGB[0], (int)meanRGB[1], (int)meanRGB[2]);
					if (!colorMap.containsKey(color)) {
						_log.error("Color " + color + " should not be present in data set");
						throw new RuntimeErrorException(null);
					}
					int c = (colorMap.get(color) + 1);
					colorMap.put(color, c);
				}
				Double[] colourPercentage = new Double[Constants.AVAILABLE_COLOURS.size()];
				int i = 0;
				for (Color color : Constants.AVAILABLE_COLOURS) {
					double percentage = colorMap.get(color) / (numberOfNeighbours * 1.0);
					colourPercentage[i] = percentage;
					i++;
				}
				if (Constants.USE_HISTOGRAMS_3D) {
					features.add(new Continous3DFeature(colourPercentage, startingIndex));
					startingIndex += 3;
				} else {
					for (int j = 0; j < colourPercentage.length; j++) {
						features.add(new ContinousFeature(colourPercentage[j], startingIndex++));
					}
					
				}
			} catch (GridOutOfBoundsException e) {
				if (Constants.USE_HISTOGRAMS_3D) {
					features.add(new Continous3DFeature(new Double[]{null, null, null}, startingIndex)); 
					startingIndex += 3;
				} else {
					for (int j = 0; j < Constants.AVAILABLE_COLOURS.size(); j++) {
						features.add(new ContinousFeature(null, startingIndex++));
					}
				}
			}
		}
		return features;
	}
	
	private Map<Color, Integer> getColourCounterMap() {
		Map<Color, Integer> colorMap = new HashMap<Color, Integer>();
		for (Color color : Constants.AVAILABLE_COLOURS) {
			colorMap.put(color, 0);
		}
		return colorMap;
		
	}
	private List<Double> getColour(double [] rgb, int startingIndex) {
		try {
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
		} catch (ColorSpaceException e) {
			throw new RuntimeException(e);
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
		List<Double> featureValues = Helper.initFixedSizedListDouble(3 * Constants.COLOR_FEATURE_HISTOGRAM_DIVISIONS);
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
		
		if (numberOfColorIndexes % Constants.COLOR_FEATURE_HISTOGRAM_DIVISIONS != 0) {
			_log.error(numberOfColorIndexes + " has to be divisable by the number of histogram divisions");
			throw new RuntimeErrorException(null);
		}
		
		int blockSize = numberOfColorIndexes / Constants.COLOR_FEATURE_HISTOGRAM_DIVISIONS;
		for (int section = 0; section < Constants.COLOR_FEATURE_HISTOGRAM_DIVISIONS; section ++) {
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
		
	public FeatureVector getLocalImageFi(Integer objectLabel, ImageMask imageMask, ImageDTO trainImage,
			List<ImageDTO> trainImageList, ParametersContainer parameterContainer) {
		
		return CRFUtils.getLocalImageFi(this.superPixelIndex, objectLabel, imageMask, trainImage, trainImageList,
				parameterContainer, this.localFeatureVector.getFeatures());
	}
	
	public FeatureVector getPairwiseImageFi(SuperPixelDTO superPixel, ImageMask mask, Integer label, Integer variableLabel, ImageDTO trainImage, ParametersContainer parametersContainer){
		return CRFUtils.getPairwiseImageFi(this.superPixelIndex, superPixel, mask, label, variableLabel, trainImage, this.pairwiseFeatureVector.getFeatures(), parametersContainer);
			
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
	public GridPoint getSamplePixel() {
		if (this.samplePixel == null) {
			this.samplePixel = getMassCentreOfSuperPixel();
		}
		return this.samplePixel;
	}
	private GridPoint getMassCentreOfSuperPixel() {
		int xSum = 0;
		int ySum = 0;
		
		for (PixelDTO pixel : this.pixels) {
			xSum += pixel.getXIndex();
			ySum += pixel.getYIndex();
		}
		int x = (int)(Math.round((xSum * 1.0 )/ this.pixels.size()));
		int y = (int)(Math.round((ySum * 1.0 )/ this.pixels.size()));
		return new GridPoint(x,y);
	}
	private GridPoint getSamplePixelInSuperPixelBounds() {
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
		return new GridPoint(x,y);
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
	
	private static transient Logger _log = Logger.getLogger(SuperPixelDTO.class);

}
