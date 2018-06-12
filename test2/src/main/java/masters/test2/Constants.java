package masters.test2;

import org.apache.log4j.Logger;

import masters.test2.sampler.GibbsSampler;

public class Constants {

	public static enum ColorSpace {
	    RGB, HSL, CIELAB
	}
	
	public static ColorSpace colorSpace = ColorSpace.RGB; 
	public static Logger _log = Logger.getLogger(App.class);
//	public static ColorSpace colorSpace = ColorSpace.HSL; 
//	public static ColorSpace colorSpace = ColorSpace.CIELAB; 
}
