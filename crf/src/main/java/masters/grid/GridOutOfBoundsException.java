package masters.grid;

public class GridOutOfBoundsException  extends Exception {

	private static final long serialVersionUID = 1L;
	
	public GridOutOfBoundsException(Throwable ex) {
		super(ex);
	}
	public GridOutOfBoundsException(String msg) {
		super(msg);
	}

}
