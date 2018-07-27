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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.management.RuntimeErrorException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

import masters.test2.colors.ColorSpaceException;
import masters.test2.image.ImageDTO;
import masters.test2.image.PixelDTO;
import masters.test2.superpixel.SuperPixelDTO;
import masters.test2.train.GradientDescentTrainer;

public class DataHelper {
	//private static String FOREGROUND_HEX_COLOUR = "#c00080";
	private static List<String>	SEGMENTED_HEX_COLOURS = Arrays.asList(new String [] {"#000000","#ffffff","#7f7f7f", "#ff00ff"});
	private static Map<Integer,Color> LABEL_TO_COLOUR_MAP = new HashMap<Integer,Color>();
	static {
        Color label0Colour =  new Color(0, 0, 0); 		// Color black
        Color label1Colour = new Color(255, 255, 255); 	// Color white
        Color label2Colour = new Color(127,127,127); 	// Color gray
        Color label3Colour = new Color(127,0,127); 	// Color gray
        LABEL_TO_COLOUR_MAP.put(0, label0Colour);
        LABEL_TO_COLOUR_MAP.put(1, label1Colour);
        LABEL_TO_COLOUR_MAP.put(2, label2Colour);
        LABEL_TO_COLOUR_MAP.put(3, label3Colour);
	}
	
	private static String IMAGE_PATH = "E:\\Studia\\CSIT\\praca_magisterska\\datasets\\VOCtrainval_11-May-2012\\VOCdevkit\\VOC2012\\JPEGImages";
	private static String SEGMENTATION_PATH = "E:\\Studia\\CSIT\\praca_magisterska\\datasets\\VOCtrainval_11-May-2012\\VOCdevkit\\VOC2012\\SegmentationClass";
	private static String SEGMENTATION_PATH_HORSE = "E:\\Studia\\CSIT\\praca_magisterska\\datasets\\VOCtrainval_11-May-2012\\VOCdevkit\\VOC2012\\SegmentationHorse";
	
	private static String INPUT_EXTENSION = ".jpg";
	private static String OUTPUT_EXTENSION = ".jpg";
	
