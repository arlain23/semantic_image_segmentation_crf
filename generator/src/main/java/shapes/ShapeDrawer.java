package shapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import generator.GeneratorConstants;

public class ShapeDrawer {
	
	private static final int BASE_BLOCK_SIZE = GeneratorConstants.BLOCK_SIZE;
	public static void drawCircle(Graphics2D g2d, Color color, int x, int y, int radius) {
	    g2d.setColor(color);
	    g2d.fill(new Ellipse2D.Float(x, y, radius, radius));
	}
	public static void initDraw(Graphics2D trainG2d, Graphics2D resultG2d) {
		Random random = new Random();
		int scale = random.nextInt(GeneratorConstants.MAX_SCALE) + 1;
		int blockSize = scale * BASE_BLOCK_SIZE;
		
		//get init position
		int initX = random.nextInt(GeneratorConstants.WIDTH/2) + GeneratorConstants.WIDTH/4;
		int initY = random.nextInt(GeneratorConstants.HEIGHT/2) + GeneratorConstants.HEIGHT/4;
		Color foregroundColor = GeneratorConstants.FOREGROUND_COLOR;
		Color neighbourColor = Color.GREEN;
		
		drawBaseShape(trainG2d, initX, initY, blockSize, foregroundColor);
		drawBaseShapeNeighbourhood(trainG2d, initX, initY, blockSize, scale, neighbourColor);
		
		if (resultG2d != null){
			Color foregroundMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(GeneratorConstants.COLOR_TO_LABEL_MAP.size());
			Color neighbourMarkup = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
					GeneratorConstants.COLOR_TO_LABEL_MAP.get(neighbourColor));
			
			drawBaseShape(resultG2d, initX, initY, blockSize, foregroundMarkup);
			drawBaseShapeNeighbourhood(resultG2d, initX, initY, blockSize, scale, neighbourMarkup);
		}
	}
	public static void drawBaseShapeNeighbourhood(Graphics2D g2d, int initX, int initY, int blockSize, int scale, Color color) {
		int baseX = initX - GeneratorConstants.NEIGHBOURHOOD_GRID_MAX_LEVEL * BASE_BLOCK_SIZE;
		int baseY = initY - GeneratorConstants.NEIGHBOURHOOD_GRID_MAX_LEVEL * BASE_BLOCK_SIZE;
		
		int topBorderY = initY + blockSize;
		int bottomBorderY = initY + 2 * blockSize;
		
		int y = baseY;
		//first row
		for (int x = 0; x < (4 + 3 * scale); x++) {
			drawSquare(g2d, color, baseX + x * BASE_BLOCK_SIZE, y, BASE_BLOCK_SIZE);
		}
		//second row
		y = baseY + BASE_BLOCK_SIZE;
		for (int x = 0; x < (4 + 3 * scale); x++) {
			drawSquare(g2d, color, baseX + x * BASE_BLOCK_SIZE, y, BASE_BLOCK_SIZE);
		}
		
		for (int i = 0; i < scale; i++) {
			y = baseY + (2+i)*BASE_BLOCK_SIZE;
			int x = baseX;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
			x += BASE_BLOCK_SIZE;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
			x += BASE_BLOCK_SIZE + blockSize;
			for (int j = 0; j < scale; j++) {
				drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
				x += BASE_BLOCK_SIZE;
			}
			x += blockSize;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
			x += BASE_BLOCK_SIZE;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
		}
		
		for (int i = 0; i < scale; i++) {
			y = baseY + (2+i)*BASE_BLOCK_SIZE + blockSize;
			int x = baseX;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
			x += BASE_BLOCK_SIZE;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
			
			x += BASE_BLOCK_SIZE + 3* blockSize;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
			x += BASE_BLOCK_SIZE;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
		}
		
		for (int i = 0; i < scale; i++) {
			y = baseY + (2+i)*BASE_BLOCK_SIZE + 2*blockSize;
			int x = baseX;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
			x += BASE_BLOCK_SIZE;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
			x += BASE_BLOCK_SIZE + blockSize;
			for (int j = 0; j < scale; j++) {
				drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
				x += BASE_BLOCK_SIZE;
			}
			x += blockSize;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
			x += BASE_BLOCK_SIZE;
			drawSquare(g2d, color, x, y, BASE_BLOCK_SIZE);
		}
		
		//second last row
		y = baseY + 2 * BASE_BLOCK_SIZE + 3 * blockSize;
		for (int x = 0; x < (4 + 3 * scale); x++) {
			drawSquare(g2d, color, baseX + x * BASE_BLOCK_SIZE, y, BASE_BLOCK_SIZE);
		}
		//last row
		y = baseY + 3 * BASE_BLOCK_SIZE + 3 * blockSize;
		for (int x = 0; x < (4 + 3 * scale); x++) {
			drawSquare(g2d, color, baseX + x * BASE_BLOCK_SIZE, y, BASE_BLOCK_SIZE);
		}
		
		//first column
	}
	public static void drawBaseShape(Graphics2D g2d, int initX, int initY, int blockSize, Color color) {
		int x,y;
		x = initX;
		y = initY;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX + 2 * blockSize;
		y = initY;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX;
		y = initY + blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX + blockSize;
		y = initY + blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX + 2 * blockSize;
		y = initY + blockSize;
		drawSquare(g2d, color, x, y, blockSize);
		x = initX;
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
