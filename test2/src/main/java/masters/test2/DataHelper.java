package masters.test2;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.management.RuntimeErrorException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

import masters.test2.image.ImageDTO;
import masters.test2.image.PixelDTO;
import masters.test2.superpixel.SuperPixelDTO;

public class DataHelper {
	private static List<String>	SEGMENTED_HEX_COLOURS = Constants.SEGMENTED_HEX_COLOURS;
	private static Map<Integer,Color> LABEL_TO_COLOUR_MAP = Constants.LABEL_TO_COLOUR_MAP;
	
	public static String TEST_TEST_SET = Constants.TEST_PATH;
	public static String TEST_TRAIN_SET = Constants.TRAIN_PATH;
	public static String TEST_SEGMENTATION_RESULTS = Constants.RESULT_PATH;
	
	public List<ImageDTO> getTrainingDataTest() {
		
		List<File> trainFiles = getFilesFromDirectory(TEST_TRAIN_SET);
		List<File> segmentatedFiles = getFilesFromDirectory(TEST_SEGMENTATION_RESULTS);
		
		List <ImageDTO> imageList = new ArrayList<ImageDTO>();
		
		for (int i = 0; i < trainFiles.size(); i++) {
			File segmentedFile = segmentatedFiles.get(i);
			BufferedImage segmentedImg = openImage(segmentedFile.getPath());
			if (segmentedImg != null) {

				File trainFile = trainFiles.get(i);
				BufferedImage img = openImage(trainFile.getPath());
				ImageDTO imageObj = new ImageDTO(trainFile.getPath(), img.getWidth(),img.getHeight(), img);
				PixelDTO[][] pixelData = getPixelDTOs(img, false);
				imageObj.pixelData = pixelData;
				
				//setForegroundProperty(imageObj, segmentedPixelData);
				
				imageList.add(imageObj);
			}
		}
		return imageList;
		
	}
	private static List<File> getFilesFromDirectory(String path) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		return Arrays.asList(listOfFiles);
	}
	public static void foo () {
		String basePath = "E:\\Studia\\CSIT\\praca_magisterska\\datasets\\VOCtrainval_11-May-2012\\VOCdevkit\\VOC2012\\JPEGImages\\";
		List<File> resultFiles = getFilesFromDirectory(TEST_SEGMENTATION_RESULTS);
		for (int i = 0; i < resultFiles.size(); i++) {
			File trainFile = resultFiles.get(i);
			String fileName = trainFile.getName();
			fileName = fileName.replaceAll("_N","");
			System.out.println(fileName);
			String src = basePath + fileName;
			String dst = TEST_TRAIN_SET  + fileName;
			try {
				Files.copy(new File(src).toPath(), new File(dst).toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	public static List<ImageDTO> getTestData() {
		List<File> testFiles = getFilesFromDirectory(TEST_TEST_SET);
		
		List <ImageDTO> imageList = new ArrayList<ImageDTO>();
		for (int i = 0; i < testFiles.size(); i++) {
			if (i >= Constants.TEST_IMAGE_LIMIT) {
				break;
			}
			File trainFile = testFiles.get(i);
			
			BufferedImage trainImg = openImage(trainFile.getPath());
			
			ImageDTO imageObj = new ImageDTO(trainFile.getPath(), trainImg.getWidth(),trainImg.getHeight(), trainImg);
			PixelDTO[][] pixelData = getPixelDTOs(trainImg, false);
			imageObj.pixelData = pixelData;
			
			imageList.add(imageObj);
		}
		return imageList;
	}
	
	public static List<ImageDTO> getTrainingDataTestSegmented() {
		List<File> trainingFiles = getFilesFromDirectory(TEST_TRAIN_SET);
		List<File> resultFiles = getFilesFromDirectory(TEST_SEGMENTATION_RESULTS);
		
		List <ImageDTO> imageList = new ArrayList<ImageDTO>();
		for (int i = 0; i < trainingFiles.size(); i++) {
			if (i >= Constants.TRAIN_IMAGE_LIMIT) {
				return imageList;
			}
			File trainFile = trainingFiles.get(i);
			File segmentedFile = resultFiles.get(i);
			
			BufferedImage trainImg = openImage(trainFile.getPath());
			BufferedImage segmentedImg = openImage(segmentedFile.getPath());
			
			ImageDTO imageObj = new ImageDTO(trainFile.getPath(), trainImg.getWidth(),trainImg.getHeight(), trainImg);
			PixelDTO[][] pixelData = getPixelDTOs(trainImg, false);
			imageObj.pixelData = pixelData;
			
			PixelDTO[][] segmentedPixelData = getPixelDTOs(segmentedImg, true);
			updateLabelFromSegmentedImage(imageObj,segmentedPixelData);
			
			imageList.add(imageObj);
		}
		return imageList;
	}
	
	public ImageDTO readImageToImageDTO(String path) {
		BufferedImage trainImg = openImage(path);
		
		ImageDTO imageObj = new ImageDTO(path, trainImg.getWidth(),trainImg.getHeight(), trainImg);
		
		PixelDTO[][] pixelData = getPixelDTOs(trainImg, false);
		imageObj.pixelData = pixelData;
			
		return imageObj;
	}
	
	private static void updateLabelFromSegmentedImage(ImageDTO imageObj, PixelDTO[][] segmentedPixelData) {
		PixelDTO[][] pixelData = imageObj.pixelData;
		for (int i = 0; i < pixelData[0].length; i++) {
			for (int j = 0; j < pixelData.length; j++) {
				PixelDTO pixel = pixelData[j][i];
				PixelDTO segmentedPixel = segmentedPixelData[j][i];
				int label = segmentedPixel.getLabel();
				pixel.setLabel(label);
			}
		}
		
	}
	private static BufferedImage openImage(String path) {
		BufferedImage img;
		try {
			img = ImageIO.read(new File(path));
			return img;
		} catch(IOException e) {
			return null;
		}
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
	public static void saveImage(ImageDTO imageObj, String path){
		try {
			File outputfile = new File(path);
			outputfile.createNewFile();
			/* change image pixel data */
			BufferedImage originalImage = imageObj.getImage();
			
			int width = originalImage.getWidth();
	        int height = originalImage.getHeight();
	        int[][] pixel = new int[width][height];
	        Raster raster = originalImage.getData();
	        for (int i = 0; i < width; i++) {
	            for (int j = 0; j < height; j++) {
	                pixel[i][j] = raster.getSample(i, j, 0);
	            }
	        }
	        BufferedImage theImage = new BufferedImage(width, height,
	                BufferedImage.TYPE_INT_RGB);
	        for (int i = 0; i < width; i++) {
	            for (int j = 0; j < height; j++) {
	            	PixelDTO pixelDTO = imageObj.pixelData[i][j];
	            	int value;
	            	value = LABEL_TO_COLOUR_MAP.get(pixelDTO.getLabel()).getRGB();
	                theImage.setRGB(i, j, value);
	            }
	        }
	        ImageIO.write(theImage, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static BufferedImage deepCopy(BufferedImage bi) {
		 ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	public void viewImage(ImageDTO image) {
		BufferedImage img = image.getImage();
		ImageIcon icon = new ImageIcon(img);
        JFrame frame=new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(img.getWidth() + 10, img.getHeight() + 30);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void viewImageSegmented(ImageDTO image) {
		viewImageSegmented(image, "");
	}
	public static void viewImageSegmented(ImageDTO image, String title) {
		/* change image pixel data */
		BufferedImage img = image.getImage();
		
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
            	try {
            	int value = LABEL_TO_COLOUR_MAP.get(image.pixelData[x][y].getLabel()).getRGB();
            	img.setRGB(x, y, value);
            	} catch (NullPointerException e) {
            		_log.error("null", e);
                	System.out.println("image.pixelData[x][y].getLabel()" + image.pixelData[x][y].getLabel());
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
	
	public static void viewImageSegmented(ImageDTO image, List<SuperPixelDTO> superPixels, List<Integer> mask) {
		viewImageSegmented(image, superPixels, mask, "");
	}
	public static void viewImageSegmented(ImageDTO image, List<SuperPixelDTO> superPixels, List<Integer> mask, String title) {
		
		/* change image pixel data */
		BufferedImage img = image.getImage();
		for (int superPixelIndex = 0; superPixelIndex < superPixels.size(); superPixelIndex++) {
			SuperPixelDTO sp = superPixels.get(superPixelIndex);
			List<PixelDTO> pixels = sp.getPixels();
			for (PixelDTO p : pixels) {
				int myColour = LABEL_TO_COLOUR_MAP.get(mask.get(superPixelIndex)).getRGB();
				img.setRGB(p.getXIndex(), p.getYIndex(), myColour);
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
	            	int value = LABEL_TO_COLOUR_MAP.get(image.pixelData[x][y].getLabel()).getRGB();
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
	        	Point middle = superPixel.getSamplePixel();
	        	Color myColor =  new Color(superPixel.getIdentifingColorRGB());
	        	graphics.setColor(myColor);
	        	graphics.drawString(String.valueOf(superPixel.getSuperPixelIndex()), middle.x, middle.y);
	        }
	        
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
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
		try {
			ImageIO.write(img, "png", outputFile);
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
			ImageIO.write(img, "png", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	public static void viewImageSuperpixel(ImageDTO image, List<SuperPixelDTO> superPixels) {
		/* change image pixel data */
		BufferedImage img = image.getImage();
		for (SuperPixelDTO sp : superPixels) {
            int rgbSuperPixel = sp.getIdentifingColorRGB();
			List<PixelDTO> pixels = sp.getPixels();
			for (PixelDTO p : pixels) {
				img.setRGB(p.getXIndex(), p.getYIndex(), rgbSuperPixel);
			}
		}
            		
        ImageIcon icon = new ImageIcon(img);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(img.getWidth() + 10, img.getHeight() + 30);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	public static PixelDTO[][] deepCopy(PixelDTO[][] original) {
	    if (original == null) {
	        return null;
	    }

	    final PixelDTO[][] result = new PixelDTO[original.length][];
	    for (int i = 0; i < original.length; i++) {
	        result[i] = Arrays.copyOf(original[i], original[i].length);
	    }
	    return result;
	}
	
        
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
  
  
  private static Logger _log = Logger.getLogger(DataHelper.class);
}
