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
	    	super (helper);
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
		System.out.println("       " + getName());
		System.out.println("=========================================");

		ArrayList<Centroid> chosenCentroids = new ArrayList<Centroid>(k);        
		IntArrayList allCentroids = new IntArrayList();		  // All distinct chosen centroids              
		int totalPoints		 = dataset.size();			// All users
		int seedID		  	 = 0;						// Centroid
		for(int p = 0; p < k; p++) 					//for total number of clusters         
		{
		
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
//		chosenCentroids = addCentroids (allCentroids, 
//										myCandidateSeeds, 
//										chosenCentroids, 
//										totalPossibleC, 
//										d1, 
//										k,
//										true); // do we need to check condition
//			 System.out.println("userssss" + totalPossibleC);
		
		// Check if all K centroids have been chosen
		// if not, DONT check the condition and add the candidate 
		// centroids into the chosen ones until all k centroids are found
		if(chosenCentroids.size() < k) {		
			chosenCentroids = addCentroids (allCentroids, 
							  myCandidateSeeds, 
							  chosenCentroids, 
							  totalPossibleC, 
							  d1, 
							  k); // do we need to check condition

		} //end if
		}
		System.out.println("Size of the cluster is = "+ chosenCentroids.size());
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
    						    int k) {
    	
    	boolean isConditionTrue = true;
    	int seedID = 0;
		
		//Start from the highest density		
		for (int j=totalPossibleC-1;j>0; j--)
		{
			// For first seed, it is simple
			if(j == totalPossibleC-1) {
				seedID = myCandidateSeeds.get(j);
				chosenCentroids.add( new Centroid (seedID,helper)); // add centroid
			}
			// For rest of the seeds, it is conditioned that "remaining seeds are chosen by decreasing density, 
			// with the restriction that all remaining seeds must be distance "d1" away from all previous seeds"
			else {
				   
					   isConditionTrue = checkCondition(allCentroids,seedID, d1);
//			
//			}
//			
			if( (allCentroids.contains(seedID)==false) &&  isConditionTrue) {	 
				allCentroids.add(seedID);
				chosenCentroids.add( new Centroid (seedID,helper)); // add centroid
			}
			
			if(allCentroids.size() ==k) {
				break;
			}		
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
	public String getName() {
		
		String name = "SimpleKMeansDensity";
		return name;
	}
 

}


