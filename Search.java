/******************************************************************************
*  A Teaching GA					  Developed by Hal Stringer & Annie Wu, UCF
*  Version 2, January 18, 2004
*******************************************************************************/

import java.io.*;
import java.util.*;
import java.text.*;

public class Search {

/*******************************************************************************
*                           INSTANCE VARIABLES                                 *
*******************************************************************************/

/*******************************************************************************
*                           STATIC VARIABLES                                   *
*******************************************************************************/

	public static FitnessFunction problem;

	public static Chromo[] member;
	public static Chromo[] child;

	public static Chromo bestOfGenChromo;
	public static int bestOfGenR;
	public static int bestOfGenG;
	public static Chromo bestOfRunChromo;
	public static int bestOfRunR;
	public static int bestOfRunG;
	public static double bestOfRunF;
	public static Chromo bestOverAllChromo;
	public static int bestOverAllR;
	public static int bestOverAllG;

	public static double sumRawFitness;
	public static double sumRawFitness2;	// sum of squares of fitness
	public static double sumSclFitness;
	public static double sumProFitness;
	public static double defaultBest;
	public static double defaultWorst;

	public static double averageRawFitness;
	public static double stdevRawFitness;

	public static int G;
	public static int R;
	public static Random r = new Random();
	private static double randnum;

	private static int memberIndex[];
	private static double memberFitness[];
	private static int TmemberIndex;
	private static double TmemberFitness;

	private static double gensFitnessStats[][];  // 0=average fitness, 1=best fitness
	private static double runsFitnessStats[][]; // 0=best generation, 1=best fitness
	private static double fitnessLog[][][]; // run, gen, average0/best1
	public static Map<String,Location> locations = new HashMap<>();
	public static Map<Integer,Location> intlocations = new HashMap<>();
/*******************************************************************************
*                              CONSTRUCTORS                                    *
*******************************************************************************/


/*******************************************************************************
*                             MEMBER METHODS                                   *
*******************************************************************************/


/*******************************************************************************
*                             STATIC METHODS                                   *
*******************************************************************************/

