package masters.image;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;

import masters.Constants;
import masters.Constants.State;
import masters.factorisation.Factor;
import masters.factorisation.FactorGraphModel;
import masters.features.BinaryMask;
import masters.features.Continous3DFeature;
import masters.features.ContinousFeature;
import masters.features.DiscreteFeature;
import masters.features.DiscretePositionFeature;
import masters.features.Feature;
import masters.features.FeatureContainer;
import masters.features.ValueDoubleMask;
import masters.features.ValueStringMask;
import masters.grid.GridHelper;
import masters.superpixel.SuperPixelDTO;
import masters.superpixel.SuperPixelHelper;
import masters.utils.DataHelper;
import masters.utils.ParametersContainer;

public class ImageDTO implements Cloneable, Serializable {
  private static final long serialVersionUID = 4107635746274497378L;
  
  	private String path;
	private int width = 0;
	private int height = 0;
	private PixelDTO[][] pixelData;
	private List<SuperPixelDTO> superPixels;
	private final BufferedImage img;
	private ImageMask mask = null;
	
	private Map<Feature, BinaryMask> discreteFeatureMap;
	private Map<Feature, ValueDoubleMask> continuousFeatureMap;
	private Map<Feature, ValueStringMask> discretePositionFeatureMap;
	private Map<Integer, BinaryMask> labelMap;
	
	private Map<Feature, Double> betaMap;
	
	
	public ImageDTO(String path, int width, int height, PixelDTO[][] pixelData, BufferedImage img, BufferedImage segmentedImage, State state, ParametersContainer parameterContainer) {
		super();
		this.path = path;
		this.width = width;
		this.height = height;
		this.pixelData = pixelData;
		this.img = DataHelper.cloneBufferedImage(img);
		this.pixelData = getPixelDTOs(img, false);
		if (segmentedImage != null) {
			PixelDTO[][] segmentedPixelData = getPixelDTOs(segmentedImage, true);
			updateLabelFromSegmentedImage(segmentedPixelData);
		}
		prepareSuperPixels(state);
		prepareFeatureMasks();
		if (parameterContainer.getSelectedFeatureIds() != null) {
			updateSelectedFeatures(parameterContainer.getSelectedFeatureIds());
		}
	}
	public ImageDTO(String path, int width, int height, BufferedImage img, BufferedImage segmentedImage, State state, ParametersContainer parameterContainer) {
		super();
		this.path = path;
		this.width = width;
		this.height = height;
		this.img = DataHelper.cloneBufferedImage(img);;
		this.pixelData = getPixelDTOs(img, false);
		if (segmentedImage != null) {
			PixelDTO[][] segmentedPixelData = getPixelDTOs(segmentedImage, true);
			updateLabelFromSegmentedImage(segmentedPixelData);
		}
		prepareSuperPixels(state);
		prepareFeatureMasks();
		if (parameterContainer.getSelectedFeatureIds() != null) {
			updateSelectedFeatures(parameterContainer.getSelectedFeatureIds());
		}
	}
	
