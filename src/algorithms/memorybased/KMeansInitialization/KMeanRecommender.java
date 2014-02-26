package algorithms.memorybased.KMeansInitialization;

import java.io.BufferedWriter;


import java.io.FileWriter;
import java.util.*;

import netflix.memreader.*;


import netflix.algorithms.memorybased.memreader.FilterAndWeight;

import netflix.recommender.ItemItemRecommender;
import rmse.RMSECalculator;

import cern.colt.Version;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.list.LongArrayList;
import cern.colt.map.OpenIntDoubleHashMap;
import netflix.utilities.*;


/************************************************************************************************/
public class KMeanRecommender 
{
	// initialize class here 
	private CallInitializationMethods		callMethod;
	private SimpleKMeansPlusAndProbPower		simpleKPlusAndProbPowerTree;
	private SimpleKMeansPlusAndProbPower		simpleKPlusAndProbPowerTree_NoSimThr;
	private SimpleKMeansPlusAndLogPower		simpleKPlusAndLogPowerTree;

	private int								myClasses;
	private int								myTotalFolds;
	private String 							variant;
	private String 							distanceMeasure;

	 //Objects of some classes
	private static MemHelper 	trainMMh;
	private MemHelper 			allHelper;
	private MemHelper 			testMMh;
//	private MeanOrSD			MEANORSD;
	private Timer227 			timer;
	private FilterAndWeight		myUserBasedFilter;   //Filter and Weight, for User-based CF
	private ItemItemRecommender	myItemRec;		     //item-based CF    


	private long 		kMeanTime;
	private long 		kMeanTime1;
	private long 		kMeanClusterTime;
	private long 		showTime[];
	private int         kClusters;
	private int			nItrations;

	private BufferedWriter      writeData;

	private String      myPath;
	private int         totalNan=0;
	private int         totalNegatives=0;
	private int			KMeansOrKMeansPlus; 
	private int			simVersion;


	private int			powerUsersThreshold;					// power user size
	private double		simThreshold;							//
//	private int			numberOfneighbours;						// no. of neighbouring cluster for an active user

	//Regarding Results
	private double 								MAE;
	private double								MAEPerUser;
	private double 								RMSE;
	private double  							RMSEPerUser;
	private double								Roc;
	private double								coverage;
	private double								pValue;
	private double								kMeanEigen_Nmae;
	private double								kMeanCluster_Nmae;

	//SD in one fold or when we do hold-out like 20-80
	private double								SDInMAE;
	private double								SDInROC;
	private double 								SDInTopN_Precision[];
	private double 								SDInTopN_Recall[];
	private double 								SDInTopN_F1[];	

	private double            					precision[];		//evaluations   
	private double              				recall[];   
	private double              				F1[];    
	private OpenIntDoubleHashMap 		midToPredictions;	//will be used for top_n metrics (pred*100, actual)

	

	private int 				myFlg;
	private  int 				currentFold;
	private  IntArrayList		myCentroids1, myCentroids2,myCentroids3,myCentroids4,myCentroids5;


	/************************************************************************************************/

	public KMeanRecommender()    
	{

//		howMuchClusterSize = 0;
		kMeanTime			= 0;  
		kMeanClusterTime			= 0;
		kMeanTime1			= 0;

		myClasses			= 5;
		simVersion			= 1;  //1=PCCwithDefault, 2=PCCwithoutDefault
		//3=VSWithDefault,  4=VSWithDefault
		//5=PCC, 			  6=VS

		KMeansOrKMeansPlus  = 0;
		timer 				 = new Timer227();
//		MEANORSD			 = new MeanOrSD();

		showTime			= new long[20];


		//-------------------------------------------------------
		//Answers
		
//		numberOfneighbours  = 0;

		MAE 				= 0;
		MAEPerUser			= 0;
		RMSE 				= 0;
		RMSEPerUser 		= 0;
		kMeanEigen_Nmae		= 0;
		kMeanCluster_Nmae	= 0;
		Roc 				= 0;
		coverage			= 0;
		pValue				= 0;
		SDInMAE				= 0;
		SDInROC				= 0;
		SDInTopN_Precision	= new double[8];
		SDInTopN_Recall		= new double[8];
		SDInTopN_F1			= new double[8];

		midToPredictions    = new OpenIntDoubleHashMap();     	  
		precision    		= new double[8];		//topN; for six values of N (top5, 10, 15...30)
		recall  			= new double[8];		// Most probably we wil use top10, or top20
		F1					= new double[8];

		
		myCentroids1   = new IntArrayList();
		myCentroids2   = new IntArrayList();
		myCentroids3   = new IntArrayList();
		myCentroids4   = new IntArrayList();
		myCentroids5   = new IntArrayList();



	}
	

	/************************************************************************************************/

