package algorithms.memorybased.KMeansInitialization;

import java.io.BufferedWriter;


import java.io.FileWriter;
import java.util.*;

import netflix.memreader.*;


import netflix.algorithms.memorybased.memreader.FilterAndWeight;

import netflix.recommender.ItemItemRecommender;
import rmse.RMSECalculator;

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
	private SimpleKMeansPlusAndLogPower		simpleKPlusAndLogPowerTree;
	private SimpleKMeansPlusAndLogPower		simpleKPlusAndLogPowerTree_NoSimThr;

	private int								myClasses;
	private int								myTotalFolds;
	private String 							variant;

	 //Objects of some classes
	static MemHelper 			trainMMh;
	MemHelper 			allHelper;
	MemHelper 			testMMh;
	MeanOrSD			MEANORSD;
	Timer227 			timer;
	FilterAndWeight		myUserBasedFilter;   //Filter and Weight, for User-based CF
	ItemItemRecommender	myItemRec;		     //item-based CF    


	private long 		kMeanTime;
	private int         kClusters;

	BufferedWriter      writeData;

	private String      myPath;
	private int         totalNan=0;
	private int         totalNegatives=0;
	private int			KMeansOrKMeansPlus; 
	private int			simVersion;
	ArrayList<Centroid> centroids;

	// version of the seed selection to be called

	public KMeansVariant clusteringSeedSelection = null;
	
	//Related to finding the gray sheep user's predictions

	private int			powerUsersThreshold;					// power user size
	private double		simThreshold;							//
	private int			numberOfneighbours;						// no. of neighbouring cluster for an active user

	//Regarding Results
	double 								MAE;
	double								MAEPerUser;
	double 								RMSE;
	double 								RMSEPerUser;
	double								Roc;
	double								coverage;
	double								pValue;
	double								kMeanEigen_Nmae;
	double								kMeanCluster_Nmae;

	//SD in one fold or when we do hold-out like 20-80
	double								SDInMAE;
	double								SDInROC;
	double 								SDInTopN_Precision[];
	double 								SDInTopN_Recall[];
	double 								SDInTopN_F1[];	

	double            					precision[];		//evaluations   
	double              				recall[];   
	double              				F1[];    
	private OpenIntDoubleHashMap 		midToPredictions;	//will be used for top_n metrics (pred*100, actual)

	//1: fold, 2: k, 3:dim
	double              array_MAE[][];	      			// [gsu][fold]
	double              array_MAEPerUser[][];
	double              array_NMAE[][];
	double              array_NMAEPerUser[][];
	double              array_RMSE[][];
	double              array_RMSEPerUser[][];
	double              array_Coverage[][];
	double              array_ROC[][];
	double              array_BuildTime[][];


	double              array_Precision[][][]; 		   //[topnN][gsu][fold]
	double              array_Recall[][][];
	double              array_F1[][][];    

	//will store the grid results in the form of mean and sd
	double				gridResults_Mean_MAE[];
	double				gridResults_Mean_MAEPerUser[];
	double				gridResults_Mean_NMAE[];
	double				gridResults_Mean_NMAEPerUser[];
	double				gridResults_Mean_RMSE[];
	double				gridResults_Mean_RMSEPerUser[];
	double				gridResults_Mean_ROC[];		

	double				gridResults_Mean_Precision[][];   	//[TOPn][][]
	double				gridResults_Mean_Recall[][];
	double				gridResults_Mean_F1[][];
	double				gridResults_Mean_Coverage[];

	double				gridResults_Sd_MAE[];
	double				gridResults_Sd_MAEPerUser[];
	double				gridResults_Sd_NMAE[];
	double				gridResults_Sd_NMAEPerUser[];
	double				gridResults_Sd_RMSE[];
	double				gridResults_Sd_RMSEPerUser[];
	double				gridResults_Sd_ROC[];			

	double				gridResults_Sd_Precision[][];
	double				gridResults_Sd_Recall[][];
	double				gridResults_Sd_F1[][];
	double				gridResults_Sd_Coverage[];

	double              mean_MAE[];	      					// Means of results, got from diff folds
	double              mean_MAEPerUser[];
	double              mean_NMAE[];						// for each version
	double              mean_NMAEPerUser[];
	double              mean_RMSE[];
	double              mean_RMSEPerUser[];
	double              mean_Coverage[];
	double              mean_ROC[];
	double              mean_BuildTime[];
	double              mean_Precision[][];   
	double              mean_Recall[][];   
	double              mean_F1[][];       

	double              sd_MAE[];		      					// SD of results, got from diff folds
	double              sd_MAEPerUser[];
	double              sd_NMAE[];								// for each version
	double              sd_NMAEPerUser[];
	double              sd_RMSE[];
	double              sd_RMSEPerUser[];
	double              sd_Coverage[];
	double              sd_ROC[];
	double              sd_BuildTime[];
	double              sd_Precision[][];   
	double              sd_Recall[][];   
	double              sd_F1[][];   


	int 				myFlg;
	int 				currentFold;
	IntArrayList		myCentroids1, myCentroids2,myCentroids3,myCentroids4,myCentroids5;


	/************************************************************************************************/

	public KMeanRecommender()    
	{

//		howMuchClusterSize = 0;
		kMeanTime			= 0;    

		myClasses			= 5;
		simVersion			= 1;  //1=PCCwithDefault, 2=PCCwithoutDefault
		//3=VSWithDefault,  4=VSWithDefault
		//5=PCC, 			  6=VS

		KMeansOrKMeansPlus  = 0;
		timer 				 = new Timer227();
		MEANORSD			 = new MeanOrSD();



		//-------------------------------------------------------
		//Answers
		
		numberOfneighbours  = 0;

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

		//Initialize results, Mean and SD	    	
		array_MAE  	 	=   new double[3][5];
		array_MAEPerUser	=   new double[3][5]; 
		array_NMAE		 	=   new double[3][5];
		array_NMAEPerUser	=   new double[3][5];
		array_RMSE 	 	=   new double[3][5];
		array_RMSEPerUser 	=   new double[3][5];
		array_Coverage  	=   new double[3][5];
		array_ROC 		 	=   new double[3][5];
		array_BuildTime 	=   new double[3][5];

		array_Precision 	= new double[8][3][5]; //[topN][fold]
		array_Recall 	 	= new double[8][3][5];
		array_F1 		 	= new double[8][3][5];

		//So we have to print this grid result for each scheme,
		//Print in the form of "mean + sd &" 
		gridResults_Mean_MAE 			=   new double[3];	        
		gridResults_Mean_NMAE			=   new double[3];        
		gridResults_Mean_RMSE			=   new double[3];
		gridResults_Mean_MAEPerUser	=   new double[3];
		gridResults_Mean_RMSEPerUser	=   new double[3];
		gridResults_Mean_NMAEPerUser	=   new double[3];
		gridResults_Mean_ROC			=   new double[3];
		gridResults_Mean_Coverage		=   new double[3];

		gridResults_Mean_Precision		= new double[8][3]; 
		gridResults_Mean_Recall		= new double[8][3];
		gridResults_Mean_F1			= new double[8][3];       

		gridResults_Sd_MAE			= new double[3];	         
		gridResults_Sd_NMAE		= new double[3];	         
		gridResults_Sd_RMSE		= new double[3];
		gridResults_Sd_NMAEPerUser	= new double[3];
		gridResults_Sd_MAEPerUser	= new double[3];
		gridResults_Sd_RMSEPerUser = new double[3];	         
		gridResults_Sd_ROC			= new double[3];
		gridResults_Sd_Coverage	= new double[3];

		gridResults_Sd_Precision	= new double[8][3];
		gridResults_Sd_Recall		= new double[8][3];
		gridResults_Sd_F1			= new double[8][3];

		// mean and sd, may be not required
		mean_MAE 		= new double[3];	        
		mean_NMAE 		= new double[3];	        
		mean_RMSE 		= new double[3];

		mean_NMAEPerUser= new double[3];
		mean_RMSEPerUser= new double[3];
		mean_MAEPerUser = new double[3];

		mean_Coverage 	= new double[3];
		mean_ROC 		= new double[3];
		mean_BuildTime  = new double[5];
		mean_Precision	= new double[8][3];
		mean_Recall		= new double[8][3];
		mean_F1			= new double[8][3];	        

		sd_MAE 			= new double[3];	        
		sd_NMAE 		= new double[3];
		sd_RMSE 		= new double[3];

		sd_MAEPerUser	= new double[3];
		sd_NMAEPerUser 	= new double[3];
		sd_RMSEPerUser	= new double[3];


		sd_Coverage 	= new double[3];
		sd_ROC 			= new double[3];
		sd_BuildTime 	= new double[5];
		sd_Precision 	= new double[8][3];
		sd_Recall 		= new double[8][3];
		sd_F1		 	= new double[8][3];       

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
		
		timer.start();
		
		getObject(methodVariant, helper);
		
		timer.stop();
		timer.resetTimer();
		
				//-----------------------
				// K-Means Plus and 
				// Log Power
				//-----------------------    	


				 if(KMeansOrKMeansPlus==5)
				{
					myFlg =1;

					IntArrayList allCentroids     = new IntArrayList();		    // All distinct chosen centroids  
					timer.start(); 

					simpleKPlusAndLogPowerTree.cluster(kClusters, callNo, MAX_ITERATIONS, simVersion, simThreshold, powerUsersThreshold,1, allCentroids);
					//	simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, callNo, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,1, allCentroids);
					timer.stop(); 
					kMeanTime = timer.getTime();
					System.out.println("KMeans Plus and Log Power Tree took " + kMeanTime + " s to build");    	
					System.out.println("");    	
					timer.resetTimer();
					/*
					if(currentFold==1)
						myCentroids1   = 	simpleKPlusAndLogPowerTree.get_previous_Centroids();
					else if(currentFold==2)
						myCentroids2   = 	simpleKPlusAndLogPowerTree.get_previous_Centroids();
					else if(currentFold==3)
						myCentroids3   = 	simpleKPlusAndLogPowerTree.get_previous_Centroids();
					else if(currentFold==4)
						myCentroids4   = 	simpleKPlusAndLogPowerTree.get_previous_Centroids();
					else if(currentFold==5)
						myCentroids5   = 	simpleKPlusAndLogPowerTree.get_previous_Centroids();
					 */

					timer.start(); 
					if(currentFold==1)
						simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, 1, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,0,  myCentroids1);
					else if(currentFold==2)
						simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, 1, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,0,  myCentroids2);
					else if(currentFold==3)
						simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, 1, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,0,  myCentroids3);
					else if(currentFold==4)
						simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, 1, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,0,  myCentroids4);
					else if(currentFold==5)
						simpleKPlusAndLogPowerTree_NoSimThr.cluster(kClusters, 1, MAX_ITERATIONS, simVersion, -10, powerUsersThreshold,0,  myCentroids5);

					timer.stop();

					kMeanTime = timer.getTime();
					System.out.println("KMeans Plus and Log Power Tree took " + kMeanTime + " s to build"); 
					System.out.println(""); 

					timer.resetTimer();
				}
				
		//-----------------------
		// K-Means all other variant
		//-----------------------
				else 
					
				{
					timer.start();	              

					callMethod.cluster(KMeansOrKMeansPlus,kClusters, callNo, MAX_ITERATIONS, simVersion);       
					timer.stop();

					kMeanTime = timer.getTime();
					System.out.println( clusteringSeedSelection.getName (KMeansOrKMeansPlus) + " -->  took " + timer.getTime() + " s to build");    	
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
	public double recommend(int activeUser, int targetMovie, int neighbours)    
	{

		double weightSum = 0, voteSum = 0;
		
		//========================
		// recommendation 
		//=========================
		
				//------------------------
				//  neighbours priors
				//------------------------
				if (KMeansOrKMeansPlus == 5)        	
				{
//					simpleKUsers = simpleKPlusAndLogPowerTree_NoSimThr.getClusterByUID(activeUser);           		
					int activeClusterID = simpleKPlusAndLogPowerTree_NoSimThr.getClusterIDByUID(activeUser);

					OpenIntDoubleHashMap simMap = new OpenIntDoubleHashMap();	//sim b/w an active user and the clusters

					// Find sim b/w a user and the cluster he lies in        		
					double simWithMainCluster = simpleKPlusAndLogPowerTree_NoSimThr.findSimWithOtherClusters(activeUser, activeClusterID );

					// Find sim b/w a user and all the other clusters
					for(int i=0;i<kClusters; i++)
					{
						if(i!=activeClusterID)
						{
							double activeUserSim  = simpleKPlusAndLogPowerTree_NoSimThr.findSimWithOtherClusters(activeUser, i );

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
						double clusterRating  = simpleKPlusAndLogPowerTree_NoSimThr.getRatingForAMovieInACluster(clusterId, targetMovie);
						double clusterAverage = simpleKPlusAndLogPowerTree_NoSimThr.getAverageForAMovieInACluster(clusterId, targetMovie);

						if(clusterRating!=0)
						{
							//Prediction
							weightSum += Math.abs(clusterWeight);      		
							voteSum+= (clusterWeight*(clusterRating-clusterAverage));
							if(total++ == neighbours) break;
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


				else if (KMeansOrKMeansPlus==6)
				{	   
					//first go in these programs and return "0" or at-least the averages if they fail to predict
					double rat = myUserBasedFilter.recommendS(activeUser, targetMovie, 30, 1);
					return rat;

				}	   

				else if (KMeansOrKMeansPlus==7)
				{	   
					double rat = myItemRec.recommend(trainMMh, activeUser, targetMovie, 15, 4);	   
					return rat;

				}


				else if (KMeansOrKMeansPlus==8)
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
					simMap.put(activeClusterID,simWithMainCluster );

					//sort the pairs (ascending order)
					IntArrayList keys = simMap.keys();
					DoubleArrayList vals = simMap.values();        		
					simMap.pairsSortedByValue(keys, vals);        		
					int simSize = simMap.size();
					LongArrayList tempUsers = trainMMh.getUsersWhoSawMovie(targetMovie);
					LongArrayList allUsers  = new LongArrayList();

					for(int i=0;i<tempUsers.size();i++)
					{
						allUsers.add(MemHelper.parseUserOrMovie(tempUsers.getQuick(i)));
						
					}       		
					//-----------------------------------
					// Find sim * priors
					//-----------------------------------
					// How much similar clusters to take into account? 
					// Let us take upto a certain sim into account, e.g. (>0.10) sim

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
							if(total++ == neighbours) break;
						}
					}
					if (weightSum!=0)
						voteSum *= 1.0 / weightSum;    


					if (weightSum==0)				// If no weight, then it is not able to recommend????
					{ 
//						  
						totalNan++;
						return 0;	       
					}

					double answer = trainMMh.getAverageRatingForUser(activeUser) + voteSum;             
				

					//------------------------
					// Send answer back
					//------------------------          

					if(answer<=0)
					{
						totalNegatives++;
						
						return 0;
					}

					else {
//						totalRecSamples++;   
						return answer;
					}

					
				} 	
				
//				return 0;

			}



	/************************************************************************************************/

	public static void main(String[] args)    
	{

		String path ="";
		int    fold = 1;

		path = "C:/Users/Sobia/tempRecommender/GitHubRecommender/netflix/netflix/DataSets/SML_ML/FiveFoldData/";

		
		//create class object
		
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

		//optimal clusters, (1) sml =150, (2) ft1= 100, ft5 = 140

		//		myTotalFolds = 5; 
		myTotalFolds = 1; 
		int START    = 0; 			 // 0=gsu, 1=remaining users, 3=all users
		int pThr = 30;		
		int k=90;
		powerUsersThreshold = pThr;

		kClusters  = k;
		simVersion = 1;
		openFile();

		System.out.println("==========================================================================");
		System.out.println(" Clusters = "+ k);
		System.out.println("==========================================================================");



		KMeansOrKMeansPlus = 12;	
		int sThr = 1 ;	
		simThreshold = sThr/(2* 10.0);	
		int noNeigh = 70;
		numberOfneighbours = noNeigh;

		//Kepping everthing fixed, I have to change this manulally and check how it evolves (starts from 1 to 10), keeping the 
		//other parameters fixed (to the optimal ones)
		for(int noItr=2;noItr<=2;noItr++)
		{


			for(int fold=1 ;fold<=myTotalFolds;fold++)
			{ 	
				currentFold = fold;
				if(sThr == 1)
					myFlg = 1;
				else 
					myFlg = 0;	

				String  trainFile  = path  +  "sml_trainSetStoredFold" + (fold)+ ".dat";
				String  testFile	= path  +  "sml_testSetStoredFold" + (fold)+ ".dat";
				String  mainFile	= trainFile;

				allHelper = new MemHelper(mainFile);
				trainMMh  = new MemHelper(trainFile);
				testMMh 	= new MemHelper(testFile);	  

				//User based Filter setting
				myUserBasedFilter = new FilterAndWeight(trainMMh,1); 		       //with mmh object

				//ibcf
				myItemRec = new ItemItemRecommender(true, 5);

				//	long t1= System.currentTimeMillis();

				//Make the objects and keep them fixed throughout the program

				for (int v=KMeansOrKMeansPlus;v<=20;v++)
				{	  
					if(v==5)  
					{
						simpleKPlusAndLogPowerTree = new SimpleKMeansPlusAndLogPower(trainMMh);
						simpleKPlusAndLogPowerTree_NoSimThr = new SimpleKMeansPlusAndLogPower(trainMMh);
					}

					if (v==1) {

							callMethod = new SimpleKMeans(trainMMh);

					}

					if (v==2) {
						callMethod =new SimpleKMeansPlus(trainMMh);
					}

					if (v==3) {
						callMethod =new SimpleKMeanModifiedPlus(trainMMh);
					}

					if (v==4) { 
						callMethod =new SimpleKMeansPlusAndPower(trainMMh);
					}

					if (v==9) {
						callMethod =new SimpleKMeansQuantile(trainMMh);
					}

					if (v==10) {	
						callMethod =new SimpleKMeansNormalDistribution(trainMMh);
					}

					if (v==11) {	
						callMethod =new SimpleKMeansVariance(trainMMh);
					}

					if (v==12) {
						callMethod =new SimpleKMeansUniform(trainMMh);
					}

					if (v==13) {
						callMethod =new SimpleKMeansDensity(trainMMh);
					}

					if (v==14) {
						callMethod =new SimpleKMeansSamples(trainMMh);
					}

					if (v==15) {
						callMethod =new SimpleKMeansLog(trainMMh);
					}

					if (v==16) {
						callMethod =new SimpleKMeansHyperGeometric(trainMMh);
					}

					if (v==17) {
						callMethod =new SimpleKMeansPoisson(trainMMh);
					}

					if (v==18) {
						callMethod =new SimpleKMeansPlusPlus(trainMMh);
					}

					if (v==19) {
						callMethod =new SimpleKMeansSinglePass(trainMMh);
					}  

				
						

				}

	
				System.out.println("done reading objects");				   	 
				System.out.println("=====================");
				System.out.println(" Fold="+ fold);	 	
				System.out.println("=====================");


				//Build clusters
		
				callKTree (KMeansOrKMeansPlus, allHelper, myFlg , noItr);//it is converging after 6-7 iterations	

				for (int neighbours = numberOfneighbours; neighbours >=10; neighbours -= 20)
				{
					testWithMemHelper(testMMh,neighbours);	
					writeResults(neighbours, KMeansOrKMeansPlus) ;
				}
			}//end of number of iterations
		} //end fold					 


		timer.resetTimer(); 

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

		for (int i = 0; i < totalUsers; i++)        
		{

			uid = users.getQuick(i);  
			userThereInScenario.add(uid);
			movies = testmh.getMoviesSeenByUser(uid);

			for (int j = 0; j < movies.size(); j++)             
			{	      	
				total++;

				mid = MemHelper.parseUserOrMovie(movies.getQuick(j));

				timer.start();
				double rrr = recommend(uid, mid, neighbours);
	
				timer.stop();


				double myRating=0.0;

				myRating = testmh.getRating(uid, mid);			 		// get actual ratings?


				if (myRating==-99 )                           
					System.out.println(" rating error, uid, mid, rating" + uid + "," + mid + ","+ myRating);

				if(rrr>5 || rrr<-1)
					totalExtremeErrors++;

				else if(Math.abs(rrr-myRating)<=0.5)
					totalErrorLessThanPoint5++;


				else if(Math.abs(rrr-myRating)<=1.0)
					totalErrorLessThan1++;

				else if (rrr==myRating)
					totalEquals++;


				//-------------
				// Add ROC
				//-------------
				if(rrr!=0)
					rmse.ROC4(myRating, rrr, myClasses, trainMMh.getAverageRatingForUser(uid));		
				//rmse.ROC4(myRating, rrr, myClasses, TopNThreshold);

				//-------------
				//Add Error
				//-------------

				if(rrr!=0)
				{
					rmse.add(myRating,rrr);                            	
					midToPredictions.put(mid, rrr);                            	                                
				}		


				//-------------
				//Add Coverage
				//-------------

				rmse.addCoverage(rrr);                                 

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
		System.out.println("SDInMAE  = " + SDInMAE);
		System.out.println("SDInROC  = " +  SDInROC );
		System.out.println("coverage = " + coverage);


		//Reset final values
		rmse.resetValues();   
		rmse.resetFinalROC();
		rmse.resetFinalMAE();
		/*if(ImputationMethod >2)
        	rmse.resetPairTPrediction();*/

	}//end of function


	//----------------------------

	public void openFile()    
	{

		try {

			writeData = new BufferedWriter(new FileWriter(myPath + "Results.csv", true));   
			System.out.println("Result File Created at  ::: "+ myPath + "Results");
//			System.out.println("Rec File Created at"+ "new");

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
	
			variant = clusteringSeedSelection.getName(name);


			//***********

		///	System.out.println("Result File Created at  ::: "+ myPath + "Results");
			writeData.append("\n");
			writeData.append("Variant");
			writeData.append(",");
			writeData.append("Number of Neibours");
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
			writeData.append("\n");

			writeData.append(""+ variant);
			writeData.append(",");
			writeData.append(""+neighbours);
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

	public  KMeansVariant getObject(int variant, MemHelper helper)
	{

		if (variant==1) {
	
				clusteringSeedSelection = new SimpleKMeans(helper);
		}
	
		else if (variant==2) {
			 clusteringSeedSelection =new SimpleKMeansPlus(helper);
		}
	
		else if (variant==3) {
			 clusteringSeedSelection =new SimpleKMeanModifiedPlus(helper);
		}
	
		else if (variant==4) { 
			 clusteringSeedSelection =new SimpleKMeansPlusAndPower(helper);
		}
	
		else if (variant==9) {
			clusteringSeedSelection =new SimpleKMeansQuantile(helper);
		}
	
		else if (variant==10) {	
			clusteringSeedSelection =new SimpleKMeansNormalDistribution(helper);
		}
	
		else if (variant==11) {	
			 clusteringSeedSelection =new SimpleKMeansVariance(helper);
		}
	
		else if (variant==12) {
			 clusteringSeedSelection =new SimpleKMeansUniform(helper);
		}
	
		else if (variant==13) {
			 clusteringSeedSelection =new SimpleKMeansDensity(helper);
		}
	
		else if (variant==14) {
			 clusteringSeedSelection =new SimpleKMeansSamples(helper);
		}
	
		else if (variant==15) {
			 clusteringSeedSelection =new SimpleKMeansLog(helper);
		}
	
		else if (variant==16) {
			 clusteringSeedSelection =new SimpleKMeansHyperGeometric(helper);
		}
	
		else if (variant==17) {
			 clusteringSeedSelection =new SimpleKMeansPoisson(helper);
		}
	
		else if (variant==18) {
			clusteringSeedSelection =new SimpleKMeansPlusPlus(helper);
		}
	
		else if (variant==19) {
			clusteringSeedSelection =new SimpleKMeansSinglePass(helper);
		}
		return clusteringSeedSelection;  
	
	}


}//end class