package algorithms.memorybased.KMeansInitialization;

import java.util.*;

import algorithms.memorybased.KMeansInitialization.Centroid;

import netflix.memreader.*;

import cern.colt.list.*;


// k=4, 0.97
// k=8, 1.0
// k =1, 0.9523 (simple CF)

/************************************************************************************************/
public class SimpleKMeans extends CallInitializationMethods implements KMeansVariant 
/************************************************************************************************/

{

	    private MemHelper 	helper;
	    ArrayList<Centroid> centroids;
	    public ArrayList<Centroid> newCentroids;
    
/************************************************************************************************/

	    public SimpleKMeans(MemHelper helper)    
	    {
	    	super (helper);
	        this.helper   = helper;

	    }
	    

	    
	
/**********************************************************************************************/
 
    /**
     * Chooses k users to serve as intial centroids for 
     * the kMeansPlus algorithm.
     * Each time, we have to choose the centorid which is at the farthest distant from the current one 
     *
     * @param  dataset  The list uids. 
     * @param  k  The number of centroids (clusters) desired. 
     * @return A List of randomly chosen centroids. 
     */

	@Override
	public ArrayList<Centroid> chooseCentroids(int variant, IntArrayList dataset, int k,double cliqueAverage)
			
	{
		
		System.out.println("=========================================");
		System.out.println("       " + getName());
		System.out.println("=========================================");

		Random rand = new Random();

		ArrayList<Centroid> centroids = new ArrayList<Centroid>(k);
		newCentroids = new ArrayList<Centroid>(k);
		IntArrayList chosenCentroids = new IntArrayList(); 

		//_______________________________
		//code to check duplicate entries

		int number 							= 0;
		IntArrayList centroidAlreadyThere	= new IntArrayList();       
		int C								= 0;
		int datasetSize 					= dataset.size();

		for(int i = 0; i < k; i++)         
		{        
			while(true)        		
			{        		       	
				number = rand.nextInt(datasetSize-1);
				C= dataset.get(number);        		

				if(!centroidAlreadyThere.contains(C));
				{
					centroidAlreadyThere.add(C);
					break;
				}
			}

			centroids.add( new Centroid (C,helper));
			chosenCentroids.add(C);

		}
		 
		return centroids;

	}

		//----------------
		//  get variant name
		// ---------------

@Override
public String getName() {
	

	return "SimpleKMeans";
}
      
 /*******************************************************************************************************/
 
}

