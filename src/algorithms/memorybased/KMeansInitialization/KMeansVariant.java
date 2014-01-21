package algorithms.memorybased.KMeansInitialization;

import java.util.ArrayList;
import cern.colt.list.IntArrayList;

public interface KMeansVariant 

{
	
	/**
	 * 
	 * @param variant
	 * @param kClusters
	 * @param call
	 * @param iterations
	 * @param sVersion
	 */
	public void cluster(int variant , int kClusters,  int call, int iterations, int sVersion);
	
	/**
	 * 
	 * @param variant
	 * @param dataset
	 * @param k
	 * @param cliqueAverage
	 * @return
	 */
	public abstract ArrayList<Centroid> chooseCentroids(int variant, IntArrayList dataset, 		//all users in the database
														int k, 	double cliqueAverage);

	/**
	 * 
	 * @param variant
	 * @return
	 */
	public  String getName();

	

}
