package masters.image;

import java.util.ArrayList;
import java.util.List;

import masters.Constants;
import masters.utils.Helper;

public class ImageMask {
	private List<Integer> mask;
	
	public ImageMask(int maskSize) {
		mask = new ArrayList<Integer>(maskSize);
		
	}
	public ImageMask(List<Integer> mask) {
		this.mask = mask;
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
	public List<Integer> getLabelCounts() {
		List<Integer> labelCount = Helper.initFixedSizedListInteger(Constants.NUMBER_OF_STATES);
		for (Integer label : this.mask) {
			int previousValue = labelCount.get(label);
			labelCount.set(label, ++previousValue);
		}
		return labelCount;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ImageMask [ ");
		for (int i : mask) {
			sb.append(i + " ");
		}
		sb.append("]");
		return sb.toString();
	}
	

}
