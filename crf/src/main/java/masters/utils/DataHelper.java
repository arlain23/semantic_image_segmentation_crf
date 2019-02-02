package masters.utils;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.management.RuntimeErrorException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hpsf.Array;

import masters.Constants;
import masters.Constants.State;
import masters.grid.GridPoint;
import masters.image.ImageDTO;
import masters.image.PixelDTO;
import masters.superpixel.SuperPixelDTO;
import masters.train.WeightVector;

public class DataHelper {
	private static Map<Integer,Color> LABEL_TO_COLOUR_MAP = Constants.LABEL_TO_COLOUR_MAP;

	public static String IMAGE_EXTENSION = Constants.IMAGE_EXTENSION;
	public static String RESULT_IMAGE_SUFFIX = Constants.RESULT_IMAGE_SUFFIX;

	/* getters */ 


	public static List<ImageDTO> getValidationDataSegmented(ParametersContainer parameterContainer) {
		Map<String, File> validationFiles = getFilesFromDirectory(Constants.VALIDATION_PATH);
		Map<String, File> resultFiles = getFilesFromDirectory(Constants.VALIDATION_RESULT_PATH);

		return getDataSegmented(validationFiles, resultFiles, State.VALIDATION, parameterContainer);
	}

	public static List<ImageDTO> getTrainingDataSegmented(ParametersContainer parameterContainer) {
		Map<String, File> trainingFiles = getFilesFromDirectory(Constants.TRAIN_PATH);
		Map<String, File> resultFiles = getFilesFromDirectory(Constants.TRAIN_RESULT_PATH);

		return getDataSegmented(trainingFiles, resultFiles, State.TRAIN, parameterContainer);
	}

	private static List<ImageDTO> getDataSegmented(
			Map<String, File> files, Map<String, File> resultFiles, State state, ParametersContainer parameterContainer) {
		List <ImageDTO> imageList = new ArrayList<ImageDTO>();
		int i = 0;
		for (String key : files.keySet()) {
			if (++i >= Constants.TRAIN_IMAGE_LIMIT + 1) {
				return imageList;
			}

			File trainFile = files.get(key);
			File segmentedFile = resultFiles.get(key + RESULT_IMAGE_SUFFIX);

			imageList.add(getSingleImageSegmented(trainFile, segmentedFile, state, parameterContainer));
		}
		return imageList;
	}

	public static ImageDTO getSingleImageSegmented(
			File trainFile, File segmentedFile, State state, ParametersContainer parameterContainer) {
		BufferedImage trainImg = openImage(trainFile);
		BufferedImage segmentedImg = null;
		if (segmentedFile != null) {
			segmentedImg = openImage(segmentedFile);
		}

		
		ImageDTO imageObj = new ImageDTO(trainFile.getPath(), trainImg.getWidth(),trainImg.getHeight(), trainImg, segmentedImg, state, parameterContainer);
		return imageObj;

	}




	public static List<ImageDTO> getTestData(ParametersContainer parameterContainer) {
		Map<String, File> testFiles = getFilesFromDirectory(Constants.TEST_PATH);

		List <ImageDTO> imageList = new ArrayList<ImageDTO>();
		int i = 0;
		for (String key : testFiles.keySet()) {
			if (++i >= Constants.TEST_IMAGE_LIMIT + 1) {
				break;
			}
			File trainFile = testFiles.get(key);

			BufferedImage trainImg = openImage(trainFile);

			ImageDTO imageObj = new ImageDTO(trainFile.getPath(), trainImg.getWidth(),trainImg.getHeight(), trainImg, null, State.TEST, parameterContainer);

			imageList.add(imageObj);
		}
		return imageList;
	}

	public static Map<String,File> getFilesFromDirectory(String fullpath, boolean flag) {
		File folder;
		folder = new File(fullpath);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles == null) {
			_log.error("path " + folder.getAbsolutePath() + " has no files");
			throw new RuntimeErrorException(null);
		}
		Map<String,File> result = new HashMap<String, File>();

