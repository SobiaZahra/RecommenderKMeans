package algorithms.memorybased.KMeansInitialization;

import java.util.*;

import algorithms.memorybased.KMeansInitialization.Centroid;

import netflix.memreader.*;
import cern.colt.list.*;
import cern.colt.map.*;

/************************************************************************************************/
public class SimpleKMeansVarianceCorrected  extends CallInitializationMethods implements KMeansVariant
/************************************************************************************************/ 

{
	    private MemHelper 	helper;
	    ArrayList<Centroid> centroids;
	    ArrayList<Centroid> newCentroids;
	    OpenIntIntHashMap   clusterMap;
	    boolean 			converged;					  //Algorithm converged or not
	    int 				simVersion;
	    
/************************************************************************************************/

	    /**
	     * Builds the RecTree and saves the resulting clusters.
	     */
	    
	    public SimpleKMeansVarianceCorrected(MemHelper helper) {
	        this.helper   = helper;
	    }


/**********************************************************************************************/
 
    /**
     * Chooses k users to serve as intial centroids for 
     * the kMeansPlus algorithm.
     * Each time, we have to choose the centorid at varying distance from overall mean
     *
     * @param  dataset  The list uids. 
     * @param  k  The number of centroids (clusters) desired. 
     * @return A List of randomly chosen centroids. 
     */
	    @Override
		public ArrayList<Centroid> chooseCentroids(int variant,IntArrayList dataset, int k, double cliqueAverage) 
		
		{

			System.out.println("=========================================");
			System.out.println("       " + getName(variant));
			System.out.println("=========================================");

	    	ArrayList<Centroid> chosenCentroids = new ArrayList<Centroid>(k);
	    	newCentroids = new ArrayList<Centroid>(k);        
	    	IntArrayList allCentroids = new IntArrayList();		    // All distinct chosen centroids              

	    	int totalPoints			 = dataset.size();			// All users
	    	int seed					 = 0;						// Centroid			
	    	int possibleC			 = 0;						// A point from dataset
	    	double possibleCSim		 = 0;	 					// Sim of the point from the dataset
	    	double  avg				 = helper.getGlobalAverage();
	    	

	    		

	    		//------------------------------
	    		// Find sim to centroid
	    		//------------------------------
	    		OpenIntDoubleHashMap uidToCentroidSim = new OpenIntDoubleHashMap();
	    		for(int j=0;j<totalPoints;j++) //for all points
	    		{
	    			//Get a point
	    			possibleC  = dataset.get(j);		
	    			possibleCSim = findEucledianDistanceBetweenAvgAndEntity(avg, possibleC, true);
	    			uidToCentroidSim.put(possibleC, possibleCSim);

	    		}
	    		
	    		//Sort them ... "varying distance from overall mean"
	    		IntArrayList myUsers 	 	= uidToCentroidSim.keys();
	    		DoubleArrayList myWeights   = uidToCentroidSim.values();      		   
	    		uidToCentroidSim.pairsSortedByValue(myUsers, myWeights);

	    		int totalPossibleC = uidToCentroidSim.size();
	    		int number	=		0;
	    		int m;

	    		// Look at this formula, I dont know what It means,
	    		// But you need to confirm it from the paper
	    		// sL = x(1+( L-1) *M/K). 
	    		for (int j=1;j<totalPossibleC; j++ ) {
	    			m = (j-1)*totalPoints;
	    			number = 1+ m/k;
	    			seed= myUsers.get(number);
	    			
	    			if(!allCentroids.contains(seed)) {	 
	    				allCentroids.add(seed);
	    				chosenCentroids.add( new Centroid (seed,helper));        					  	
	    			}
	    			if(chosenCentroids.size() == k){
	    				break;
	    			}
	    		} 

	       		this.centroids = chosenCentroids; 
	    		return chosenCentroids;

		}

 
        //----------------
        //  get variant name
        // ---------------

      	@Override
      	public String getName(int variant) {
   
      		return "SimpleKMeansVarianceCorrected";
      	}
 /*******************************************************************************************************/
 
}

