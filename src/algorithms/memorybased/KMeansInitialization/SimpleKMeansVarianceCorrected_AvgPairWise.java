package algorithms.memorybased.KMeansInitialization;

import java.util.*;
import algorithms.memorybased.KMeansInitialization.Centroid;
import netflix.memreader.*;
import cern.colt.list.*;
import cern.colt.map.*;

/************************************************************************************************/
public class SimpleKMeansVarianceCorrected_AvgPairWise  extends CallInitializationMethods implements KMeansVariant
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
	    
	    public SimpleKMeansVarianceCorrected_AvgPairWise(MemHelper helper) {
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
	    	int C					 = 0;						// Centroid			
	    	int possibleC			 = 0;						// A point from dataset
	    	double possibleCSim		 = 0;	 					// Sim of the point from the dataset
	    	double  avg				 = helper.getGlobalAverage();
	    	

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
			
			

	    	for(int i = 0; i < k; i++) 					//for total number of clusters         
	    	{
	    		OpenIntDoubleHashMap uidToCentroidSim = new OpenIntDoubleHashMap();	

	    		//------------------------------
	    		// Find sim to centroid
	    		//------------------------------

	    		// The chnage is, instead of avg of the users, we are taking the average pair wise distance
	    		// between all users 
	    		for(int j=0;j<totalPoints;j++) //for all points
	    		{
	    			//Get a point
	    			possibleC  = dataset.get(j);		
	    			possibleCSim = findEucledianDistanceBetweenAvgAndEntity(d1, possibleC, true);
	    			uidToCentroidSim.put(possibleC, possibleCSim);

	    		}
	    		
	    		//Sort them ... "varying distance from overall mean"
	    		IntArrayList myUsers 	 	= uidToCentroidSim.keys();
	    		DoubleArrayList myWeights   = uidToCentroidSim.values();      		   
	    		uidToCentroidSim.pairsSortedByValue(myUsers, myWeights);

	    		int totalPossibleC = uidToCentroidSim.size();
	    		int number	=		0;
	    		int m;

	    		// Here, you have mean and I am sure, there are very simple approaches which are different 
	    		// from the one proposed in the paper; to choose seeds at varying distance from the mean
	    	
	    		// The only problem here is that, we might not be able to add all 'K' seeds here.
	    		// e.g. lets say k=100 and this loop only find 10 ... how to add rest 90?
	    		for (int j=1;j<totalPossibleC; j++ )
	    		{

	    			m= (j-1)*totalPoints;
	    			number = 1+ m/k;
	    			C= myUsers.get(number);
	    			
	    			if(allCentroids.contains(C)==false) {	 
	    				allCentroids.add(C);        				  			
	    				chosenCentroids.add( new Centroid (C,helper));	        					  	
	    			}
	    			if(chosenCentroids.size() == k){
	    				break;
	    			}
	    		} 
	    		
	    		this.centroids = chosenCentroids; 
	    	}

	    	return chosenCentroids;
		}

 
        //----------------
        //  get variant name
        // ---------------

      	@Override
      	public String getName(int variant) {
   
      		return "SimpleKMeansVarianceCorrected_AvgPairWise";
      	}
 /*******************************************************************************************************/
 
}

