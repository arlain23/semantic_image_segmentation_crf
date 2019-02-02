package shapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;

import colours.ColourUtil;
import generator.GeneratorConstants;
import generator.GeneratorConstants.Shape;
import masters.Constants;

public class ShapeDrawer {
	
	private static final int BASE_BLOCK_SIZE = GeneratorConstants.BLOCK_SIZE;
	private static final Color FOREGROUND_COLOR = GeneratorConstants.FOREGROUND_COLOR;
	private static final Color NEIGHBOUR_COLOR = GeneratorConstants.NEIGHBOUR_COLOR;
	private static int SHAPE_SIZE = 4;
	
	
	public static void drawNonLinearShapes(Graphics2D trainG2d,
			Graphics2D resultG2d) {
		
		int fillnessIndex = ThreadLocalRandom.current().nextInt(5);
		if (fillnessIndex < 1) {
			//background
			Color color = getRandomColor();
			ColourUtil.fillColour(trainG2d, resultG2d, color);
			
			//foreground
			drawRandomShape(trainG2d, resultG2d, 0, 0, GeneratorConstants.WIDTH, GeneratorConstants.HEIGHT, 2);
			
		} else if (fillnessIndex < 2) {
			Color color = getRandomColor();
			Color color2 = getRandomColor();
			
			ColourUtil.fillColour2sections(trainG2d, resultG2d, color, color2);
			
			//foreground
			drawRandomShape(trainG2d, resultG2d, 0, 0, GeneratorConstants.WIDTH/2, GeneratorConstants.HEIGHT, 1);
			drawRandomShape(trainG2d, resultG2d, GeneratorConstants.WIDTH/2, 0, GeneratorConstants.WIDTH, GeneratorConstants.HEIGHT, 1);
			
		} else if (fillnessIndex < 4) {
			Color color = getRandomColor();
			Color color2 = getRandomColor();
			List<Color> availableColors = new ArrayList<>(GeneratorConstants.AVAILABLE_COLOURS);
			availableColors.remove(color);
			availableColors.remove(color2);
			Color color3 = availableColors.get(0);
			
			ColourUtil.fillColour3sections(trainG2d, resultG2d, color, color2);
			
			//foreground
			drawRandomShape(trainG2d, resultG2d, 0, 0*GeneratorConstants.HEIGHT, GeneratorConstants.WIDTH, GeneratorConstants.HEIGHT, color3, 2);
			
		} else if (fillnessIndex < 5) {
			Color color = getRandomColor();
			Color color2 = getRandomColor();
			Color color3 = getRandomColor();
			Color color4 = getRandomColor();
			
			ColourUtil.fillColour4sections(trainG2d, resultG2d, color, color2, color3, color4);
			//foreground
			drawRandomShape(trainG2d, resultG2d, 0, 0, GeneratorConstants.WIDTH/2, GeneratorConstants.HEIGHT/2, 1);
			drawRandomShape(trainG2d, resultG2d, GeneratorConstants.WIDTH/2, 0, GeneratorConstants.WIDTH, GeneratorConstants.HEIGHT/2, 1);
			drawRandomShape(trainG2d, resultG2d, 0, GeneratorConstants.HEIGHT/2, GeneratorConstants.WIDTH/2, GeneratorConstants.HEIGHT, 1);
			drawRandomShape(trainG2d, resultG2d, GeneratorConstants.WIDTH/2, GeneratorConstants.HEIGHT/2, GeneratorConstants.WIDTH, GeneratorConstants.HEIGHT, 1);
			
		}
		
	}
	public static void initDrawVersion2(Graphics2D trainG2d,
			Graphics2D resultG2d, Shape shape, boolean isPrimaryShape) {
		
		ColourUtil.fillColour(trainG2d, resultG2d, NEIGHBOUR_COLOR);
		
		Random random = new Random();
		int blockSize = (int)(SHAPE_SIZE * BASE_BLOCK_SIZE);
		int neighbourhoodSize = GeneratorConstants.NEIGHBOURHOOD_GRID;
		int buffer = GeneratorConstants.BUFFER;
		
		
		int numberOfBlocks = (int)GeneratorConstants.WIDTH / BASE_BLOCK_SIZE;
		
		int lowBound = buffer + neighbourhoodSize;
		int highBound = (numberOfBlocks - buffer - neighbourhoodSize - SHAPE_SIZE) + 1;
		
		random = new Random();
		int initShape_XGridPos = random.nextInt(highBound-lowBound) + lowBound;
		random = new Random();
		int initShape_YGridPos = random.nextInt(highBound-lowBound) + lowBound;
		
		
		int initShape_X = (initShape_XGridPos) * BASE_BLOCK_SIZE;
		int initShape_Y = (initShape_YGridPos) * BASE_BLOCK_SIZE;
		
		trainG2d.setColor(FOREGROUND_COLOR);
		if (shape == Shape.SQUARE) {
			drawSquare(trainG2d, initShape_X, initShape_Y, blockSize);
		} else if (shape == Shape.CIRCLE) {
			drawCircle(trainG2d, initShape_X, initShape_Y, blockSize);
		} else if (shape == Shape.PENTAGON) {
			drawPentagon(resultG2d, initShape_X, initShape_Y, blockSize);
		}
		
		if (resultG2d != null){
			Color foregroundMarkup;
			if (isPrimaryShape) {
				foregroundMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get
						(GeneratorConstants.COLOR_TO_LABEL_MAP.size());
			} else {
				foregroundMarkup = getColorMarkup(FOREGROUND_COLOR);
			}
			resultG2d.setColor(foregroundMarkup);
			if (shape == Shape.SQUARE) {
				drawSquare(resultG2d, initShape_X, initShape_Y, blockSize);
			} else if (shape == Shape.CIRCLE) {
				drawCircle(resultG2d, initShape_X, initShape_Y, blockSize);
			} else if (shape == Shape.PENTAGON) {
				drawPentagon(resultG2d, initShape_X, initShape_Y, blockSize);
			}
		}
		
	}
	
