package masters.test2.sampler;

import java.util.ArrayList;
import java.util.List;

import masters.test2.superpixel.SuperPixelDTO;

public class ImageMask {
	private List<Integer> mask;
	
	public ImageMask(int maskSize) {
		mask = new ArrayList<Integer>(maskSize);
		
	}
	public void setMask(List<Integer> mask) {
		this.mask = mask;
	}
	public void setMaskValue(int index, int value) {
		this.mask.set(index, value);
	}
	public void setMaskWithOffset(List<Integer> mask, int offset) {
		for (int i = offset; i < mask.size(); i++) {
			setMaskValue(i, mask.get(i));
		}
	}
	public List<Integer> getMask() {
		return mask;
	}

}
