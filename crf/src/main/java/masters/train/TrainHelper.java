package masters.train;

import java.util.List;
import java.util.Map;

import masters.factorisation.FactorGraphModel;
import masters.image.ImageDTO;
import masters.utils.ParametersContainer;

public class TrainHelper {
	
	public static WeightVector train(List<Double> initWeightList, ParametersContainer parameterContainer) {
		WeightVector weights;
		if (initWeightList == null) {
			GradientDescentTrainer trainer = new GradientDescentTrainer(parameterContainer);
			  weights = trainer.train(null);
		} else {
			WeightVector pretrainedWeights = new WeightVector(initWeightList);
			GradientDescentTrainer trainer = new GradientDescentTrainer(parameterContainer);
			weights = trainer.train(pretrainedWeights);
		}
		return weights;
	}
}
