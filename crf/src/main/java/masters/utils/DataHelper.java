package masters.utils;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
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

import masters.Constants;
import masters.grid.GridPoint;
import masters.image.ImageDTO;
import masters.image.PixelDTO;
import masters.superpixel.SuperPixelDTO;
import masters.train.WeightVector;

public class DataHelper {
	private static List<String>	SEGMENTED_HEX_COLOURS = Constants.SEGMENTED_HEX_COLOURS;
	private static Map<Integer,Color> LABEL_TO_COLOUR_MAP = Constants.LABEL_TO_COLOUR_MAP;
	
	public static String TEST_TEST_SET = Constants.TEST_PATH;
	public static String TEST_TRAIN_SET = Constants.TRAIN_PATH;
	public static String TEST_SEGMENTATION_RESULTS = Constants.RESULT_PATH;
	
	public static String IMAGE_EXTENSION = Constants.IMAGE_EXTENSION;
	public static String RESULT_IMAGE_SUFFIX = Constants.RESULT_IMAGE_SUFFIX;
	
	/* getters */ 

	
	public static List<ImageDTO> getTrainingDataTestSegmented() {
		Map<String, File> trainingFiles = getFilesFromDirectory(TEST_TRAIN_SET);
		Map<String, File> resultFiles = getFilesFromDirectory(TEST_SEGMENTATION_RESULTS);
		
		List <ImageDTO> imageList = new ArrayList<ImageDTO>();
		int i = 0;
		for (String key : trainingFiles.keySet()) {
			if (++i >= Constants.TRAIN_IMAGE_LIMIT + 1) {
				return imageList;
			}
			
			File trainFile = trainingFiles.get(key);
			File segmentedFile = resultFiles.get(key + RESULT_IMAGE_SUFFIX);
			
			BufferedImage trainImg = openImage(trainFile.getPath());
			BufferedImage segmentedImg = openImage(segmentedFile.getPath());
			
			ImageDTO imageObj = new ImageDTO(trainFile.getPath(), trainImg.getWidth(),trainImg.getHeight(), trainImg);
			PixelDTO[][] pixelData = getPixelDTOs(trainImg, false);
			imageObj.setPixelData(pixelData);
			
			PixelDTO[][] segmentedPixelData = getPixelDTOs(segmentedImg, true);
			updateLabelFromSegmentedImage(imageObj,segmentedPixelData);
			
			imageList.add(imageObj);
		}
		return imageList;
	}

	public static List<ImageDTO> getTestData() {
		Map<String, File> testFiles = getFilesFromDirectory(TEST_TEST_SET);
		
		List <ImageDTO> imageList = new ArrayList<ImageDTO>();
		int i = 0;
		for (String key : testFiles.keySet()) {
			if (++i >= Constants.TEST_IMAGE_LIMIT + 1) {
				break;
			}
			File trainFile = testFiles.get(key);
			
			BufferedImage trainImg = openImage(trainFile.getPath());
			
			ImageDTO imageObj = new ImageDTO(trainFile.getPath(), trainImg.getWidth(),trainImg.getHeight(), trainImg);
			PixelDTO[][] pixelData = getPixelDTOs(trainImg, false);
			imageObj.setPixelData(pixelData);
			
			imageList.add(imageObj);
		}
		return imageList;
	}
	
	public static PixelDTO[][] getPixelDTOs (BufferedImage image, boolean isSegmented) {
		Set<String> col = new HashSet<String>();
		int[] tmpArray = null;
		WritableRaster rasterImage = image.getRaster();
		int width = image.getWidth();
		int height = image.getHeight();
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
					  for (int i = 0; i < SEGMENTED_HEX_COLOURS.size(); i++) {
						  String segmentedHexColour = SEGMENTED_HEX_COLOURS.get(i);
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
	
	private static void updateLabelFromSegmentedImage(ImageDTO imageObj, PixelDTO[][] segmentedPixelData) {
		PixelDTO[][] pixelData = imageObj.getPixelData();
		for (int i = 0; i < pixelData[0].length; i++) {
			for (int j = 0; j < pixelData.length; j++) {
				PixelDTO pixel = pixelData[j][i];
				PixelDTO segmentedPixel = segmentedPixelData[j][i];
				int label = segmentedPixel.getLabel();
				pixel.setLabel(label);
			}
		}
	}
	public static BufferedImage openImage(String path) {
		BufferedImage img;
		try {
			img = ImageIO.read(new File(path));
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
        ImageIcon icon = new ImageIcon(img);
        JFrame frame = new JFrame(title);
        frame.setLayout(new FlowLayout());
        frame.setSize(img.getWidth() + 10, img.getHeight() + 30);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
            		
        ImageIcon icon = new ImageIcon(img);
        JFrame frame = new JFrame(title);
        frame.setLayout(new FlowLayout());
        frame.setSize(img.getWidth() + 10, img.getHeight() + 30);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
	
	public static void saveImageSuperpixelBordersOnly(ImageDTO image, List<SuperPixelDTO> superPixels, String path) {
		BufferedImage img = image.getImage();
		for (SuperPixelDTO superPixel : superPixels) {
            int rgbSuperPixel = superPixel.getIdentifingColorRGB();
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
	public static void saveImageFi1Probabilities(ImageDTO image, List<SuperPixelDTO> superPixels,
			String basePath, int objectLabel,
			List<Double> superPixelProbs) {

			String filePath = basePath + "label_" + objectLabel + "." + Constants.IMAGE_EXTENSION;
			File outputfile = new File(filePath);
			outputfile.getParentFile().mkdirs();
			try {
				outputfile.createNewFile();
				BufferedImage initImg = cloneBufferedImage(image.getImage());
				BufferedImage img = new BufferedImage(initImg.getWidth(), initImg.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
				for (SuperPixelDTO superPixel : superPixels) {
					double labelProbability = superPixelProbs.get(superPixel.getSuperPixelIndex());
					int colour = (int)Math.round(labelProbability * 255);
					colour = (colour << 16) + (colour << 8) + colour;
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
	
	public static void saveImageWithSuperPixelsIndex(ImageDTO image, List<SuperPixelDTO> superPixels, String path) throws IOException {
		File outputfile = new File(path);
		outputfile.getParentFile().mkdirs();
		outputfile.createNewFile();
		BufferedImage img = cloneBufferedImage(image.getImage());
		
		/* update superpixel index */
        Graphics graphics = img.getGraphics();
        graphics.setFont(new Font("Arial Black", Font.BOLD, 16));
        
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
	
	
	public static void saveTrainAndResult(ImageDTO imageObj) throws URISyntaxException, IOException {
		String trainPath = imageObj.getPath();
		File trainFile = new File(trainPath);
		
		System.out.println("TRAIN " + trainPath);
		
		String[] fileName = trainFile.getName().split(".png");
		String resultPath = Constants.RESULT_PATH + fileName[0] + "_N." + Constants.IMAGE_EXTENSION;
		File resultFile =  new File("E:\\Studia\\CSIT\\praca_magisterska\\repository\\crf\\target\\classes\\" + resultPath );
		System.out.println("RES " + resultFile.getAbsolutePath());
		
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
	
	
	private static BufferedImage cloneBufferedImage(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
		}
	
  private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
  private static Logger _log = Logger.getLogger(DataHelper.class);

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

}
