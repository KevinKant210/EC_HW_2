/******************************************************************************
*  A Teaching GA					  Developed by Hal Stringer & Annie Wu, UCF
*  Version 2, January 18, 2004
*******************************************************************************/

import java.io.*;
import java.util.*;
import java.text.*;

public class Chromo
{
/*******************************************************************************
*                            INSTANCE VARIABLES                                *
*******************************************************************************/

	public String chromo;
	public double rawFitness;
	public double sclFitness;
	public double proFitness;

/*******************************************************************************
*                            INSTANCE VARIABLES                                *
*******************************************************************************/

	private static double randnum;

/*******************************************************************************
*                              CONSTRUCTORS                                    *
*******************************************************************************/

	public static String edgeRecomb(Chromo parent_1,Chromo parent_2){
		
		
		ArrayList<Integer> values_1 = new ArrayList<>();
		ArrayList<Integer> values_2 = new ArrayList<>();

		
		for(int i = 0 ;i < parent_1.chromo.length(); i += 2){
			
			values_1.add(Integer.parseInt(parent_1.chromo.substring(i,i+2)));
			values_2.add(Integer.parseInt(parent_2.chromo.substring(i,i+2)));
			
		}
		
		int n = values_1.size();
		Map<Integer,Set<Integer>> edges = new HashMap<>();

		for(int i =0; i < values_1.size(); i++){
			Set<Integer> set = new HashSet<>();

			int next = (i+1)%n;
			int prev = (i-1 +n)%n;

			set.add(values_1.get(next));
			set.add(values_1.get(prev));

			int index = values_2.indexOf(values_1.get(i));

			next = (index + 1)%n;
			prev = (index-1 + n)%n;

			set.add(values_2.get(next));
			set.add(values_2.get(prev));

			edges.put(values_1.get(i), set);
		}

		ArrayList<Integer> child = new ArrayList<>();
		ArrayList<Integer> added = new ArrayList<>();
		Integer chosen = Search.r.nextInt(0,48);

		while(child.size() != n){
			child.add(chosen);
			if(child.size() == n)break;
			added.add(chosen);
			
			//we sort added here to keep get random with exclusion working
			added.sort(Comparator.naturalOrder());
			//get the list of neighbors that the chosen one is connected too
			ArrayList<Integer> neighbors = new ArrayList<>(edges.get(chosen));

			//from here we need to remove chosen from all its neighbors so we do that

			//we set candidate equal initially to a random value for the case that there are no neighbors of this node
			int candidate = -1;
			int min_size = Integer.MAX_VALUE;

			for(int i = 0 ; i < neighbors.size(); i++){
				edges.get(neighbors.get(i)).remove(chosen);
				//once removed we can then assess if this neighbor is a valid candidate for adding 

				if(edges.get(neighbors.get(i)).size() < min_size){
					min_size = edges.get(neighbors.get(i)).size();
					candidate = neighbors.get(i);
				}
			}
			//we can then choose this value  and set it to a chosen valeu

			if(candidate == -1){
				chosen = getRandomWithExclusion(Search.r, 0, 47, added);
			}else{
				chosen = candidate;
			}

			
		}

		String values = "";
		for(int i = 0 ; i < child.size(); i++){
			
			String add = "";
			if(child.get(i) < 10){
				add = "0";

			}

			add += Integer.toString(child.get(i));

			values += add;
		}

		
		
		return values;
	}

	public static int getRandomWithExclusion(Random rnd, int start, int end, ArrayList<Integer> exclude) {
		int random = start + rnd.nextInt(end - start + 1 - exclude.size());
		for (int ex : exclude) {
			if (random < ex) {
				break;
			}
			random++;
		}
		return random;
	}

