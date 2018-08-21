package masters.colors;

public class ColorSpaceConverter {

	/*********************  CIELab   *************************************/
	public static Double[] lab2rgb(double[] lab) {
		Double y = (lab[0] + 16) / 116.0;
		Double x = lab[1] / 500.0 + y;
		Double z = y - lab[2] / 200.0;
		Double r, g, b;

		x = 0.95047 * ((x * x * x > 0.008856) ? x * x * x : (x - 16/116) / 7.787);
		y = 1.00000 * ((y * y * y > 0.008856) ? y * y * y : (y - 16/116) / 7.787);
		z = 1.08883 * ((z * z * z > 0.008856) ? z * z * z : (z - 16/116) / 7.787);

		r = x *  3.2406 + y * -1.5372 + z * -0.4986;
		g = x * -0.9689 + y *  1.8758 + z *  0.0415;
		b = x *  0.0557 + y * -0.2040 + z *  1.0570;

		r = (r > 0.0031308) ? (1.055 * Math.pow(r, 1/2.4) - 0.055) : 12.92 * r;
		g = (g > 0.0031308) ? (1.055 * Math.pow(g, 1/2.4) - 0.055) : 12.92 * g;
		b = (b > 0.0031308) ? (1.055 * Math.pow(b, 1/2.4) - 0.055) : 12.92 * b;

		Double [] rgb = new Double[3];
		rgb[0] = Math.max(0, Math.min(1, r)) * 255.0;
		rgb[1] = Math.max(0, Math.min(1, g)) * 255.0;
		rgb[2] = Math.max(0, Math.min(1, b)) * 255.0;
		
		return rgb;
	}


