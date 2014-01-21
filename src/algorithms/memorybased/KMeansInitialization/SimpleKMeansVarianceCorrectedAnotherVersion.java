package algorithms.memorybased.KMeansInitialization;

import java.util.*;
import algorithms.memorybased.KMeansInitialization.Centroid;
import netflix.memreader.*;
import netflix.utilities.MeanOrSD;
import cern.colt.list.*;
import cern.colt.map.*;

/************************************************************************************************/
public class SimpleKMeansVarianceCorrectedAnotherVersion  extends CallInitializationMethods implements KMeansVariant
/************************************************************************************************/ 

{
	    private MemHelper 	helper;
	    ArrayList<Centroid> centroids;
	    final static int DEVAITION = 2; // 2 stdDev
	    
/************************************************************************************************/

	    /**
	     * Builds the RecTree and saves the resulting clusters.
	     */
	    
	    public SimpleKMeansVarianceCorrectedAnotherVersion(MemHelper helper) {
	    	super (helper);
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
			System.out.println("       " + getName());
			System.out.println("=========================================");

	    	ArrayList<Centroid> chosenCentroids = new ArrayList<Centroid>(k);
	       	IntArrayList allCentroids = new IntArrayList();		    // All distinct chosen centroids              

	    	int totalPoints			 = dataset.size();			// All users			
	    	int possibleC			 = 0;						// A point from dataset
	    	double possibleCSim		 = 0;	 					// Sim of the point from the dataset
	    	double  avg				 = helper.getGlobalAverage();
	    	

	    		OpenIntDoubleHashMap uidToCentroidSim = new OpenIntDoubleHashMap();	

	    		//------------------------------
	    		// Find sim to centroid
	    		//------------------------------

	    		for(int j=0;j<totalPoints;j++) //for all points
	    		{
	    			//Get a point
	    			possibleC  = dataset.get(j);		
	    			possibleCSim = findEucledianDistanceBetweenAvgAndEntity(avg, possibleC, true);
	    			uidToCentroidSim.put(possibleC, possibleCSim);

	    		}
	    		
	    		//Sort them ... "varying distance from overall mean"
	    		IntArrayList myUsers = uidToCentroidSim.keys();
	    		DoubleArrayList myWeights = uidToCentroidSim.values();      		   
	    		uidToCentroidSim.pairsSortedByValue(myUsers, myWeights);

	    		// standard deviation
	    		int totalPossibleC = uidToCentroidSim.size();
	    		double stdDev = MeanOrSD.calculateMeanOrSD (myWeights, totalPossibleC, 1); 
	    		double threshold  =  0;
	    		
	    		// we keep finding the seeds, until we find the max seeds
	    		for(int t=10;t>=0; t++) {
		    			threshold = t + stdDev;
		    			chosenCentroids =  selectAndAddCentroids (  allCentroids,
							    									chosenCentroids,
							    									myUsers,
														    	    myWeights,
																    totalPossibleC, 
																    threshold,
																    avg,
																    k);
		    			if(chosenCentroids.size() == k) {
		    				break;
		    			}
		    		}
	    		
	    		this.centroids = chosenCentroids; 
	    		return chosenCentroids;

		}

	    private ArrayList<Centroid> selectAndAddCentroids ( IntArrayList allCentroids,
	    												    ArrayList<Centroid> chosenCentroids,
	    												    IntArrayList myUsers,
	    										    	    DoubleArrayList myWeights,
	    												    int totalPossibleC, 
	    												    double threshold,
	    												    double avg, 
	    												    int maximumSeeds) {
	    	
	    // Here, you have mean and I am sure, there are very simple approaches which are different 
	    // from the one proposed in the paper; to choose seeds at varying distance from the mean
		for (int j=1;j<totalPossibleC; j++ )  {
		
			// why not use Standard deviation, and using seeds that are away 2SD from mean?
			int seed = myUsers.get(j);
			double weight  =myWeights.get(j);
			
			// points lying away from avg + threshold (which is essentially stdDev)
			if( Math.abs(weight - (avg + threshold)) >=0) {
					
				if(allCentroids.contains(seed) == false) {
    				allCentroids.add(seed);
    				chosenCentroids.add( new Centroid (seed,helper));
    				break;        					  	
    			}
			}
			
			if(chosenCentroids.size() == maximumSeeds) {
				break;
			}
		}
		
		return chosenCentroids;
	    	
	} // only the last one will be added
		
        //----------------
        //  get variant name
        // ---------------

      	@Override
      	public String getName() {
   
      		return "SimpleKMeansVarianceCorrectedAnotherVersion";
      	}
 /*******************************************************************************************************/
 
}