		for (int i = 0; i < listOfFiles.length; i++) {
			File file = listOfFiles[i];
			result.put(FilenameUtils.removeExtension(file.getName()), file);
		}
		return result;

	}
	public static Map<String,File> getFilesFromDirectory(String path) {
		File folder;
		try {
			folder = new File(DataHelper.class.getClassLoader().getResource(path).toURI());
			File[] listOfFiles = folder.listFiles();
			if (listOfFiles == null) {
				_log.error("path " + folder.getAbsolutePath() + " has no files");
				throw new RuntimeErrorException(null);
			}
			Map<String,File> result = new HashMap<String, File>();

			for (int i = 0; i < listOfFiles.length; i++) {
				File file = listOfFiles[i];
				result.put(FilenameUtils.removeExtension(file.getName()), file);
			}
			return result;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeErrorException(null);
		}

	}

	public static BufferedImage openImage(File file) {
		BufferedImage img;
		try {
			img = ImageIO.read(file);
			return img;
		} catch(IOException e) {
			return null;
		}
	}




	/* viewers */

	public static void viewImageSegmented(ImageDTO image) {
		viewImageSegmented(image, "");
	}
	public static void viewImageSegmented(ImageDTO image, String title) {
		/* change image pixel data */
		BufferedImage img = image.getImage();

		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				try {
					int value = LABEL_TO_COLOUR_MAP.get(image.getPixelData()[x][y].getLabel()).getRGB();
					img.setRGB(x, y, value);
				} catch (NullPointerException e) {
					_log.error("null", e);
					System.out.println("image.pixelData[x][y].getLabel()" + image.getPixelData()[x][y].getLabel());
					throw new RuntimeErrorException(null);
				}
			}
		}
		showImageInJframe(title, img);
	}
	public static void viewImage(ImageDTO image) {
		viewImage(image, "");
	}
	public static void viewImage(ImageDTO image, String title) {
		BufferedImage img = cloneBufferedImage(image.getImage());
		showImageInJframe(title, img);
	}
	public static void viewImage(BufferedImage img, String title) {
		showImageInJframe(title, img);
	}


	public static void viewImageSegmentedSuperPixels(ImageDTO image, List<SuperPixelDTO> superPixels, String title) {
		for (SuperPixelDTO superPixel : superPixels) {
			superPixel.updatePixelLabels();
		}
		viewImageSegmented(image, title);
	}

	public static void viewImageSegmentedSuperPixels(ImageDTO image, List<SuperPixelDTO> superPixels, List<Integer> mask) {
		for (SuperPixelDTO superPixel : superPixels) {
			superPixel.updatePixelLabels(mask);
		}
		viewImageSegmented(image);
	}



	public static void viewImageSuperpixelBordersOnly(ImageDTO image, List<SuperPixelDTO> superPixels) {
		viewImageSuperpixelBordersOnly(image, superPixels, "");
	}
	public static void viewImageSuperpixelBordersOnly(ImageDTO image, List<SuperPixelDTO> superPixels, String title) {
		BufferedImage img = image.getImage();
		for (SuperPixelDTO superPixel : superPixels) {
			int rgbSuperPixel = superPixel.getIdentifingColorRGB();
			List<PixelDTO> borderPixels = superPixel.getBorderPixels();
			for (PixelDTO borderPixel : borderPixels) {
				img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
			}
		}
		showImageInJframe(title, img);
	}

	public static void viewImageSuperpixelMeanData(ImageDTO image, List<SuperPixelDTO> superPixels) {
		viewImageSuperpixelMeanData(image, superPixels, "");
	}
	public static void viewImageSuperpixelMeanData(ImageDTO image, List<SuperPixelDTO> superPixels, String title) {
		/* change image pixel data */
		BufferedImage img = image.getImage();
		for (SuperPixelDTO sp : superPixels) {
			double[] rgbArray = sp.getMeanRGB();
			int rgb = new Color((int)rgbArray[0],(int)rgbArray[1], (int)rgbArray[2]).getRGB();
			List<PixelDTO> pixels = sp.getPixels();
			for (PixelDTO p : pixels) {
				img.setRGB(p.getXIndex(), p.getYIndex(), rgb);
			}
		}

		showImageInJframe(title, img);
	}

	public static void viewImageWithSuperPixelsIndex(ImageDTO image, List<SuperPixelDTO> superPixels, String title) {
		BufferedImage img = cloneBufferedImage(image.getImage());

		/* update superpixel index */
		Graphics graphics = img.getGraphics();
		graphics.setFont(new Font("Arial Black", Font.BOLD, 6));

		/* update borders */
		for (SuperPixelDTO superPixel : superPixels) {
			int rgbSuperPixel = superPixel.getIdentifingColorRGB();
			List<PixelDTO> borderPixels = superPixel.getBorderPixels();
			for (PixelDTO borderPixel : borderPixels) {
				img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
			}
		}

		for (SuperPixelDTO superPixel : superPixels) {
			GridPoint middle = superPixel.getSamplePixel();
			Color myColor =  new Color(superPixel.getIdentifingColorRGB());
			graphics.setColor(myColor);
			graphics.drawString(String.valueOf(superPixel.getSuperPixelIndex()), middle.x, middle.y);
		}
		showImageInJframe(title, img);
	}


	public static String getFileNameFromImageDTO(ImageDTO image) {
		File imageFile = new File(image.getPath());
		String name = imageFile.getName();
		name = name.replaceAll("." + Constants.IMAGE_EXTENSION, "");
		return name;
	}

	/* savers */

	public static void saveImageSuperPixelIdentifingColor(ImageDTO image, List<SuperPixelDTO> superPixels, String path) {
		try {
			File outputfile = new File(path);
			outputfile.getParentFile().mkdirs();
			outputfile.createNewFile();

			/* change image pixel data */
			BufferedImage img = image.getImage();

			for (SuperPixelDTO superPixel : superPixels) {
				double rgb[] = superPixel.getMeanRGB();
				Color color = new Color((int)rgb[0], (int)rgb[1], (int)rgb[2]);
				int value = Constants.COLOR_TO_MARKING_COLOUR_MAP.get(color).getRGB();

				List<PixelDTO> pixels = superPixel.getPixels();
				for (PixelDTO pixel : pixels) {
					img.setRGB(pixel.getXIndex(), pixel.getYIndex(), value);
				}
			}
			/* update borders */
			for (SuperPixelDTO superPixel : superPixels) {
				int rgbSuperPixel = superPixel.getIdentifingColorRGB();
				List<PixelDTO> borderPixels = superPixel.getBorderPixels();
				for (PixelDTO borderPixel : borderPixels) {
					img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
				}
			}


			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveImageSuperpixelsMeanColour(ImageDTO image, List<SuperPixelDTO> superPixels, String path) {
		BufferedImage img = cloneBufferedImage(image.getImage());
		for (SuperPixelDTO superPixel : superPixels) {
			double[] meanRGB = superPixel.getMeanRGB();
			int pixelColor = (new Color((int)meanRGB[0], (int)meanRGB[1], (int)meanRGB[2])).getRGB();
			List<PixelDTO> allPixels = superPixel.getPixels();
			for (PixelDTO pixel : allPixels) {
				img.setRGB(pixel.getXIndex(), pixel.getYIndex(), pixelColor);
			}
		}
		File outputFile = new File(path);
		outputFile.getParentFile().mkdirs();
		try {
			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void saveImageSuperpixelBordersOnly(ImageDTO image, List<SuperPixelDTO> superPixels, String path) {
		BufferedImage img = cloneBufferedImage(image.getImage());
		for (SuperPixelDTO superPixel : superPixels) {
			int rgbSuperPixel = superPixel.getIdentifingColorRGB();
			List<PixelDTO> allPixels = superPixel.getPixels();
			for (PixelDTO pixel : allPixels) {
				int pixelColor = (new Color(pixel.getR(), pixel.getG(), pixel.getB())).getRGB();
				img.setRGB(pixel.getXIndex(), pixel.getYIndex(), pixelColor);
			}
			
			List<PixelDTO> borderPixels = superPixel.getBorderPixels();
			for (PixelDTO borderPixel : borderPixels) {
				img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
			}
		}
		File outputFile = new File(path);
		outputFile.getParentFile().mkdirs();
		try {
			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void saveImageSuperpixels(ImageDTO image, List<SuperPixelDTO> superPixels, String path) {
		BufferedImage img = cloneBufferedImage(image.getImage());
		for (SuperPixelDTO superPixel : superPixels) {
			int rgbSuperPixel = superPixel.getIdentifingColorRGB();
			List<PixelDTO> allPixels = superPixel.getPixels();
			for (PixelDTO pixel : allPixels) {
				img.setRGB(pixel.getXIndex(), pixel.getYIndex(), rgbSuperPixel);
			}
		}
		File outputFile = new File(path);
		outputFile.getParentFile().mkdirs();
		try {
			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void saveImageSuperpixelMeanData(ImageDTO image, List<SuperPixelDTO> superPixels, String path) {
		BufferedImage img = image.getImage();
		/* change image pixel data */
		for (SuperPixelDTO sp : superPixels) {
			double[] rgbArray = sp.getMeanRGB();
			int rgb = new Color((int)rgbArray[0],(int)rgbArray[1], (int)rgbArray[2]).getRGB();
			List<PixelDTO> pixels = sp.getPixels();
			for (PixelDTO p : pixels) {
				img.setRGB(p.getXIndex(), p.getYIndex(), rgb);
			}
		}

		File outputFile = new File(path);
		try {
			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveImageSegmentedSuperPixels(ImageDTO image, List<SuperPixelDTO> superPixels, String path) {
		for (SuperPixelDTO superPixel : superPixels) {
			superPixel.updatePixelLabels();
		}
		saveImageSegmentedBySuperPixels(image, superPixels, path);
	}
	public static void saveImageSegmentedBySuperPixels(ImageDTO image, List<SuperPixelDTO> superPixels, String path) {
		try {
			File outputfile = new File(path);
			outputfile.getParentFile().mkdirs();
			outputfile.createNewFile();

			/* change image pixel data */
			BufferedImage img = cloneBufferedImage(image.getImage());
			for (SuperPixelDTO superPixel : superPixels) {
				List<PixelDTO> pixels = superPixel.getPixels();
				for (PixelDTO pixel : pixels) {
					int value = LABEL_TO_COLOUR_MAP.get(pixel.getLabel()).getRGB();
					img.setRGB(pixel.getXIndex(), pixel.getYIndex(), value);
				}
			}
			/* update borders */
			for (SuperPixelDTO superPixel : superPixels) {
				int rgbSuperPixel = superPixel.getIdentifingColorRGB();
				List<PixelDTO> borderPixels = superPixel.getBorderPixels();
				for (PixelDTO borderPixel : borderPixels) {
					img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
				}
			}

			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void saveImageFi1ProbabilitiesWithMarkedSuperPixel(ImageDTO image, SuperPixelDTO superPixelToBeMarked,
			String basePath, int objectLabel,
			List<Double> superPixelProbs) {

		String filePath = basePath + "label_" + objectLabel + "." + Constants.IMAGE_EXTENSION;
		File outputfile = new File(filePath);
		outputfile.getParentFile().mkdirs();
		try {
			outputfile.createNewFile();
			BufferedImage img = cloneBufferedImage(image.getImage());
			for (SuperPixelDTO superPixel : image.getSuperPixels()) {
				double labelProbability = superPixelProbs.get(superPixel.getSuperPixelIndex());
				int gray = (int)Math.round(labelProbability * 255);
				Color colour = getRGB(gray);
				
				List<PixelDTO> pixels = superPixel.getPixels();
				for (PixelDTO pixel : pixels) {
					img.setRGB(pixel.getXIndex(), pixel.getYIndex(), colour.getRGB());


				}
			}

			
			/* update borders */
			for (SuperPixelDTO superPixel : image.getSuperPixels()) {
				int rgbSuperPixel = Color.red.getRGB();
				List<PixelDTO> borderPixels = superPixel.getBorderPixels();
				for (PixelDTO borderPixel : borderPixels) {
					img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
				}
			}
			
			// update chosen superPixel
			List<PixelDTO> borderPixels = superPixelToBeMarked.getBorderPixels();
			int red = Color.BLUE.getRGB();
			for (PixelDTO borderPixel : borderPixels) {
				img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), red);
			}

			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
		}
	}
	
	
	public static void saveSegmentationByImageFi1Probabilities(ImageDTO image, List<List<Double>> labelProbabilities,
			String basePath, boolean print) {

		List<SuperPixelDTO> superPixels = image.getSuperPixels();
		String filePath = basePath + "result." + Constants.IMAGE_EXTENSION;
		File outputfile = new File(filePath);
		outputfile.getParentFile().mkdirs();
		
		List<Double> superPixelProbs0 = labelProbabilities.get(0);
		List<Double> superPixelProbs1 = labelProbabilities.get(1);
		List<Double> superPixelProbs2 = labelProbabilities.get(2);
		List<Double> superPixelProbs3 = labelProbabilities.get(3);
		
		try {
			outputfile.createNewFile();
			BufferedImage img = cloneBufferedImage(image.getImage());
			for (SuperPixelDTO superPixel : superPixels) {
				double minProb = Integer.MAX_VALUE;
				int winningLabel = -1;

				
				double labelProbability0 = -Math.log(superPixelProbs0.get(superPixel.getSuperPixelIndex()));
				double labelProbability1 = -Math.log(superPixelProbs1.get(superPixel.getSuperPixelIndex()));
				double labelProbability2 = -Math.log(superPixelProbs2.get(superPixel.getSuperPixelIndex()));
				double labelProbability3 = -Math.log(superPixelProbs3.get(superPixel.getSuperPixelIndex()));
				
//				if (superPixel.getSuperPixelIndex() == 509 && print) {
//					System.out.println("0 -> " + labelProbability0);
//					System.out.println("1 -> " + labelProbability1);
//					System.out.println("2 -> " + labelProbability2);
//					System.out.println("3 -> " + labelProbability3);
//					
//				}
//				
				if (labelProbability0 < minProb) {
					minProb = labelProbability0;
					winningLabel = 0;
				}
				
				if (labelProbability1 < minProb) {
					minProb = labelProbability1;
					winningLabel = 1;
				}
				
				if (labelProbability2 < minProb) {
					minProb = labelProbability2;
					winningLabel = 2;
				}
				
				if (labelProbability3 < minProb) {
					minProb = labelProbability3;
					winningLabel = 3;
				}
				
				Color labelColour = Constants.LABEL_TO_COLOUR_MAP.get(winningLabel);
				int rgb = labelColour.getRGB();
				List<PixelDTO> pixels = superPixel.getPixels();
				for (PixelDTO pixel : pixels) {
					img.setRGB(pixel.getXIndex(), pixel.getYIndex(), rgb);

				}
			}

			/* update borders */
			for (SuperPixelDTO superPixel : superPixels) {
				int rgbSuperPixel = superPixel.getIdentifingColorRGB();
				List<PixelDTO> borderPixels = superPixel.getBorderPixels();
				for (PixelDTO borderPixel : borderPixels) {
					img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
				}
			}

			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
		}
	}
	public static void saveImageFi1Probabilities(ImageDTO image, List<SuperPixelDTO> superPixels,
			String basePath, int objectLabel,
			List<Double> superPixelProbs) {

		
		String filePath = basePath + "label_" + objectLabel + "." + Constants.IMAGE_EXTENSION;
		File outputfile = new File(filePath);
		outputfile.getParentFile().mkdirs();
		try {
			outputfile.createNewFile();
			BufferedImage img = cloneBufferedImage(image.getImage());
			for (SuperPixelDTO superPixel : superPixels) {
				double labelProbability = superPixelProbs.get(superPixel.getSuperPixelIndex());
				int colour = (int)Math.round(labelProbability * 255);
				Color rgb = getRGB(colour);
				colour = rgb.getRGB();
				List<PixelDTO> pixels = superPixel.getPixels();
				for (PixelDTO pixel : pixels) {
					img.setRGB(pixel.getXIndex(), pixel.getYIndex(), colour);


				}
			}

			/* update borders */
			for (SuperPixelDTO superPixel : superPixels) {
				int rgbSuperPixel = superPixel.getIdentifingColorRGB();
				List<PixelDTO> borderPixels = superPixel.getBorderPixels();
				for (PixelDTO borderPixel : borderPixels) {
					img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
				}
			}

			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
		}
	}
	public static void saveImageSegmented(ImageDTO image, List<SuperPixelDTO> superPixels, String path) {
		try {
			File outputfile = new File(path);
			outputfile.getParentFile().mkdirs();
			outputfile.createNewFile();

			/* change image pixel data */
			BufferedImage img = cloneBufferedImage(image.getImage());

			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {
					int value = LABEL_TO_COLOUR_MAP.get(image.getPixelData()[x][y].getLabel()).getRGB();
					img.setRGB(x, y, value);
				}
			}
			/* update borders */
			for (SuperPixelDTO superPixel : superPixels) {
				int rgbSuperPixel = superPixel.getIdentifingColorRGB();
				List<PixelDTO> borderPixels = superPixel.getBorderPixels();
				for (PixelDTO borderPixel : borderPixels) {
					img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
				}
			}

			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void saveImageBySuperPixelsPixelData(ImageDTO image, List<SuperPixelDTO> superPixels, String path) {
		try {
			File outputfile = new File(path);
			outputfile.getParentFile().mkdirs();
			outputfile.createNewFile();

			/* change image pixel data */
			BufferedImage img = cloneBufferedImage(image.getImage());

			for (SuperPixelDTO superPixel : superPixels) {
				List<PixelDTO> pixels = superPixel.getPixels();
				for (PixelDTO pixel : pixels) {
					int x = pixel.getXIndex();
					int y = pixel.getYIndex();

					int r = pixel.getR();
					int g = pixel.getG();
					int b = pixel.getB();

					Color color = new Color(r,g,b);

					img.setRGB(x, y, color.getRGB());
				}
			}

			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveImageWithChosenSuperPixel(ImageDTO image, SuperPixelDTO chosenSuperpixel, String path) {
		File outputfile = new File(path);
		outputfile.getParentFile().mkdirs();
		try {
			outputfile.createNewFile();
			BufferedImage img = cloneBufferedImage(image.getImage());

			// update chosen superPixel
			List<PixelDTO> pixels = chosenSuperpixel.getPixels();
			int black = Color.black.getRGB();
			for (PixelDTO pixel : pixels) {
				img.setRGB(pixel.getXIndex(), pixel.getYIndex(), black);
			}

			/* update borders */
			for (SuperPixelDTO superPixel : image.getSuperPixels()) {
				int rgbSuperPixel = superPixel.getIdentifingColorRGB();
				List<PixelDTO> borderPixels = superPixel.getBorderPixels();
				for (PixelDTO borderPixel : borderPixels) {
					img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
				}
			}


			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static void saveImageWithSuperPixelsIndex(ImageDTO image, List<SuperPixelDTO> superPixels, String path) {
		try {
			File outputfile = new File(path);
			outputfile.getParentFile().mkdirs();
			outputfile.createNewFile();
			BufferedImage img = cloneBufferedImage(image.getImage());
	
			/* update superpixel index */
			Graphics graphics = img.getGraphics();
			graphics.setFont(new Font("Arial", Font.PLAIN, 9));
	
			/* update borders */
			for (SuperPixelDTO superPixel : superPixels) {
				int rgbSuperPixel = superPixel.getIdentifingColorRGB();
				List<PixelDTO> borderPixels = superPixel.getBorderPixels();
				for (PixelDTO borderPixel : borderPixels) {
					img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
				}
			}
	
			for (SuperPixelDTO superPixel : superPixels) {
				GridPoint middle = superPixel.getSamplePixel();
				Color myColor =  new Color(superPixel.getIdentifingColorRGB());
				graphics.setColor(myColor);
				graphics.drawString(String.valueOf(superPixel.getSuperPixelIndex()), middle.x, middle.y);
			}
			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void saveImageSegmentedWithMask(ImageDTO image, List<SuperPixelDTO> superPixels, List<Integer> mask, String path) {
		try {
			File outputfile = new File(path);
			outputfile.createNewFile();

			for (SuperPixelDTO superPixel : superPixels) {
				superPixel.updatePixelLabels(mask);
			}
			/* change image pixel data */
			BufferedImage img = image.getImage();

			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {
					int value = LABEL_TO_COLOUR_MAP.get(image.getPixelData()[x][y].getLabel()).getRGB();
					img.setRGB(x, y, value);
				}
			}
			/* update borders */
			for (SuperPixelDTO superPixel : superPixels) {
				int rgbSuperPixel = superPixel.getIdentifingColorRGB();
				List<PixelDTO> borderPixels = superPixel.getBorderPixels();
				for (PixelDTO borderPixel : borderPixels) {
					img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
				}
			}

			/* update superpixel index */
			Graphics graphics = img.getGraphics();
			graphics.setFont(new Font("Arial Black", Font.BOLD, 20));
			for (SuperPixelDTO superPixel : superPixels) {
				GridPoint middle = superPixel.getSamplePixel();
				Color myColor =  new Color(superPixel.getIdentifingColorRGB());
				graphics.setColor(myColor);
				graphics.drawString(String.valueOf(superPixel.getSuperPixelIndex()), middle.x, middle.y);
			}

			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void saveImageWithGrid(List<GridPoint> grid, SuperPixelDTO sp, ImageDTO image,
			List<SuperPixelDTO> superPixels, String path) {

		Color color1 = Color.BLACK;
		BufferedImage img = cloneBufferedImage(image.getImage());

		for (SuperPixelDTO superPixel : superPixels) {
			int rgbSuperPixel = superPixel.getIdentifingColorRGB();
			List<PixelDTO> borderPixels = superPixel.getBorderPixels();
			for (PixelDTO borderPixel : borderPixels) {
				img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
			}

			if (superPixel.equals(sp)) {
				List<PixelDTO> pixels = sp.getPixels();
				for (PixelDTO pixel : pixels) {
					img.setRGB(pixel.getXIndex(), pixel.getYIndex(), color1.getRGB());
				}
			}
		}

		Graphics2D g2d = img.createGraphics();
		Color color = Color.PINK;
		for (GridPoint point : grid) {
			g2d.setColor(color);
			g2d.fill(new Ellipse2D.Float(point.x, point.y, 5, 5));

		}

		File outputFile = new File(path);
		outputFile.getParentFile().mkdirs();
		try {
			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void save1DFeatureProbabilities(
			List<Double> featureOnLabelsProbabilities, List<Double> colourValues, String path) {

		List<Double> positionProbabilities = new ArrayList<>();
		if (Constants.ADD_COLOUR_LOCAL_FEATURE) {
			if (Constants.ADD_COLOUR_LOCAL_FEATURE_WITH_POSITION) {
				int numberOfGridBlocks = (Constants.GRID_SIZE + Constants.GRID_SIZE + 1) * (Constants.GRID_SIZE + Constants.GRID_SIZE + 1);
				if (Constants.ADD_NEIGBOUR_FEATURES) {
					positionProbabilities = featureOnLabelsProbabilities.subList(numberOfGridBlocks * 3, featureOnLabelsProbabilities.size());
				} else {
					positionProbabilities = new ArrayList<>();
					ArrayList<Double> tmp = new ArrayList<>();
					for (int i = 0; i < featureOnLabelsProbabilities.size(); i++) {
						positionProbabilities.add(featureOnLabelsProbabilities.get(i));
						tmp.add(null);
						tmp.add(null);
						tmp.add(null);
						colourValues.add(null);
						colourValues.add(null);
						colourValues.add(null);
					}
					featureOnLabelsProbabilities = tmp;
				}
			}
		} else  {
			positionProbabilities = new ArrayList<>();
			for (int i = 0; i < featureOnLabelsProbabilities.size(); i++) {
				if (i%3 == 0) positionProbabilities.add(null);
			}
		}
		
		int blockSize = 30;
		int divisorSize = 20;
		int numberOfBlocksInLine = Constants.GRID_SIZE * 2 + 1;
		
		int buffer = 30;
		
		
		BufferedImage img = new BufferedImage(buffer + numberOfBlocksInLine * (3*blockSize + divisorSize) + buffer, 
				buffer + numberOfBlocksInLine * (3*blockSize + divisorSize) + buffer, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g2d = img.createGraphics();
		
		int x,y,colour,gray;
		Color fillColor;
		Color borderColor;
		
		x = buffer;
		y = buffer;

		if (Constants.ADD_COLOUR_LOCAL_FEATURE && !Constants.ADD_COLOUR_LOCAL_FEATURE_WITH_POSITION) {
			Double colourProbability = featureOnLabelsProbabilities.get(featureOnLabelsProbabilities.size() - 1);
			colour = (int)Math.round(colourProbability * 255);
			colour = (colour << 16) + (colour << 8) + colour;
			fillColor = new Color(colour);
			borderColor = Color.PINK;
			
			drawSquare(g2d, x, y, blockSize, fillColor, borderColor);
			
			y += blockSize + divisorSize;
		}
		
		//draw percentageFeatures
		int blockIterator = 0;
		for (int i = 0,index = 0; index < colourValues.size() - 1; index +=3, i++) {
			Double redValue = colourValues.get(index);
			Double greenValue = colourValues.get(index + 1);
			Double blueValue = colourValues.get(index + 2);
			
			int initX = x;
			int initY = y;
			
			if (redValue == null) {
				fillColor = Color.PINK;
			} else {
				colour = (int)Math.round(redValue * 255);
				fillColor = new Color(colour, 0 ,0);
			}
			borderColor = Color.RED;
			drawSquare(g2d, initX, initY, blockSize, fillColor, borderColor);
			initX += blockSize;
			
			if (greenValue == null) {
				fillColor = Color.PINK;
			} else {
				colour = (int)Math.round(greenValue * 255);
				fillColor = new Color(0, colour ,0);
			}
			borderColor = Color.GREEN;
			drawSquare(g2d, initX, initY, blockSize, fillColor, borderColor);
			initX += blockSize;
			
			if (blueValue == null) {
				fillColor = Color.PINK;
			} else {
				colour = (int)Math.round(blueValue * 255);
				fillColor = new Color(0, 0, colour);
			}
			borderColor = Color.BLUE;
			drawSquare(g2d, initX, initY, blockSize, fillColor, borderColor);
			initX += blockSize;
			
			initX = x;
			initY = y + blockSize;
			
			Double redProbability = featureOnLabelsProbabilities.get(index);
			Double greenProbability = featureOnLabelsProbabilities.get(index + 1);
			Double blueProbability = featureOnLabelsProbabilities.get(index + 2);
			
			if (redProbability == null) {
				fillColor = Color.PINK;
			} else {
				gray = (int)Math.round(redProbability * 255);
				fillColor = getRGB(gray);
			}
			borderColor = Color.RED;
			drawSquare(g2d, initX, initY, blockSize, fillColor, borderColor);
			initX += blockSize;
			
			if (greenProbability == null) {
				fillColor = Color.PINK;
			} else {
				gray = (int)Math.round(greenProbability * 255);
				fillColor = getRGB(gray);
			}
			borderColor = Color.GREEN;
			drawSquare(g2d, initX, initY, blockSize, fillColor, borderColor);
			initX += blockSize;
			
			if (blueProbability == null) {
				fillColor = Color.PINK;
			} else {
				gray = (int)Math.round(blueProbability * 255);
				fillColor = getRGB(gray);
			}
			borderColor = Color.BLUE;
			drawSquare(g2d, initX, initY, blockSize, fillColor, borderColor);
			initX += blockSize;
			
			
			initX = x;
			initY = y + 2 *blockSize;
			

			Double colorPositionProbability = positionProbabilities.get(i);
			if (colorPositionProbability == null) {
				fillColor = Color.PINK;
			} else {
				gray = (int)Math.round(colorPositionProbability * 255);
				fillColor = getRGB(gray);
			}
			
			borderColor = Color.RED;
			drawSquare(g2d, initX, initY, blockSize, fillColor, borderColor);
			initX += blockSize;
			
			x += 3 * blockSize;
			
			x += divisorSize;
			blockIterator++;
			if (blockIterator >= numberOfBlocksInLine) {
				x = buffer;
				y += 3* blockSize + divisorSize;
				blockIterator = 0;
			}
		}
		
		
		File outputFile = new File(path);
		outputFile.getParentFile().mkdirs();
		try {
			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void save3DFeatureProbabilities(
			List<Double> featureOnLabelsProbabilities, String path) {

		int numberOfPercentageFeaturesBlocks;
		if (Constants.ADD_COLOUR_LOCAL_FEATURE) {
			numberOfPercentageFeaturesBlocks = ((featureOnLabelsProbabilities.size() - 1));
		} else  {
			numberOfPercentageFeaturesBlocks = featureOnLabelsProbabilities.size();
		}
		double colourProbability = featureOnLabelsProbabilities.get(featureOnLabelsProbabilities.size() - 1);
		
		int blockSize = 30;
		int divisorSize = 20;
		int numberOfBlocksInLine = 10;
		
		int numberOfRows = (int) (1 + Math.ceil(numberOfPercentageFeaturesBlocks / numberOfBlocksInLine));
		int buffer = 30;
		
		BufferedImage img = new BufferedImage(buffer + numberOfBlocksInLine * (blockSize + divisorSize) + buffer, 
				buffer + numberOfRows * (blockSize + divisorSize) + buffer, BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g2d = img.createGraphics();
		
		int x,y,colour;
		Color fillColor;
		Color borderColor;
		
		x = buffer;
		y = buffer;

		if (Constants.ADD_COLOUR_LOCAL_FEATURE) {
			colour = (int)Math.round(colourProbability * 255);
			colour = (colour << 16) + (colour << 8) + colour;
			fillColor = new Color(colour);
			borderColor = Color.PINK;
			
			drawSquare(g2d, x, y, blockSize, fillColor, borderColor);
			
			y += blockSize + divisorSize;
		}
		
		//draw percentageFeatures
		int blockIterator = 0;
		for (int index = 0; index < featureOnLabelsProbabilities.size() - 1; index ++) {
			double blockProbability = featureOnLabelsProbabilities.get(index);
			
			colour = (int)Math.round(blockProbability * 255);
			colour = (colour << 16) + (colour << 8) + colour;
			fillColor = new Color(colour);
			borderColor = Color.PINK;
			drawSquare(g2d, x, y, blockSize, fillColor, borderColor);
			x += blockSize;
			x += buffer;
			blockIterator++;
			if (blockIterator > numberOfBlocksInLine) {
				x = buffer;
				y += blockSize + divisorSize;
				blockIterator = 0;
			}
		}
		
		File outputFile = new File(path);
		outputFile.getParentFile().mkdirs();
		try {
			ImageIO.write(img, Constants.IMAGE_EXTENSION, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private static void drawSquare(Graphics2D g2d, int x, int y, int size, Color fillColor, Color borderColor) {
		g2d.setColor(fillColor);
		g2d.fill(new Rectangle2D.Double(x, y, size, size)); 

		g2d.setColor(borderColor);
		g2d.draw(new Rectangle2D.Double(x, y, size, size)); 
	}

	public static void saveTrainAndResult(ImageDTO imageObj) throws URISyntaxException, IOException {
		String trainPath = imageObj.getPath();
		File trainFile = new File(trainPath);

		String[] fileName = trainFile.getName().split(".png");
		String resultPath = Constants.TRAIN_RESULT_PATH + fileName[0] + "_N." + Constants.IMAGE_EXTENSION;
		File resultFile =  new File("E:\\Studia\\CSIT\\praca_magisterska\\repository\\crf\\target\\classes\\" + resultPath );

		PixelDTO[][] pixels = imageObj.getPixelData();
		int height = imageObj.getHeight();
		int width = imageObj.getWidth();

		BufferedImage originalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		BufferedImage segmentedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				PixelDTO pixelDTO = pixels[i][j];
				Color color = new Color(pixelDTO.getR(), pixelDTO.getG(), pixelDTO.getB());
				originalImage.setRGB(i, j, color.getRGB());
				int segmentedColor = LABEL_TO_COLOUR_MAP.get(pixelDTO.getLabel()).getRGB();
				segmentedImage.setRGB(i, j, segmentedColor);
			}
		}


		ImageIO.write(originalImage, Constants.IMAGE_EXTENSION, trainFile);
		ImageIO.write(segmentedImage, Constants.IMAGE_EXTENSION, resultFile);
	}

	public static void saveImage(ImageDTO imageObj, String path){
		try {
			File outputfile = new File(path);
			outputfile.createNewFile();
			/* change image pixel data */
			BufferedImage originalImage = imageObj.getImage();

			int width = originalImage.getWidth();
			int height = originalImage.getHeight();


			BufferedImage theImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					PixelDTO pixelDTO = imageObj.getPixelData()[i][j];
					int value;
					value = LABEL_TO_COLOUR_MAP.get(pixelDTO.getLabel()).getRGB();
					theImage.setRGB(i, j, value);
				}
			}
			ImageIO.write(theImage, Constants.IMAGE_EXTENSION, outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* others */


	public static Set <String> getPixelColors(BufferedImage image){
		Set <String> hashColours = new HashSet<String>();
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++ ) {
				int clr=  image.getRGB(x,y); 
				int  r   = (clr & 0x00ff0000) >> 16;
			int  g = (clr & 0x0000ff00) >> 8;
				int  b  =  clr & 0x000000ff;

				String hex = String.format("#%02x%02x%02x", r, g, b);  
				hashColours.add(hex);
			}
		}
		return hashColours;
	}

	public static void saveWeights(WeightVector weightVector, String currentDate, boolean append) {
		String pathName = "resources/weights" + currentDate + ".txt";
		String logPathName = "resources/weight_log" + currentDate + ".txt";
		File outputFile = new File(pathName);
		File logFile = new File(logPathName);
		StringBuilder sb = new StringBuilder();
		Date now = new Date();
		sb.append(dateFormat.format(now));
		sb.append("    ");
		sb.append("(");
		for (double weight : weightVector.getWeights()) {
			sb.append(weight);
			sb.append(", ");
		}
		sb.append(")");
		sb.append(System.getProperty("line.separator"));
		try {
			if (append) {
				FileUtils.writeStringToFile(logFile, sb.toString(), Charset.defaultCharset(), true);
			} else {
				FileUtils.writeStringToFile(outputFile, sb.toString(), Charset.defaultCharset(), false);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void showImageInJframe(String title, BufferedImage img) {
		ImageIcon icon = new ImageIcon(img);
		JFrame frame=new JFrame(title);
		frame.setLayout(new FlowLayout());
		frame.setSize(img.getWidth() + 10, img.getHeight() + 30);
		JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static BufferedImage cloneBufferedImage(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public static void clearDirectory(String path) {
		
		File f = new File(path);
		clearDirectory(f);
		
	}
	
	private static void clearDirectory(File f){
	    for (File c : f.listFiles()) {
	    	if (c.isDirectory()) {
	    		clearDirectory(c);
	    	}
	    }
		try {
			FileUtils.cleanDirectory(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Color getRGB(int gray) {
	    int rgb = gray << 16 | gray << 8 | gray;
	    
//	    return new Color(gray, gray, gray);
	   return new Color(rgb); 
//	    for (int i = 0; i < 3; i++) {
//	        rgb[i] = (int) (gray / WEIGHTS[i]);
//	        if (rgb[i] < 256)
//	            return new Color(rgb[0], rgb[1], rgb[2]); // Successfully "distributed" all of gray, return it
//
//	        // Not quite there, cut it...
//	        rgb[i] = 255;
//	        // And distribute the remaining on the rest of the RGB components:
//	        gray -= (int) (255 * WEIGHTS[i]);
//	    }

//	    return new Color(rgb[0], rgb[1], rgb[2]);
	}
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	private static Logger _log = Logger.getLogger(DataHelper.class);


}