	public static Double[] rgb2lab(double[] rgb) {
		
		Double r = rgb[0] / 255.0;
		Double g = rgb[1] / 255.0;
		Double b = rgb[2] / 255.0;
		Double x, y, z;

		r = (r > 0.04045) ? Math.pow((r + 0.055) / 1.055, 2.4) : r / 12.92;
		g = (g > 0.04045) ? Math.pow((g + 0.055) / 1.055, 2.4) : g / 12.92;
		b = (b > 0.04045) ? Math.pow((b + 0.055) / 1.055, 2.4) : b / 12.92;
		
		x = (r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.95047;
		y = (r * 0.2126 + g * 0.7152 + b * 0.0722) / 1.00000;
		z = (r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.08883;
		
		x = (x > 0.008856) ? Math.pow(x, 1.0/3.0) : (7.787 * x) + 16/116;
		y = (y > 0.008856) ? Math.pow(y, 1.0/3.0) : (7.787 * y) + 16/116;
		z = (z > 0.008856) ? Math.pow(z, 1.0/3.0) : (7.787 * z) + 16/116;
		
		Double[] lab = new Double [3];
		lab[0] = (116.0 * y) - 16.0;
		lab[1] = 500.0 * (x - y);
		lab[2] = 200.0 * (y - z);

		return lab;
	}

	public static Double deltaE(double[] labA, double[] labB) {
		
		Double deltaL = labA[0] - labB[0];
		Double deltaA = labA[1] - labB[1];
		Double deltaB = labA[2] - labB[2];
		Double c1 = Math.sqrt(labA[1] * labA[1] + labA[2] * labA[2]);
		Double c2 = Math.sqrt(labB[1] * labB[1] + labB[2] * labB[2]);
		Double deltaC = c1 - c2;
		Double deltaH = deltaA * deltaA + deltaB * deltaB - deltaC * deltaC;
		deltaH = deltaH < 0 ? 0 : Math.sqrt(deltaH);
		Double sc = 1.0 + 0.045 * c1;
		Double sh = 1.0 + 0.015 * c1;
		Double deltaLKlsl = deltaL / (1.0);
		Double deltaCkcsc = deltaC / (sc);
		Double deltaHkhsh = deltaH / (sh);
		Double i = deltaLKlsl * deltaLKlsl + deltaCkcsc * deltaCkcsc + deltaHkhsh * deltaHkhsh;
		
		return i < 0 ? 0 : Math.sqrt(i);
	}
	/**************************HSV**************************/
	
	public static Double[] rgb2hsv (double [] rgb) {
		
		Double r = rgb[0] / 255.0;
		Double g = rgb[1] / 255.0;
		Double b = rgb[2] / 255.0;
		
		Double computedH = 0.0;
		Double computedS = 0.0;
		Double computedV = 0.0;


		Double minRGB = Math.min(r, Math.min(g,b));
		Double maxRGB = Math.max(r, Math.max(g,b));

		 // Black-gray-white
		if (minRGB.equals(maxRGB)) {
			computedV = minRGB;
		  return new Double [] {0.0, 0.0, computedV};
		}

		 // Colors other than black-gray-white:
		Double d = (r.equals(minRGB) ? g-b : ((b.equals(minRGB)) ? r-g : b-r));
		Double h = (r.equals(minRGB) ? 3.0 : ((b.equals(minRGB)) ? 1.0 : 5.0));
		computedH = 60.0*(h - d/(maxRGB - minRGB));
		computedS = (maxRGB - minRGB)/maxRGB;
		computedV = maxRGB;
		return new Double[]{computedH, computedS, computedV};
	}
	
	/************************* HSL ************************/
	public static Double[] rgb2hsl(double[] rgb) {
		//  Get RGB values in the range 0 - 1

		Double r = rgb[0];
		Double g = rgb[1];
		Double b = rgb[2];

		//	Minimum and Maximum RGB values are used in the HSL calculations

		Double min = Math.min(r, Math.min(g, b));
		Double max = Math.max(r, Math.max(g, b));

		//  Calculate the Hue

		Double h = 0.0;

		if (max.equals(min))
			h = 0.0;
		else if (max.equals(r))
			h = ((60.0 * (g - b) / (max - min)) + 360.0) % 360.0;
		else if (max.equals(g))
			h = (60.0 * (b - r) / (max - min)) + 120.0;
		else if (max.equals(b))
			h = (60.0 * (r - g) / (max - min)) + 240.0;

		//  Calculate the Luminance

		Double l = (max + min) / 2.0;

		//  Calculate the Saturation

		Double s = 0.0;

		if (max.equals(min))
			s = 0.0;
		else if (l <= 0.5)
			s = (max - min) / (max + min);
		else
			s = (max - min) / (2.0 - max - min);

		Double[] hsl = new Double[3];
		hsl[0] = h;
		hsl[1] = s * 100.0;
		hsl[2] = l * 100.0;
		System.out.println("hsl " + h + " "  +  s + " "+ l);
		return hsl;
	}
	
	public static Double[] hsl2rgb(double[] hsl) {
		Double h = hsl[0];
		Double s = hsl[1];
		Double l = hsl[2];
		
		//  Formula needs all values between 0 - 1.
		h = h % 360.0;
		h /= 360.0;
		s /= 100.0;
		l /= 100.0;

		Double q = 0.0;

		if (l < 0.5)
			q = l * (1.0 + s);
		else
			q = (l + s) - (s * l);

		Double p = 2.0 * l - q;

		Double r = Math.max(0, hue2rgb(p, q, h + (1.0 / 3.0)));
		Double g = Math.max(0, hue2rgb(p, q, h));
		Double b = Math.max(0, hue2rgb(p, q, h - (1.0 / 3.0)));

		Double[] rgb = new Double[3];
		rgb[0] =  Math.min(r, 1.0);
		rgb[1] = Math.min(g, 1.0);
		rgb[2] =  Math.min(b, 1.0);
		
		return rgb;
	}
	
	private static Double hue2rgb(double p, double q, double h)
	{
		if (h < 0.0) h += 1.0;

		if (h > 1.0 ) h -= 1.0;

		if (6.0 * h < 1)
		{
			return p + ((q - p) * 6.0 * h);
		}

		if (2.0 * h < 1.0 )
		{
			return  q;
		}

		if (3.0 * h < 2.0)
		{
			return p + ( (q - p) * 6.0 * ((2.0 / 3.0) - h) );
		}

   		return p;
	}
	
	
	
}