	public static void main(String[] args) throws java.io.IOException{

		Calendar dateAndTime = Calendar.getInstance(); 
		Date startTime = dateAndTime.getTime();

	//  Read Parameter File
		// System.out.println("\nParameter File Name is: " + args[0] + "\n");
		Parameters parmValues = new Parameters(args[0]);

	//  Write Parameters To Summary Output File
		// String summaryFileName = Parameters.expID + "_summary.txt";
		// FileWriter summaryOutput = new FileWriter(summaryFileName);
		// parmValues.outputParameters(summaryOutput);

		FileWriter summaryGenStats = new FileWriter(Parameters.expID + "_genstats.csv");
		// parmValues.outputParameters(summaryOutput);

		try{
			File file = new File("opt.txt");

			Scanner input = new Scanner(file);

			int counter = 0;

			while(input.hasNextLine()){
				String line = input.nextLine();

				String[] parts = line.split(" ");

				if(parts[0].length() == 1){
					parts[0] = '0' + parts[0];
				}

				Location loc = new Location(Double.parseDouble(parts[1]),Double.parseDouble(parts[2]),parts[0]);
				locations.put(loc.id, loc);

				intlocations.put(Integer.parseInt(loc.id),loc);
				// int a = 65;
				// int b = a + 6;

				// if(counter > 25){
				// 	locations.put((char) (b+counter),loc);
					
					
				// }else{
				// 	locations.put((char) (a+counter),loc);
					
				// }

				
				// counter++;

				
			}

			input.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
			
		}

	//	Set up Fitness Statistics matrix
		gensFitnessStats = new double[2][Parameters.generations];
		for (int i=0; i<Parameters.generations; i++){
			gensFitnessStats[0][i] = 0;
			gensFitnessStats[1][i] = 0;
		}

		runsFitnessStats = new double[2][Parameters.numRuns+1];
		for (int i=0; i<=Parameters.numRuns; i++){
			runsFitnessStats[0][i] = 0;
			runsFitnessStats[1][i] = 0;
		}

		fitnessLog = new double[Parameters.numRuns+1][Parameters.generations][2];
		for (int i=1; i<=Parameters.numRuns; i++){
			for (int j=0; j<Parameters.generations; j++){
				fitnessLog[i][j][0] = 0;
				fitnessLog[i][j][0] = 0;
			}
		}

	//	Problem Specific Setup - For new new fitness function problems, create
	//	the appropriate class file (extending FitnessFunction.java) and add
	//	an else_if block below to instantiate the problem.
 
		if (Parameters.problemType.equals("NM")){
				problem = new NumberMatch();
		}
		else if (Parameters.problemType.equals("TS")){
				problem = new TravelingSalesman();
		}
		else System.out.println("Invalid Problem Type");

		// System.out.println(problem.name);

	//	Initialize RNG, array sizes and other objects
		r.setSeed(Parameters.seed);
		memberIndex = new int[Parameters.popSize];
		memberFitness = new double[Parameters.popSize];
		member = new Chromo[Parameters.popSize];
		child = new Chromo[Parameters.popSize];
		bestOfGenChromo = new Chromo();
		bestOfRunChromo = new Chromo();
		bestOverAllChromo = new Chromo();

		if (Parameters.minORmax.equals("max")){
			defaultBest = 0;
			defaultWorst = 999999999999999999999.0;
		}
		else{
			defaultBest = 999999999999999999999.0;
			defaultWorst = 0;
		}

		bestOverAllChromo.rawFitness = defaultBest;

		

		//  Start program for multiple runs
		for (R = 1; R <= Parameters.numRuns; R++){

			bestOfRunChromo.rawFitness = defaultBest;
			// System.out.println();

			//	Initialize First Generation
			for (int i=0; i<Parameters.popSize; i++){
				member[i] = new Chromo();
				child[i] = new Chromo();
			}

			//	Begin Each Run
			for (G=0; G<Parameters.generations; G++){

				sumProFitness = 0;
				sumSclFitness = 0;
				sumRawFitness = 0;
				sumRawFitness2 = 0;
				bestOfGenChromo.rawFitness = defaultBest;

				//	Test Fitness of Each Member
				for (int i=0; i<Parameters.popSize; i++){

					member[i].rawFitness = 0;
					member[i].sclFitness = 0;
					member[i].proFitness = 0;

					problem.doRawFitness(member[i]);

					sumRawFitness = sumRawFitness + member[i].rawFitness;
					sumRawFitness2 = sumRawFitness2 +
						member[i].rawFitness * member[i].rawFitness;

					if (Parameters.minORmax.equals("max")){
						if (member[i].rawFitness > bestOfGenChromo.rawFitness){
							Chromo.copyB2A(bestOfGenChromo, member[i]);
							bestOfGenR = R;
							bestOfGenG = G;
						}
						if (member[i].rawFitness > bestOfRunChromo.rawFitness){
							Chromo.copyB2A(bestOfRunChromo, member[i]);
							bestOfRunR = R;
							bestOfRunG = G;
							bestOfRunF = member[i].rawFitness;
						}
						if (member[i].rawFitness > bestOverAllChromo.rawFitness){
							Chromo.copyB2A(bestOverAllChromo, member[i]);
							bestOverAllR = R;
							bestOverAllG = G;
						}
					}
					else {
						if (member[i].rawFitness < bestOfGenChromo.rawFitness){
							Chromo.copyB2A(bestOfGenChromo, member[i]);
							bestOfGenR = R;
							bestOfGenG = G;
						}
						if (member[i].rawFitness < bestOfRunChromo.rawFitness){
							Chromo.copyB2A(bestOfRunChromo, member[i]);
							bestOfRunR = R;
							bestOfRunG = G;
							bestOfRunF = member[i].rawFitness;
						}
						if (member[i].rawFitness < bestOverAllChromo.rawFitness){
							Chromo.copyB2A(bestOverAllChromo, member[i]);
							bestOverAllR = R;
							bestOverAllG = G;
						}
					}
				}

				// Accumulate fitness statistics
				gensFitnessStats[0][G] += sumRawFitness / Parameters.popSize;
				gensFitnessStats[1][G] += bestOfGenChromo.rawFitness;

				fitnessLog[R][G][0] += sumRawFitness / Parameters.popSize;
				fitnessLog[R][G][1] += bestOfGenChromo.rawFitness;

				averageRawFitness = sumRawFitness / Parameters.popSize;
				stdevRawFitness = Math.sqrt(
							Math.abs(sumRawFitness2 - 
							sumRawFitness*sumRawFitness/Parameters.popSize)
							/
							(Parameters.popSize-1)
							);

				// Output generation statistics to screen
				// System.out.println(R + "\t" + G +  "\t" + (int)bestOfGenChromo.rawFitness + "\t" + averageRawFitness + "\t" + stdevRawFitness);

				// Output generation statistics to summary file
				// summaryOutput.write(" R ");
				// Hwrite.right(R, 3, summaryOutput);
				// summaryOutput.write(" G ");
				// Hwrite.right(G, 3, summaryOutput);
				// Hwrite.right((int)bestOfGenChromo.rawFitness, 7, summaryOutput);
				// Hwrite.right(averageRawFitness, 11, 3, summaryOutput);
				// Hwrite.right(stdevRawFitness, 11, 3, summaryOutput);
				// summaryOutput.write("\n");

				// run
				




		// *********************************************************************
		// **************** SCALE FITNESS OF EACH MEMBER AND SUM ***************
		// *********************************************************************

				switch(Parameters.scaleType){

				case 0:     // No change to raw fitness
					for (int i=0; i<Parameters.popSize; i++){
						member[i].sclFitness = member[i].rawFitness + .000001;
						sumSclFitness += member[i].sclFitness;
					}
					break;

				case 1:     // Fitness not scaled.  Only inverted.
					for (int i=0; i<Parameters.popSize; i++){
						member[i].sclFitness = 1/(member[i].rawFitness + .000001);
						sumSclFitness += member[i].sclFitness;
					}
					break;

				case 2:     // Fitness scaled by Rank (Maximizing fitness)

					//  Copy genetic data to temp array
					for (int i=0; i<Parameters.popSize; i++){
						memberIndex[i] = i;
						memberFitness[i] = member[i].rawFitness;
					}
					//  Bubble Sort the array by floating point number
					for (int i=Parameters.popSize-1; i>0; i--){
						for (int j=0; j<i; j++){
							if (memberFitness[j] > memberFitness[j+1]){
								TmemberIndex = memberIndex[j];
								TmemberFitness = memberFitness[j];
								memberIndex[j] = memberIndex[j+1];
								memberFitness[j] = memberFitness[j+1];
								memberIndex[j+1] = TmemberIndex;
								memberFitness[j+1] = TmemberFitness;
							}
						}
					}
					//  Copy ordered array to scale fitness fields
					for (int i=0; i<Parameters.popSize; i++){
						member[memberIndex[i]].sclFitness = i;
						sumSclFitness += member[memberIndex[i]].sclFitness;
					}

					break;

				case 3:     // Fitness scaled by Rank (minimizing fitness)

					//  Copy genetic data to temp array
					for (int i=0; i<Parameters.popSize; i++){
						memberIndex[i] = i;
						memberFitness[i] = member[i].rawFitness;
					}
					//  Bubble Sort the array by floating point number
					for (int i=1; i<Parameters.popSize; i++){
						for (int j=(Parameters.popSize - 1); j>=i; j--){
							if (memberFitness[j-i] < memberFitness[j]){
								TmemberIndex = memberIndex[j-1];
								TmemberFitness = memberFitness[j-1];
								memberIndex[j-1] = memberIndex[j];
								memberFitness[j-1] = memberFitness[j];
								memberIndex[j] = TmemberIndex;
								memberFitness[j] = TmemberFitness;
							}
						}
					}
					//  Copy array order to scale fitness fields
					for (int i=0; i<Parameters.popSize; i++){
						member[memberIndex[i]].sclFitness = i;
						sumSclFitness += member[memberIndex[i]].sclFitness;
					}

					break;

				default:
					System.out.println("ERROR - No scaling method selected");
				}


		// *********************************************************************
		// ****** PROPORTIONALIZE SCALED FITNESS FOR EACH MEMBER AND SUM *******
		// *********************************************************************

				for (int i=0; i<Parameters.popSize; i++){
					member[i].proFitness = member[i].sclFitness/sumSclFitness;
					sumProFitness = sumProFitness + member[i].proFitness;
				}

		// *********************************************************************
		// ************ CROSSOVER AND CREATE NEXT GENERATION *******************
		// *********************************************************************

				int parent1 = -1;
				int parent2 = -1;

				//  Assumes always two offspring per mating
				for (int i=0; i<Parameters.popSize; i=i+2){

					//	Select Two Parents
					parent1 = Chromo.selectParent();
					parent2 = parent1;
					while (parent2 == parent1){
						parent2 = Chromo.selectParent();
					}

					//	Crossover Two Parents to Create Two Children
					randnum = r.nextDouble();
					if (randnum < Parameters.xoverRate){
						Chromo.mateParents(parent1, parent2, member[parent1], member[parent2], child[i], child[i+1]);
					}
					else {
						Chromo.mateParents(parent1, member[parent1], child[i]);
						Chromo.mateParents(parent2, member[parent2], child[i+1]);
					}
				} // End Crossover

				//	Mutate Children
				for (int i=0; i<Parameters.popSize; i++){
					child[i].doMutation();
				}

				//	Swap Children with Last Generation
				for (int i=0; i<Parameters.popSize; i++){
					Chromo.copyB2A(member[i], child[i]);
				}

			} //  Repeat the above loop for each generation

			// Hwrite.left(bestOfRunR, 4, summaryOutput);
			// Hwrite.right(bestOfRunG, 4, summaryOutput);
			
			// csv.write("run,best_gen,best_fitness");
			// csv.newLine();
			// csv.write(bestOfRunR);
			// csv.write(bestOfRunG);
			// csv.write((int)bestOfRunF);
			// csv.newLine();

			runsFitnessStats[0][bestOfRunR] = bestOfRunG;
			runsFitnessStats[1][bestOfRunR] = (double)bestOfRunChromo.rawFitness;

			// problem.doPrintGenes(bestOfRunChromo, summaryOutput);

			// System.out.println(R + "\t" + "B" + "\t"+ (int)bestOfRunChromo.rawFitness);

		} //End of a Run

		// Hwrite.left("B", 8, summaryOutput);

		// problem.doPrintGenes(bestOverAllChromo, summaryOutput);

		//	Output Fitness Statistics matrix
		// summaryOutput.write("Gen                 AvgFit              BestFit \n");

		System.out.println("\nBest: " + bestOverAllChromo.rawFitness + " " + bestOverAllChromo.chromo);

		for (int i=0; i<Parameters.generations; i++){
			// Hwrite.left(i, 15, summaryOutput);
			// Hwrite.left(gensFitnessStats[0][i]/Parameters.numRuns, 20, 2, summaryOutput);
			// Hwrite.left(gensFitnessStats[1][i]/Parameters.numRuns, 20, 2, summaryOutput);
			// summaryOutput.write("\n");

			// generation
			summaryGenStats.write(i + ",");

			// average average fitness
			summaryGenStats.write(gensFitnessStats[0][i]/Parameters.numRuns + ",");
			
			// stdev average fitness
			double sumAvgFitness = 0;
			for (int j=1; j<=Parameters.numRuns; j++) {
				sumAvgFitness += fitnessLog[j][i][0];
			}
			double avgAvgFitness = sumAvgFitness / Parameters.numRuns;
			double stdevAvgFitness = 0;
			for (int j=1; j<=Parameters.numRuns; j++) {
				stdevAvgFitness += Math.pow(fitnessLog[j][i][0] - avgAvgFitness, 4);
			}
			stdevAvgFitness = Math.sqrt(stdevAvgFitness / Parameters.numRuns);

			summaryGenStats.write(stdevAvgFitness + ",");

			// average best fitness
			summaryGenStats.write(gensFitnessStats[1][i]/Parameters.numRuns + ",");

			// stdev best fitness
			double sumBestFitness = 0;
			for (int j=1; j<=Parameters.numRuns; j++) {
				sumBestFitness += fitnessLog[j][i][1];
			}
			double avgBestFitness = sumBestFitness / Parameters.numRuns;
			double stdevBestFitness = 0;
			for (int j=1; j<=Parameters.numRuns; j++) {
				stdevBestFitness += Math.pow(fitnessLog[j][i][1] - avgBestFitness, 4);
			}
			stdevBestFitness = Math.sqrt(stdevBestFitness / Parameters.numRuns);

			summaryGenStats.write(stdevBestFitness + ",");
			summaryGenStats.write("\n");
		}

		

		double sumTotalAvgBestFitness = 0;
		double sumGenOptInd = 0;

		// for (int i=1; i<=Parameters.numRuns; i++){
			
		// 	// run
		// 	csv.write(i);

		// 	// best gen
		// 	csv.write(runsFitnessStats[0][i], 4);

		// 	// best fitness
		// 	csv.write(runsFitnessStats[1][i], 4);
		// 	csv.newLine();

		// 	sumTotalAvgBestFitness += runsFitnessStats[1][i];
		// 	sumGenOptInd += runsFitnessStats[0][i];
		// }

		

		


		// summaryOutput.write("\n");
		// summaryOutput.close();

		summaryGenStats.close();

		// System.out.println();
		// System.out.println("Start:  " + startTime);
		dateAndTime = Calendar.getInstance(); 
		Date endTime = dateAndTime.getTime();
		// System.out.println("End  :  " + endTime);

	} // End of Main Class

}   // End of Search.Java ******************************************************