	public static void initDrawH(Graphics2D trainG2d, Graphics2D resultG2d) {
		Random random = new Random();
		int blockSize = BASE_BLOCK_SIZE;
		int neighbourhoodSize = GeneratorConstants.NEIGHBOURHOOD_GRID;
		int buffer = GeneratorConstants.BUFFER;
		
		int numberOfBlocks = (int)GeneratorConstants.WIDTH / BASE_BLOCK_SIZE;
		
		int lowBound = buffer + neighbourhoodSize;
		int highBound = (numberOfBlocks - buffer - neighbourhoodSize - SHAPE_SIZE) + 1;
		
		random = new Random();
		int initH_XGridPos = random.nextInt(highBound-lowBound) + lowBound;
		random = new Random();
		int initH_YGridPos = random.nextInt(highBound-lowBound) + lowBound;
		
		
		int initH_X = (initH_XGridPos) * BASE_BLOCK_SIZE;
		int initH_Y = (initH_YGridPos) * BASE_BLOCK_SIZE;
		
		drawMainShape(trainG2d, resultG2d, initH_X, initH_Y, blockSize);
		
	}
	
	public static void initDrawOthers(Graphics2D trainG2d, Graphics2D resultG2d) {
		Random random = new Random();
		
		// choose shape 
		List<Shape> availableshapes = GeneratorConstants.availableShapes;
		Collections.shuffle(availableshapes);
		int index = random.nextInt(availableshapes.size());
		Shape chosenShape = availableshapes.get(index);
		
		
		int blockSize = (int)(SHAPE_SIZE * BASE_BLOCK_SIZE);
		int neighbourhoodSize = GeneratorConstants.NEIGHBOURHOOD_GRID;
		int buffer = GeneratorConstants.BUFFER;
		
		int numberOfBlocks = (int)GeneratorConstants.WIDTH / BASE_BLOCK_SIZE;
		
		int lowBound = buffer + neighbourhoodSize;
		int highBound = (numberOfBlocks - buffer - neighbourhoodSize - SHAPE_SIZE) + 1;
		
		random = new Random();
		int initXGridPos = random.nextInt(highBound-lowBound) + lowBound;
		random = new Random();
		int initYGridPos = random.nextInt(highBound-lowBound) + lowBound;
		
		int initX = (initXGridPos) * BASE_BLOCK_SIZE;
		int initY = (initYGridPos) * BASE_BLOCK_SIZE;
		
		drawOtherRegions(trainG2d, resultG2d, initX, initY, blockSize, chosenShape);
	}
	
