import java.util.ArrayList;
import java.util.Random;

public class CVRP {

	private static final int DEPOT = 1;
	private ArrayList<Location> map; // current map
	private ArrayList<Location> mapRecord; // the original map which doesn't
											// change
	private ArrayList<ArrayList<Integer>> population;
	private ArrayList<ArrayList<Integer>> bestRoutes;
	private double bestCost = 99999;

	public static void main(String[] args) {
		
		int popNum = Integer.parseInt(args[0]);
		int generation = Integer.parseInt(args[1]);
		int crossRate = Integer.parseInt(args[2]);
		int mutationRate = Integer.parseInt(args[3]);
		CVRP cvrp = new CVRP();		
		cvrp.run(popNum, generation, crossRate, mutationRate);		
		System.out.println("Best solution: ");
		for (int i = 0; i < cvrp.bestRoutes.size(); i ++) {
			System.out.println(cvrp.bestRoutes.get(i));
		}
		System.out.println(cvrp.bestCost);

	}

	/* operation over generation */
	private void run(int popNum, int generation, int crossRate,
			int mutationRate) {
		initMap();
		ArrayList<Integer> bestRoute = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> routeMin = new ArrayList<ArrayList<Integer>>();
		double costMin = 9999;
		double costMinTotal = 0;
		Truck truck;

		do {
			costMin = 9999;
			truck = new Truck();
			moveTruckAdvanced(truck);
			if (truck.getRouteInIndex().size() <= 2)
				break;
			ArrayList<Integer> routeConvert = new ArrayList<Integer>();
			for (int i = 0; i < truck.getRoute().size(); i ++)
				routeConvert.add(truck.getRoute().get(i).getindex());
			populate(routeConvert, popNum);
			for (int i = 0; i < generation; i++) {
				crossOver(crossRate);
				mutation(mutationRate);
				for (int j = 0; j < population.size(); j++) {
					if (calcRouteInt(new ArrayList<Integer>(population.get(j))) < costMin) {
						costMin = calcRouteInt(new ArrayList<Integer>(
								population.get(j)));
						bestRoute = new ArrayList<Integer>(population.get(j));
					}
				}				
			}
			costMinTotal += costMin;
			routeMin.add(new ArrayList<Integer>(bestRoute));
		} while (truck.getRoute().size() > 2);

		if (costMinTotal < bestCost) {
			bestCost = costMinTotal;
			bestRoutes = new ArrayList<ArrayList<Integer>>(routeMin);
		}
	}

	/* cross over the population one time */
	private ArrayList<ArrayList<Integer>> crossOver(int crossRate) {
		ArrayList<ArrayList<Integer>> nextGen = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> parents = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> parent1 = new ArrayList<Integer>();
		ArrayList<Integer> parent2 = new ArrayList<Integer>();
		ArrayList<Integer> child1 = new ArrayList<Integer>();
		ArrayList<Integer> child2 = new ArrayList<Integer>();
		Random random = new Random();
		int position1 = 0;
		int position2 = 0;
		// for (int i = 0; i < generation; i ++) {
		while (population.size() != 0) {
			parents = getParents(crossRate);
			parent1 = parents.get(0);
			parent2 = parents.get(1);
			child1 = parent1;
			child2 = parent2;
			position1 = random.nextInt(parent1.size() - 2) + 1;
			position2 = random.nextInt(parent1.size() - 2 - position1 + 1)
					+ position1 + 1;
			for (int j = position1; j <= position2; j++) {
				int gene1 = child1.get(j);
				int gene2 = child2.get(j);
				int tmp1 = gene2;
				int tmp2 = gene1;
				child1.set(child1.indexOf(gene2), gene1);
				child1.set(j, new Integer(tmp1));
				child2.set(child2.indexOf(gene1), gene2);
				child2.set(j, new Integer(tmp2));
			}
			nextGen.add(new ArrayList<Integer>(child1));
			nextGen.add(new ArrayList<Integer>(child2));
		}
		population = nextGen;
		return nextGen;
	}

