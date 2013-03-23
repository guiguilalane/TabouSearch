package structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import JaCoP.core.IntDomain;

public class Neighborhoods {
	private ArrayList<Integer[]> subsets;
	private int[] sol;
	
	public Neighborhoods(IntDomain[] domains, int [] solution, List<Pair> tabuMoves) {
		subsets = new ArrayList<Integer[]>();
		sol = cloneSolution(solution);
		//parcourir les valeurs de la solutions
		Integer[][] sols = new Integer[sol.length][sol.length-1];
		for(int i = 0; i < sol.length; ++i) {
			//pour chaque valeur de la solution courante, il y a n-1 autre solution
			int k = 0;
			//pour chaques valeurs des autres solutions, parcourir le domaine possible
			for(int j = domains[i].min(); j <= domains[i].max(); j++) {
				//vérifier que la valeur courante (j) n'est pas la solution
				//auquel cas l'ajouter dans le tableau de solution
				if(j != sol[i]) {
					sols[i][k] = j;
					++k;
//					System.out.println("current sol : " + sol[i] + ", possible sol : " + j);
				}
			}
		}
//		System.out.print("[");
//		for(int i = 0; i < sol.length; ++i) {
//			System.out.print("[");
//			for(int k = 0; k < sol.length-1; ++k) {
//				System.out.print(sols[i][k]);
//				if(k!=sol.length - 2) {
//					System.out.print(", ");
//				} else {
//					System.out.print("]");
//				}
//			}
//			if(i!=sol.length - 1) {
//				System.out.println(",");
//			} else {
//				System.out.println("]");
//			}
//		}
		Integer[] temp;
		for(int i = 0; i < sols.length; ++i) {
			for(int j = 0; j < sols[i].length; ++j) {
				temp = copySol(sol);
				temp[i] = sols[i][j];
				if(!isTabuMove(tabuMoves, i, sols[i][j])) {
					subsets.add(temp);
				}
			}
		}
		//TODO: calculate all differents moves
	}
	
	private int[] cloneSolution(int[] toClone) {
		int[] res = new int[toClone.length];
		for(int i = 0; i < toClone.length; ++i) {
			res[i] = toClone[i];
		}
		return res;
	}
	
	private Integer[] copySol(int[] toCopy) {
		Integer[] res = new Integer[toCopy.length];
		for(int i = 0; i < toCopy.length; ++i) {
			res[i] = toCopy[i];
		}
		return res;
	}
	
	public boolean isTabuMove(List<Pair> tabuMoves, int x, int y) {
		boolean res = false;
		Iterator<Pair> ip = tabuMoves.listIterator();
		Pair p;
		while(ip.hasNext() && !res) {
			p = ip.next();
			res = res || (p.x == x && p.y == y);
		}
		return res;
	}
	
	// Display a solution
	public static void printSolution(int[] sol) {
		System.out.print("{");
		for (int i=0; i<sol.length; ++i) {
			if (i!=0) System.out.print(", ");
			System.out.print(sol[i]);
		}
		System.out.println("}");
	}	
	
	public int[] calculateBestCandidate() {
		int[] bestCandidate = new int[subsets.get(0).length];
		int bestCost = Integer.MAX_VALUE;
		for(Integer[] i: subsets) {
			int[] current = fromtabInteger(i);
			int currentcost = fitness(current);
			if(currentcost < bestCost) {
				bestCandidate = current;
				bestCost = currentcost;
			}
		}
		return bestCandidate;
	}
	
	private int[] fromtabInteger(Integer[] tabI) {
		int[] result = new int[tabI.length];
		for(int i = 0; i < tabI.length; ++i) {
			result[i] = tabI[i];
		}
		return result;
	}
	
	// Cost or fitness of an alldifferent constraint
		private static int costAllDifferent(int[] sol) {
			int n = 0;
			for (int i=0; i<sol.length; ++i) {
				for (int j=i+1; j<sol.length; ++j) {
					if (sol[i] == sol[j]) ++n;
				}
			}
			return n;
		}
		
		// Fitness of a solution for the n-queens problem
		public static int fitness(int[] sol) {
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
}
