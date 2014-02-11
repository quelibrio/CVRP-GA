CVRP
====

This implement is a solution to the Capacitated Vehicle Routing Problem (CVRP)
##The FruityBun Challenge
Imagine that the FruityBun bakery wants to optimise its delivery of buns to shops. The delivery problem is a CVRP. The bakery decides to run a public contest to find the most efficient solution to their current CVRP and they publish its specification. However, their orders might change in the future (e.g. adding new shops, or, if the master baker starts drinking again, losing shops), so they also want to know how the solution was found. They will give prizes for the best submissions, which will be evaluated on the quality of the solution (it's cost) and how well the work was explained.

##The Capacitated Vehicle Routing Problem
The CVRP is a hard combinatorial optimisation problem. The scenario is that a set of depots contains goods to be delivered to customers by a fleet of trucks. Each customer wants a certain integer amount of goods called their demand and each truck can only carry a certain amount called its capacity. All trucks have the same capacity. The objective is to find a set of routes, one for each truck, which minimises the total distance travelled by all trucks (the cost) and satisfies the demands of all customers. All routes begin and end at a depot. When a truck arrives at a customer it must deliver as much as the customer demands. A single customer cannot be served by more than one truck. A truck only makes one trip per solution, but the number of trucks is left as a parameter which you can optimise. A solution is only valid if no truck's capacity is exceeded and if all demand is satisfied. You cannot use invalid solutions when recording your best solution (see below), but they can be given a fitness and participate in the search process, e.g. as parents in a genetic algorithm. (See the notes on constrained optimisation in the handouts.)

This plot shows the location of the single depot and all 75 customers in the FruityBun challenge, all of which exist in a 2-dimensional Euclidean world. The best known solution for this problem has a cost of approximately 687.602.

![map](http://www.mftp.info/20140202/1392082062x1927178161.png)

The next plot shows the routes taken by two trucks which happen to divide the customers into two sets based on their order in the file that specifies coordinates. This is not a good solution! In fact it's not a valid solution because it exceeds the capacity of the trucks. It's just to very roughly illustrate what solutions look like: they start and end at the depot.

![route](http://www.mftp.info/20140202/1392082142x1927178161.png)

##Solution
####Representation
The solution of CVRP problem contains a set of routes, which are the chromosomes in GA terminology. Each route by a truck contains multiple customer location indexes, which are genes. The length of a route is highly dependent on the order of its customers.

####Customer Allocation
The first problem I managed to solve was how to decide which customers a truck should visit, which is very important in this problem as once its decided, it can not be changed in later crossover or mutation. A naive way I have tried was to let a truck explore the map itself from the depot and kept moving to the nearest customer until it ran out of buns. In order to add some variation, an argument of aggression was added so that it will pick some random customers as well so that the allocation of customers is less deterministic. The pseudocode is shown below:
```Java
Void moveTruck(int aggression) {
      While(truck.getBun() > 0){
            If (randomNum > aggression)
                  truck.moveToRandomCustomer();
            Else {
                  customer = truck.getNearestCustomer();
                  truck.setLocation(customer);
                  truck.addRoute(customer);
                  truck.setBun(truck.getBun() - getDemand(customer));
                  customer.remove();
            } 
      }
}
```
The solution with this naive way did not appear to be efficient. With crossover and mutation, the best solution I achieved was around 1450. Then I modified how the truck chooses its next destination. As an invention, I added a weight of direction aberration in addition to distance between the current location and the potential customers. What made me think in this way was I discovered that in the graphs of the relatively “best” solutions, the trucks tend to move without changing its direction much, which means the truck managed to move in a straight line wherever possible. In this case, instead of getting to the nearest customer, the truck move to the the most suitable customer, where the suitability was defined as:
```Java
Double getSuitability(Customer customer, double weight) {
      distance = getDist(truck.getCurretnLocation(), customer);
      directionAberration = (truck.currentDirection-
      getDirection(truck.getLocation,customer))/
      truck.currentDirection;
      Return distance + weight * directionAberration;
}
```
After this modification, the best solution was improved to 1208. Still, I saw improvements from the graphs. Though the trucks tried their best to reach their customers in an efficient way, there were some customers who would be good to be visited by certain trucks while they were not.

Then I decided to group closed customers together to a truck while not leaving those ones far away alone. (leaving them alone at first may makes me pay more later) This time, a truck does not explore and move itself, but is given certain customers. (invention as well)This action was taken when initializing the map. When loading information of customers, they are no longer sorted by the default index, but by their angles to the depot. However, they were not ordered from 0 degree to 360 degree. I also added some variation so that the list of customers was split randomly into two parts and then joined in an inverse order. With this pre-computing, allocating customers to each truck would be easy:
```Java
Void allocation(Customers customers) {
      For(customer : customers) {
            If (customer.getDemand()<=truck.getBun()) {
                  truck.addRoute(customer);
                  truck.setBun(truck.getBun() - getDemand(customer));
                  customer.remove();
            } 
      }
}
```
After allocation, each truck was responsible for a set of customers who were in a fan- shape area, so that the trucks could really work “locally”. The best solution with this allocation algorithm was 833 with graph shown:
