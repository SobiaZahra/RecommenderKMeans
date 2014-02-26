package algorithms.memorybased.KMeansInitialization;

import java.util.*;

import algorithms.memorybased.KMeansInitialization.Centroid;

import netflix.memreader.*;
import netflix.utilities.*;
import cern.colt.list.*;
import cern.colt.map.*;
import cern.jet.random.Uniform;

// k=4, 0.97
// k=8, 1.0
// k =1, 0.9523 (simple CF)

/************************************************************************************************/
public class SimpleKMeansSinglePass extends CallInitializationMethods implements KMeansVariant
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
	    
	    public SimpleKMeansSinglePass(MemHelper helper)    
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
public ArrayList<Centroid> chooseCentroids(int variant, IntArrayList dataset,int k, double cliqueAverage) 
		
{

	//-----------------------------------
	//to be completed yet.... in progress
	//------------------------------------

	System.out.println("=========================================");
	System.out.println("       " + getName());
	System.out.println("=========================================");

	ArrayList<Centroid> centroids = new ArrayList<Centroid>(k);
	newCentroids = new ArrayList<Centroid>(k);
	IntArrayList chosenCentroids = new IntArrayList(); 

	int number 							= 0;
	IntArrayList centroidAlreadyThere	= new IntArrayList();   
	int C								= 0;
	double avg;
	//     double avgMov;
	int totalPoints			 = dataset.size();			// All users

	int possibleC			 = 0;						// A point from dataset
	double possibleCSim		 = 0;	 					// Sim of the point from the dataset
	IntArrayList myUsers = null;
	DoubleArrayList myWeights = null;
	IntArrayList mySimUsers = null;
	DoubleArrayList mySimWeights = null;

	avg= helper.getGlobalAverage();
	//     avgMov= helper.getGlobalMovAverage();

	int avgI					= (int)avg;
	OpenIntDoubleHashMap uidToUidSim = new OpenIntDoubleHashMap();	
	
	// Finding the "Average pairwise Euclidean distance" (d1)

		double d1 = 0;
		for (int i=0;i<totalPoints-1;i++) {
			int user1 = i;
			for (int j=i+1;j<totalPoints;j++) {
				int user2 = j;
				d1  += findEucledianDistanceBetweenTwoEntities (user1, user2, true);		
			}
			uidToUidSim.put(i, d1);
		}


		mySimUsers 	 	= uidToUidSim.keys();
		mySimWeights    =uidToUidSim.values();      		   
		uidToUidSim.pairsSortedByValue(mySimUsers, mySimWeights);

		int totalPossibleC = uidToUidSim.size();   
		System.out.println( " totalpossibleC " + totalPossibleC);
		System.out.println( " points sim " + uidToUidSim);
	
//		int  	min = myUsers.get(1);
//		int 	max= myUsers.get(totalPossibleC-1);
//		double 	minDistance = myWeights.get(1);
//		double 	maxDistance= myWeights.get(totalPossibleC-1);
//
//		/////////////////////
//		System.out.println( " min uid is ::: " + min + " min distance :: " + minDistance);
//		System.out.println( " max uid is ::: " + max + " max distance :: " + maxDistance);
//		System.out.println( "******************************************************* ");
//		/////////////////


		for(int p = 0; p < k; p++) 					//for total number of clusters         
		{
//		for (int j=1;j<totalPossibleC; j++ )
//		{
//			
//			C = myUsers.get(j);
			if (p==0)
			{
				C = myUsers.get(1);
				centroids.add( new Centroid (C,helper));
				chosenCentroids.add(C);
			}
				
			else 
				
			{
//				for (int j=1;j<totalPossibleC; j++ )
//				{
//					
//					C = myUsers.get(j);

			// good to make it local, as for each new centroid, we want new weights
			OpenIntDoubleHashMap uidToCentroidSim = new OpenIntDoubleHashMap();	
			int currentCentroidsSize = chosenCentroids.size();
			int existingCentroid     = 0;
			double closestWeight	 = 2;

			for(int j=0;j<totalPoints;j++) //for all points
			{
				//Get a point
				possibleC  	  = myUsers.get(j);		
				closestWeight = 10;

				for (int m=0;m<currentCentroidsSize; m++)
				{
					// Get an existing centroid
					existingCentroid =  chosenCentroids.get(m);

					//-----------------------------
					// Now we find distance of each
					// point from closest centroid
					// i.e. sim > largest 
					//-----------------------------

					//Now we find the similarity between a user and the chosen cluster.        			
					possibleCSim =  findSimVSBetweenACentroidAndUser(existingCentroid, possibleC);
					if(possibleCSim < closestWeight)
						closestWeight = possibleCSim;

				}

				// only add the distance of a point with the closest centroid
				uidToCentroidSim.put(possibleC, closestWeight);


			} // finished finding similarity b/w all users and the chosen centroid

			//-----------------------
			// Find the next centroid
			//-----------------------

			// sort weights in ascending order (So first element has the lowest sim)	
			 myUsers = uidToCentroidSim.keys();
			 myWeights = uidToCentroidSim.values();
			 uidToCentroidSim.pairsSortedByValue(myUsers, myWeights);

			int toalPossibleC = uidToCentroidSim.size();
			
				
			if( !(centroidAlreadyThere.contains(C)))
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
	

	return "SimpleKMeansSinglePass";
}
   
 /*******************************************************************************************************/
 
}