	/**
	 *  It initialise an object and call the method for building the three 
	 */
	public void callKTree(int methodVariant , MemHelper helper, int callNo, int MAX_ITERATIONS )     
	{
		KMeansOrKMeansPlus = methodVariant;
				//-----------------------
				// K-Means Plus and 
				// Prob Power
				//-----------------------    	

				 if(KMeansOrKMeansPlus==15)
				{
					myFlg =1;

					IntArrayList allCentroids     = new IntArrayList();		    // All distinct chosen centroids  
					timer.start(); 

					simpleKPlusAndProbPowerTree.cluster(kClusters, callNo, MAX_ITERATIONS, simVersion, simThreshold, powerUsersThreshold,1, allCentroids);
					//	simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, callNo, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,1, allCentroids);
					timer.stop(); 
					kMeanClusterTime = timer.getTime();
					showTime[KMeansOrKMeansPlus]=kMeanTime;
					System.out.println("KMeans Plus and Prob Power Tree took " + kMeanClusterTime + " s to build");    	
					System.out.println("");    	
					timer.resetTimer();
					
//					timer.start(); 
//					if(currentFold==1)
//						simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, 1, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,0,  myCentroids1);
//					else if(currentFold==2)
//						simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, 1, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,0,  myCentroids2);
//					else if(currentFold==3)
//						simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, 1, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,0,  myCentroids3);
//					else if(currentFold==4)
//						simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, 1, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,0,  myCentroids4);
//					else if(currentFold==5)
//						simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, 1, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,0,  myCentroids5);
//
//					timer.stop();

//					kMeanTime = timer.getTime();
//					System.out.println("KMeans Plus and Log Power Tree took " + kMeanTime + " s to build"); 
//					System.out.println(""); 
//
//					timer.resetTimer();
				}
				
				 
				//-----------------------
					// K-Means Plus and 
					// Log Power
					//-----------------------    	

					 if(KMeansOrKMeansPlus==19)
					{
						myFlg =1;

						IntArrayList allCentroids     = new IntArrayList();		    // All distinct chosen centroids  
						timer.start(); 

						simpleKPlusAndLogPowerTree.cluster(kClusters, callNo, MAX_ITERATIONS, simVersion, simThreshold, powerUsersThreshold,1, allCentroids);
						//	simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, callNo, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,1, allCentroids);
						timer.stop(); 
						kMeanClusterTime = timer.getTime();
						//showTime[KMeansOrKMeansPlus]=kMeanTime;
						System.out.println("KMeans Plus and Log Power Tree took " + kMeanClusterTime + " s to build");    	
						System.out.println("");    	
						timer.resetTimer();
						
					}
					
		//-----------------------
		// K-Means all other variant
		//-----------------------
				else if(KMeansOrKMeansPlus<15 || KMeansOrKMeansPlus>19)
					
				{
					timer.start();	              

					callMethod.cluster(KMeansOrKMeansPlus,kClusters, callNo, MAX_ITERATIONS, simVersion);       
					timer.stop();

					kMeanClusterTime = timer.getTime();
//					kMeanTime = timer.getTime();
//					showTime[KMeansOrKMeansPlus]=kMeanTime;
					System.out.println( callMethod.getName () + " -->  took " + kMeanClusterTime + " s to build");    	
					timer.resetTimer();
				}

	}   

	/**
	 * Basic recommendation method for memory-based algorithms.
	 * 
	 * @param user
	 * @param movie
	 * @return the predicted rating, or -99 if it fails (mh error)
	 */