	public Chromo(){
		//  Set gene values to a randum sequence of 1's and 0's
		char geneBit;
		chromo = "";
		ArrayList<Integer> used = new ArrayList<>();
		for (int i=0; i<Parameters.numGenes; i++){
			for (int j=0; j<Parameters.geneSize; j++){
				// randnum = Search.r.nextDouble();
				// if (randnum > 0.5) geneBit = '0';
				// else geneBit = '1';
				// this.chromo = chromo + geneBit;
				boolean exit = false;

				
				int randnumber = getRandomWithExclusion(Search.r, 0, 47, used);

				// int a = 65;
				// int b = a + 6;
				// if(randnumber > 25){
					
				// 	chromo += (char) (b+randnumber);
				// }else{
				// 	chromo += (char) (a+randnumber);
				// }
				String add = "";
				if(randnumber < 10){
					add += ('0' + Integer.toString(randnumber));
				}else{
					add += Integer.toString(randnumber);
				}

				// for(int val : used){
				// 	int comp = Integer.parseInt(add);

				// 	if(val == comp){
				// 		System.out.println("Copy!");
				// 	}
				// }

				used.add(randnumber);
				used.sort(Comparator.naturalOrder());

				
				chromo += add;
			}
		}
		// System.out.println("Start");
		// System.out.println(chromo);
		// System.out.println(chromo.length());
		// System.out.println("End");
		this.chromo = chromo;
		
		this.rawFitness = -1;   //  Fitness not yet evaluated
		this.sclFitness = -1;   //  Fitness not yet scaled
		this.proFitness = -1;   //  Fitness not yet proportionalized
	}


/*******************************************************************************
*                                MEMBER METHODS                                *
*******************************************************************************/

	//  Get Alpha Represenation of a Gene **************************************

	public String getGeneAlpha(int geneID){
		int start = geneID * Parameters.geneSize;
		int end = (geneID+1) * Parameters.geneSize;
		String geneAlpha = this.chromo.substring(start, end);
		return (geneAlpha);
	}

	//  Get Integer Value of a Gene (Positive or Negative, 2's Compliment) ****

	public int getIntGeneValue(int geneID){
		String geneAlpha = "";
		int geneValue;
		char geneSign;
		char geneBit;
		geneValue = 0;
		geneAlpha = getGeneAlpha(geneID);
		for (int i=Parameters.geneSize-1; i>=1; i--){
			geneBit = geneAlpha.charAt(i);
			if (geneBit == '1') geneValue = geneValue + (int) Math.pow(2.0, Parameters.geneSize-i-1);
		}
		geneSign = geneAlpha.charAt(0);
		if (geneSign == '1') geneValue = geneValue - (int)Math.pow(2.0, Parameters.geneSize-1);
		return (geneValue);
	}

	//  Get Integer Value of a Gene (Positive only) ****************************

	public int getPosIntGeneValue(int geneID){
		String geneAlpha = "";
		int geneValue;
		char geneBit;
		geneValue = 0;
		geneAlpha = getGeneAlpha(geneID);
		for (int i=Parameters.geneSize-1; i>=0; i--){
			geneBit = geneAlpha.charAt(i);
			if (geneBit == '1') geneValue = geneValue + (int) Math.pow(2.0, Parameters.geneSize-i-1);
		}
		return (geneValue);
	}

	//  Mutate a Chromosome Based on Mutation Type *****************************

	public void doMutation(){

		String mutChromo = "";
		char x;

		switch (Parameters.mutationType){

		case 1:     //  Replace with new random number

			for (int j=0; j<(Parameters.geneSize * Parameters.numGenes); j++){
				x = this.chromo.charAt(j);
				randnum = Search.r.nextDouble();
				if (randnum < Parameters.mutationRate){
					if (x == '1') x = '0';
					else x = '1';
				}
				mutChromo = mutChromo + x;
			}

			

			this.chromo = mutChromo;
			break;
		case 2: //random swap
			//implement random swap here
			ArrayList<Integer> holder = new ArrayList<>();
			for(int i = 0 ; i < this.chromo.length(); i +=2){

				Integer val = Integer.parseInt(this.chromo.substring(i,i+2));
				holder.add(val);

				
			}

			int index_one = Search.r.nextInt(holder.size());

			int index_two = Search.r.nextInt(holder.size());

			while(index_one == index_two){
				index_two = Search.r.nextInt(holder.size());
			}

			int temp = holder.get(index_one);
			holder.set(index_one, holder.get(index_two));

			holder.set(index_two,temp);

			for(int i = 0 ; i < holder.size(); i++){
				String add = "";
				if(holder.get(i) < 10){
					add += '0';
				}

				add += Integer.toString(holder.get(i));

				mutChromo += add;
			}
			
			this.chromo = mutChromo;
			break;
		default:
			System.out.println("ERROR - No mutation method selected");
		}
	}

/*******************************************************************************
*                             STATIC METHODS                                   *
*******************************************************************************/

