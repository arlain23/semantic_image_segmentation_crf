package masters.test2;

import java.util.ArrayList;
import java.util.List;

public class Helper {
	public static List<Double> initFixedSizedListDouble(int listSize) {
		List<Double> result = new ArrayList<Double>();
		for (int i = 0; i < listSize; i++) {
			result.add(0.0);
		}
		return result;
	}

	public static void printList(List<Double> labelProbabilities) {
		System.out.print("( ");
		for (Double d : labelProbabilities) {
			System.out.print(d + " ");
		}
		System.out.println(" )");
	}
}
