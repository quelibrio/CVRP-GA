import java.util.ArrayList;

public class Truck {

	private static final int DEPOT = 1; // index of depot on the map
	private static final int VEHICLE_CAPACITY = 220;	//capacity
	
	private int remaining;
	private double length;
	private Location location;
	private ArrayList<Location> route;
	private ArrayList<Integer> routeInIndex;
	
	public Truck() {
		route = new ArrayList<Location>();
		routeInIndex = new ArrayList<Integer>();
		this.remaining = VEHICLE_CAPACITY;
		this.length = 0;
		this.location = new Location(DEPOT);
		this.route.add(new Location(DEPOT));
		this.routeInIndex.add(DEPOT);
	}
	
	public void printRoute() {
		String result = routeInIndex.get(0).toString();
		for (int i = 1; i < routeInIndex.size(); i ++)
			result += ">" + routeInIndex.get(i).toString();
		System.out.println(result);
	}
	
	public void setRemaining(int remaining) {
		this.remaining = remaining;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void addRoute(Location dest) {
		this.route.add(dest);
	}
	
	public void addRouteInIndex(int dest) {
		this.routeInIndex.add(dest);
	}

	public int getRemaining() {
		return remaining;
	}

	public Location getLocation() {
		return location;
	}

	public ArrayList<Location> getRoute() {
		return route;
	}
	
	public ArrayList<Integer> getRouteInIndex() {
		return routeInIndex;
	}
	
	public void addLength (double length) {
		this.length += length;
	}
	
	public double getLength () {
		return this.length;
	}
}