	private static String TRAIN_LIST_PATH = "src/resources/horse_train.txt";
	private static String VAL_LIST_PATH = "src/resources/horse_val.txt";
	private static String TRAIN_AND_VAL_LIST_PATH = "src/resources/horse_trainval.txt";
	
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
		List<File> testFiles = getFilesFromDirectory(TEST_TRAIN_SET);
		
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
				break;
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
	public List<ImageDTO> getTrainingData() {
		Set<String> trainingFileNames = getObjectFileNamesWithoutExtension(TRAIN_LIST_PATH);
		List <ImageDTO> imageList = new ArrayList<ImageDTO>();
		for (String s : trainingFileNames) {
			String segmentedPath = SEGMENTATION_PATH_HORSE + "\\" + s + "_N" + OUTPUT_EXTENSION;
			String path = IMAGE_PATH + "\\" + s + INPUT_EXTENSION;
			BufferedImage segmentedImg = openImage(segmentedPath);
			if (segmentedImg != null) {
				ImageDTO segmentedImageObj = new ImageDTO(segmentedPath, segmentedImg.getWidth(),segmentedImg.getHeight(), segmentedImg);
				PixelDTO[][] segmentedPixelData = getPixelDTOs(segmentedImg, true);
				
				BufferedImage img = openImage(path);
				if (img != null) {
					ImageDTO imageObj = new ImageDTO(path, img.getWidth(),img.getHeight(), img);
					PixelDTO[][] pixelData = getPixelDTOs(img, false);
					imageObj.pixelData = pixelData;
					
					updateLabelFromSegmentedImage(imageObj, segmentedPixelData);
					
					imageList.add(imageObj);
				}
			}
		}
		return imageList;
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
				  // TODO delete alpha
				  //int alpha = pixelData[3];
				  int alpha = 0;
				  PixelDTO pixel;
				  if (isSegmented) {
					  int label = -1;
					  String hexColor = String.format("#%02x%02x%02x", r, g, b);
					  if (!col.contains(hexColor)) {
//						  _log.info(hexColor);
						  col.add(hexColor);
					  }
					  for (int i = 0; i < SEGMENTED_HEX_COLOURS.size(); i++) {
						  String segmentedHexColour = SEGMENTED_HEX_COLOURS.get(i);
						  if (hexColor.equals(segmentedHexColour)) {
							  label = i;
							  break;
						  }
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
	private Set<String> getObjectFileNamesWithoutExtension(String fileName) {
		List<List<String>> allObjects = getDataFromFile(fileName);
		Set<String> objectNames = new HashSet<String>();
		for (List<String> singleLine : allObjects) {
			String singleFileName = singleLine.get(0);
			String singleObjAnnotation = "";
			for (int i = 1; i < singleLine.size(); i++) {
				singleObjAnnotation += singleLine.get(i);
			}
			if (singleObjAnnotation.trim().equals("1")) {
				objectNames.add(singleFileName);
			}
		}
		return objectNames;
	}
	private List<List<String>> getDataFromFile(String fileName){
		FileReader dataReader;
		List<List<String>> dataFromFile = new ArrayList<List<String>>();
		try {
			dataReader = new FileReader(fileName);
			
			// read training data
			BufferedReader dataBF = new BufferedReader(dataReader);
			String singleLine;
			while ((singleLine = dataBF.readLine()) != null) {
				if (!singleLine.trim().equals("")) {
					String[] data = singleLine.split(" ");
					dataFromFile.add(Arrays.asList(data));
				}
			}
			dataReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataFromFile;
	}
	
	/*public void setForegroundProperty(ImageDTO imageObj, PixelDTO[][] segmentedPixelDTOs) {
		PixelDTO[][] pixelData = imageObj.pixelData;
		for (int y = 0; y < pixelData[0].length; y++) {
			for (int x = 0; x < pixelData.length; x++) {
				pixelData[x][y].setIsForeground(segmentedPixelDTOs[x][y].getIsForeground());
			}
		}
	}*/
	public static void saveImage(ImageDTO imageObj){
		String imagePath = imageObj.getPath();
		String [] partPaths = imagePath.split("\\\\");
		String fileName = partPaths[partPaths.length - 1];
		String newFileName = fileName.substring(0, fileName.length() - 4) + "_N" + 
				fileName.substring(fileName.length() - 4, fileName.length());
		
		String newFilePath = SEGMENTATION_PATH_HORSE + "\\" +  newFileName;
		saveImage(imageObj, newFilePath);
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
	
	public static void saveImage(int[][] pixelData){
		try {
			String newFileName = UUID.randomUUID() + ".png";
			String newFilePath = SEGMENTATION_PATH_HORSE + "\\" +  newFileName;
			File outputfile = new File(newFilePath);
			outputfile.createNewFile();
			/* change image pixel data */
			int width = pixelData[0].length;
			int height = pixelData.length;
	        BufferedImage theImage = new BufferedImage(width, height,
	                BufferedImage.TYPE_INT_RGB);
	        for (int i = 0; i < width; i++) {
	            for (int j = 0; j < height; j++) {
	            	int value = LABEL_TO_COLOUR_MAP.get(pixelData[j][i]).getRGB();
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
            		Constants._log.error("null", e);
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
		/*for (SuperPixelDTO superPixel : superPixels) {
            int rgbSuperPixel = superPixel.getIdentifingColorRGB();
			List<PixelDTO> borderPixels = superPixel.getBorderPixels();
			for (PixelDTO borderPixel : borderPixels) {
				img.setRGB(borderPixel.getXIndex(), borderPixel.getYIndex(), rgbSuperPixel);
			}
		}*/
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
	
	public static void increaseBlue(List<SuperPixelDTO> superPixels) throws ColorSpaceException {
		for (SuperPixelDTO superPixel : superPixels) {
			if (superPixel.getLabel() == 0) {
				double[] rgb = superPixel.getMeanRGB();
				System.out.println("#Niebo " + rgb[0] + " " + rgb[1] + " " + rgb[2] );
				int newGreen = (rgb[1] > 120) ? (int)rgb[1] - 120 : 0;
				rgb[0] = 0;
				rgb[1] = newGreen;
				
				superPixel.setMeanRGB(rgb);
				
				
			}
			if (superPixel.getLabel() == 2) {
				double[] rgb = superPixel.getMeanRGB();
				System.out.println("#Trawa " + rgb[0] + " " + rgb[1] + " " + rgb[2] );
				int newBlue = (rgb[2] > 40) ? (int)rgb[2] - 40 : 0;
				//superPixel.setMeanB(newBlue);
				rgb[2] = 0;
				
				superPixel.setMeanRGB(rgb);
			}
		}
		
	}
	
	/* 	not used */
	private BufferedImage openImageInFrame(String path)  {
		BufferedImage img;
		try {
			img = ImageIO.read(new File(path));
			ImageIcon icon=new ImageIcon(img);
	        JFrame frame=new JFrame();
	        frame.setLayout(new FlowLayout());
	        frame.setSize(img.getWidth() + 10,img.getHeight() + 30);
	        JLabel lbl=new JLabel();
	        lbl.setIcon(icon);
	        frame.add(lbl);
	        frame.setVisible(true);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        return img;
		} catch (IOException e) {
			return null;
		}
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
  
  
  final static Logger _log = Logger.getLogger(DataHelper.class);
}
