package algorithms.memorybased.KMeansInitialization;

import java.util.*;

import algorithms.memorybased.KMeansInitialization.Centroid;

import netflix.memreader.*;

import cern.colt.list.*;
import cern.colt.map.*;
import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;



/************************************************************************************************/
public class SimpleKMeansNormalDistributionMovie  extends CallInitializationMethods implements KMeansVariant
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
	    
	    public SimpleKMeansNormalDistributionMovie(MemHelper helper)    
	    {
	    	super (helper);
	        this.helper   = helper;
	
	    }

/**********************************************************************************************/
 
    /**
     * Chooses k users to serve as intial centroids for 
     * the kMeansPlus algorithm.
     * Each time, we have to choose the centorid based on normal distribution
     *
     * @param  dataset  The list uids. 
     * @param  k  The number of centroids (clusters) desired. 
     * @return A List of randomly chosen centroids. 
     */

		@Override
		public ArrayList<Centroid> chooseCentroids(int variant, IntArrayList dataset, int k, double cliqueAverage) 
		{


			System.out.println("=========================================");
			System.out.println("       " + getName());
			System.out.println("=========================================");
			
			ArrayList<Centroid> centroids = new ArrayList<Centroid>(k);
			newCentroids = new ArrayList<Centroid>(k);
			IntArrayList chosenCentroids = new IntArrayList(); 


			RandomEngine eng;							
			IntArrayList centroidAlreadyThere	= new IntArrayList();   
			int candidateSeed					= 0;
 
			double avgMov= helper.getGlobalMovAverage();
			Normal norm ;
			int number;

			for(int i = 1; i <= k; i++)         
			{        
				while(true)        		
				{  

					//------------------
					// Normal distribution	
					//------------------ 			

					eng= Normal.makeDefaultGenerator();
					norm = new Normal(avgMov, 0.05, eng);
					//norm = new Normal(avg, 0.1, eng);
					number= (int) norm.nextInt();

					candidateSeed=dataset.get(number);
					if(!centroidAlreadyThere.contains(candidateSeed));
					{
						centroidAlreadyThere.add(candidateSeed);
						break;
					}
				}

				centroids.add( new Centroid (candidateSeed,helper));
				chosenCentroids.add(candidateSeed);

			}

			return centroids;

		}
	    


		//----------------
		//  get variant name
		// ---------------

	@Override
	public String getName() {
		
	
		return "SimpleKMeansNormalDistributionMovie";
	}
 /*******************************************************************************************************/
 
}