	private void prepareSuperPixels(State state) {
		if (Constants.CLEAR_CACHE) {
			this.superPixels = SuperPixelHelper.getNewSuperPixels(this, Constants.NUMBER_OF_SUPERPIXELS, Constants.RIGIDNESS, state.toString(), true);
		} else {
			try {
				this.superPixels = SuperPixelHelper.getSuperPixelsCached(this, state.toString());
			} catch (Exception e) {
				this.superPixels = SuperPixelHelper.getNewSuperPixels(this, Constants.NUMBER_OF_SUPERPIXELS, Constants.RIGIDNESS, state.toString(), true);
			}
		}
		
		int meanDistance = GridHelper.getMeanSuperPixelDistance(this.superPixels);
		for (SuperPixelDTO superPixel : this.superPixels) {
			superPixel.initFeatureVector(meanDistance, this.superPixels, this);
		}
		if (state != State.TEST) {
			SuperPixelHelper.updateSuperPixelLabels(this.superPixels);
		}
	}
	private void prepareFeatureMasks() {
		int numberOfSuperPixels = this.superPixels.size();

		// prepare binary masks for features and labels
		this.discreteFeatureMap = new HashMap<Feature, BinaryMask>();
		this.continuousFeatureMap = new HashMap<Feature, ValueDoubleMask>();
		this.discretePositionFeatureMap = new HashMap<Feature, ValueStringMask>();
		this.betaMap = new HashMap<Feature, Double>();
		this.labelMap = initLabelToBinaryMaskMap(numberOfSuperPixels);
		
		
		for (SuperPixelDTO superPixel : superPixels) {
			// prepare label map
			BinaryMask labelMask = labelMap.get(superPixel.getLabel());
			labelMask.switchOnByte(superPixel.getSuperPixelIndex());
			
			if (Constants.USE_NON_LINEAR_MODEL) {
				//prepare feature masks
				List<Feature> nonLinearFeatures = superPixel.getLocalFeatureVector().getFeatures();
        
				List<Feature> features = new ArrayList<Feature>();
				for (Feature feature : nonLinearFeatures) {
					if (feature instanceof FeatureContainer) {
						features.addAll(((FeatureContainer)feature).getFeatures());
					} else {
						features.add(feature);
					}
				}
				
				for (Feature feature : features) {
					if (feature instanceof DiscreteFeature){
						if (!discreteFeatureMap.containsKey(feature)) {
							discreteFeatureMap.put(feature, new BinaryMask(numberOfSuperPixels));
						}
						BinaryMask featureMask = discreteFeatureMap.get(feature);
						featureMask.switchOnByte(superPixel.getSuperPixelIndex());
					} else if (feature instanceof DiscretePositionFeature) {
						if (!discretePositionFeatureMap.containsKey(feature)) {
							discretePositionFeatureMap.put(feature, new ValueStringMask(numberOfSuperPixels));
						}
						ValueStringMask featureMask = discretePositionFeatureMap.get(feature);
						featureMask.setValue(superPixel.getSuperPixelIndex(), (String)feature.getValue());
					} else if (feature instanceof ContinousFeature) {
						if (!continuousFeatureMap.containsKey(feature)) {
							continuousFeatureMap.put(feature, new ValueDoubleMask(numberOfSuperPixels));
						}
						ValueDoubleMask featureMask = continuousFeatureMap.get(feature);
						featureMask.setValue(superPixel.getSuperPixelIndex(), (Double)feature.getValue());
					} else if(feature instanceof Continous3DFeature) {
						Continous3DFeature feature3D = (Continous3DFeature) feature;
						List<Feature> continuousFeatures = feature3D.getFeatures();
						for (Feature continuousFeature : continuousFeatures) {
							if (!continuousFeatureMap.containsKey(continuousFeature)) {
								continuousFeatureMap.put(continuousFeature, new ValueDoubleMask(numberOfSuperPixels));
							}
							ValueDoubleMask featureMask = continuousFeatureMap.get(continuousFeature);
							featureMask.setValue(superPixel.getSuperPixelIndex(), (Double)continuousFeature.getValue());
						}
						
					}
				}
			}
			
		}
		
	}
	
