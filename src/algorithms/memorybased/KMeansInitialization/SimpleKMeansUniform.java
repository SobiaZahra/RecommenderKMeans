package algorithms.memorybased.KMeansInitialization;

import java.util.*;

import algorithms.memorybased.KMeansInitialization.Centroid;

import netflix.memreader.*;
import cern.colt.list.*;
import cern.colt.map.*;
import cern.jet.random.Uniform;


/************************************************************************************************/
public class SimpleKMeansUniform extends CallInitializationMethods implements KMeansVariant
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
	    
	    public SimpleKMeansUniform(MemHelper helper)    
	    {
	    	super (helper);
	        this.helper   = helper;

	    }



/**********************************************************************************************/
 
    /**
     * Chooses k users to serve as intial centroids for 
     * the kMeansPlus algorithm.
     * Each time, we have to choose the centorid based on uniform distribution
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
			
			ArrayList<Centroid> centroids = new ArrayList<Centroid>(k);
			newCentroids = new ArrayList<Centroid>(k);
			IntArrayList chosenCentroids = new IntArrayList(); 

			int number 							= 0;
			IntArrayList centroidAlreadyThere	= new IntArrayList();   
			int C								= 0;
			double avg;
			//	         double avgMov;
			int totalPoints			 = dataset.size();			// All users

			int possibleC			 = 0;						// A point from dataset
			double possibleCSim		 = 0;	 					// Sim of the point from the dataset


			avg= helper.getGlobalAverage();
			//	         avgMov= helper.getGlobalMovAverage();

			int avgI  = (int)avg;

			
			for(int i = 0; i < k; i++)         
			{     
				System.out.println("Inside K clusers");
				OpenIntDoubleHashMap uidToCentroidSim = new OpenIntDoubleHashMap();	

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
				uidToCentroidSim.pairsSortedByValue(myUsers, myWeights);
	
				//	     	int totalPossibleC = uidToCentroidSim.size();   
				int  min = myUsers.get(1);
				int  max = myUsers.get(totalPoints-1);
				while(true)        		
				{   
					//------------------
					//Uniform distribution 
					//------------------
	
					//	              Uniform unif;
					//	      		unif=new Uniform(0,k,avgI);
					//	     			number= unif.nextInt();
	
					//-------------------------------
					//Uniform distribution with min max
					//--------------------------------
	
					number= Uniform.staticNextIntFromTo(min, max);
	
	
					C=dataset.get(number);
					if(!centroidAlreadyThere.contains(C));
					{
						centroidAlreadyThere.add(C);
						break;
					}
				}//end while
	
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
	

	return "SimpleKMeansUniform";
}

             //----------------------------------------------------------------      
/*******************************************************************************************************/
     
}

