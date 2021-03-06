package masters.features;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import masters.utils.Helper;

public class ValueStringMask implements Serializable {
	private static final long serialVersionUID = -5207202416466490499L;

	private List<String> valueList;
	private int listSize;

	public ValueStringMask (int listSize) {
		this.valueList = Helper.initFixedSizedListString(listSize);
		this.listSize = listSize;
	}

	public ValueStringMask (List<String> list) {
		this.valueList = list;
		this.listSize = list.size();
	}
	public ValueStringMask(ValueStringMask featureMask, BinaryMask labelMask) {
		this(featureMask.listSize);
		for (int i = 0; i < featureMask.listSize; i++) {
			if (labelMask.getValue(i) == 1) {
				setValue(i, featureMask.getValue(i));
			} else {
				setValue(i, null);
			}
		}
	}

	public String getValue(int index) {
		return valueList.get(index);
	}
	public void setValue(int index, String value) {
		valueList.set(index, value);
	}
	public int getListSize() {
		return listSize;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String val : valueList) {
			sb.append(val + " ");
		}
		return "ValueMask [" + sb.toString() + "]";
	}


}
