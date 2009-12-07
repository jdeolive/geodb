package geodb;

public class GeoString {

    static byte[] toBits(double f) {
        return toBits(f,32);
    }
    
    static byte[] toBits(double f, int depth) {
        f *= (1L << depth);
        byte[] bits = new byte[depth];
        for ( int i = 1; i < depth+1; i++) {
            bits[i-1] = (byte) (((long)f >> (depth-i)) & 1);
        }
        return bits;
    }
    
    static String bitstring(double x, double y, Box b, int depth) {
        byte[] xbits = toBits( (x - b.l) / ( b.r - b.l ), depth );
        byte[] ybits = toBits( (y - b.b) / ( b.t - b.b ), depth );
        
        StringBuffer bits = new StringBuffer();
        for ( int i = 0; i < xbits.length; i++ ) {
            bits.append(xbits[i]).append(ybits[i]);
        }
        return bits.toString();
    }
    
    static double round( double d, int n ) {
        double scale = Math.pow( 10, n );
        double r = d*scale + 0.5;
        return ((int)r) / scale; 
    }
    
    Box bound;
    int depth;
    double originx,originy;
    double sizex,sizey;
    String hash;
    
    public GeoString( double x, double y) {
        this(x,y,32);
    }

    public GeoString( double x, double y, Box bound) {
        this(x,y,bound,32);
    }

    public GeoString( double x, double y, int depth) {
        this(x,y,Box.WGS84,depth);
    }
    
    public GeoString( double x, double y, Box bound, int depth ) {
        this.bound = bound;
        this.depth = depth;
        originx = bound.l;
        originy = bound.b;
        sizex = bound.r - bound.l;
        sizey = bound.t - bound.b;
        hash = bitstring(x, y, bound, depth);
    }
    
    public GeoString( String hash ) {
        this(hash,32);
    }

    public GeoString( String hash, Box bound) {
        this(hash,bound,32);
    }

    public GeoString( String hash, int depth) {
        this(hash,Box.WGS84,depth);
    }
    
    public GeoString( String hash, Box bound, int depth ) {
        this.bound = bound;
        this.depth = depth;
        this.hash = hash;
        originx = bound.l;
        originy = bound.b;
        sizex = bound.r - bound.l;
        sizey = bound.t - bound.b;
    }
    
    public Box bbox() {
        return bbox(hash.length());
    }
    
    public Box bbox(int prefix) {
        String bits = this.hash.substring(0,prefix);
        int depth = bits.length()/2;
        double minx = 0, miny = 0;
        double maxx = 1, maxy = 1;
        
        for ( int i = 0; i < depth+1; i++ ) {
            try {
                minx += Double.parseDouble( String.valueOf(bits.charAt(i*2)) ) / (2L<<i);
                miny += Double.parseDouble( String.valueOf(bits.charAt(i*2+1)) ) / (2L<<i);
            }
            catch( StringIndexOutOfBoundsException e ) {
                //ok
            }
        }
        
        if ( depth > 0 ) {
            maxx = minx + 1.0/((double)(2L<<(depth-1)));
            maxy = miny + 1.0/((double)(2L<<(depth-1)));
        }
        else if ( bits.length() == 1 ) {
            maxx = Math.min(minx + 0.5, 1.0);
        }
        
        minx = round(originx + minx*sizex, 6);
        maxx = round(originx + maxx*sizex, 6);
        miny = round(originy + miny*sizey, 6);
        maxy = round(originy + maxy*sizey, 6);
        
        return new Box(minx,miny,maxx,maxy);
    }
    
    public GeoString union(GeoString string) {
        String other = string.hash;
        int n = Math.min(hash.length(),other.length());
        int i = 0;
        for ( ; i < n; i++ ) {
            if ( hash.charAt(i) != other.charAt(i) ) {
                break;
            }
        }
        
        String union = hash.substring(0,i);
        return new GeoString(union,this.bound,this.depth);
    }
    
    @Override
    public String toString() {
        return hash;
    }
    
    public static void main(String[] args) {
        GeoString gs = new GeoString(-0.25,51.5);
        System.out.println(gs);
        System.out.println(gs.bbox());
    }
}
