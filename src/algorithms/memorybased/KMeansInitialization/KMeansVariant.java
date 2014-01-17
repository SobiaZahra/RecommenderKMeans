package algorithms.memorybased.KMeansInitialization;

import java.util.ArrayList;

import cern.colt.list.IntArrayList;

public interface KMeansVariant 

{
	public void cluster(int variant , int kClusters,  int call, int iterations, int sVersion);
	
	public abstract ArrayList<Centroid> chooseCentroids(int variant, IntArrayList dataset, 		//all users in the database
			int k, 					
			double cliqueAverage);

	public  String getName(int variant);

}