	//We call it for active user and a target movie
	public double recommend(int activeUser, int targetMovie, int totalNeighbours)    
	{

		double weightSum = 0, voteSum = 0;
		
		//========================
		// recommendation 
		//=========================
		
				//------------------------
				//  neighbours priors prob plus
				//------------------------
				if (KMeansOrKMeansPlus == 15)        	
				{
//					simpleKUsers = simpleKPlusAndLogPowerTree_NoSimThr.getClusterByUID(activeUser);           		
					int activeClusterID = simpleKPlusAndProbPowerTree.getClusterIDByUID(activeUser);

					OpenIntDoubleHashMap simMap = new OpenIntDoubleHashMap();	//sim b/w an active user and the clusters

					// Find sim b/w a user and the cluster he lies in        		
					double simWithMainCluster = simpleKPlusAndProbPowerTree.findSimWithOtherClusters(activeUser, activeClusterID );

					// Find sim b/w a user and all the other clusters
					for(int i=0;i<kClusters; i++)
					{
						if(i!=activeClusterID)
						{
							double activeUserSim  = simpleKPlusAndProbPowerTree.findSimWithOtherClusters(activeUser, i );

							if(activeUserSim!=0)					//zero must not be there?, as some negative are there (not sure)
								simMap.put(i,activeUserSim );      					
						} 

					} //end for

					// Put the mainCluster sim as well
					simMap.put(activeClusterID,simWithMainCluster );

					//sort the pairs (ascending order)
					IntArrayList keys = simMap.keys();
					DoubleArrayList vals = simMap.values();        		
					simMap.pairsSortedByValue(keys, vals);        		
					int simSize = simMap.size();
					LongArrayList tempUsers = trainMMh.getUsersWhoSawMovie(targetMovie);
					IntArrayList  allUsers  = new IntArrayList();

					
					for(int i=0;i<tempUsers.size();i++)
					{
						allUsers.add(MemHelper.parseUserOrMovie(tempUsers.getQuick(i)));
						
					}       		
					//-----------------------------------
					// Find sim * priors
					//-----------------------------------
					// How much similar clusters to take into account? 
					// Let us take upto a certain sim into account, e.g. (>0.10) sim

					int total=0;
					for (int i=simSize-1;i>=0;i--)
					{
						//Get a cluster id
						int clusterId = keys.get(i);

						//Get currentCluster weight with the active user
						double clusterWeight = vals.get(i);

						//Get rating, average given by a cluster
						double clusterRating  = simpleKPlusAndProbPowerTree.getRatingForAMovieInACluster(clusterId, targetMovie);
						double clusterAverage = simpleKPlusAndProbPowerTree.getAverageForAMovieInACluster(clusterId, targetMovie);

						if(clusterRating!=0)
						{
							//Prediction
							weightSum += Math.abs(clusterWeight);      		
							voteSum+= (clusterWeight*(clusterRating-clusterAverage));
							if(total++ == totalNeighbours) break;
						}
					}
					if (weightSum!=0)
						voteSum *= 1.0 / weightSum;        


					//CBF
					double avgRat   = trainMMh.getAverageRatingForMovie(targetMovie);
					double prob_rat = trainMMh.getNumberOfMoviesSeen(activeUser)/150.0;
					double prob_sim = vals.get(simSize-1);		            
					double alpha    = Math.min(1, Math.max(prob_sim+prob_rat, 0));

					double finalRat = (1-alpha)* avgRat ;  

					if (weightSum==0)				// If no weight, then it is not able to recommend????
					{ 
						return finalRat;

						//This is just for learning the training parameters
						// return trainMMh.getAverageRatingForUser(activeUser);
					}

					double answer = trainMMh.getAverageRatingForUser(activeUser) + voteSum;
					finalRat =  ((1-alpha)* avgRat + alpha * answer);

					//------------------------
					// Send answer back
					//------------------------          

					if(answer<=0)
					{	
						totalNegatives++;			         	  
						//	 return 0;

						// This is just for learning the training parameters
						// return trainMMh.getAverageRatingForUser(activeUser);
						
						return finalRat;
					}

					else {
//						totalRecSamples++;   
						//return answer;
						return finalRat;
					}
				}

				//------------------------
				//  neighbours priors log power plus
				//------------------------
				if (KMeansOrKMeansPlus == 19)        	
				{
//					simpleKUsers = simpleKPlusAndLogPowerTree_NoSimThr.getClusterByUID(activeUser);           		
					int activeClusterID = simpleKPlusAndLogPowerTree.getClusterIDByUID(activeUser);

					OpenIntDoubleHashMap simMap = new OpenIntDoubleHashMap();	//sim b/w an active user and the clusters

					// Find sim b/w a user and the cluster he lies in        		
					double simWithMainCluster = simpleKPlusAndLogPowerTree.findSimWithOtherClusters(activeUser, activeClusterID );

					// Find sim b/w a user and all the other clusters
					for(int i=0;i<kClusters; i++)
					{
						if(i!=activeClusterID)
						{
							double activeUserSim  = simpleKPlusAndLogPowerTree.findSimWithOtherClusters(activeUser, i );

							if(activeUserSim!=0)					//zero must not be there?, as some negative are there (not sure)
								simMap.put(i,activeUserSim );      					
						} 

					} //end for

					// Put the mainCluster sim as well
					simMap.put(activeClusterID,simWithMainCluster );

					//sort the pairs (ascending order)
					IntArrayList keys = simMap.keys();
					DoubleArrayList vals = simMap.values();        		
					simMap.pairsSortedByValue(keys, vals);        		
					int simSize = simMap.size();
					LongArrayList tempUsers = trainMMh.getUsersWhoSawMovie(targetMovie);
					IntArrayList  allUsers  = new IntArrayList();

					
					for(int i=0;i<tempUsers.size();i++)
					{
						allUsers.add(MemHelper.parseUserOrMovie(tempUsers.getQuick(i)));
						
					}       		
					//-----------------------------------
					// Find sim * priors
					//-----------------------------------
					// How much similar clusters to take into account? 
					// Let us take upto a certain sim into account, e.g. (>0.10) sim

					int total=0;
					for (int i=simSize-1;i>=0;i--)
					{
						//Get a cluster id
						int clusterId = keys.get(i);

						//Get currentCluster weight with the active user
						double clusterWeight = vals.get(i);

						//Get rating, average given by a cluster
						double clusterRating  = simpleKPlusAndLogPowerTree.getRatingForAMovieInACluster(clusterId, targetMovie);
						double clusterAverage = simpleKPlusAndLogPowerTree.getAverageForAMovieInACluster(clusterId, targetMovie);

						if(clusterRating!=0)
						{
							//Prediction
							weightSum += Math.abs(clusterWeight);      		
							voteSum+= (clusterWeight*(clusterRating-clusterAverage));
							if(total++ == totalNeighbours) break;
						}
					}
					if (weightSum!=0)
						voteSum *= 1.0 / weightSum;        


					//CBF
					double avgRat   = trainMMh.getAverageRatingForMovie(targetMovie);
					double prob_rat = trainMMh.getNumberOfMoviesSeen(activeUser)/150.0;
					double prob_sim = vals.get(simSize-1);		            
					double alpha    = Math.min(1, Math.max(prob_sim+prob_rat, 0));

					double finalRat = (1-alpha)* avgRat ;  

					if (weightSum==0)				// If no weight, then it is not able to recommend????
					{ 
						return finalRat;

						//This is just for learning the training parameters
						// return trainMMh.getAverageRatingForUser(activeUser);
					}

					double answer = trainMMh.getAverageRatingForUser(activeUser) + voteSum;
					finalRat =  ((1-alpha)* avgRat + alpha * answer);

					//------------------------
					// Send answer back
					//------------------------          

					if(answer<=0)
					{	
						totalNegatives++;			         	  
						//	 return 0;

						// This is just for learning the training parameters
						// return trainMMh.getAverageRatingForUser(activeUser);
						
						return finalRat;
					}

					else {
//						totalRecSamples++;   
						//return answer;
						return finalRat;
					}
				}
				//---------------------------------------------
				// Simple using CF--User-based or Item-based
				//---------------------------------------------	   


				else if (KMeansOrKMeansPlus==16)
				{	   
					//first go in these programs and return "0" or at-least the averages if they fail to predict
					double rat = myUserBasedFilter.recommendS(activeUser, targetMovie, 30, 1);
					return rat;

				}	   

				else if (KMeansOrKMeansPlus==17)
				{	   
					double rat = myItemRec.recommend(trainMMh, activeUser, targetMovie, 15, 4);	   
					return rat;

				}


				else if (KMeansOrKMeansPlus==18)
				{	   
					double		rat = (trainMMh.getAverageRatingForMovie(targetMovie) + 
							trainMMh.getAverageRatingForUser(activeUser))/2.0;

					return rat;
				}


				//-----------------------
				//KMeans All other variants
				//-----------------------


				else       	
				{
//					simpleKUsers = callMethod.getClusterByUID(activeUser);            //simpleKPlus 

					int activeClusterID = callMethod.getClusterIDByUID(activeUser);
					OpenIntDoubleHashMap simMap = new OpenIntDoubleHashMap();	//sim b/w an active user and the clusters
					

					// Find sim b/w a user and the cluster he lies in 
					double simWithMainCluster= CallInitializationMethods.findSimWithOtherClusters(activeUser, activeClusterID);
				//	double simWithMainCluster = callMethod.findSimWithOtherClusters(activeUser, activeClusterID );

					// Find sim b/w a user and all the other clusters
					for(int i=0;i<kClusters; i++)
					{
						if(i!=activeClusterID)
						{
//							double activeUserSim  = findSimWithOtherClusters(activeUser, i );
							double activeUserSim  = CallInitializationMethods.findSimWithOtherClusters(activeUser, i );
							simMap.put(i,activeUserSim );      					
						} 

					} //end for

					// Put the mainCluster sim as well
					simMap.put(activeClusterID,simWithMainCluster);

					//sort the pairs (ascending order)
					IntArrayList keys = simMap.keys();
					DoubleArrayList vals = simMap.values();        		
					simMap.pairsSortedByValue(keys, vals);        		
					int simSize = simMap.size();
					int total = 0 ;
					
					for (int i=simSize-1;i>=0;i--)
					{
						//Get a cluster id
						int clusterId =keys.get(i);

						//Get currentCluster weight with the active user
						double clusterWeight =vals.get(i);
				
						//Get rating, average given by a cluster
						double clusterRating = callMethod.getRatingForAMovieInACluster(clusterId, targetMovie);
						double clusterAverage = callMethod.getAverageForAMovieInACluster(clusterId, targetMovie);
				
						if(clusterRating!=0)
						{
							//Prediction
							weightSum += Math.abs(clusterWeight);      		
							voteSum+= (clusterWeight*(clusterRating-clusterAverage)) ;
							if(total++ == totalNeighbours) break;
						}
					}
					if (weightSum!=0)
						voteSum *= 1.0 / weightSum;    


					if (weightSum==0)				// If no weight, then it is not able to recommend????
					{   
						totalNan++;
						return 0;	       
					}

					double answer = trainMMh.getAverageRatingForUser(activeUser) + voteSum;             
					if(answer<=0) {
						totalNegatives++;	
						return 0;
					}

					else {
//						totalRecSamples++;   
						return answer;
					}
				} 	
			}



