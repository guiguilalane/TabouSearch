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
	private IntVar[] Q; // Main variables: Q[i] represents the column of the
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
			store.impose(new XplusCeqZ(Q[i], i, z[i]));
			store.impose(new XplusCeqZ(y[i], i, Q[i]));
		}

		// All different: no attack on columns
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
	public int[] generateSolution(IntDomain[] domains) {
		Random rand = new Random();
		int[] solution = new int[domains.length];
		ArrayList<Integer> usedSol = new ArrayList<Integer>();
		for (int i = 0; i < domains.length; ++i) {
			IntDomain d = (IntDomain) domains[i].clone();
			if (!usedSol.isEmpty()) { // When a value is affect to a queen, this
										// value was delete of the domain of
										// next queens
				d = removeValueFromDomain(d, usedSol);
			}
			ValueEnumeration values = d.valueEnumeration();
			int r = rand.nextInt(d.getSize()); // 0 .. getSize()-1
			int v = -1;
			for (int j = 0; j <= r; ++j) {
				v = values.nextElement(); // Only the r-th is relevant
				solution[i] = v;
			}
			usedSol.add(v);
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

	public boolean tabuSearch(int sizeOfTabuMoves) {

		boolean cost = false;
		// Generate a first solution
		IntDomain[] domains = getDomains();
		int[] sol = generateSolution(domains);
		// State that correspond to the best Solution known
		int[] bestSol = sol;

		// List<Pair> tabuMoves = new ArrayList<Pair>(sizeOfTabuMoves);
		TabuMoves tabuMoves = new TabuMoves(sizeOfTabuMoves);

		// Iteration number
		int k = 0;
		Neighborhoods subsets;
		Pair forbiddenMove = null;
		while (stopConditions(k) && Neighborhoods.fitness(bestSol) != 0) {

			subsets = new Neighborhoods(domains, sol, tabuMoves.getTabuMoves());// Candidate
																				// solutions
			forbiddenMove = new Pair(0, 0);
			int[] bestCandidate = subsets.calculateBestCandidate(forbiddenMove);// Best
																				// candidate
																				// solution
			sol = bestCandidate; // Update current solution
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
				// Aspiration condition
				int fitOfBestA = fitOfBestC;
				int[] altSol = Neighborhoods.cloneSolution(bestSol);
				for (Pair p : tabuMoves.getTabuMoves()) {
					if (bestSol[p.x] != p.y) {
						/*
						 * If the queen value of the tabuMove is different from
						 * the queen value of the bestCandidate Solution, we
						 * check if the cost of the move improve the fitness.
						 */
						altSol[p.x] = p.x;
						fitOfBestA = Neighborhoods.fitness(altSol);
					}
				}
				if (fitOfBestA < fitOfBestS) {
					bestSol = altSol;
				} else if (fitOfBestC == fitOfBestS) { // If it doesn't improve
														// the fitness
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
			cost = Neighborhoods.fitness(bestSol) == 0;
			System.out.println("cout : " + cost);
			System.out.println("**************************************");
			k++;
		}

		Neighborhoods.printSolution(bestSol);
		System.out.println("cout : " + Neighborhoods.fitness(bestSol));

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

		result = model.tabuSearch(25);

	}

}
