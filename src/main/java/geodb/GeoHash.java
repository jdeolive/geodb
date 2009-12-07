package geodb;

public class GeoHash {

    public static String geohash( double x1, double y1, double x2, double y2 ) {
        GeoString s1 = new GeoString(x1,y1);
        GeoString s2 = new GeoString(x2,y2);
        
        return s1.union(s2).toString();
    }
}
