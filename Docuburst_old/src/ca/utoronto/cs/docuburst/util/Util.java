package ca.utoronto.cs.docuburst.util;

public class Util {
    
    public static float sum(float[] array){
        if (array==null)
            return 0;
        float sum = 0;
        for (int i = 0; i < array.length; i++)
            sum += array[i];
        
        return sum;
    }
}
