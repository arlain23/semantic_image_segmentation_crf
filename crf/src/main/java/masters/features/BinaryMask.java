package masters.features;

import java.io.Serializable;
import java.util.List;

import masters.image.ImageMask;
import masters.utils.Helper;

public class BinaryMask implements Serializable {
  private static final long serialVersionUID = -6289462124334681440L;
  
  private List<Integer> binaryList;
	private int listSize;
	
	
	public BinaryMask (BinaryMask mask1, BinaryMask mask2) {
		this(mask1.listSize);
		for (int bit = 0; bit < mask1.listSize; bit++) {
			setValue(bit, (mask1.getValue(bit) & mask2.getValue(bit)));
		}
	}
	
	public BinaryMask (int listSize) {
		this.binaryList = Helper.initFixedSizedListInteger(listSize);
		this.listSize = listSize;
	}
	
	public BinaryMask (List<Integer> list) {
		this.binaryList = list;
		this.listSize = list.size();
	}
	public BinaryMask(ImageMask mask, int label) {
		List<Integer> maskList = mask.getMask();
		binaryList = Helper.initFixedSizedListInteger(maskList.size());
		for (int i = 0; i < maskList.size(); i++) {
			if (maskList.get(i) == label) {
				binaryList.set(i, 1);
			} else {
				binaryList.set(i, 0);
			}
		}
		this.listSize = maskList.size();
	}


	public int getValue(int index) {
		return binaryList.get(index);
	}
	public void setValue(int index, int value) {
		binaryList.set(index, value);
	}
	public void switchOnByte(int index) {
		binaryList.set(index, 1);
	}
	public void switchOffByte(int index) {
		binaryList.set(index, 0);
	}
	
	public int getNumberOfOnBytes() {
		int sum = 0;
		for (Integer i : this.binaryList) {
			if (i == 1) sum++;
		}
		return sum;
	}
	
	public int getListSize() {
		return listSize;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Integer bit : binaryList) {
			sb.append(bit + " ");
		}
		return "BinaryMask [" + sb.toString() + "]";
	}
	
	
}
