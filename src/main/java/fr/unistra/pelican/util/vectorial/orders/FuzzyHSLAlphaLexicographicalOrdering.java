package fr.unistra.pelican.util.vectorial.orders;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This class represents a lexicographical ordering scheme for double valued pixels in the [0,1] interval.
 * The input pixels are considered in the HSL order..
 * 
 * The priority of the components is customizable either by modifying their order or by division 
 * with an argument alpha, aiming to decrease the dynamic range of the component.
 * 
 * additionally...with respect to the standard HSLAlphaAnguloLexicographical ordering here
 * we have a softener beta...which smoothens the discrete passages due to alpha's roundings.
 * 
 * In the latter case the values are once more transfered to the [0,255] interval.
 * 
 * For now, only the first component can be "softened" with division (H is not yet supported for softening).
 * 
 * @author E.A.
 *
 */

public class FuzzyHSLAlphaLexicographicalOrdering implements VectorialOrdering,Comparator
{
	private int alpha;
	private int beta;
	private int order;
	private double refHue;
	
	public static final int HLS = 0;
	public static final int HSL = 1;
	public static final int LSH = 2;
	public static final int LHS = 3;
	public static final int SHL = 4;
	public static final int SLH = 5;
	
	/**
	 * Alpha is to be chosen as if the pixels values were in [0,255]
	 * 
	 * @param alpha the rounding parameter
	 * @param order the prioritization order of the channels
	 * @param refHue the hue origin
	 */
	public FuzzyHSLAlphaLexicographicalOrdering(int alpha,int beta,int order,double refHue)
	{
		if(alpha > 0) this.alpha = alpha;
		else this.alpha = 1;
		
		if(beta >= 1 && beta < (alpha + 1) / 2) this.beta = beta;
		else this.beta = 1;
		
		this.order = order;
		this.refHue = refHue;
	}
	
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[][])
	 */
	public double[] max(double[][] p)
	{
		double[] max = p[0];

		for(int i = 1; i < p.length; i++){
			if(this.compare(max,p[i]) < 0) max = p[i];
		}

		return max;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[][])
	 */
	public double[] min(double[][] p)
	{
		double[] min = p[0];

		for(int i = 1; i < p.length; i++){
			if(this.compare(min,p[i]) > 0) min = p[i];
		}

		return min;
	}

	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#rank(double[][], int)
	 */
	public double[] rank(double[][] p,int r)
	{
		Arrays.sort(p,this);

		return p[r];
	}
	
	/**
	 * Compares the given arguments according to this ordering
	 * 
	 * @param o1 first double valued array or vector pixel
	 * @param o2 second double valued array or vector pixel
	 * @return 1,-1 or 0 if o1 is respectively superior, inferior or equal to o2 
	 */
	public int compare(Object o1,Object o2)
	{
		double[] p1 = null,p2 = null;

		try{
			if(o1.getClass().getName().equals("[D")){
				p1 = (double[])o1;
				p2 = (double[])o2;
			}else throw new ClassCastException();

		}catch(ClassCastException ex){
			ex.printStackTrace();
		}
		
		int tmp1,tmp2;
		double dtmp1,dtmp2;
		
		switch(order){
			case LSH:
				// L rounded..
				if(alpha > 1.0){
					dtmp1 = 0.0;
					dtmp2 = 0.0;
					
					double ipixel = Math.round(p1[2] * 255.0);
					
					if(ipixel < alpha - beta) dtmp1 = Math.floor((double)ipixel/(double)alpha);
					
					else if(ipixel % alpha <= beta){
						double m = (double)ipixel/(double)alpha - Math.floor((double)ipixel/(double)alpha);
						
						dtmp1 = Math.floor((double)ipixel/(double)alpha) - ((double)beta/(double)alpha - m);
						
					}else if(ipixel % alpha >= alpha - beta){
						double m = Math.floor((double)ipixel/(double)alpha) + 1 - (double)ipixel/(double)alpha;
						
						dtmp1 = Math.floor((double)ipixel/(double)alpha) + (double)(beta + 1)/(double)alpha - m;
						
					}else
						dtmp1 = Math.floor((double)ipixel/(double)alpha);
					
					double ipixel2 = Math.round(p2[2] * 255.0);
					
					if(ipixel2 < alpha - beta) dtmp1 = Math.floor((double)ipixel2/(double)alpha);
					
					else if(ipixel2 % alpha <= beta){
						double m = (double)ipixel2/(double)alpha - Math.floor((double)ipixel2/(double)alpha);
						
						dtmp2 = Math.floor((double)ipixel2/(double)alpha) - ((double)beta/(double)alpha - m);
						
					}else if(ipixel2 % alpha >= alpha - beta){
						double m = Math.floor((double)ipixel2/(double)alpha) + 1 - (double)ipixel2/(double)alpha;
						
						dtmp2 = Math.floor((double)ipixel2/(double)alpha) + (double)(beta + 1)/(double)alpha - m;
						
					}else
						dtmp2 = Math.floor((double)ipixel2/(double)alpha);
					
					//System.err.println(ipixel + " " + ipixel2 + " " + dtmp1 + " " + dtmp2 + " " + tmp1 + " " + tmp2);
				
					if(dtmp1 < dtmp2) return -1;
					else if(dtmp1 > dtmp2) return 1;
				}else{
					if(p1[2] < p2[2]) return -1;
					else if(p1[2] > p2[2]) return 1;
				}
				
				// S natural ordering
				if(p1[1] < p2[1]) return -1;
				else if(p1[1] > p2[1]) return 1;
				
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				return 0;
			case SLH:
				// S rounded..
				if(alpha > 1.0){
					tmp1 = (int)Math.ceil(p1[1] * 255.0 / alpha);
					tmp2 = (int)Math.ceil(p2[1] * 255.0 / alpha);
				
					if(tmp1 < tmp2) return -1;
					else if(tmp1 > tmp2) return 1;
				}else{
					if(p1[1] < p2[1]) return -1;
					else if(p1[1] > p2[1]) return 1;
				}
				
				// L natural ordering
				if(p1[2] < p2[2]) return -1;
				else if(p1[2] > p2[1]) return 1;
				
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				return 0;
			case LHS:
				// L rounded..
				if(alpha > 1.0){
					tmp1 = (int)Math.ceil(p1[2] * 255.0 / alpha);
					tmp2 = (int)Math.ceil(p2[2] * 255.0 / alpha);
				
					if(tmp1 < tmp2) return -1;
					else if(tmp1 > tmp2) return 1;
				}else{
					if(p1[2] < p2[2]) return -1;
					else if(p1[2] > p2[2]) return 1;
				}
				
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				// S natural ordering
				if(p1[1] < p2[1]) return -1;
				else if(p1[1] > p2[1]) return 1;
				
				return 0;
			case SHL:
				// S rounded..
				if(alpha > 1.0){
					tmp1 = (int)Math.ceil(p1[1] * 255.0 / alpha);
					tmp2 = (int)Math.ceil(p2[1] * 255.0 / alpha);
				
					if(tmp1 < tmp2) return -1;
					else if(tmp1 > tmp2) return 1;
				}else{
					if(p1[1] < p2[1]) return -1;
					else if(p1[1] > p2[1]) return 1;
				}
				
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				// L natural ordering
				if(p1[2] < p2[2]) return -1;
				else if(p1[2] > p2[2]) return 1;
				
				return 0;
			case HSL:
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				// S natural ordering
				if(p1[1] < p2[1]) return -1;
				else if(p1[1] > p2[1]) return 1;
				
				// L natural ordering
				if(p1[2] < p2[2]) return -1;
				else if(p1[2] > p2[2]) return 1;
				
				return 0;
			case HLS:
				dtmp1 = Math.abs(refHue - p1[0]);
				dtmp2 = Math.abs(refHue - p2[0]);
				
				if(dtmp1 > 0.5) dtmp1 = 1.0 - dtmp1;
				if(dtmp2 > 0.5) dtmp2 = 1.0 - dtmp2;
				
				// reverse the order direction so the "closer" to the reference is "bigger"..
				if(dtmp1 < dtmp2) return 1;
				else if(dtmp1 > dtmp2) return -1;
				
				// L natural ordering
				if(p1[2] < p2[2]) return -1;
				else if(p1[2] > p2[2]) return 1;
				
				// S natural ordering
				if(p1[1] < p2[1]) return -1;
				else if(p1[1] > p2[1]) return 1;
				
				return 0;
			default:
				System.err.println("not available");
				return 0;
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#max(double[], double[])
	 */
	public double[] max(double[] p,double[] r)
	{
		if(compare(p,r) == 1) return p;
		else return r;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see fr.unistra.pelican.util.vectorial.orders.VectorialOrdering#min(double[], double[])
	 */
	public double[] min(double[] p,double[] r)
	{
		if(compare(p,r) == 1) return r;
		else return p;
	}
}
