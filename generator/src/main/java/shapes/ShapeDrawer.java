package shapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import generator.GeneratorConstants;

public class ShapeDrawer {
	
	private static final int BASE_BLOCK_SIZE = GeneratorConstants.BLOCK_SIZE;
	private static final Color FOREGROUND_COLOR = GeneratorConstants.FOREGROUND_COLOR;
	private static final Color NEIGHBOUR_COLOR = GeneratorConstants.NEIGHBOUR_COLOR;
	private static int SHAPE_SIZE = 3;
	
	public static void drawCircle(Graphics2D g2d, Color color, int x, int y, int radius) {
	    g2d.setColor(color);
	    g2d.fill(new Ellipse2D.Float(x, y, radius, radius));
	}
	public static void drawPolygon(Graphics2D trainG2d, Graphics2D resultG2d) {
		Polygon p = new Polygon();
	    for (int i = 0; i < 5; i++) {
	    	p.addPoint((int) (100 + 50 * Math.cos(i * 2 * Math.PI / 5)),
	          (int) (100 + 50 * Math.sin(i * 2 * Math.PI / 5)));
	    }
	    trainG2d.drawPolygon(p);
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
		
		drawOtherRegions(trainG2d, resultG2d, initX, initY, blockSize);
	}
	
	public static void drawOtherRegions(Graphics2D trainG2d, Graphics2D resultG2d, int initX, int initY, int blockSize) {
		drawOtherShapeNeighbourhood(trainG2d, initX, initY, blockSize, NEIGHBOUR_COLOR );
		drawOtherShape(trainG2d, initX, initY, FOREGROUND_COLOR);
		
		if (resultG2d != null){
			Color foregroundMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
					GeneratorConstants.COLOR_TO_LABEL_MAP.get(FOREGROUND_COLOR));
			Color neighbourMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
					GeneratorConstants.COLOR_TO_LABEL_MAP.get(NEIGHBOUR_COLOR));
			
			drawOtherShapeNeighbourhood(resultG2d, initX, initY, blockSize, neighbourMarkup);
			drawOtherShape(resultG2d, initX, initY, foregroundMarkup);
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
		
		int x,y;
		x = initX;
		y = initY;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX;
		y = initY + blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX;
		y = initY + 2 * blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		
		
		x = initX + blockSize;
		y = initY + blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		
		
		x = initX + 2 * blockSize;
		y = initY;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX + 2 * blockSize;
		y = initY + blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX + 2 * blockSize;
		y = initY + 2 * blockSize;
		drawSquare(g2d, color, x, y, blockSize);
	
	}
	
	public static void drawOtherShape(Graphics2D g2d, int initX, int initY, Color color) {
		int blockSize = GeneratorConstants.BLOCK_SIZE;
		
		int x,y;
		x = initX;
		y = initY;
		drawSquare(g2d, color, x, y, SHAPE_SIZE * blockSize);
	
	}
	public static void drawSquare(Graphics2D g2d, Color color, int x, int y, int size) {
		g2d.setColor(color);
		g2d.fill(new Rectangle2D.Double(x, y, size, size)); 
	}
	public static void drawRectangle(Graphics2D g2d, Color color, int x, int y, int width, int height) {
		g2d.setColor(color);
		g2d.fill(new Rectangle2D.Double(x, y, width, height)); 
	}
}
