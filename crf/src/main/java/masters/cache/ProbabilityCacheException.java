package masters.cache;

public class ProbabilityCacheException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ProbabilityCacheException(Throwable ex){
		super(ex);
	}
	public ProbabilityCacheException(String msg) {
		super(msg);
	}
}