	/************************************************************************************************/

	public static void main(String[] args)    
	{

		//FT
//	String path = "C:/Users/Sobia/tempRecommender/GitHubRecommender/netflix/netflix/DataSets/FT/";
		
		
		
		//SML
//		String path = "/Projects/RecommenderKMeans/DataSets/SML_ML/FiveFoldData/";
		String path = "C:/Users/Sobia/tempRecommender/GitHubRecommender/netflix/netflix/DataSets/SML_ML/FiveFoldData/";
		KMeanRecommender rec = new KMeanRecommender(); 
		
		//Compute the results
		rec.computeResults(path);
	}


	/************************************************************************************************/

	/**
	 * Compute results over five fold and write them into a file
	 * We are curently using Log and Power version of Clustering. 
	 */

	public void computeResults(String path)
	{   
		myPath = path; 
		myTotalFolds = 1; 
		int pThr = 30;
		
		final int startK = 130;
		final int endK = 130;
		final int incK  =10;
		
		powerUsersThreshold = pThr;
		simVersion = 1;
		openFile();

			
		int sThr = 1 ;	
		simThreshold = sThr/(2* 10.0);	

		final int startItr = 4;
		final int endItr = 4;
		final int incItr  =1;
		
		final int startNeighbour = 30;
		final int endNeighbour = 30;
		final int incNeighbour  =10;

		//Keeping everthing fixed, I have to change this manulally and check how it evolves (starts from 1 to 10), keeping the 
		//other parameters fixed (to the optimal ones)
		// No of clusters, need to learn keeoing everything fixed
		
		//Make the objects and keep them fixed throughout the program
		//int version = KMeansOrKMeansPlus;
		for ( int version=24;version<= 24;version++) {	  
			
			System.out.println("Version is changed to ="+ version);
			
		for(int k = startK; k <= endK; k+=incK) {
			
			System.out.println("==========================================================================");
			System.out.println(" Clusters = "+ k);
			System.out.println("==========================================================================");
			kClusters  = k;
			
			// number of iterations, need to learn keeping everything fixed
			for(int noItr=startItr;noItr<=endItr;noItr+= incItr) {
				System.out.println("==========================================================================");
				System.out.println(" Iterations = "+ noItr);
				System.out.println("==========================================================================");
				nItrations = noItr;
				
				// number of fold (keep this only fold1 at the moment)
				for(int fold=1 ;fold<=myTotalFolds;fold++) { 	
				currentFold = fold;
				
				// Distance Measure  //1=PCCwithDefault, 2=PCCwithoutDefault
				//3=VSWithDefault,  4=VSWithoutDefault
				//5=PCC, 			  6=VS
				for(int sVersion=1 ;sVersion<=1;sVersion++) { 	
				simVersion = sVersion;
				if (simVersion == 1)
					distanceMeasure = "PCCwithDefault";
				if (simVersion == 2)
					distanceMeasure = "PCCwithoutDefault";
				if (simVersion == 3)
					distanceMeasure = "VSWithDefault";
				if (simVersion == 4)
					distanceMeasure = "VSWithoutDefault";
				if (simVersion == 5)
					distanceMeasure = "PCC";
				if (simVersion == 6)
					distanceMeasure = "VS";
				
				System.out.println("=============================================");
				System.out.println(" Distance Measure = "+ distanceMeasure);
				System.out.println("=============================================");
				
				
				if(sThr == 1)
					myFlg = 1;
				else 
					myFlg = 0;	
				
				//SML

				String  trainFile  = path  +  "sml_trainSetStoredFold" + (fold)+ ".dat";
				String  testFile	= path  +  "sml_testSetStoredFold" + (fold)+ ".dat";
				
				//FT
//				String  trainFile  = path  +  "ft_trainSetStoredBothFold1" + (fold)+ ".dat";
//				String  testFile	= path  +  "ft_testSetStoredBothFold1" + (fold)+ ".dat";
				
				String  mainFile	= trainFile;

				allHelper = new MemHelper(mainFile);
				trainMMh  = new MemHelper(trainFile);
				testMMh 	= new MemHelper(testFile);	  


	
					if(version==15)  
					{
						simpleKPlusAndProbPowerTree = new SimpleKMeansPlusAndProbPower(trainMMh);
						//simpleKPlusAndLogPowerTree_NoSimThr = new SimpleKMeansPlusAndLogPower(trainMMh);
					}

					if (version==1) {

							callMethod = new SimpleKMeans(trainMMh);

					}

					if (version==2) {
						callMethod =new SimpleKMeansPlus(trainMMh);
					}

					if (version==3) {
						callMethod =new SimpleKMeanModifiedPlus(trainMMh);
					}

					if (version==4) { 
						callMethod =new SimpleKMeansPlusAndPower(trainMMh);
					}

					if (version==5) {
						callMethod =new SimpleKMeansQuantile(trainMMh);
					}

					if (version==6) {	
						callMethod =new SimpleKMeansNormalDistribution(trainMMh);
					}

					if (version==7) {	
						callMethod =new SimpleKMeansVarianceCorrected(trainMMh);
					}

					if (version==8) {
						callMethod =new SimpleKMeansUniform(trainMMh);
					}


					
					if (version==9) {
						callMethod =new SimpleKMeansSamples(trainMMh);
					}

					if (version==10) {
						callMethod =new SimpleKMeansLog(trainMMh);
					}

					if (version==11) {
						callMethod =new SimpleKMeansHyperGeometric(trainMMh);
					}

					if (version==12) {
						callMethod =new SimpleKMeansPoisson(trainMMh);
					}

					if (version==22) {
						callMethod =new SimpleKMeansPlusPlus(trainMMh);
					}

					if (version==23) {
						callMethod =new SimpleKMeansOnePass(trainMMh);
					}  		

					//User based Filter setting
					if (version==16)
					myUserBasedFilter = new FilterAndWeight(trainMMh,1); 		       //with mmh object

					//ibcf
					if (version==17)
					myItemRec = new ItemItemRecommender(true, 5);
					
					if (version==24) {
						callMethod =new SimpleKMeansDensity(trainMMh);
					}

//		
//					if (version==30) {
//						callMethod =new SimpleKMeansNormalDistributionMovie(trainMMh);
//					} 
					if (version==13) {
						callMethod =new SimpleKMeansNormalDistributionUser(trainMMh);
					}
//					if (version==30) {
//						callMethod =new SimpleKMeansNormalDistributionUserAndMovie(trainMMh);
//					}
					if (version==14) {
						callMethod =new SimpleKMeansVarianceCorrectedAnotherVersion(trainMMh);
					}
					if (version==20) {
						callMethod =new SimpleKMeansVarianceCorrected_AvgPairWise(trainMMh);
					}
					if (version==19) {
						simpleKPlusAndLogPowerTree = new SimpleKMeansPlusAndLogPower(trainMMh);
					}
					if (version==21) {
						callMethod = new SimpleKMeansUniformAnotherVersion(trainMMh);
					}
					else 
					{
						simpleKPlusAndProbPowerTree = new SimpleKMeansPlusAndProbPower(trainMMh);
						//simpleKPlusAndLogPowerTree_NoSimThr = new SimpleKMeansPlusAndLogPower(trainMMh);
					}
	
				System.out.println("done reading objects");				   	 
				System.out.println("=====================");
				System.out.println(" Fold="+ fold);	 	
				System.out.println("=====================");


				//Build clusters
//				timer.start();
				callKTree (version, allHelper, myFlg , noItr);//it is converging after 6-7 iterations	
//				timer.stop();
//				kMeanTime1 = timer.getTime();
//				if (version<15||version>19)
//				{
//					
//				System.out.println("Time taken for version = "+ callMethod.getName() +"-->"+kMeanTime1);
//				
//				}
//				timer.resetTimer();
			//	int neighbours =50;
				//for (int neighbours = numberOfneighbours; neighbours >=numberOfneighbours; neighbours -= 20)
				for (int neighbours = startNeighbour; neighbours <=endNeighbour; neighbours += incNeighbour)
				{
					testWithMemHelper(testMMh,neighbours);	
					writeResults(neighbours, version) ;
				}
			  }//end of number of distance measure
			} //end of folds
		} // end no if iterations
	  } //end clusters
	 
	} //end version
	
		closeFile();
	}


