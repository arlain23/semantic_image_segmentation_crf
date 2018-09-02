package graphics;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class GraphicsFactoryUtil {
	public static Graphics2D getGraphics(BufferedImage img) {
		return img.createGraphics();
	}
}
