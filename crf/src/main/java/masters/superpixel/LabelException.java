package masters.superpixel;

public class LabelException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public LabelException(Throwable ex){
		super(ex);
	}
	public LabelException(String msg) {
		super(msg);
	}
}