	//  Select a parent for crossover ******************************************

	public static int selectParent(){

		double rWheel = 0;
		int j = 0;
		int k = 0;

		switch (Parameters.selectType){

		case 1:     // Proportional Selection
			randnum = Search.r.nextDouble();
			for (j=0; j<Parameters.popSize; j++){
				rWheel = rWheel + Search.member[j].proFitness;
				if (randnum < rWheel) return(j);
			}
			break;

		case 3:     // Random Selection
			randnum = Search.r.nextDouble();
			j = (int) (randnum * Parameters.popSize);
			return(j);

		case 2:     //  Tournament Selection
			//k tournament size
			int k_tourn = 50;

			ArrayList<Integer> tourn_pop = new ArrayList<>();
			
			for(int i = 0 ; i < k_tourn; i++){
				tourn_pop.add(Search.r.nextInt(Parameters.popSize));
				
			}

			int best = tourn_pop.get(0);

			

			for(int i =1 ; i < k_tourn; i++){

				if(Search.member[tourn_pop.get(i)].rawFitness < Search.member[best].rawFitness)best = tourn_pop.get(i);
			}

			return best;

		default:
			System.out.println("ERROR - No selection method selected");
		}
	return(-1);
	}

	//  Produce a new child from two parents  **********************************

	public static void mateParents(int pnum1, int pnum2, Chromo parent1, Chromo parent2, Chromo child1, Chromo child2){

		int xoverPoint1;
		int xoverPoint2;

		switch (Parameters.xoverType){

		case 1:     //  Single Point Crossover

			//  Select crossover point
			xoverPoint1 = 1 + (int)(Search.r.nextDouble() * (Parameters.numGenes * Parameters.geneSize-1));

			//  Create child chromosome from parental material
			child1.chromo = parent1.chromo.substring(0,xoverPoint1) + parent2.chromo.substring(xoverPoint1);
			child2.chromo = parent2.chromo.substring(0,xoverPoint1) + parent1.chromo.substring(xoverPoint1);
			
			break;

		case 2:     //  Two Point Crossover
			break;
		case 3:     //  Uniform Crossover
			
			break;
		case 4: //Edge Recombination
			//edge recombination based on characters in the chromo
			
			child1.chromo = edgeRecomb(parent1,parent2);
			
			child2.chromo = edgeRecomb(parent2, parent1);

			break;
		default:
			System.out.println("ERROR - Bad crossover method selected");
		}

		//  Set fitness values back to zero
		child1.rawFitness = -1;   //  Fitness not yet evaluated
		child1.sclFitness = -1;   //  Fitness not yet scaled
		child1.proFitness = -1;   //  Fitness not yet proportionalized
		child2.rawFitness = -1;   //  Fitness not yet evaluated
		child2.sclFitness = -1;   //  Fitness not yet scaled
		child2.proFitness = -1;   //  Fitness not yet proportionalized
	}

	//  Produce a new child from a single parent  ******************************

	public static void mateParents(int pnum, Chromo parent, Chromo child){

		//  Create child chromosome from parental material
		child.chromo = parent.chromo;

		//  Set fitness values back to zero
		child.rawFitness = -1;   //  Fitness not yet evaluated
		child.sclFitness = -1;   //  Fitness not yet scaled
		child.proFitness = -1;   //  Fitness not yet proportionalized
	}

	//  Copy one chromosome to another  ***************************************

	public static void copyB2A (Chromo targetA, Chromo sourceB){

		targetA.chromo = sourceB.chromo;

		targetA.rawFitness = sourceB.rawFitness;
		targetA.sclFitness = sourceB.sclFitness;
		targetA.proFitness = sourceB.proFitness;
		return;
	}

}   // End of Chromo.java ******************************************************
