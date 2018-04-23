package masters.test2.crf;

import java.util.List;

public interface Node {
	public List<Double> getEnergies();
	
	public double getEnergy(int label1, int label2);
	
	public String toString();
}