	public static void drawOtherRegions(Graphics2D trainG2d, Graphics2D resultG2d, int initX, int initY, int blockSize, Shape chosenShape) {
		drawOtherShapeNeighbourhood(trainG2d, initX, initY, blockSize, NEIGHBOUR_COLOR );
		drawOtherShape(trainG2d, initX, initY, FOREGROUND_COLOR, chosenShape);
		
		if (resultG2d != null){
			Color foregroundMarkup = getColorMarkup(FOREGROUND_COLOR);
			Color neighbourMarkup = getColorMarkup(NEIGHBOUR_COLOR);
			
			drawOtherShapeNeighbourhood(resultG2d, initX, initY, blockSize, neighbourMarkup);
			drawOtherShape(resultG2d, initX, initY, foregroundMarkup, chosenShape);
		}
	}
	public static void drawMainShape(Graphics2D trainG2d, Graphics2D resultG2d, int initX, int initY, int blockSize) {
		drawBaseShapeNeighbourhood(trainG2d, initX, initY, NEIGHBOUR_COLOR);
		drawBaseShape(trainG2d, initX, initY, FOREGROUND_COLOR);
		
		if (resultG2d != null){
			Color foregroundMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(GeneratorConstants.COLOR_TO_LABEL_MAP.size());
			Color neighbourMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
					GeneratorConstants.COLOR_TO_LABEL_MAP.get(NEIGHBOUR_COLOR));
			
			drawBaseShapeNeighbourhood(resultG2d, initX, initY, neighbourMarkup);
			drawBaseShape(resultG2d, initX, initY, foregroundMarkup);
		}
		
	}
	public static void drawBaseShapeNeighbourhood(Graphics2D g2d, int initX, int initY, Color color) {
		int blockSize = GeneratorConstants.BLOCK_SIZE;
		int neighbourhoodSize = GeneratorConstants.NEIGHBOURHOOD_GRID;
		int neigbourBlockSize = GeneratorConstants.NEIGHBOURHOOD_BLOCK_SIZE;
		int neighbourSize = neighbourhoodSize * neigbourBlockSize;
		int baseX = initX - neighbourSize;
		int baseY = initY - neighbourSize;
		
		int width = SHAPE_SIZE * blockSize + 2 * neighbourhoodSize * neigbourBlockSize;
		int height = SHAPE_SIZE * blockSize + 2 * neighbourhoodSize * neigbourBlockSize;
		drawRectangle(g2d, color, baseX, baseY, width, height);
	}
	public static void drawOtherShapeNeighbourhood(Graphics2D g2d, int initX, int initY, int shapeBlockSize, Color color) {
		int blockSize = GeneratorConstants.BLOCK_SIZE;
		int neighbourhoodSize = GeneratorConstants.NEIGHBOURHOOD_GRID;
		int neighbourSize = neighbourhoodSize * blockSize;
		
		int baseX = initX - neighbourSize;
		int baseY = initY - neighbourSize;
		
		int width = neighbourSize + shapeBlockSize + neighbourSize;
		int height = neighbourSize + shapeBlockSize + neighbourSize;
		drawRectangle(g2d, color, baseX, baseY, width, height);
		
	}
	public static void drawBaseShape(Graphics2D g2d, int initX, int initY, Color color) {
		int blockSize = GeneratorConstants.BLOCK_SIZE;
		g2d.setColor(color);
		int x,y;
		x = initX;
		y = initY;
		drawSquare(g2d, x, y, blockSize);
		x = initX;
		y = initY + blockSize;
		drawSquare(g2d, x, y, blockSize);
		x = initX;
		y = initY + 2 * blockSize;
		drawSquare(g2d, x, y, blockSize);
		x = initX;
		y = initY + 3 * blockSize;
		drawSquare(g2d, x, y, blockSize);
		
		
		x = initX + blockSize;
		y = (int) Math.round(initY + 1.5 * blockSize);
		drawSquare(g2d, x, y, blockSize);
		
		x = initX + 2 * blockSize;
		y = (int) Math.round(initY + 1.5 * blockSize);
		drawSquare(g2d, x, y, blockSize);
		drawSquare(g2d, x, y, blockSize);
		
		
		x = initX + 3 * blockSize;
		y = initY;
		drawSquare(g2d, x, y, blockSize);
		x = initX + 3 * blockSize;
		y = initY + blockSize;
		drawSquare(g2d, x, y, blockSize);
		x = initX + 3 * blockSize;
		y = initY + 2 * blockSize;
		drawSquare(g2d, x, y, blockSize);
		x = initX + 3 * blockSize;
		y = initY + 3 * blockSize;
		drawSquare(g2d, x, y, blockSize);
	
	}
	
	private static Color getRandomColor() {
		int colourIndex = ThreadLocalRandom.current().nextInt(Constants.AVAILABLE_COLOURS.size());
		Color color = Constants.AVAILABLE_COLOURS.get(colourIndex);
		return color;
	}
	
	private static Color getColorMarkup(Color color) {
		return GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
				GeneratorConstants.COLOR_TO_LABEL_MAP.get(color));
	}
	
	private static Shape getRandomShape() {
		Random rand = new Random();
		int index = rand.nextInt(GeneratorConstants.availableShapes.size());
		return GeneratorConstants.availableShapes.get(index);
	}
	
	
	public static void drawRandomShape(Graphics2D trainG2d, Graphics2D resultG2d, int minX, int minY,
			int maxX, int maxY, Color color, int sizeModfier) {
		
		Shape chosenShape = getRandomShape();
		
		int blockSize = GeneratorConstants.BLOCK_SIZE;
		int figureSize = SHAPE_SIZE * blockSize * sizeModfier;
		int buffer = 15;
		int x = ThreadLocalRandom.current().nextInt(minX + buffer, maxX - figureSize - buffer + 1);
		int y = ThreadLocalRandom.current().nextInt(minY + buffer, maxY - figureSize - buffer + 1);
		
		drawOtherShape(trainG2d, x, y, color, chosenShape, sizeModfier);
		if (resultG2d != null) {
			Color resultColor = getColorMarkup(color);
			drawOtherShape(resultG2d, x, y, resultColor, chosenShape, sizeModfier);
		}
		
	}
	public static void drawRandomShape(Graphics2D trainG2d, Graphics2D resultG2d, int minX, int minY,
			int maxX, int maxY, int sizeModifier) {
		
		Color color = getRandomColor();
		drawRandomShape(trainG2d, resultG2d, minX, minY, maxX, maxY, color, sizeModifier);
	}
	
	public static void drawOtherShape(Graphics2D g2d, int x, int y, Color color, Shape chosenShape, int sizeModifier) {
		int blockSize = GeneratorConstants.BLOCK_SIZE;
		int figureSize = SHAPE_SIZE * blockSize * sizeModifier;
		g2d.setColor(color);
		
		if (chosenShape.equals(Shape.SQUARE)) {
			drawSquare(g2d, x, y, figureSize);
		} else if (chosenShape.equals(Shape.CIRCLE)) {
			drawCircle(g2d, x, y, figureSize);
		} else if (chosenShape.equals(Shape.PENTAGON)) {
			drawPentagon(g2d, x, y, figureSize);
		} else if (chosenShape.equals(Shape.HEXAGON)) {
			drawHexagon(g2d, x, y, figureSize);
		} else {
			_log.error("shape: " + chosenShape + " not implemented"); 
		}
	}
	
	public static void drawOtherShape(Graphics2D g2d, int x, int y, Color color, Shape chosenShape) {
		drawOtherShape(g2d, x, y, color, chosenShape, 1);
	}
	
	private static void drawHexagon(Graphics2D g2d, int initX, int initY, int diameter) {
		   Polygon p = new Polygon();
		   int radius = diameter / 2;
		   int x = initX + radius;
		   int y = initY + radius;
			
		   for (int i = 0; i < 6; i++)
		      p.addPoint((int) (x + radius * Math.cos(i * 2 * Math.PI / 6)),
		          (int) (y + radius * Math.sin(i * 2 * Math.PI / 6)));

		    g2d.fillPolygon(p);

		}
	
	private static void drawPentagon(Graphics2D g2d, int initX, int initY, int diameter) {
	   Polygon p = new Polygon();
	   int radius = diameter / 2;
	   int x = initX + radius;
	   int y = initY + radius;
		
	   for (int i = 0; i < 5; i++)
	      p.addPoint((int) (x + radius * Math.cos(i * 2 * Math.PI / 5)),
	          (int) (y + radius * Math.sin(i * 2 * Math.PI / 5)));

	    g2d.fillPolygon(p);

	}
	
	private static void drawCircle(Graphics2D g2d, int initX, int initY, int diameter) {
		int radius = diameter / 2;
		int x = initX + radius;
		int y = initY + radius;
		g2d.fillOval(x - radius, y - radius, diameter, diameter); 

	}
	
	private static void drawSquare(Graphics2D g2d, int x, int y, int size) {

		g2d.fill(new Rectangle2D.Double(x, y, size, size)); 
	}
	
	public static void drawRectangle(Graphics2D g2d, Color color, int x, int y, int width, int height) {
		g2d.setColor(color);
		g2d.fill(new Rectangle2D.Double(x, y, width, height)); 
	}

	private static Logger _log = Logger.getLogger(ShapeDrawer.class);

	
}
