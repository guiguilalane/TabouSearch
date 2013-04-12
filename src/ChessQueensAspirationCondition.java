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

public class ChessQueensAspirationCondition {
	private Store store;
	private IntVar[] Q; // main variables: Q[i] represents the column of the
						// queen on the i-th row

	public ChessQueensAspirationCondition(int n) {
		store = new Store();
		Q = new IntVar[n];

		IntVar[] y = new IntVar[n];
		IntVar[] z = new IntVar[n];

		for (int i = 0; i < n; ++i) {
			Q[i] = new IntVar(store, "Q" + i, 0, n - 1);
			y[i] = new IntVar(store, "y" + i, -i, n - 1 - i);
			z[i] = new IntVar(store, "z" + i, i, n - 1 + i);
			// System.out.println("y[i] : " + y[i]);
			store.impose(new XplusCeqZ(Q[i], i, z[i]));
			store.impose(new XplusCeqZ(y[i], i, Q[i]));
		}

		// all different: no attack on columns
		store.impose(new Alldifferent(Q));
		store.impose(new Alldifferent(y));
		store.impose(new Alldifferent(z));
	}

	// Get the domains of the main variables
	public IntDomain[] getDomains() {
		IntDomain[] tab = new IntDomain[Q.length];
		for (int i = 0; i < Q.length; ++i) {
			tab[i] = Q[i].domain;
		}
		return tab;
	}

	// Generate randomly a solution within the domains
	// TODO: rewrite this method
	// public int[] generateSolution(IntDomain[] domains) {
	// Random rand = new Random();
	// int[] solution = new int[domains.length];
	// for (int i=0; i<domains.length; ++i) {
	// ValueEnumeration values = domains[i].valueEnumeration();
	// int r = rand.nextInt(domains[i].getSize()); // 0 .. getSize()-1
	// for (int j=0; j<=r; ++j) {
	//
	// solution[i] = values.nextElement(); // only the r-th is relevant
	// }
	// System.out.println("\t\tSolution : " + solution[i]);
	// }
	// return solution;
	// }

	// Generate randomly a solution within the domains
	public int[] generateSolution(IntDomain[] domains) {
		Random rand = new Random();
		int[] solution = new int[domains.length];
		ArrayList<Integer> usedSol = new ArrayList<Integer>();
		for (int i = 0; i < domains.length; ++i) {
			IntDomain d = (IntDomain) domains[i].clone();
			if (!usedSol.isEmpty()) { // lorsqu'un valeur est affectée à une
										// reine, cette valeur est retirée du
										// domaine des reines suivantes
				d = removeValueFromDomain(d, usedSol);
			}
			ValueEnumeration values = d.valueEnumeration();
			int r = rand.nextInt(d.getSize()); // 0 .. getSize()-1
			// System.out.println("r : " + r);
			int v = -1;
			for (int j = 0; j <= r; ++j) {
				v = values.nextElement(); // only the r-th is relevant
				// System.out.println("\tvalues : " + v);
				solution[i] = v;
			}
			usedSol.add(v);
			// System.out.println("\t\tSolution : " + solution[i]);
		}
		return solution;
	}

	private IntDomain removeValueFromDomain(IntDomain d,
			ArrayList<Integer> usedSol) {
		for (Integer i : usedSol) {
			d.subtractAdapt(i);
		}
		return d;
	}

	public boolean stopConditions(int k) {
		return k < 100;
	}

