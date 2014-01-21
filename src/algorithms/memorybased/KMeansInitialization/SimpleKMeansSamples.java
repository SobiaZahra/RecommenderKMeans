package algorithms.memorybased.KMeansInitialization;

import java.util.*;

import algorithms.memorybased.KMeansInitialization.Centroid;

import netflix.memreader.*;
import cern.colt.list.*;
import cern.colt.map.*;



/************************************************************************************************/
public class SimpleKMeansSamples extends CallInitializationMethods implements KMeansVariant
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
	    
	    public SimpleKMeansSamples(MemHelper helper)    
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
		public ArrayList<Centroid> chooseCentroids(int variant, IntArrayList dataset, int k, double cliqueAverage)
		
		{

			System.out.println("=========================================");
			System.out.println("       " + getName(variant));
			System.out.println("=========================================");

			ArrayList<Centroid> centroids = new ArrayList<Centroid>(k);
			newCentroids = new ArrayList<Centroid>(k);
			IntArrayList chosenCentroids = new IntArrayList(); 

			IntArrayList centroidAlreadyThere	= new IntArrayList();   
			int C								= 0;
			double avg;
			int totalPoints			 = dataset.size();			// All users      	
			int possibleC			 = 0;						// A point from dataset
			double possibleCSim		 = 0;	 					// Sim of the point from the dataset
			avg= helper.getGlobalAverage();
			int avgI					= (int)avg;

			for(int i = 0; i < k; i++)         
			{    
				OpenIntDoubleHashMap uidToCentroidSim = new OpenIntDoubleHashMap();	
				OpenIntDoubleHashMap uidToCentroidProb = new OpenIntDoubleHashMap(); //For KMeans prob counting
				//------------------------------
				// Find sim to centroid
				//------------------------------

				for(int j=0;j<totalPoints;j++) //for all points
				{
					//Get a point
					possibleC  	   = dataset.get(j);		
					possibleCSim =  findSimPCCBetweenACentroidAndUser(possibleC, avgI);
					uidToCentroidSim.put(possibleC, possibleCSim);

				}
				IntArrayList myUsers 	 	= uidToCentroidSim.keys();
				DoubleArrayList myWeights    =uidToCentroidSim.values();   

				int totalUsersSize 			= myUsers.size();

				for (int m=0;m<totalUsersSize;m++)
				{
					double alfa= 0.3;
					int uid 			=  myUsers.get(m);
					double pointXWeight =  myWeights.get(m);
					double prob 		=  alfa*(pointXWeight);

					uidToCentroidProb.put(uid,prob);		//Uid to Prob

				}

				// sort prob weights in ascending order (So first element has the lowest sim)			
				IntArrayList    myProbUsers 	 = uidToCentroidProb.keys();
				DoubleArrayList myProbWeights    = uidToCentroidProb.values();      		  	
				uidToCentroidSim.pairsSortedByValue(myProbUsers, myProbWeights);

				int toalPossibleC = uidToCentroidProb.size(); 		  

				// As both are sorted, so it should be in the first index

				for (int j=0;j<toalPossibleC; j++ )
				{
					C = myProbUsers.get(j);
					int moviesSeenByUser = helper.getNumberOfMoviesSeen(C);

					if( !(centroidAlreadyThere.contains(C)) && moviesSeenByUser>1)
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
public String getName(int variant) {
	

	return "SimpleKMeansSamples";
}


 /*******************************************************************************************************/
 
}

