import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import structure.Neighborhoods;
import structure.Pair;
import JaCoP.constraints.Alldifferent;
import JaCoP.constraints.PrimitiveConstraint;
import JaCoP.constraints.XplusCeqZ;
import JaCoP.core.IntDomain;
import JaCoP.core.IntVar;
import JaCoP.core.Store;
import JaCoP.core.ValueEnumeration;
import JaCoP.search.DepthFirstSearch;
import JaCoP.search.ExitChildListener;
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
	public int[] generateSolution(IntDomain[] domains) {
		Random rand = new Random();
		int[] solution = new int[domains.length];
		
		for (int i=0; i<domains.length; ++i) {
			ValueEnumeration values = domains[i].valueEnumeration();
			int r = rand.nextInt(domains[i].getSize());   // 0 .. getSize()-1

			for (int j=0; j<=r; ++j) {
				solution[i] = values.nextElement();  // only the r-th is relevant
			}
		}
		
		return solution;
	}
	
	// Cost or fitness of an alldifferent constraint
	public int costAllDifferent(int[] sol) {
		int n = 0;
		for (int i=0; i<sol.length; ++i) {
			for (int j=i+1; j<sol.length; ++j) {
				if (sol[i] == sol[j]) ++n;
			}
		}
		return n;
	}
	
	// Fitness of a solution for the n-queens problem
	public int fitness(int[] sol) {
		int n = 0;

		// allDifferent on Q
		n += costAllDifferent(sol);

		// allDifferent on y
		int[] aux = new int[sol.length];
		for (int i=0; i<sol.length; ++i) {
			aux[i] = sol[i] + i;
		}
		n += costAllDifferent(aux);

		// allDifferent on z
		for (int i=0; i<sol.length; ++i) {
			aux[i] = sol[i] - i;
		}
		n += costAllDifferent(aux);
		
		return n;
	}
	
	// Display a solution
	public static void printSolution(int[] sol) {
		System.out.print("{");
		for (int i=0; i<sol.length; ++i) {
			if (i!=0) System.out.print(", ");
			System.out.print(sol[i]);
		}
		System.out.print("}");
	}
	
	public boolean stopConditions(int k) {
		return k < 15;
	}
	
	// Main algorithm... to be completed
	public boolean tabuSearch(int sizeOfTabuMoves) {
		
		// Generate a first solution
		IntDomain[] domains = getDomains();
		int[] sol = generateSolution(domains);
		System.out.println("First generated solution");
//		printSolution(sol);
		//State that correspond to the best Solution known
		int[] bestSol = sol;
		
		//TODO: Créer nouvelle structure pour gérer la taille maximal de la liste.
		List<Pair> tabuMoves = new ArrayList<Pair>(sizeOfTabuMoves);
		
		//iteration number
		int k = 0;
		Neighborhoods subsets;
		while(stopConditions(k)) {
			k++;
			subsets = new Neighborhoods(domains, sol, tabuMoves);//candidate solutions
			
			int[] bestCandidate = subsets.calculateBestCandidate();//best candidate solution
			sol = bestCandidate; //update current solution
			if(Neighborhoods.fitness(bestCandidate) < Neighborhoods.fitness(bestSol)) {
				bestSol = bestCandidate;
			}
			//TODO : update tabu list an aspiration condition
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
		final int n = 5;
		ChessQueens model = new ChessQueens(n);

//		 boolean result = model.completeSearch();
		
		boolean result = model.tabuSearch(5);
		
	}

}
