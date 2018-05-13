package masters.test2.factorisation;

import java.util.Objects;

public class FactorEdgeKey {
    private final Factor factor;
    private final Node variable;

    public FactorEdgeKey(Factor factor, Node variable) {
        this.factor = Objects.requireNonNull(factor);
        this.variable = Objects.requireNonNull(variable);
    }

    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FactorEdgeKey other = (FactorEdgeKey) obj;
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

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((factor == null) ? 0 : factor.hashCode());
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "FactorEdgeKey [factor=" + factor + ", variable=" + variable + "]";
	}

	public Factor getFactor() {
		return factor;
	}

	public Node getVariable() {
		return variable;
	}
	
    
    

}
