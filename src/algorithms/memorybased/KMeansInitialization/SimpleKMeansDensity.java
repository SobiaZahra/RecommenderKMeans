package algorithms.memorybased.KMeansInitialization;

import java.util.*;

import algorithms.memorybased.KMeansInitialization.Centroid;

import netflix.memreader.*;
//import netflix.utilities.*;
import cern.colt.list.*;
import cern.colt.map.*;

/**
 * Class that uses density as parameter to chose the initial seeds for KMeans clustering
 * @author Musi
 * 
 */
/************************************************************************************************/
public class SimpleKMeansDensity  extends CallInitializationMethods implements KMeansVariant
/************************************************************************************************/

{

	    private MemHelper 	helper;
	   
/************************************************************************************************/

	    /**
	     * Builds the RecTree and saves the resulting clusters.
	     */
	    
	    public SimpleKMeansDensity(MemHelper helper)    
	    {
	        this.helper   = helper;

	    }


/**********************************************************************************************/
 
    /**
     * Chooses k users to serve as intial centroids for 
     * the kMeansPlus algorithm.
     * Each time, we have to choose the centorid based on density
     *
     * @param  dataset  The list uids. 
     * @param  k  The number of centroids (clusters) desired. 
     * @return A List of randomly chosen centroids. 
     */

	@Override
	public ArrayList<Centroid> chooseCentroids(int variant, IntArrayList dataset,int k, double cliqueAverage)
	
	{
	
	
		System.out.println("=========================================");
		System.out.println("       " + getName(variant));
		System.out.println("=========================================");

//	//------------------------------------------------
//	//
//	//			not correct, to be modified yet  
//	//--------------------------------------------------
//
//	ArrayList<Centroid> chosenCentroids = new ArrayList<Centroid>(k);
//	newCentroids = new ArrayList<Centroid>(k);        
//	IntArrayList allCentroids = new IntArrayList();		    // All distinct chosen centroids              
//	OpenIntIntHashMap powerUsers	 = new OpenIntIntHashMap();	// All user who seen greater than Movies_threshold movies
//
//	int totalPoints			 = dataset.size();			// All users
//	int C					 = 0;						// Centroid
//	//	int previousC			 = 0;						// Previous centroid
//	int possibleC			 = 0;						// A point from dataset
//	int moviesThreshold		 = 50;						// power user defination
//	double possibleCSim		 = 0;	 					// Sim of the point from the dataset
//
//
//	//---------------------------
//	// Find power users
//	//---------------------------
//
//	for(int j=0;j<totalPoints;j++) //for all points
//	{
//		possibleC  	   = dataset.get(j);
//		int moviesSeen = helper.getNumberOfMoviesSeen(possibleC);
//
//		if( moviesSeen > moviesThreshold)
//		{
//			powerUsers.put(possibleC, moviesSeen);
//		}
//	}
//
//	int powerUsersSize= powerUsers.size();
//
//	IntArrayList myPowerUsers 	 	= powerUsers.keys();
//	IntArrayList myPowerWeights    = powerUsers.values();      		   
//	powerUsers.pairsSortedByValue(myPowerUsers, myPowerWeights);
//
//	System.out.println("power users found = " + powerUsersSize);
//
//	for(int i = 0; i < k; i++) 					//for total number of clusters         
//	{
//
//		//-----------------------------------
//		// For first loop, we find the point 
//		// at uniformly random
//		//-----------------------------------        	
//
//		if(i==0) 
//		{
//			C = myPowerUsers.get(powerUsersSize-1);
//			allCentroids.add(C);
//			chosenCentroids.add( new Centroid (C,helper));
//			this.centroids = chosenCentroids; 
//		}
//
//		//-----------------------------------
//		// Now choose points using KMeans Plus 
//		// 
//		//-----------------------------------        	
//
//		else
//		{
//			// good to make it local, as for each new centroid, we want new weights
//			OpenIntDoubleHashMap uidToCentroidSim = new OpenIntDoubleHashMap();	
//			int currentCentroidsSize = allCentroids.size();
//			//			int existingCentroid     = 0;
//			double closestWeight	 = 10;
//
//			//------------------------------
//			// Find sim for only power users
//			//------------------------------
//
//			for(int j=0;j<powerUsersSize;j++) //for all points
//			{
//				//Get a point
//				possibleC  	  = myPowerUsers.get(j);		
//				//         			closestWeight = 10;
//
//				for (int m=0;m<currentCentroidsSize; m++)
//				{
//					// Get an existing centroid
//					//					existingCentroid =  allCentroids.get(m);
//
//					//-----------------------------
//					// Now we find distance of each
//					// point from closest centroid
//					// i.e. sim > largest 
//					//-----------------------------
//
//					//Now we find the similarity between a user and the chosen cluster.    
//					//In fact, we will find the min possibleCSim here(means the farthest distance)
//					possibleCSim =  findSimWithOtherClusters(possibleC, m);
//					if(closestWeight == possibleCSim)
//						closestWeight = possibleCSim;
//
//				}
//
//				// only add the distance of a point with the closest centorid
//				uidToCentroidSim.put(possibleC, closestWeight);
//
//			} // finsihed finding similarity b/w all users and the chosen centroid
//
//			//-----------------------
//			// Find the next centroid
//			//-----------------------
//
//			// sort weights in ascending order (So first element has the lowest sim)	
//			IntArrayList myUsers 	 	= uidToCentroidSim.keys();
//			DoubleArrayList myWeights = uidToCentroidSim.values();
//			uidToCentroidSim.pairsSortedByValue(myUsers, myWeights);
//
//			int toalPossibleC = uidToCentroidSim.size();
//
//			// As both are sorted, so it should be in the first index
//			// Make sure, we have not already added this in the list of centroids
//			for (int j=toalPossibleC-1;j>0; j-- )
//			{
//				C = myUsers.get(j);
//
//
//				if(allCentroids.contains(C)==false)
//				{	 
//					allCentroids.add(C);        				  			
//					break;        					  	
//				}
//
//			} // only the last one will be added
//
//			chosenCentroids.add( new Centroid (C,helper));
//			this.centroids = chosenCentroids; 
//
//		} //end of else
//
//
//	}

		ArrayList<Centroid> chosenCentroids = new ArrayList<Centroid>(k);
		newCentroids = new ArrayList<Centroid>(k);        
		IntArrayList allCentroids = new IntArrayList();		  // All distinct chosen centroids              
		int totalPoints		 = dataset.size();			// All users
		int seedID		  	 = 0;						// Centroid
	
		
		// Finding the "Average pairwise Euclidean distance" (d1)
		double d1 = 0;
		for (int i=0;i<totalPoints-1;i++) {
			int user1 = i;
				for (int j=i+1;j<totalPoints;j++) {
					int user2 = j;
					d1  += findEucledianDistanceBetweenTwoEntities (user1, user2, true);		
			}
		}
		// Avg pair wise Eucleadian distance
		d1  = d1/(totalPoints * (totalPoints-1));
		
		// Choose the seeds
		OpenIntIntHashMap uidToDensityCount = new OpenIntIntHashMap();
		int user1 = 0;
		int user2 = 0;
		int count = 0;
			
		// The Þrst seed is chosen as the point xi with the largest number of points within
		// a multidimensional sphere with radius d1 that is centered at xi 
		for(int i=0;i<totalPoints; i++) {
			user1 = i;
			count = 0;
			
			for (int j=0;j<totalPoints;j++) {
				user2 = j;
			
				if(i!=j) {
					double d = findEucledianDistanceBetweenTwoEntities (user1, user2, true);
					if (d <= d1) {
						count++;
					}
				}
			} // end of counting density for one user
			
			uidToDensityCount.put(user1, count);		
		}
		
		// Sort weights in ascending order (So first element has the lowest density)	
		IntArrayList myCandidateSeeds	 = uidToDensityCount.keys();
		IntArrayList myCandidateDensity  = uidToDensityCount.values();
		uidToDensityCount.pairsSortedByValue(myCandidateSeeds, myCandidateDensity);

		int totalPossibleC = myCandidateSeeds.size();
		chosenCentroids = addCentroids (allCentroids, 
										myCandidateSeeds, 
										chosenCentroids, 
										totalPossibleC, 
										d1, 
										k,
										true); // do we need to check condition
			 
		
		// Check if all K centroids have been chosen
		// if not, DONT check the condition and add the candidate 
		// centroids into the chosen ones until all k centroids are found
		if(chosenCentroids.size() < k) {		
			chosenCentroids = addCentroids (allCentroids, 
							  myCandidateSeeds, 
							  chosenCentroids, 
							  totalPossibleC, 
							  d1, 
							  k,
							  false); // do we need to check condition

		} //end if
			    
		this.centroids = chosenCentroids; 
		return chosenCentroids;

	}

 
	/**
	 * Condition:
	 * For rest of the seeds, it is conditioned that "remaining seeds are chosen by decreasing density, 
	 * with the restriction that all remaining seeds must be distance "d1" away from all previous seeds"
	 *		
	 * Code to check the condition
	 * @param allCentroids
	 * @param myCandidateSeeds
	 * @param chosenCentroids
	 * @param totalPossibleC
	 * @param d1
	 * @param k
	 * @param checkCondition
	 */
    private ArrayList<Centroid> addCentroids ( IntArrayList allCentroids, 
    						    IntArrayList myCandidateSeeds, 
    						    ArrayList<Centroid> chosenCentroids,
    						    int totalPossibleC, 
    						    double d1, 
    						    int k,
    						    boolean checkCondition) {
    	
    	boolean isConditionTrue = true;
    	int seedID = 0;
		
		//Start from the highest density		
		for (int j=totalPossibleC-1;j>0; j--)
		{
			// For first seed, it is simple
			if(j == totalPossibleC-1) {
				seedID = myCandidateSeeds.get(j);
			}
			// For rest of the seeds, it is conditioned that "remaining seeds are chosen by decreasing density, 
			// with the restriction that all remaining seeds must be distance "d1" away from all previous seeds"
			else {
				   if(checkCondition)
					   isConditionTrue = checkCondition(allCentroids,seedID, d1);
				   else
					   isConditionTrue = true;
			}
			
			if( (allCentroids.contains(seedID)==false) &&  isConditionTrue) {	 
				allCentroids.add(seedID);
				chosenCentroids.add( new Centroid (seedID,helper)); // add centroid
			}
			
			if(allCentroids.size() ==k) {
				break;
			}			
		} // only the last one will be added
		
		return chosenCentroids;
		
    }
    
 	private boolean checkCondition (IntArrayList allCentroids, int candidateSeed, double distance) {
 		
 		for (int i=0; i< allCentroids.size();i++) {
 			int previousSeed = allCentroids.get(i);
  			double d = findEucledianDistanceBetweenTwoEntities (previousSeed, candidateSeed, true);
				if (d <= distance) {
					return false;
				}
 		} 			
 		return true;
 	}

	//----------------
	//  get variant name
	// ---------------

	@Override
	public String getName(int variant) {
		
		String name = "SimpleKMeansDensity";
		return name;
	}
 

}


