package shapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import generator.GeneratorConstants;

public class ShapeDrawer {
	
	private static final int BASE_BLOCK_SIZE = GeneratorConstants.BLOCK_SIZE;
	private static final Color FOREGROUND_COLOR = GeneratorConstants.FOREGROUND_COLOR;
	private static final Color NEIGHBOUR_COLOR = GeneratorConstants.NEIGHBOUR_COLOR;
	
	public static void drawCircle(Graphics2D g2d, Color color, int x, int y, int radius) {
	    g2d.setColor(color);
	    g2d.fill(new Ellipse2D.Float(x, y, radius, radius));
	}
	
	public static void initDraw(Graphics2D trainG2d, Graphics2D resultG2d) {
		Random random = new Random();
		double scale = 0.5;
		int blockSize = (int)(scale * BASE_BLOCK_SIZE);
		
		int blockDivider = 1;
		int buffer = GeneratorConstants.NEIGHBOURHOOD_GRID_MAX_LEVEL;

		// base shape
		int figureSizeH = (int)(3 * scale);
		
		int initH_XGridPos= random.nextInt(blockDivider * ((int)GeneratorConstants.WIDTH/BASE_BLOCK_SIZE - buffer - figureSizeH)) + blockDivider * buffer;
		int initH_YGridPos = random.nextInt(blockDivider * ((int)GeneratorConstants.HEIGHT/BASE_BLOCK_SIZE - buffer - figureSizeH)) + blockDivider * buffer;
		
		int initH_X = (initH_XGridPos / blockDivider) * BASE_BLOCK_SIZE;
		int initH_Y = (initH_YGridPos / blockDivider) * BASE_BLOCK_SIZE;
		
		//get boundaries
		int xminH, xmaxH, yminH, ymaxH;
		xminH = initH_X;
		xmaxH = xminH + (buffer + figureSizeH + buffer) * blockSize;
		yminH = initH_Y;
		ymaxH = yminH + (buffer + figureSizeH + buffer) * blockSize; 
		
		
		
		
		// other regions
		int figureSize = (int)(2 * scale);
		boolean isHidden = true;
		
		int initXGridPos = 0,initYGridPos = 0;
		int xmin, xmax, ymin, ymax;
		int initX = 0, initY = 0;
		
		
		int counter = 0;
		while(isHidden) {
			initXGridPos = random.nextInt(blockDivider * ((int)GeneratorConstants.WIDTH/BASE_BLOCK_SIZE - buffer - figureSize)) + blockDivider * buffer;
			initYGridPos = random.nextInt(blockDivider * ((int)GeneratorConstants.HEIGHT/BASE_BLOCK_SIZE - buffer - figureSize)) + blockDivider * buffer;

			initX = (initXGridPos / blockDivider) * BASE_BLOCK_SIZE;
			initY = (initYGridPos / blockDivider) * BASE_BLOCK_SIZE;
			
			// get boundaries
			
			xmin = initX;
			xmax = xmin + (buffer + figureSize + buffer) * blockSize;
			ymin = initY;
			ymax = ymin + (buffer + figureSize + buffer) * blockSize; 
			
			if ((xmax < xminH || xmin > xmaxH) && (ymax < yminH || ymin > yminH)) {
				isHidden = false;
			}
			if (++counter > 100) {
				isHidden = false;
			}
		}

		//draw figures
		drawOtherRegions(trainG2d, resultG2d, initH_X, initH_Y, blockSize);
		drawMainShape(trainG2d, resultG2d, initX, initY, blockSize, scale);
	}
	public static void drawOtherRegions(Graphics2D trainG2d, Graphics2D resultG2d, int initX, int initY, int blockSize) {
		
		Color foregroundColor = FOREGROUND_COLOR;
		Color neighbourColor = NEIGHBOUR_COLOR;
		
		Color foregroundMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
				GeneratorConstants.COLOR_TO_LABEL_MAP.get(foregroundColor));
		Color neighbourMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
				GeneratorConstants.COLOR_TO_LABEL_MAP.get(NEIGHBOUR_COLOR));
		
		drawOtherRegions(trainG2d, initX, initY, blockSize, neighbourColor, foregroundColor);
		if (resultG2d != null) {
			drawOtherRegions(resultG2d, initX, initY, blockSize, neighbourMarkup, foregroundMarkup);
		}
	}
	
	private static void drawOtherRegions(Graphics2D g2d, int initX, int initY, int blockSize, Color neighbourColor, Color foregroundColor) {
		//first row
		int tmpX, tmpY;
		
		tmpX = initX;
		tmpY = initY;
		for (int i = 0; i < 6; i++) {
			drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
			tmpX += blockSize;
		}
		
		//second row
		tmpX = initX;
		tmpY = initY + blockSize;
		
		for (int i = 0; i < 6; i++) {
			drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
			tmpX += blockSize;
		}
		
		//figure row 
		tmpX = initX;
		tmpY = initY + 2 * blockSize;
		drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		drawSquare(g2d, foregroundColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		drawSquare(g2d, foregroundColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		
		tmpX = initX;
		tmpY = initY + 3 * blockSize;
		drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		drawSquare(g2d, foregroundColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		drawSquare(g2d, foregroundColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
		tmpX += blockSize;
		
		//second last
		tmpX = initX;
		tmpY = initY + 4 * blockSize;
		
		for (int i = 0; i < 6; i++) {
			drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
			tmpX += blockSize;
		}
		
		//last
		tmpX = initX;
		tmpY = initY + 5 * blockSize;
		
		for (int i = 0; i < 6; i++) {
			drawSquare(g2d, neighbourColor, tmpX, tmpY, blockSize);
			tmpX += blockSize;
		}
	}
	public static void drawMainShape(Graphics2D trainG2d, Graphics2D resultG2d, int initX, int initY, int blockSize, double scale) {
		drawBaseShape(trainG2d, initX, initY, blockSize, FOREGROUND_COLOR);
		drawBaseShapeNeighbourhood(trainG2d, initX, initY, blockSize, scale, NEIGHBOUR_COLOR);
		
		if (resultG2d != null){
			Color foregroundMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(GeneratorConstants.COLOR_TO_LABEL_MAP.size());
			Color neighbourMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
					GeneratorConstants.COLOR_TO_LABEL_MAP.get(NEIGHBOUR_COLOR));
			
			drawBaseShape(resultG2d, initX, initY, blockSize, foregroundMarkup);
			drawBaseShapeNeighbourhood(resultG2d, initX, initY, blockSize, scale, neighbourMarkup);
		}
		
	}
	public static void drawBaseShapeNeighbourhood(Graphics2D g2d, int initX, int initY, int blockSize, double scale, Color color) {
		int baseX = initX - GeneratorConstants.NEIGHBOURHOOD_GRID_MAX_LEVEL * blockSize;
		int baseY = initY - GeneratorConstants.NEIGHBOURHOOD_GRID_MAX_LEVEL * blockSize;
		
		int y = baseY;
		//first row
		for (int x = 0; x < 7; x++) {
			drawSquare(g2d, color, baseX + x * blockSize, y, blockSize);
		}
		//second row
		y = baseY + blockSize;
		for (int x = 0; x < 7; x++) {
			drawSquare(g2d, color, baseX + x * blockSize, y, blockSize);
		}
		//third row
		y = baseY + 2 * blockSize;
		for (int x = 0; x < 2; x++) {
			drawSquare(g2d, color, baseX + x * blockSize, y, blockSize);
		}
		for (int x = 3; x < 7; x++) {
			drawSquare(g2d, color, baseX + x * blockSize, y, blockSize);
		}
		// 4th row
		y = baseY + 3 * blockSize;
		for (int x = 0; x < 2; x++) {
			drawSquare(g2d, color, baseX + x * blockSize, y, blockSize);
		}
		for (int x = 4; x < 7; x++) {
			drawSquare(g2d, color, baseX + x * blockSize, y, blockSize);
		}
		//5th row 
		y = baseY + 4 * blockSize;
		for (int x = 0; x < 2; x++) {
			drawSquare(g2d, color, baseX + x * blockSize, y, blockSize);
		}
		for (int x = 5; x < 7; x++) {
			drawSquare(g2d, color, baseX + x * blockSize, y, blockSize);
		}
		//second last row
		y = baseY + 2 * blockSize + 3 * blockSize;
		for (int x = 0; x < 7; x++) {
			drawSquare(g2d, color, baseX + x * blockSize, y, blockSize);
		}
		//last row
		y = baseY + 3 * blockSize + 3 * blockSize;
		for (int x = 0; x < 7; x++) {
			drawSquare(g2d, color, baseX + x * blockSize, y, blockSize);
		}
		
	}
	public static void drawBaseShape(Graphics2D g2d, int initX, int initY, int blockSize, Color color) {
		int x,y;
		x = initX;
		y = initY;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX;
		y = initY + blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX + blockSize;
		y = initY + blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX;
		y = initY + 2 * blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX + blockSize;
		y = initY + 2 * blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX + 2 * blockSize;
		y = initY + 2 * blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		
	}
	public static void drawSquare(Graphics2D g2d, Color color, int x, int y, int size) {
		g2d.setColor(color);
		g2d.fill(new Rectangle2D.Double(x, y, size, size)); 
	}
}
