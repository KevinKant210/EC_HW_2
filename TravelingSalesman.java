public class TravelingSalesman extends FitnessFunction {
    public TravelingSalesman(){
        name = "Traveling Salesman Problem";
    }

    public void doRawFitness(Chromo X){
        double distance = 0;
        for(int i = 0 ; i < X.chromo.length()-4; i = i+4){
            
            Location a = Search.locations.get(X.chromo.substring(i, i+2));
            
            Location b = Search.locations.get(X.chromo.substring(i+2, i+4));

            // System.out.println(X.chromo.substring(i+2, i+4));

            
            //     System.out.println(X.chromo);
            
            distance += Location.distance(a, b);
            
        }

        //need to add the distance between the final node and the first one 

        X.rawFitness = distance;
    }
}
