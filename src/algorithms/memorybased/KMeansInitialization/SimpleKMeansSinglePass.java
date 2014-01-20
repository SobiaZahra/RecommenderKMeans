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
	System.out.println("       " + getName(variant));
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


	avg= helper.getGlobalAverage();
	//     avgMov= helper.getGlobalMovAverage();

	int avgI					= (int)avg;
	OpenIntDoubleHashMap uidToUidSim = new OpenIntDoubleHashMap();	
	for(int j=0;j<totalPoints;j++) //for all points
	{
		//Get a point
		possibleC  	   = dataset.get(j);

		double distance = 0 ;
		/////////////////
		for(int m=0;m<totalPoints;m++) //for all points
		{

			distance = findSimPCCBetweenACentroidAndUser(j,m);
			distance += distance;



		}
		uidToUidSim.put(j, distance);
		/////////////////////
		myUsers 	 	= uidToUidSim.keys();
		myWeights    =uidToUidSim.values();      		   
		uidToUidSim.pairsSortedByValue(myUsers, myWeights);

		//	     	int totalPossibleC = uidToCentroidSim.size();   

	}
	int  	min = myUsers.get(0);
	int 	max= myUsers.get(totalPoints-1);
	double 	minDistance = myWeights.get(0);
	double 	maxDistance= myWeights.get(totalPoints-1);

	/////////////////////
	System.out.println( " min uid is ::: " + min + " min distance :: " + minDistance);
	System.out.println( " max uid is ::: " + max + " max distance :: " + maxDistance);
	System.out.println( "******************************************************* ");
	/////////////////
	for(int i = 0; i < k; i++)         
	{     OpenIntDoubleHashMap uidToCentroidSim = new OpenIntDoubleHashMap();	


	//------------------------------
	// Find sim to centroid
	//------------------------------

	// 	for(int j=0;j<totalPoints;j++) //for all points
	// 	{
	// 		//Get a point
	// 		possibleC  	   = dataset.get(j);
	// 		
	// 		double distance = 0 ;
	// 		/////////////////
	// 		for(int m=0;m<totalPoints;m++) //for all points
	//     	{
	// 			 distance = findSimPCCBetweenACentroidAndUser(j,m);
	// 			 distance += distance;
	// 			
	//     	
	//     	}
	// 		uidToUidSim.put(j, distance);
	// 		System.out.println( "uid is ::: " + j + " Sum Distance is :: " + distance);
	// 		
	// 		/////////////////
	// 		possibleCSim =  findSimPCCBetweenACentroidAndUser(possibleC, avgI);
	// 		uidToCentroidSim.put(possibleC, possibleCSim);
	//
	// 	}
	// 	IntArrayList myUsers 	 	= uidToCentroidSim.keys();
	// 	DoubleArrayList myWeights    =uidToCentroidSim.values();      		   
	// 	uidToCentroidSim.pairsSortedByValue(myUsers, myWeights);

	// 	int totalPossibleC = uidToCentroidSim.size();   
	// 	int  	min = myUsers.get(1);
	// 	int 	max= myUsers.get(totalPoints-1);
	while(true)        		
	{   
		//------------------
		//Uniform distribution 
		//------------------

		Uniform unif;
		unif=new Uniform(0,k,avgI);
		number= unif.nextInt();

		//-------------------------------
		//Uniform distribution with min max
		//--------------------------------

		//  			number= Uniform.staticNextIntFromTo(min, max);


		C=dataset.get(number);
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
public String getName(int variant) {
	

	return "SimpleKMeansSinglePass";
}
   
 /*******************************************************************************************************/
 
}

