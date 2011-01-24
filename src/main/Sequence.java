package main;

import java.util.ArrayList;

public class Sequence implements Comparable<Sequence> {
	private ArrayList<Integer> drops = new ArrayList<Integer>();
	private int score;
	
	public Sequence(ArrayList<Integer> drops, int score) {
		this.drops = drops;
		this.score = score;
	}
	
	public ArrayList<Integer> getDrops() { return drops; }
	public int getScore() { return score; }
	
	public void copyDrops() { 
		drops = new ArrayList<Integer>(drops);
	}
	
	public int compareTo(Sequence arg0) {
		return this.score - arg0.score;
	}
	
	public String toString() {
		return String.format("score:%d drops:%s", score, drops);
	}
}
