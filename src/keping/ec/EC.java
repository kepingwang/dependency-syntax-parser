package keping.ec;

import java.util.Comparator;

public class EC {
    private ECTuple tuple;
    private ECType  type;
    public EC(ECTuple tuple, ECType type) {
        this.tuple = tuple;
        this.type = type;
    }
    public ECTuple getTuple() { return tuple; }
    public ECType  getType()  { return type;  }
    
    public static class TComparator implements Comparator<EC> {
        public int compare(EC ec1, EC ec2) {
            if      (ec1.tuple.t() > ec2.tuple.t()) return  1;
            else if (ec1.tuple.t() < ec2.tuple.t()) return -1;
            else                                    return  0;
        }
    }
}
