package shapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import generator.GeneratorConstants;
import generator.GeneratorConstants.Shape;
import masters.superpixel.SuperPixelHelper;

public class ShapeDrawer {
	
	private static final int BASE_BLOCK_SIZE = GeneratorConstants.BLOCK_SIZE;
	private static final Color FOREGROUND_COLOR = GeneratorConstants.FOREGROUND_COLOR;
	private static final Color NEIGHBOUR_COLOR = GeneratorConstants.NEIGHBOUR_COLOR;
	private static int SHAPE_SIZE = 3;
	
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
			Color foregroundMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
					GeneratorConstants.COLOR_TO_LABEL_MAP.get(FOREGROUND_COLOR));
			Color neighbourMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
					GeneratorConstants.COLOR_TO_LABEL_MAP.get(NEIGHBOUR_COLOR));
			
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
		int neighbourSize = neighbourhoodSize * blockSize;
		int baseX = initX - neighbourSize;
		int baseY = initY - neighbourSize;
		
		int width = (neighbourhoodSize + SHAPE_SIZE + neighbourhoodSize) * blockSize;
		int height = (neighbourhoodSize + SHAPE_SIZE + neighbourhoodSize) * blockSize;
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
		
		
		x = initX + blockSize;
		y = initY + blockSize;
		drawSquare(g2d, x, y, blockSize);
		
		
		x = initX + 2 * blockSize;
		y = initY;
		drawSquare(g2d, x, y, blockSize);
		x = initX + 2 * blockSize;
		y = initY + blockSize;
		drawSquare(g2d, x, y, blockSize);
		x = initX + 2 * blockSize;
		y = initY + 2 * blockSize;
		drawSquare(g2d, x, y, blockSize);
	
	}
	
	public static void drawOtherShape(Graphics2D g2d, int x, int y, Color color, Shape chosenShape) {
		int blockSize = GeneratorConstants.BLOCK_SIZE;
		int figureSize = SHAPE_SIZE * blockSize;
		g2d.setColor(color);
		
		if (chosenShape.equals(Shape.SQUARE)) {
			drawSquare(g2d, x, y, figureSize);
		} else if (chosenShape.equals(Shape.CIRCLE)) {
			drawCircle(g2d, x, y, figureSize);
		} else if (chosenShape.equals(Shape.PENTAGON)) {
			drawPentagon(g2d, x, y, figureSize);
		} else {
			_log.error("shape: " + chosenShape + " not implemented"); 
		}
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
