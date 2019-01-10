package masters.features;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import masters.utils.Helper;

public class ValueDoubleMask implements Serializable {
  private static final long serialVersionUID = -5207202416466490499L;
  
  private List<Double> valueList;
	private int listSize;
	
	public ValueDoubleMask (int listSize) {
		this.valueList = Helper.initFixedSizedListDouble(listSize);
		this.listSize = listSize;
	}
	
	public ValueDoubleMask (List<Double> list) {
		this.valueList = list;
		this.listSize = list.size();
	}
	public ValueDoubleMask(ValueDoubleMask featureMask, BinaryMask labelMask) {
		this(featureMask.listSize);
		for (int i = 0; i < featureMask.listSize; i++) {
			if (labelMask.getValue(i) == 1) {
				setValue(i, featureMask.getValue(i));
			} else {
				setValue(i, null);
			}
		}
	}

	public Double getValue(int index) {
		return valueList.get(index);
	}
	public void setValue(int index, Double value) {
		valueList.set(index, value);
	}
	public int getListSize() {
		return listSize;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Double val : valueList) {
			sb.append(val + " ");
		}
		return "ValueMask [" + sb.toString() + "]";
	}

	public ValueDoubleMask getDifference(ValueDoubleMask trainingValueMask) {
		List<Double> differenceValues = new ArrayList<Double>();
		
		for (int i = 0; i < trainingValueMask.getListSize(); i++) {
			differenceValues.add(this.getValue(i) - trainingValueMask.getValue(i));
		}
		return new ValueDoubleMask(differenceValues);
	}
	
}