	/***************************************************************************************************/  
	/**
	 * We wanna build SVM Regression modelm, It will make predictions and will store them in the files (.dat)
	 * We can read those files and can get predictions.   
	 *
	 */

	// But It is five fold data, means if we have separate test and train files then we have to build model SVM REGRESSION for each 
	// train set .........? think

	// NICE thing is build a sperate class or may be one class for all the classifiers (e.g. KNN, naive bayes) embeede in it,....and 
	// tey take a input test, train object, active user, target movie.. and return the prediction for it.


	public void buildSVMRegModel()
	{
		for(int fold=1;fold<=myTotalFolds;fold++)
		{

			//SML

			String  trainFile = myPath +  "sml_trainSetStoredFold" + (fold)+ ".dat";
			String  testFile	= myPath +  "sml_testSetStoredFold" + (fold)+ ".dat";
			
			//FT
//
//			String  trainFile = myPath +  "ft_myRatings" + (fold)+ ".dat";
//			String  testFile	= myPath +  "ft_myRatings" + (fold)+ ".dat";

			String  mainFile	= trainFile;

			allHelper = new MemHelper(mainFile);
			trainMMh  = new MemHelper(trainFile);
			testMMh 	= new MemHelper(testFile);	  		


		}//end for	  
	}//end method



