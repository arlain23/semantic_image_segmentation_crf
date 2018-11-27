package colours;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import generator.GeneratorConstants;
import generator.GeneratorConstants.Direction;
import shapes.ShapeDrawer;

public class ColourUtil {
	public static void fillColour(Graphics2D trainG2d, Graphics2D resultG2d, Color color) {
		trainG2d.setColor(color);
		trainG2d.fillRect(0, 0, GeneratorConstants.WIDTH, GeneratorConstants.HEIGHT);
		
		if (resultG2d != null) {
			Color markupColor = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
					GeneratorConstants.COLOR_TO_LABEL_MAP.get(color));
			resultG2d.setColor(markupColor);
			resultG2d.fillRect(0, 0, GeneratorConstants.WIDTH, GeneratorConstants.HEIGHT);
		}
	}
	
	public static Color getRandomColor() {
		Random random = new Random();
		int index = random.nextInt(GeneratorConstants.AVAILABLE_COLOURS.size());
		return GeneratorConstants.AVAILABLE_COLOURS.get(index);
	}
	public static void fillGraphicsWithRandomColours(Graphics2D trainG2d, Graphics2D resultG2d, Color baseColour) {
		int step =  GeneratorConstants.WIDTH / GeneratorConstants.BLOCK_SIZE;
		Random random = new Random();
		int numberOfBlocks = random.nextInt(2) +  3;
		int imageWidth = GeneratorConstants.WIDTH;
		int imageHeight = GeneratorConstants.HEIGHT;
		
		Set<Direction> directionTaken = new HashSet<>();
		boolean wasBaseColourRepainted = false;
		for (int i = 0; i < numberOfBlocks; i++ ) {
			
			random = new Random();	
			Color color = null;
			do {
				int colorR = random.nextInt(100);
				if (colorR > 50) color =  Color.RED;
				else color = Color.GREEN;
				
				if (!color.equals(baseColour)) {
					wasBaseColourRepainted = true;
				}
			} while (!wasBaseColourRepainted);
			int cornerX = 0;
			int cornerY = 0;
			int width = 0;
			int height = 0;

			//get direction
			int middlePointX = imageWidth / 2;
			int middlePointY = imageHeight / 2;
			int x = 0,y = 0;
			Direction direction = null;
			boolean isDirectionUnique = false;
			
			while(!isDirectionUnique) {

				random = new Random();
				// get random pivot point
				x = (random.nextInt(step - 6) + 3) * GeneratorConstants.BLOCK_SIZE;
				y = (random.nextInt(step - 6) + 3) * GeneratorConstants.BLOCK_SIZE;
				
				if (x >= middlePointX && y <= middlePointY) {
					direction = Direction.UP_RIGHT;
				} else if (x >= middlePointX && y > middlePointY) {
					direction = Direction.DOWN_RIGHT;
				} else if (x < middlePointX && y <= middlePointY) {
					direction = Direction.UP_LEFT;
				} else {
					direction = Direction.DOWN_LEFT;
				}
				if (!directionTaken.contains(direction)) {
					directionTaken.add(direction);
					isDirectionUnique = true;
				}
			}
			
			switch (direction) {
				case DOWN_RIGHT: 
					cornerX = x;
					cornerY = y;
					
					width = imageWidth - x;
					height = imageHeight - y;
					
					break;
				case DOWN_LEFT:
					cornerX = 0;
					cornerY = y;
					
					width = x;
					height = y;
					
					break;
				case UP_LEFT: 
					cornerX = 0;
					cornerY = 0;
					
					width = x;
					height = y;
	
					break;
				case UP_RIGHT:  
					cornerY = 0;
					cornerX = x;
					
					width = imageWidth - x;
					height = y;
					break;
			}
			
			ShapeDrawer.drawRectangle(trainG2d, color, cornerX, cornerY, width, height);
			Color markupColor = GeneratorConstants.LABEL_TO_MARKUP_MAP.get(
					GeneratorConstants.COLOR_TO_LABEL_MAP.get(color));
			
			if (resultG2d != null) {
				ShapeDrawer.drawRectangle(resultG2d, markupColor, cornerX, cornerY, width, height);
				
			}
		}
		
	}
}
