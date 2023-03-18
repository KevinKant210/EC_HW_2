import java.lang.Math;

public class Location{
    //when storing these store them in radian values
    public double latitude;
    public double longitude;
    public String id;
    public static double R = 3958.8;

    public Location(Double lat, Double longi,String id_in){
        latitude = Math.toRadians(lat);
        longitude = Math.toRadians(longi);
        id = id_in;
    }

    public static double distance(Location a, Location b){
        
        double deltaLat = b.latitude - a.latitude;
        double deltaLong = b.longitude - a.longitude;

        double equation_a = Math.pow(Math.sin(deltaLat/2),2) + Math.cos(a.latitude)*Math.cos(b.latitude)*Math.pow(Math.sin(deltaLong/2),2);

        // double equation_c = 2 * Math.atan2(Math.sqrt(equation_a),Math.sqrt(1-equation_a));

         double equation_c = 2 * Math.asin(Math.sqrt(equation_a));

        return Location.R * equation_c;
    }
}