package masters.factorisation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import masters.Constants;

public class Edge {
	private List<Double> factorToVariableMsgList;
	private List<Double> variableToFactorMsgList;
	
	Factor factor;
	Node variable;
	
	public Edge(Factor factor, Node variable) {
		factorToVariableMsgList = new ArrayList<Double>();
		variableToFactorMsgList = new ArrayList<Double>();
		for (int label = 0; label < Constants.NUMBER_OF_STATES; label++) {
			factorToVariableMsgList.add(0.0);
			variableToFactorMsgList.add(0.0);
		}
		this.factor = factor;
		this.variable = variable;
	}
	
	public List<Double> getFactorToVariableMsg() {
		return factorToVariableMsgList;
	}

	public void setFactorToVariableMsg(List<Double> factorToVariableMsg) {
		this.factorToVariableMsgList = factorToVariableMsg;
	}

	public void setFactorToVariableMsgValue(int index, double value) {
		this.factorToVariableMsgList.set(index, value);
	}
	
	public List<Double> getVariableToFactorMsg() {
		return variableToFactorMsgList;
	}

	public void setVariableToFactorMsg(List<Double> variableToFactorMsg) {
		this.variableToFactorMsgList = variableToFactorMsg;
	}
	
	public void setVariableToFactorMsgValue(int index, double value) {
		this.variableToFactorMsgList.set(index, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((factor == null) ? 0 : factor.hashCode());
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (factor == null) {
			if (other.factor != null)
				return false;
		} else if (!factor.equals(other.factor))
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}
	
	public String toString() {
		return "Edge: " + variable + " " + factor;
	}
	
}	
