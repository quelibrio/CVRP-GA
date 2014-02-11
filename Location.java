public class Location {

	private static final int X_COORDINATE = 0; // x-axis coordinate is dimension
	private static final int Y_COORDINATE = 1; // y-axis coordinate is dimension
	private int x_coord;
	private int y_coord;
	private int demand;
	private int index;
	private double degree;

	/*public Location(int x_coord, int y_coord, int demand) {
		this.x_coord = x_coord;
		this.y_coord = y_coord;
		this.demand = demand;		
	}*/

	public double getDegree() {
		return degree;
	}

	public void setDegree(double degree) {
		this.degree = degree;
	}

	@SuppressWarnings("static-access")
	public Location(int index) {
		CVRPData data = new CVRPData();
		this.x_coord = data.coords[index][X_COORDINATE];
		this.y_coord = data.coords[index][Y_COORDINATE];
		this.demand = data.demand[index];
		this.index = index;
		this.degree = 999;
	}
	
	public Location (Location location) {
		this.x_coord = location.x_coord;
		this.y_coord = location.y_coord;
		this.demand = location.demand;
		this.index = location.index;
		this.degree = location.degree;
	}
	
	@SuppressWarnings("static-access")
	public Location(int index, double degree) {
		CVRPData data = new CVRPData();
		this.x_coord = data.coords[index][X_COORDINATE];
		this.y_coord = data.coords[index][Y_COORDINATE];
		this.demand = data.demand[index];
		this.index = index;
		this.degree = degree;
	}

	public String toString() {
		return x_coord + " " + y_coord + " " + demand;
	}

	public int getX_coord() {
		return x_coord;
	}

	public int getY_coord() {
		return y_coord;
	}

	public int getDemand() {
		return demand;
	}
	
	public int getindex() {
		return index;
	}
}