	/***************************************************************************************************/

	//__________________________________


	public void testWithMemHelper(MemHelper testmh, int neighbours)     
	{

		RMSECalculator rmse = new  RMSECalculator();

		IntArrayList users;
		LongArrayList movies;


		double mov, pred,actual, uAvg;
		int uid, mid, total				= 0;    		       	
		int totalUsers					= 0;
		int totalExtremeErrors 			= 0 ;
		int totalEquals 					= 0;
		int totalErrorLessThanPoint5		= 0;
		int totalErrorLessThan1 			= 0;    	
		IntArrayList userThereInScenario	= new IntArrayList();
		int activeClusterID				= 0;
		
		System.out.println("Number of Neighbours");				   	 
		System.out.println("=====================");
		System.out.println("      "+ neighbours);	 	
		System.out.println("=====================");


		// For each user, make recommendations
		users = testmh.getListOfUsers();
		totalUsers= users.size();

		//to check
		System.out.println("no. of users ::" + totalUsers);
		double uidToPredictions[][] = new double[totalUsers][101]; // 1-49=predictions; 50-99=actual; (Same order); 100=user average

		//________________________________________

		for (int i = 0; i < totalUsers; i++)  {

			uid = users.getQuick(i);  
			userThereInScenario.add(uid);
			movies = testmh.getMoviesSeenByUser(uid);

			for (int j = 0; j < movies.size(); j++)             
			{	      	
				total++;

				mid = MemHelper.parseUserOrMovie(movies.getQuick(j));

				timer.start();
				double predictedRating = recommend(uid, mid, neighbours);
				double actualRating=0.0;
				actualRating = testmh.getRating(uid, mid);			 		// get actual ratings?


				// Should not print this, else there is error
				if (actualRating==-99 )                           
					System.out.println(" rating error, uid, mid, rating" + uid + "," + mid + ","+ actualRating);

//				if(predictedRating>5 || predictedRating<-1)
//					totalExtremeErrors++;
//
//				else if(Math.abs(predictedRating-actualRating)<=0.5)
//					totalErrorLessThanPoint5++;
//
//				else if(Math.abs(predictedRating-actualRating)<=1.0)
//					totalErrorLessThan1++;
//
//				else if (predictedRating==actualRating)
//					totalEquals++;


				//-------------
				// Add ROC
				//-------------
				if(predictedRating!=0)
					rmse.ROC4(actualRating, predictedRating, myClasses, trainMMh.getAverageRatingForUser(uid));		

				//-------------
				//Add Error
				//-------------

				if(predictedRating!=0) {
					rmse.add(actualRating,predictedRating);                            	
					midToPredictions.put(mid, predictedRating);                            	                                
				}		


				//-------------
				//Add Coverage
				//-------------
				rmse.addCoverage(predictedRating);                                 

			} //end of movies for

			//--------------------------------------------------------
			//A user has ended, now, add ROC and reset
			rmse.addROCForOneUser();
			rmse.resetROCForEachUser();
			rmse.addMAEOfEachUserInFinalMAE();
			rmse.resetMAEForEachUser();

			//sort the pairs (ascending order)
			IntArrayList keys = midToPredictions.keys();
			DoubleArrayList vals = midToPredictions.values();        		
			midToPredictions.pairsSortedByValue(keys, vals);

			int movSize = midToPredictions.size();
			if(movSize>50)
				movSize = 50;      	

			for(int x=0;x<movSize;x++)
			{
				mov = keys.getQuick(x);
				pred = vals.getQuick(x);
				actual = testmh.getRating(uid,(int) mov);	
				uidToPredictions[i][x] = pred;
				uidToPredictions[i][50+x] = actual;
			}//end for

			uidToPredictions[i][100] = trainMMh.getAverageRatingForUser(uid);
			midToPredictions.clear();

		} //end of checking if it is gray sheep cluster etc
   

		MAE		 	= rmse.mae(); 
		SDInMAE		= rmse.getMeanErrorOfMAE();
		SDInROC 	= rmse.getMeanSDErrorOfROC();
		Roc 		= rmse.getSensitivity();
		MAEPerUser 	= rmse.maeFinal();
		RMSE 		= rmse.rmse();
		RMSEPerUser	= rmse.rmseFinal();
		coverage	= rmse.getItemCoverage();

		kMeanEigen_Nmae 	= rmse.nmae_Eigen(1,5);
		kMeanCluster_Nmae 	= rmse.nmae_ClusterKNNFinal(1, 5);



		//-------------------------------------------------
		//Calculate top-N    		            

//		totalUsersInThisScenario = userThereInScenario.size();

		for(int i=0;i<8;i++)	//N from 5 to 30
		{
			for(int j=0;j<totalUsers;j++)//All users
			{
				int tempUid = users.getQuick(j);

				//check if this user was there in this scenario
				if(userThereInScenario.contains(tempUid))
				{
					//get user avg
					uAvg =  uidToPredictions [j][100];	

					for(int k=0;k<((i+1)*5);k++)	//for topN predictions
					{
						//get prediction and actual vals
						pred =  uidToPredictions [j][k];
						actual =  uidToPredictions [j][50+k];

						//add to topN
						rmse.addTopN(actual, pred, myClasses, uAvg);
						// rmse.addTopN(actual, pred, myClasses, TopNThreshold);
					}

					//after each user, first add TopN, and then reset
					rmse.AddTopNPrecisionRecallAndF1ForOneUser();
					rmse.resetTopNForOneUser();   		            		
				}
			} //end for

			//Now we finish finding Top-N for a particular value of N
			//Store it 
			precision[i]=rmse.getTopNPrecision();
			recall[i]=rmse.getTopNRecall();
			F1[i]=rmse.getTopNF1(); 

			//Get variance 
			SDInTopN_Precision[i] = rmse.getMeanSDErrorOfTopN(1); 
			SDInTopN_Recall[i] = rmse.getMeanSDErrorOfTopN(2);
			SDInTopN_F1 [i]= rmse.getMeanSDErrorOfTopN(2);

			//Reset all topN values    		            	
			rmse.resetTopNForOneUser();
			rmse.resetFinalTopN();
			

		} //end of for   		        	

		System.out.println("time:" + timer.getTime());
		timer.resetTimer();
		System.out.println("totalExtremeErrors="+totalExtremeErrors + ", Total ="+total);
		System.out.println("totalErrorLessThanPoint5="+totalErrorLessThanPoint5 );	       
		System.out.println("totalErrorLessThan1="+totalErrorLessThan1 );
		System.out.println("totalEquals="+totalEquals );    	

		// to check 
		System.out.println("MAE      = " + MAE );
		System.out.println("ROC      = " + Roc  );
		System.out.println("precision     = " + precision[3]  );
		System.out.println("recall      = " + recall[3]  );
		System.out.println("F1      = " + F1[3]  );
//		System.out.println("Time      = " + showTime[KMeansOrKMeansPlus]  );
		System.out.println("SDInMAE  = " + SDInMAE);
		System.out.println("SDInROC  = " +  SDInROC );
		System.out.println("coverage = " + coverage);
		System.out.println("Cluster Building Time = " + kMeanClusterTime );

		//Reset final values
		rmse.resetValues();   
		rmse.resetFinalROC();
		rmse.resetFinalMAE();
	
	}//end of function


