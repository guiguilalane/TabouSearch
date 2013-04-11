package structure;

import java.util.ArrayList;
import java.util.List;

public class TabuMoves {
	
	private List<Pair> tabuMoves;
	private int size;
	private int currentPos;
	
	public TabuMoves(int size) {
		this.size = size;
		this.currentPos = 0;
		tabuMoves = new ArrayList<Pair>(size);
	}
	
	public void add(Pair p) {
		if(currentPos >= size) {
			tabuMoves.set(currentPos%size, p);
		} else {
			tabuMoves.add(currentPos%size, p);
		}
		++currentPos;
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