	/* get two parents from populatioin for crossover */
	private ArrayList<ArrayList<Integer>> getParents(int crossRate) {
		Random random = new Random();
		ArrayList<ArrayList<Integer>> parent = new ArrayList<ArrayList<Integer>>();
		int prob = 0;
		double fitness = 0;
		while ((population.size() != 0) && (parent.size() < 2)) {
			prob = random.nextInt(population.size());
			if (prob < crossRate) {
				fitness = random.nextDouble() * getFitnessTotal(population);
				if (fitness < getFitness(population, prob)) {
					parent.add(new ArrayList<Integer>(population.get(prob)));
					population.remove(prob);
				}
			}
		}
		return parent;
	}

	/* calculate the fitness of a chromosome */
	private double getFitness(ArrayList<ArrayList<Integer>> pop, int index) {
		double fitness = 0;
		double totalLength = 0;
		for (int i = 0; i < pop.size(); i++) {
			totalLength += calcRouteInt(pop.get(i));
		}
		fitness = totalLength / calcRouteInt(pop.get(index));
		return fitness;
	}

	/* calculate the fitness of a population */
	private double getFitnessTotal(ArrayList<ArrayList<Integer>> pop) {
		double fitnessTotal = 0;
		double totalLength = 0;
		for (int i = 0; i < pop.size(); i++) {
			totalLength += calcRouteInt(pop.get(i));
		}
		for (int i = 0; i < pop.size(); i++) {
			fitnessTotal += totalLength / calcRouteInt(pop.get(i));
		}
		return fitnessTotal;
	}

	/* make a population based on original route, with mutation rate of 50 */
	private void populate(ArrayList<Integer> route, int popNum) {
		population = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> tmp = new ArrayList<Integer>();
		for (int i = 0; i < popNum; i++) {
			tmp = mutationInt(route, 75);
			population.add(new ArrayList<Integer>(tmp));
		}
	}

	/*
	 * move the truck continuously until run out of buns how aggressive it is to
	 * choose the closest customer every time is controlled by argument
	 * aggression
	 */
	private void moveTruck(Truck truck, int aggression) {
		double previousDgree = 0;
		while (true) {
			Random random = new Random(); // random generator
			int counter = 0; // for random search count
			int prob; // probability of being aggressive
			int location; // random search location
			int dest = -1; // closest destination
			double distMin = 9999; // minimum distance
			int cost = 0; // cost of the route
			prob = random.nextInt(100); // get random number
			// if not being aggressive, explore the map for a random location
			if (prob > aggression) {
				do {
					counter++;
					location = random.nextInt(map.size() - 2) + 2;
					if (counter == 100) // stop the loop when no idea result
										// found
						break;
				} while ((map.get(location) == null)
						|| (truck.getRemaining() < getDemand(location)));
				// skip random location when invalid
				if ((map.get(location) != null)
						&& (truck.getRemaining() >= getDemand(location))) {
					distMin = getDist(truck.getLocation(), map.get(location));
					dest = location;
					cost = getDemand(location);
				}
			} else { // if being aggressive, use the closest location as
						// destination
				for (int i = 2; i < map.size(); i++) {
					if ((map.get(i) != null)
							&& (truck.getRemaining() >= getDemand(i))) {
						if ((getDist(truck.getLocation(), map.get(i)) + 0
								* getDist(truck.getLocation(), map.get(i))
								* (double) Math.abs((previousDgree - getDegree(
										truck.getLocation(), map.get(i)))
										/ getDegree(truck.getLocation(),
												map.get(i)))) < distMin) {
							distMin = getDist(truck.getLocation(), map.get(i))
									+ 0
									* getDist(truck.getLocation(), map.get(i))
									* (double) Math
											.abs((previousDgree - getDegree(
													truck.getLocation(),
													map.get(i)))
													/ getDegree(
															truck.getLocation(),
															map.get(i)));
							dest = i;
							cost = getDemand(i);
						}
					}
				}
			}
			/*
			 * if did not find a destination, which is due to insufficient buns
			 * break the loop, and this is the life of one truck
			 */

			if (dest == -1)
				break;
			/*
			 * update truck information and remove visited location, so that it
			 * will not be searched by another truck
			 */
			previousDgree = getDegree(truck.getLocation(), map.get(dest));
			truck.setLocation(map.get(dest));
			truck.setRemaining(truck.getRemaining() - cost);
			truck.addRoute(map.get(dest));
			truck.addRouteInIndex(dest);
			truck.addLength(distMin);
			map.set(dest, null);
		}
		truck.addRoute(map.get(DEPOT));
		truck.addRouteInIndex(DEPOT);
		truck.addLength(getDist(truck.getLocation(), map.get(DEPOT)));
	}