	//----------------------------

	public void openFile()    
	{

		try {

			writeData = new BufferedWriter(new FileWriter(myPath + "Density.csv", true));   
			System.out.println("Result File Created at  ::: "+ myPath + "Density");
			
			
			writeData.append("\n");
			writeData.append("Variant");
			writeData.append(",");
			writeData.append("Distance Measure");
			writeData.append(",");
			writeData.append("Number of Clusters");
			writeData.append(",");
			writeData.append("Number of Neibours");
			writeData.append(",");
			writeData.append("Number of Iterations");
			writeData.append(",");
			writeData.append("Coverage");
			writeData.append(",");
			writeData.append("MAE");
			writeData.append(",");
			writeData.append("ROC");
			writeData.append(",");
			writeData.append("Precision");
			writeData.append(",");
			writeData.append("Recall");
			writeData.append(",");
			writeData.append("F1");
			writeData.append(",");
			writeData.append("Time");
			writeData.append(",");
			writeData.append("SD in MAE");
			writeData.append(",");
			writeData.append("SD in ROC");
			writeData.append(",");
			writeData.append("\n");


		}

		catch (Exception E)
		{
			System.out.println("error opening the file pointer of Result file");
			System.exit(1);
		}


	}

	//__________________________________


	public void writeResults(int neighbours, int name)    
	{
		
		try {
			if (name<15||name>19&& name!=26)
			variant =  callMethod.getName();
			else if (name == 15)
				variant = "KMeansPlusAndProbPower";
			else if (name == 19)
				variant = "KMeansPlusAndLogPower";
			else if (name == 16)
				variant = "UserBase";	
			else if (name == 17)
				variant = "ItemBase";
			else if (name == 18)
				variant = "MovieAverageRating";


//			writeData.append("\n");
//			writeData.append("Variant");
//			writeData.append(",");
//			writeData.append("Number of Clusters");
//			writeData.append(",");
//			writeData.append("Number of Neibours");
//			writeData.append(",");
//			writeData.append("Number of Iterations");
//			writeData.append(",");
//			writeData.append("Coverage");
//			writeData.append(",");
//			writeData.append("MAE");
//			writeData.append(",");
//			writeData.append("ROC");
//			writeData.append(",");
//			writeData.append("Precision");
//			writeData.append(",");
//			writeData.append("Recall");
//			writeData.append(",");
//			writeData.append("F1");
//			writeData.append(",");
//			writeData.append("Time");
//			writeData.append(",");
//			writeData.append("\n");

			writeData.append(""+ variant);
			writeData.append(",");
			writeData.append(""+ distanceMeasure);
			writeData.append(",");
			writeData.append(""+ kClusters);
			writeData.append(",");
			writeData.append(""+neighbours);
			writeData.append(",");
			writeData.append(""+nItrations);
			writeData.append(",");
			writeData.append(""+coverage);
			writeData.append(",");
			writeData.append(""+MAE);
			writeData.append(",");
			writeData.append(""+Roc);
			writeData.append(",");	
			writeData.append(""+precision[3]);
			writeData.append(",");
			writeData.append("" +recall[3]);
			writeData.append(",");
			writeData.append("" +F1[3]);
			writeData.append(",");
			writeData.append("" +kMeanClusterTime);
			writeData.append(",");
			writeData.append(""+SDInMAE);
			writeData.append(",");
			writeData.append(""+SDInROC);
			writeData.append(",");
			writeData.append("\n");


		}

		catch (Exception E)
		{
			System.out.println("error opening the file pointer of result");
			System.exit(1);
		}

		
	}

	public void closeFile()    
	{

		try {

			writeData.close();
			System.out.println("Files closed");
		}

		catch (Exception E)
		{
			System.out.println("error closing the roc file pointer");
		}


	}

}//end class

