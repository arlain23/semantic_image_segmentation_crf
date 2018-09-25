package masters.grid;

import java.io.Serializable;

public class GridPoint implements Serializable {
	private static final long serialVersionUID = -7255173558134827051L;
	
	public int x;
	public int y;
	
	public GridPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public double getDistance(GridPoint otherPoint) {
		return Math.sqrt(Math.pow(x - otherPoint.x, 2) + Math.pow(y - otherPoint.y, 2));
	}
	@Override
	public String toString() {
		return "GridPoint (" + x + "," + y + ")";
	}
	
	
}
