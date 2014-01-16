package algorithms.memorybased.KMeansInitialization;

import java.util.ArrayList;

import cern.colt.list.IntArrayList;

public interface KMeansVariant 

{
	public abstract ArrayList<Centroid> chooseCentroids(int variant, IntArrayList dataset, 		//all users in the database
			int k, 					
			double cliqueAverage);

	String getName(int variant);

}