	private void moveTruckAdvanced(Truck truck) {
		for (int i = 2; i < map.size(); i++) {
			//System.out.println(truck.getRemaining());
			if (map.get(i) == null)
				continue;
			if (truck.getRemaining() < getDemand(i))
				break;
			if ((map.get(i) != null) && (truck.getRemaining() >= getDemand(i))) {
				truck.setRemaining(truck.getRemaining() - getDemand(i));
				truck.addLength(getDist(truck.getLocation(), map.get(i)));
				truck.setLocation(map.get(i));
				truck.addRoute(map.get(i));
				truck.addRouteInIndex(i);
				map.set(i, null);
			}
		}
		truck.addRoute(map.get(DEPOT));
		truck.addRouteInIndex(DEPOT);
		truck.addLength(getDist(truck.getLocation(), map.get(DEPOT)));
	}

	/* calculate degree between two locations */
	private double getDegree(Location target, Location origin) {
		double degree = Math.toDegrees(Math.atan((double) ((double) (target
				.getY_coord() - origin.getY_coord()))
				/ ((double) (target.getX_coord() - origin.getX_coord()))));
		return degree;
	}

	/* route mutation, which switches the order of locations */
	private void mutation(int mutationRate) {
		for (int i = 0; i < population.size(); i++) {
			population.set(i, mutationInt(population.get(i), mutationRate));
		}
	}

	private ArrayList<Integer> mutationInt(ArrayList<Integer> route,
			int mutationRate) {

		int tmp;
		Random random = new Random();
		int prob;
		for (int i = 1; i < route.size() - 1; i++) {
			prob = random.nextInt(100);
			if (prob <= mutationRate) {
				tmp = 0;
				prob = random.nextInt(route.size() - 2);
				tmp = route.get(i);
				route.set(i, route.get(prob + 1));
				route.set(prob + 1, tmp);
			}
		}
		return route;
	}

