public class TravelingSalesman extends FitnessFunction {
    public TravelingSalesman(){
        name = "Traveling Salesman Problem";
    }

    public void doRawFitness(Chromo X){
        double distance = 0;
        for(int i =1 ; i < X.chromo.length(); i++){
            Location a = Search.locations.get(X.chromo.charAt(i-1));
            Location b = Search.locations.get(X.chromo.charAt(i));
            distance += Location.distance(a, b);
        }

        X.rawFitness = distance;
    }
}
