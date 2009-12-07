package geodb;

public class Box {
    
    static Box WGS84 = new Box(-180,-90,180,90);
    double l,b,r,t;
    
    public Box( double... d ) {
        l = d[0];
        b = d[1];
        r = d[2];
        t = d[3];
    }
    
    @Override
    public String toString() {
        return "(" + l + "," + b + "," + r + "," + t + ")";
    }
    
}
