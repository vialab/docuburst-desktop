package ca.utoronto.cs.prefuseextensions.lib;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MyMathLib {

	private MyMathLib() {
		throw new AssertionError("Don't instantiate MyMathLib.");
	}
	
	public final static double EPSILON = 1e-6;
	
	public static boolean doublePointsEqual(Point2D p1, Point2D p2) {
		return (p1.distance(p2) < EPSILON);
	}
	

	public static double mean(int[] list) {
		double sum = 0;
		double results = 0.0;
		if (list.length > 0) {
			for (int i = 0; i < list.length; i++) {
				sum += list[i];
				results = (sum / list.length);
			}
		}
		return results;
	}

	public static double median(int[] list) {
		Arrays.sort(list);
		return list[(int) Math.floor(list.length/2)];
	}

	// calculate Mode (value that occurs most often in list)
	public static int mode(int[] list) {
		// find range of values
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i : list) {
			if (i < min)
				min = i;
			if (i > max)
				max = i;
		}
		int size = max - min;

		int[] count = new int[size+1];

		for (int i = 0; i < list.length; i++) {
			count[list[i] - min]++;
		}

		int maxCountValue = 0;

		for (int j = 1; j < count.length; j++) {
			if (count[j] > count[maxCountValue]) {
				maxCountValue = j;
			}
		}
		return maxCountValue + min;
	}

	public static final double TWO_PI = 2 * Math.PI;
	
	public static double clamp(double low, double value, double high) {
		if (value < low)
			return low;
		if (value > high)
			return high;
		return value;
	}
	
	public static float clamp(float low, float value, float high) {
		if (value < low)
			return low;
		if (value > high)
			return high;
		return value;
	}

	public static double max3(double a, double b, double c) {
		return Math.max(Math.max(a,b),c);
	}
	
	public static double min3(double a, double b, double c) {
		return Math.min(Math.min(a,b),c);
	}
	

	public static double sinh(double x) {
		if (Double.isNaN(x))
			return Double.NaN;
		if (Double.isInfinite(x))
			return x > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		if (x == 0)
			return x;
		return (Math.pow(Math.E, x) - Math.pow(Math.E, -x)) / 2;
	}

	public static double cosh(double x) {
		if (Double.isNaN(x))
			return Double.NaN;
		if (Double.isInfinite(x))
			return Double.POSITIVE_INFINITY;
		if (x == 0)
			return 1.0;
		return (Math.pow(Math.E, x) + Math.pow(Math.E, -x)) / 2;
	}

	public static double tanh(double x) {
		if (Double.isNaN(x))
			return Double.NaN;
		if (x == 0)
			return x;
		if (Double.isInfinite(x))
			return x > 0 ? +1.0 : -1.0;
		return sinh(x) / cosh(x);
	}
	
	public static int sign(double x) {
		if (x > 0) return 1; else
		if (x < 0) return -1; else
		return 0;
	}
	
	/**
	 * Linear interpolation between two values: computes t*y+(1-t)*x.
	 * @param x First value.
	 * @param y Second value.
	 * @param t Interpolation constant.
	 * @return The interpolated value: t*y+(1-t)*x.
	 */
	public static double interpolate(double x, double y, double t)
	{
		return t*y+(1-t)*x;
	}
	
	public static Collection randomSample(Collection coll, int size) {
		if (size >= coll.size() | size < 0)
			return coll;
		else
		{
			Random generator = new Random();
			List colllist = new ArrayList();
			colllist.addAll(coll);
			Set result = new HashSet();
			while (result.size() < size) {
				result.add(colllist.get(generator.nextInt(coll.size())));
			}
			return result;
		}		
	}
	
}
