import java.util.ArrayList;
import java.util.Random;

import structure.Neighborhoods;
import structure.Pair;
import structure.TabuMoves;
import JaCoP.constraints.Alldifferent;
import JaCoP.constraints.XplusCeqZ;
import JaCoP.core.IntDomain;
import JaCoP.core.IntVar;
import JaCoP.core.Store;
import JaCoP.core.ValueEnumeration;
import JaCoP.search.DepthFirstSearch;
import JaCoP.search.IndomainMedian;
import JaCoP.search.SelectChoicePoint;
import JaCoP.search.SimpleSelect;
import JaCoP.search.SmallestDomain;


public class ChessQueens {
	private Store store;
	private IntVar[] Q;		// main variables: Q[i] represents the column of the queen on the i-th row 
	
	public ChessQueens(int n) {
		store = new Store();
		Q = new IntVar[n];

		IntVar[] y = new IntVar[n];
		IntVar[] z = new IntVar[n];

		for (int i=0; i<n; ++i) {
			Q[i] = new IntVar(store,"Q" + i,0,n-1);
			y[i] = new IntVar(store,"y" + i,-i,n-1-i);
			z[i] = new IntVar(store,"z" + i,i,n-1+i);
			System.out.println("y[i] : " + y[i]);
			store.impose(new XplusCeqZ(Q[i],i,z[i]));
			store.impose(new XplusCeqZ(y[i],i,Q[i]));
		}

		// all different: no attack on columns
		store.impose(new Alldifferent(Q));
		store.impose(new Alldifferent(y));
		store.impose(new Alldifferent(z));
	}
	
	// Get the domains of the main variables
	public IntDomain[] getDomains() {
		IntDomain[] tab = new IntDomain[Q.length];
		for (int i=0; i<Q.length; ++i) {
			tab[i] = Q[i].domain;
		}
		return tab;
	}
	
	// Generate randomly a solution within the domains
	//TODO: rewrite this method
//	public int[] generateSolution(IntDomain[] domains) {
//		Random rand = new Random();
//		int[] solution = new int[domains.length];
//		for (int i=0; i<domains.length; ++i) {
//			ValueEnumeration values = domains[i].valueEnumeration();
//			int r = rand.nextInt(domains[i].getSize());   // 0 .. getSize()-1
//			for (int j=0; j<=r; ++j) {
//				
//				solution[i] = values.nextElement();  // only the r-th is relevant
//			}
//			System.out.println("\t\tSolution : " + solution[i]);
//		}
//		return solution;
//	}
	
	// Generate randomly a solution within the domains
	public int[] generateSolution(IntDomain[] domains) {
		Random rand = new Random();
		int[] solution = new int[domains.length];
		ArrayList<Integer> usedSol = new ArrayList<Integer>();
		for (int i=0; i<domains.length; ++i) {
			IntDomain d = (IntDomain)domains[i].clone();
			if(!usedSol.isEmpty()) { //lorsqu'un valeur est affectée à une reine, cette valeur est retirée du domaine des reines suivantes
				d = removeValueFromDomain(d, usedSol);
			}
			ValueEnumeration values = d.valueEnumeration();
			int r = rand.nextInt(d.getSize());   // 0 .. getSize()-1
//			System.out.println("r : " + r);
			int v = -1;
			for (int j=0; j<=r; ++j) {
				v = values.nextElement();  // only the r-th is relevant
//				System.out.println("\tvalues : " + v);
				solution[i] = v;
			}
			usedSol.add(v);
//			System.out.println("\t\tSolution : " + solution[i]);
		}
		return solution;
	}
	
	private IntDomain removeValueFromDomain(IntDomain d,
			ArrayList<Integer> usedSol) {
		for(Integer i: usedSol)	{
			d.subtractAdapt(i);
		}
		return d;
	}

	public boolean stopConditions(int k) {
		return k < 100;
	}
	
	// Main algorithm... to be completed
	public boolean tabuSearch(int sizeOfTabuMoves) {
		
		// Generate a first solution
		IntDomain[] domains = getDomains();
		int[] sol = generateSolution(domains);
		System.out.println("First generated solution");
		Neighborhoods.printSolution(sol);
		System.out.println();
		//State that correspond to the best Solution known
		int[] bestSol = sol;
		
		//TODO: Créer nouvelle structure pour gérer la taille maximal de la liste.
//		List<Pair> tabuMoves = new ArrayList<Pair>(sizeOfTabuMoves);
		TabuMoves tabuMoves = new TabuMoves(sizeOfTabuMoves);
		
		//iteration number
		int k = 0;
		Neighborhoods subsets;
		Pair forbiddenMove = null;
		while(stopConditions(k) && Neighborhoods.fitness(bestSol) != 0) {
			
			subsets = new Neighborhoods(domains, sol, tabuMoves.getTabuMoves());//candidate solutions
			forbiddenMove = new Pair(0,0);
			int[] bestCandidate = subsets.calculateBestCandidate(forbiddenMove);//best candidate solution
			sol = bestCandidate; //update current solution
			if(Neighborhoods.fitness(bestCandidate) < Neighborhoods.fitness(bestSol)) {
				bestSol = bestCandidate;
			}
//			System.out.println(forbiddenMove);
			System.out.println("k : " + k + ", sizeOfTabuMoves : " + sizeOfTabuMoves + ", k%sizeOfTabuMove = " + k%sizeOfTabuMoves);
			System.out.println(forbiddenMove);
			tabuMoves.add(forbiddenMove);
//			tabuMoves.add(k%sizeOfTabuMoves, forbiddenMove);
//			Pair p = tabuMoves.getTabuMove(k);
			System.out.println(tabuMoves);
//			System.out.println(p);
			System.out.println("cout : " + Neighborhoods.fitness(bestSol));
			System.out.println("**************************************");
			//TODO : update tabu list an aspiration condition
			k++;
		}
		
		Neighborhoods.printSolution(bestSol);
		System.out.println("cout : " + Neighborhoods.fitness(bestSol));
		
		// Calculate the cost of the curent solution
//		int cost = fitness(sol);
				
		//System.out.println("\nfitness : " + cost);
		
		// ...
		
		return true;
	}
	
	
	public boolean completeSearch() {
		DepthFirstSearch<IntVar> search = new DepthFirstSearch<IntVar>();

		search.getSolutionListener().searchAll(true);
		search.getSolutionListener().recordSolutions(true);

		SelectChoicePoint<IntVar> select =
				new SimpleSelect<IntVar>(Q,
										 new SmallestDomain<IntVar>(),
										 new IndomainMedian<IntVar>());
		
		
		

		boolean result = search.labeling(store, select);

		for (int i=1; i<=search.getSolutionListener().solutionsNo(); i++){
			System.out.print("Solution " + i + ": [");
			for (int j=0; j<search.getSolution(i).length; j++) {
				if (j!=0) System.out.print(", ");
			    System.out.print(search.getSolution(i)[j]);
			}
			System.out.println("]");
		}
		
		return result;
	}

	public static void main(String[] args) {
		final int n = 100;
		ChessQueens model = new ChessQueens(n);

//		 boolean result = model.completeSearch();
		
		boolean result = model.tabuSearch(25);
		
	}

}
