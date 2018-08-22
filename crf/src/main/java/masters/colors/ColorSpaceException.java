package masters.colors;

public class ColorSpaceException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public ColorSpaceException(Throwable ex) {
		super(ex);
	}
	public ColorSpaceException(String msg) {
		super(msg);
	}
}
