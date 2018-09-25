package masters.concurency;

import java.util.Random;
import java.util.stream.IntStream;

import jcuda.Pointer;
import jcuda.runtime.JCuda;

public class JCudaUtil {
	public static void test () {
		
		int n = 100000;
		double[] a = initArray(n);
		double[] b = new double[n];
		double[] c = new double[n];
		long startTime, endTime;
		System.out.println("SEQENTIAL");
		startTime = System.nanoTime();
		sequentialTest(a, b, c, n);
		endTime = System.nanoTime();
		System.out.println(endTime - startTime);
		System.out.println("PARALLEL");
		startTime = System.nanoTime();
		parallelTest(a, b, c, n);
		endTime = System.nanoTime();
		System.out.println(endTime - startTime);
	}
	
	public static double[] initArray(int n) {
		double[] result = new double[n];
		Random rand = new Random();
		for (int i = 0; i < n; i++) {
			result[i] = rand.nextDouble();
		}
		return result;
		
	}
	public static void sequentialTest (double[] a, double[] b, double[] c, int n) {
		for (int i = 0; i < n; i++) {
			b[i] = Math.log(Math.pow(Math.sqrt(a[i]), 4)) * 2.0;
			c[i] = Math.log(Math.pow(Math.sqrt(a[i]), 3)) * 3.0;
		}
	}
	
	public static void parallelTest(double[] a, double[] b, double[] c, int n) {
		IntStream.range(0, n).parallel().forEach(i -> {
			b[i] = Math.log(Math.pow(Math.sqrt(a[i]), 0.5)) * 2.0;
			c[i] = Math.log(Math.pow(Math.sqrt(a[i]), 3)) * 3.0;
		});
	}
	
	
}
