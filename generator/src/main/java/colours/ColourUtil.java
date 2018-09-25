package colours;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

import generator.GeneratorConstants;
import shapes.ShapeDrawer;

public class ColourUtil {
	public static void fillColour(Graphics2D g2d, Color color) {
		g2d.setColor(color);
        g2d.fillRect(0, 0, GeneratorConstants.WIDTH, GeneratorConstants.HEIGHT);
	}
	public static Color getRandomColor() {
		Random random = new Random();
		int index = random.nextInt(GeneratorConstants.AVAILABLE_COLOURS.size());
		return GeneratorConstants.AVAILABLE_COLOURS.get(index);
	}
	public static void fillGraphicsWithRandomColours(Graphics2D trainG2d, Graphics2D resultG2d) {
		Random random = new Random();
		int blockSize = 1 * GeneratorConstants.BLOCK_SIZE;
		int numberOfXBlocks = GeneratorConstants.WIDTH / blockSize;
		int numberOfYBlocks = GeneratorConstants.HEIGHT / blockSize;
		
		Color previousColor = null;
		for (int y = 0; y < numberOfXBlocks; y++) {
			for (int x = 0; x < numberOfYBlocks; x++) {
				
				
				int r = random.nextInt(100);
				Color color;
				if (r > 50) color =  Color.BLUE;
				else if (r > 20) color = Color.RED;
				else color = Color.GREEN;
				
//				Color color = GeneratorConstants.AVAILABLE_COLOURS.get(
//						random.nextInt(GeneratorConstants.AVAILABLE_COLOURS.size()));
				
				if (random.nextInt(100) > 60  && previousColor != null) {
					color = previousColor;
				}

				ShapeDrawer.drawSquare(trainG2d, color, x*blockSize, y*blockSize, blockSize);
				
				Color markupColor = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
						GeneratorConstants.COLOR_TO_LABEL_MAP.get(color));
				
				if (resultG2d != null) {
					ShapeDrawer.drawSquare(resultG2d, markupColor, x*blockSize, y*blockSize, blockSize);
				}
				
				
				previousColor = color;
			}
		}
		
		
		
	}
}