	/* convert location to integer */
	private ArrayList<Integer> locToInt(ArrayList<Location> route) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < route.size(); i++) {
			result.add(route.get(i).getindex());
		}
		return result;
	}

	/* compute whole length of a route */
	private double calcRoute(ArrayList<Location> route) {
		double result = 0;
		for (int i = 0; i < route.size() - 1; i++)
			result += getDist(route.get(i), route.get(i + 1));
		return result;
	}

	private double calcRouteInt(ArrayList<Integer> route) {
		double result = 0;
		for (int i = 0; i < route.size() - 1; i++){
			result += getDist(route.get(i), route.get(i + 1));
			//System.out.println(route.get(i));
		}
		return result;
	}

	/* get the demand of a customer by its index */
	private int getDemand(int index) {
		return map.get(index).getDemand();
	}

	/* calculate distance between two locations */
	private double getDist(int from, int to) {
		int x1 = mapRecord.get(from).getX_coord();
		int y1 = mapRecord.get(from).getY_coord();
		int x2 = mapRecord.get(to).getX_coord();
		int y2 = mapRecord.get(to).getY_coord();

		return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}

	public double getDist(Location from, Location to) {
		int x1 = from.getX_coord();
		int y1 = from.getY_coord();
		int x2 = to.getX_coord();
		int y2 = to.getY_coord();

		return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}

	/* initialize map, put all the data into */
	private void initMap() {
		ArrayList<Location> tmpMap = new ArrayList<Location>();
		map = new ArrayList<Location>();
		mapRecord = new ArrayList<Location>();
		for (int i = 0; i < 77; i++) {
			map.add(new Location(i));
			if (i > 1) {
				if ((map.get(i).getX_coord() > map.get(DEPOT).getX_coord())
						&& (map.get(i).getY_coord() > map.get(DEPOT)
								.getY_coord()))
					map.get(i).setDegree(getDegree(map.get(i), map.get(DEPOT)));
				else if ((map.get(i).getX_coord() < map.get(DEPOT).getX_coord())
						&& (map.get(i).getY_coord() > map.get(DEPOT)
								.getY_coord()))
					map.get(i).setDegree(
							getDegree(map.get(i), map.get(DEPOT)) + 180);
				else if ((map.get(i).getX_coord() < map.get(DEPOT).getX_coord())
						&& (map.get(i).getY_coord() < map.get(DEPOT)
								.getY_coord()))
					map.get(i).setDegree(
							getDegree(map.get(i), map.get(DEPOT)) + 180);
				else if ((map.get(i).getX_coord() > map.get(DEPOT).getX_coord())
						&& (map.get(i).getY_coord() < map.get(DEPOT)
								.getY_coord()))
					map.get(i).setDegree(
							getDegree(map.get(i), map.get(DEPOT)) + 360);
				else if ((map.get(i).getX_coord() == map.get(DEPOT)
						.getX_coord())
						&& (map.get(i).getY_coord() > map.get(DEPOT)
								.getY_coord()))
					map.get(i).setDegree(getDegree(map.get(i), map.get(DEPOT)));
				else if ((map.get(i).getX_coord() == map.get(DEPOT)
						.getX_coord())
						&& (map.get(i).getY_coord() < map.get(DEPOT)
								.getY_coord()))
					map.get(i).setDegree(
							getDegree(map.get(i), map.get(DEPOT)) + 360);
				else if ((map.get(i).getX_coord() > map.get(DEPOT).getX_coord())
						&& (map.get(i).getY_coord() == map.get(DEPOT)
								.getY_coord()))
					map.get(i).setDegree(getDegree(map.get(i), map.get(DEPOT)));
				else if ((map.get(i).getX_coord() < map.get(DEPOT).getX_coord())
						&& (map.get(i).getY_coord() == map.get(DEPOT)
								.getY_coord()))
					map.get(i).setDegree(
							getDegree(map.get(i), map.get(DEPOT)) + 180);
			}
		}

		tmpMap.add(new Location(map.get(0)));
		tmpMap.add(new Location(map.get(DEPOT)));
		while (map.size() > 2) {
			int degreeMinIndex = 0;
			double degreeMin = 9999;
			for (int j = 2; j < map.size(); j++) {
				if (map.get(j).getDegree() <= degreeMin) {
					degreeMin = map.get(j).getDegree();
					degreeMinIndex = j;
				}
			}
			tmpMap.add(new Location(map.get(degreeMinIndex)));
			map.remove(degreeMinIndex);
		}
		map = new ArrayList<Location>(tmpMap);

		Random random = new Random();
		int ranNum = random.nextInt(75) + 2;
		ArrayList<Location> tmp1 = new ArrayList<Location>();
		ArrayList<Location> tmp2 = new ArrayList<Location>();
		ArrayList<Location> tmp3 = new ArrayList<Location>();
		for (int i = 0; i < 2; i++)
			tmp1.add(new Location(map.get(i)));
		for (int i = 2; i < ranNum; i++)
			tmp2.add(new Location(map.get(i)));
		for (int i = ranNum; i < map.size(); i++)
			tmp3.add(new Location(map.get(i)));
		map.clear();
		for (int i = 0; i < tmp1.size(); i++)
			map.add(tmp1.get(i));
		for (int i = 0; i < tmp3.size(); i++)
			map.add(tmp3.get(i));
		for (int i = 0; i < tmp2.size(); i++)
			map.add(tmp2.get(i));
		for (int i = 0; i < 77; i++)
			mapRecord.add(new Location(i));
	}
}