	// Main algorithm... to be completed
	public boolean tabuSearch(int sizeOfTabuMoves) {

		boolean cost = false;
		// Generate a first solution
		IntDomain[] domains = getDomains();
		int[] sol = generateSolution(domains);
		// System.out.println("First generated solution");
		// Neighborhoods.printSolution(sol);
		// System.out.println();
		// State that correspond to the best Solution known
		int[] bestSol = sol;

		// TODO: Créer nouvelle structure pour gérer la taille maximal de la
		// liste.
		// List<Pair> tabuMoves = new ArrayList<Pair>(sizeOfTabuMoves);
		TabuMoves tabuMoves = new TabuMoves(sizeOfTabuMoves);

		// iteration number
		int k = 0;
		Neighborhoods subsets;
		Pair forbiddenMove = null;
		while (stopConditions(k) && Neighborhoods.fitness(bestSol) != 0) {

			subsets = new Neighborhoods(domains, sol, tabuMoves.getTabuMoves());// candidate
																				// solutions
			forbiddenMove = new Pair(0, 0);
			int[] bestCandidate = subsets.calculateBestCandidate(forbiddenMove);// best
																				// candidate
																				// solution
			sol = bestCandidate; // update current solution
			int fitOfBestC = Neighborhoods.fitness(bestCandidate);
			int fitOfBestS = Neighborhoods.fitness(bestSol);
			if (fitOfBestC < fitOfBestS) {
				bestSol = bestCandidate;
				System.out.println("k : " + k + ", sizeOfTabuMoves : "
						+ sizeOfTabuMoves + ", k%sizeOfTabuMove = " + k
						% sizeOfTabuMoves);
				System.out.println(forbiddenMove);
				tabuMoves.add(forbiddenMove);
			} else if (fitOfBestC >= fitOfBestS) {
				// aspiration condition
				System.out.println("pas d'amélioration");
				int fitOfBestA = fitOfBestC;
				int[] altSol = Neighborhoods.cloneSolution(bestSol);
				for (Pair p : tabuMoves.getTabuMoves()) {
					// System.out.println("pair : " + p);
					// System.out.println("bestCandidate : " + bestSol[p.x]);
					// System.out.println(bestSol[p.x] != p.y);
					if (bestSol[p.x] != p.y) {
						/*
						 * if the queen value of the tabuMove is different from
						 * the queen value of the bestCandidate Solution, we
						 * check if the cost of the move improve the fitness.
						 */
						altSol[p.x] = p.x;
						fitOfBestA = Neighborhoods.fitness(altSol);
						// System.out.println("fitness of bestCandidate with tabuMove : "
						// + fitOfBestA);
					}
				}
				if (fitOfBestA < fitOfBestS) {
					System.out.println("Improvement");
					bestSol = altSol;
				} else if (fitOfBestC == fitOfBestS) { // if it doesn't improve
														// the fitness
					System.out.println("No improvement");
					// It set the move to a tabuMove, to avoid infint loop
					tabuMoves.add(forbiddenMove);
				}
			} else {
				System.out.println("k : " + k + ", sizeOfTabuMoves : "
						+ sizeOfTabuMoves + ", k%sizeOfTabuMove = " + k
						% sizeOfTabuMoves);
				System.out.println(forbiddenMove);
				tabuMoves.add(forbiddenMove);
			}
			// System.out.println(forbiddenMove);

			// tabuMoves.add(k%sizeOfTabuMoves, forbiddenMove);
			// Pair p = tabuMoves.getTabuMove(k);
			// System.out.println(tabuMoves);
			// System.out.println(p);
			cost = Neighborhoods.fitness(bestSol) == 0;
			System.out.println("cout : " + cost);
			System.out.println("**************************************");
			// TODO : update tabu list an aspiration condition
			k++;
		}

		Neighborhoods.printSolution(bestSol);
		System.out.println("cout : " + Neighborhoods.fitness(bestSol));

		// Calculate the cost of the curent solution
		// int cost = fitness(sol);

		// System.out.println("\nfitness : " + cost);

		// ...

		return cost;
	}

	public boolean completeSearch() {
		DepthFirstSearch<IntVar> search = new DepthFirstSearch<IntVar>();

		search.getSolutionListener().searchAll(true);
		search.getSolutionListener().recordSolutions(true);

		SelectChoicePoint<IntVar> select = new SimpleSelect<IntVar>(Q,
				new SmallestDomain<IntVar>(), new IndomainMedian<IntVar>());

		boolean result = search.labeling(store, select);

		for (int i = 1; i <= search.getSolutionListener().solutionsNo(); i++) {
			System.out.print("Solution " + i + ": [");
			for (int j = 0; j < search.getSolution(i).length; j++) {
				if (j != 0)
					System.out.print(", ");
				System.out.print(search.getSolution(i)[j]);
			}
			System.out.println("]");
		}

		return result;
	}

	public static void main(String[] args) {
		final int n = 300;
		boolean result = true;
		ChessQueensAspirationCondition model = new ChessQueensAspirationCondition(
				n);

		// boolean result = model.completeSearch();

		result = model.tabuSearch(25);

	}

}
