package ca.utoronto.cs.docuburst.util.scale;

public class LinearScale {
    double[] domain;
    double[] range;
    
    public LinearScale(double domainMin, double domainMax, double rangeMin, double rangeMax) {
        this.domain = new double[]{domainMin, domainMax};
        this.range  = new double[]{rangeMin,  rangeMax};
    }
    
    public double scale (double v){
        double[] d = domain;
        double[] r = range;
        
        return (v - d[0]) * (r[1] - r[0]) / (d[1] - d[0]) + r[0];
    }
    
    
}
