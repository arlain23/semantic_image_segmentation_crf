package graphics;

import java.awt.image.BufferedImage;

import generator.GeneratorConstants;

public class BufferedImageFactoryUtil {
	public static BufferedImage getImage() {
		return new BufferedImage(GeneratorConstants.WIDTH, GeneratorConstants.HEIGHT,
                BufferedImage.TYPE_INT_RGB);
	}
}
