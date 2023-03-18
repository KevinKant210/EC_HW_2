import java.util.ArrayList;

public class TravelingSalesman extends FitnessFunction {
    public TravelingSalesman(){
        name = "Traveling Salesman Problem";
    }

    public void doRawFitness(Chromo X){
        double distance = 0;
        // for(int i = 0 ; i < X.chromo.length()-4; i = i+4){
            
        //     Location a = Search.locations.get(X.chromo.substring(i, i+2));
            
        //     Location b = Search.locations.get(X.chromo.substring(i+2, i+4));

        //     // System.out.println(X.chromo.substring(i+2, i+4));

            
        //     //     System.out.println(X.chromo);
            
        //     distance += Location.distance(a, b);
            
        // }
        ArrayList<Integer> values = new ArrayList<>();
        for(int i =0 ; i < X.chromo.length(); i += 2){
            values.add(Integer.parseInt(X.chromo.substring(i,i+2)));
           
        }

        for(int i =0 ; i < values.size(); i++){
            int next = (i+1)%values.size();

            
            Location a = Search.intlocations.get(values.get(i));
            Location b = Search.intlocations.get(values.get(next));

            distance += Location.distance(a, b);
        }

        //need to add the distance between the final node and the first one 

        X.rawFitness = distance;
    }
}
