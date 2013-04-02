package structure;

import java.util.ArrayList;
import java.util.List;

public class TabuMoves {
	
	private List<Pair> tabuMoves;
	private int size;
	
	public TabuMoves(int size) {
		this.size = size;
		tabuMoves = new ArrayList<Pair>(size);
	}
	
	public void add(int pos, Pair p) {
		if(pos >= size) {
			tabuMoves.set(pos%size, p);
		} else {
			tabuMoves.add(pos%size, p);
		}
	}
	
	public List<Pair> getTabuMoves() {
		return tabuMoves;
	}
	
	public Pair getTabuMove(int pos) {
		return tabuMoves.get(pos%size);
	}
	
	@Override
	public String toString() {
		String s = "[";
		for(Pair p : tabuMoves) {
			s += p + ", ";
		}
		s += "]";
		return s;
		
	}

}
