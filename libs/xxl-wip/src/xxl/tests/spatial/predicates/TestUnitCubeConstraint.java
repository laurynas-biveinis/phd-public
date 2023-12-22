package xxl.tests.spatial.predicates;

import java.io.File;
import java.util.Iterator;

import xxl.core.spatial.cursors.PointInputCursor;
import xxl.core.spatial.predicates.UnitCubeConstraint;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class UnitCubeConstraint.
 */
public class TestUnitCubeConstraint {
    
    
    /**
     use-case:
     checks whether all Objects delivered by an Iterator fulfill
     the given constraint
     
     @param args args[0] file-name, args[1] dimensionality of the data
     */
    public static void main(String[] args){
        if(args.length != 2){
            System.out.println("usage: java xxl.core.spatial.UnitCubeConstraint <file-name> <dim>");
            return;	
        }
        
        Iterator it = 
            new PointInputCursor(new File(args[0]), PointInputCursor.FLOAT_POINT, Integer.parseInt(args[1]));
        
        System.out.println("# file-name        : "+args[0]);
        System.out.println("# dimensionality   : "+args[1]);
        
        boolean constraint = true;
        
        for(int count = 0; it.hasNext(); count++){
            Object next = it.next();
            if( !UnitCubeConstraint.DEFAULT_INSTANCE.invoke(next) ){
                if(constraint){
                    constraint = false;
                    System.out.println("# The constraint is FALSE for the following elements.");
                    System.out.println("# format: <n-th element of input-iterator>: <next().toString()>");
                }
                System.out.println(count+":\t"+next);
            }
        }
        if(constraint)
            System.out.println("# The constraint is TRUE for all elements.");
    }

}