	private Map<Integer, BinaryMask> initLabelToBinaryMaskMap(int numberOfSuperPixels) {
		Map<Integer, BinaryMask> map = new HashMap<Integer, BinaryMask> ();
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			map.put(label, new BinaryMask(numberOfSuperPixels));
		}
		return map;
	}
	
	public ImageMask getImageMask() {
		if (this.mask == null) {
			List<Integer> mask = new ArrayList<Integer>();
			for (SuperPixelDTO superPixel : this.superPixels) {
				mask.add(superPixel.getLabel());
			}
			this.mask =  new ImageMask(mask);
		}
		return this.mask;
	}
	
	public Double getBeta(Feature feature) {
		Double beta = this.betaMap.get(feature);
		return (beta == null) ? getBetaImageValue(feature) : beta;
	}
	
	private Double getBetaImageValue(Feature feature) {
		int featureIndex = feature.getFeatureIndex();
		double betaSum = 0;
		int numberOfPairs = 0;
		
		Set<String> usedSuperPixelPairs = new HashSet<String>();
		
		for (SuperPixelDTO superPixel: this.superPixels) {
			List<SuperPixelDTO> neigbouringSuperPixels = superPixel.getNeigbouringSuperPixels();
			for (SuperPixelDTO neigbour : neigbouringSuperPixels) {
				String id = superPixel.getSuperPixelIndex() + "_" + neigbour.getSuperPixelIndex();
				String id2 = neigbour.getSuperPixelIndex() + "_" +  superPixel.getSuperPixelIndex();
				
				if (!usedSuperPixelPairs.contains(id)) {
					usedSuperPixelPairs.add(id);
					usedSuperPixelPairs.add(id2);
					
					Feature leftFeature = superPixel.getPairwiseFeatureVector().getFeatures().get(featureIndex);
					Feature rightFeature = neigbour.getPairwiseFeatureVector().getFeatures().get(featureIndex);
					
					
					betaSum += Math.pow(leftFeature.getDifference(rightFeature), 2);
					numberOfPairs++;
					
				}
			}
		}
		double betaValue = betaSum / numberOfPairs;
		
		betaValue *= 2.0;
		if (betaValue == 0) betaValue = 1;
		else betaValue = 1.0 / betaValue;
		
		
		this.betaMap.put(feature, betaValue);
		
		return betaValue;
	}
	
	public SuperPixelDTO getSuperPixel(int superPixelIndex) {
		return this.superPixels.get(superPixelIndex);
	}
	
	public List<SuperPixelDTO> getSuperPixels() {
		return superPixels;
	}
	
	public String getPath() {
		return path;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public BufferedImage getImage() {
		return this.img;
	}
	
	public PixelDTO[][] getPixelData() {
		return pixelData;
	}
	
	public List<PixelDTO> getAdjacentPixels(int x, int y) {
		List<PixelDTO> adjacentPixels = new ArrayList<PixelDTO>();
		// left pixel (y the same, x - 1)
		if (x-1 >= 0) {
			adjacentPixels.add(pixelData[x-1][y]);
		}
		// right pixel (y the same, x + 1)
		if (x+1 < this.width) {
			adjacentPixels.add(pixelData[x+1][y]);
		}
		// top pixel (y + 1, x the same)
		if (y+1 < this.height) {
			adjacentPixels.add(pixelData[x][y+1]);
		}
		// bottom pixel (y - 1, x the same)
		if (y-1 >= 0) {
			adjacentPixels.add(pixelData[x][y-1]);
		}
		return adjacentPixels;
	}
	
	private PixelDTO[][] getPixelDTOs (BufferedImage img, boolean isSegmented) {
		Set<String> col = new HashSet<String>();
		int[] tmpArray = null;
		WritableRaster rasterImage = img.getRaster();
		int width = img.getWidth();
		int height = img.getHeight();
		PixelDTO[][] pixelArray = new PixelDTO[width][height];
		for (int y = 0; y < height; y++) {
			  for (int x = 0; x < width; x++ ) {
				  int[] pixelData = rasterImage.getPixel(x, y, tmpArray);
				  
				  int  r = pixelData[0];
				  int  g = pixelData[1];
				  int  b = pixelData[2];
				  int alpha = 0;
				  PixelDTO pixel;
				  if (isSegmented) {
					  int label = -1;
					  String hexColor = String.format("#%02x%02x%02x", r, g, b);
					  if (!col.contains(hexColor)) {
						  col.add(hexColor);
					  }
					  for (int i = 0; i < Constants.SEGMENTED_HEX_COLOURS.size(); i++) {
						  String segmentedHexColour = Constants.SEGMENTED_HEX_COLOURS.get(i);
						  if (hexColor.equals(segmentedHexColour)) {
							  label = i;
							  break;
						  }
					  }
					  if (label == -1) {
						  _log.error("getPixelDTOs: chosen label -1 for hex colour " + hexColor);
						  throw new RuntimeErrorException(null);
					  }
					  pixel = new PixelDTO(x, y, r, g, b, alpha, label);
				  } else {
					  pixel = new PixelDTO(x, y, r, g, b, alpha, null);
				  }
				  
				  pixelArray[x][y] = pixel;
			  }
		  }
		return pixelArray;
	}
	private void updateLabelFromSegmentedImage(PixelDTO[][] segmentedPixelData) {
		PixelDTO[][] pixelData = this.pixelData;
		for (int i = 0; i < pixelData[0].length; i++) {
			for (int j = 0; j < pixelData.length; j++) {
				PixelDTO pixel = pixelData[j][i];
				PixelDTO segmentedPixel = segmentedPixelData[j][i];
				int label = segmentedPixel.getLabel();
				pixel.setLabel(label);
			}
		}
	}
	public void updateSelectedFeatures(List<Integer> selectedFeatureIds) {
		Set<Integer> selectedFeatureIdsSet = new HashSet<>(selectedFeatureIds);
		
		for (SuperPixelDTO sp : this.superPixels) {
			sp.updateSelectedFeatures(selectedFeatureIdsSet);
		}
	}
	
	
	public Map<Feature, BinaryMask> getFeatureMap() {
		return discreteFeatureMap;
	}

	public Map<Feature, ValueDoubleMask> getContinuousFeatureMap() {
		return continuousFeatureMap;
	}

	public Map<Integer, BinaryMask> getLabelMap() {
		return labelMap;
	}
	public BinaryMask getLabelBinaryMask(Integer label) {
		return this.labelMap.get(label);
	}
	public BinaryMask getDiscreteFeatureBinaryMask(Feature feature) {
		return this.discreteFeatureMap.get(feature);
	}
	
	public ValueDoubleMask getContinuousFeatureValueMask(Feature feature) {
		return this.continuousFeatureMap.get(feature);
	}
	
	public ValueStringMask getDiscretePositionFeatureValueMask(Feature feature) {
		return this.discretePositionFeatureMap.get(feature);
	}
	
	private String getFileName(String path) {
		try {
			return (new File(path)).getName();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuffer bf = new StringBuffer();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				bf.append(pixelData[x][y] + " ");
			}
			bf.append(System.getProperty("line.separator"));
		}
		return bf.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String fileName = getFileName(path);
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
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
		ImageDTO other = (ImageDTO) obj;
		String fileName = getFileName(path);
		String otherFileName = getFileName(other.path);
		
		if (fileName == null) {
			if (otherFileName != null)
				return false;
		} else if (!fileName.equals(otherFileName))
			return false;
		return true;
	}

	private static transient Logger _log = Logger.getLogger(ImageDTO.class);
	
}
