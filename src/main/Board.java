package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class Board {
	private static ArrayList<Integer> fallingPieces;
	private static ArrayList<Integer> raisingPieces;
	private static ArrayList<Integer> chainScores;
	
	private static String[] pieces = { " . ",
		" 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " ? ",
		" 1)", " 2)", " 3)", " 4)", " 5)", " 6)", " 7)", " ?)",
		"(1)", "(2)", "(3)", "(4)", "(5)", "(6)", "(7)", "(?)",
	};
	
	private int level;
	private int piecesLeft;
	private int fallingIndex;
	private int raisingIndex;
	
	private int score;
	private int scoreDelta;
	private int chainIndex;
	private boolean gameOver;
	
	private int maxChain;
	private int clearedCount;
	private int lastFallingPiece;
	
	private ArrayList<Integer> drops = new ArrayList<Integer>();
	
	// board index layout:
	// [0][0] [0][1] ... [0][6]
	// [1][0] [1][1] ... [1][6]
	//   ..     ..   ...   ..
	// [6][0] [6][1] ... [6][6]
	private int[][] values  = new int[7][7];
	private int[][] indexes = new int[7][7];
	
	private boolean[][] hits = new boolean[7][7];
	
	public Board() throws FileNotFoundException {
		if (fallingPieces == null) fallingPieces = readIntegers(new File("data", "fallingPieces.txt"));
		if (raisingPieces == null) raisingPieces = readIntegers(new File("data", "raisingPieces.txt"));
		if (chainScores   == null) chainScores   = readIntegers(new File("data", "chainScores.txt"));
		
		levelUp();
	}
	
	public boolean isGameOver() { return gameOver; }
	public Sequence getSequence() { return new Sequence(drops, score); }
	public int getScore() { return score; }
	public int getLength() { return fallingIndex; }
	public int getClearedCount() { return clearedCount; }
	
	public void drop(int column) {
		if (values[0][column-1] != 0)
			gameOver = true;
		
		if (gameOver) return;
		
		drops.add(column--);
		
		values[0][column] = nextFallingPiece();
		indexes[0][column] = fallingIndex;
		chainIndex = 0;
		
		applyGravity(column);
		burnDown();
		
		if (piecesLeft == 0) {
			score += 7000;
			levelUp();
			burnDown();
		}
	}
	
	public void drop(int[] columns) {
		for (int column : columns) {
			drop(column);
		}
	}
	
	public void drop(Collection<Integer> columns) {
		for (int column : columns) {
			drop(column);
		}
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		int first = drops.size() > 0 ? drops.get(drops.size()-1) : 0;
		builder.append(drops.size() + " (" + pieces[lastFallingPiece] + "@" + first + ") " + drops + "\n");
		builder.append("level:" + level + " piecesLeft:" + piecesLeft + " gameOver:" + gameOver + " score:" + score + " maxChain:" + maxChain + " clearedCount:" + clearedCount + "\n");
		
		for (int y = 0; y < 7; ++y) {
			for (int x = 0; x < 7; ++x) {
				builder.append(" " + pieces[values[y][x]]);
			}
			
			builder.append("\t");
			
			for (int x = 0; x < 7; ++x) {
				builder.append("\t" + indexes[y][x]);
			}
			
			builder.append("\n");
		}
		
		return builder.toString();
	}
	
	private void burnDown() {
		if (gameOver) return;
		
		while (checkColumnHits() | checkRowHits()) {
			increaseChain();
			explodeHits();
			
			if (!applyGravity())
				boardCleared();
		}
	}
	
	private boolean checkRowHits() {
		boolean hit = false;
		
		for (int y = 6; y >= 0; --y) {
			int x1 = 0;
			int x2 = 0;
			
			while (x2 < 7) {
				for (x1 = x2; x1 < 7 && values[y][x1] == 0; ++x1);
				
				if (x1 == 7) break;
				
				for (x2 = x1; x2 < 7 && values[y][x2] != 0; ++x2);
				
				int l = x2 - x1;
				
				for (int x = x1; x < x2; ++x) {
					if (values[y][x] == l) {
						hits[y][x] = true;
						hit = true;
					}
				}
			}
		}
		
		return hit;
	}

	private boolean checkColumnHits() {
		boolean hit = false;
		
		for (int x = 0; x < 7; ++x) {
			int y2 = 6;
			
			while (y2 >= 0 && values[y2][x] != 0)
				--y2;
			
			int l = 6 - y2;
			
			for (int y = 6; y > y2; --y) {
				if (values[y][x] == l) {
					hits[y][x] = true;
					hit = true;
				}
			}
		}
		
		return hit;
	}
	
	private void increaseChain() {
		if (chainIndex >= chainScores.size()) {
			System.out.println("New max chain found!\n" + this);
			scoreDelta = 0;
		} else {
			scoreDelta = chainScores.get(chainIndex);
		}
		
		++chainIndex;
		
		if (maxChain < chainIndex)
			maxChain = chainIndex;
		
	}
	
	private void explodeHits() {
		for (int y = 0; y < 7; ++y) {
			for (int x = 0; x < 7; ++x) {
				if (hits[y][x]) {
					hits[y][x] = false;
					
					values[y][x] = 0;
					indexes[y][x] = 0;
					
					score += scoreDelta;
					
					boolean unknownBonus = false;
					
					if (y > 0 && values[y-1][x] > 8) {
						values[y-1][x] -= 8;
						if (unknownBonus && values[y-1][x] == 8) score += 100000000;
					}
					if (y < 6 && values[y+1][x] > 8) {
						values[y+1][x] -= 8;
						if (unknownBonus && values[y+1][x] == 8) score += 100000000;
					}
					if (x > 0 && values[y][x-1] > 8) {
						values[y][x-1] -= 8;
						if (unknownBonus && values[y][x-1] == 8) score += 100000000;
					}
					if (x < 6 && values[y][x+1] > 8) {
						values[y][x+1] -= 8;
						if (unknownBonus && values[y][x+1] == 8) score += 100000000;
					}
				}
			}
		}
	}
	
	private boolean applyGravity(int x) {
		int y1 = 6;
		
		for (int y2 = 6; y2 >=0; --y2) {
			if (values[y2][x] != 0) {
				if (y1 != y2) {
					values[y1][x] = values[y2][x];
					indexes[y1][x] = indexes[y2][x];
				}
				--y1;
			}
		}
		
		if (y1 == 6) return false;
		
		for (; y1 >= 0; --y1) {
			values[y1][x] = 0;
			indexes[y1][x] = 0;
		}
		
		return true;
	}
	
	private boolean applyGravity() {
		boolean found = false;
		
		for (int x = 0; x < 7; ++x) {
			if (applyGravity(x))
				found = true;;
		}
		
		return found;
	}
	
	private void boardCleared() {
		score += 70000;
		++clearedCount;
		
		if (chainIndex > 21)
			System.out.println("Board Cleared! (chain " + chainIndex + ")\n" + this);
	}
	
	private void levelUp() {
		++level;
		piecesLeft = Math.max(31 - level, 5);
		
		for (int x = 0; x < 7; ++x) {
			if (values[0][x] != 0)
				gameOver = true;
			
			for (int y = 0; y < 6; ++y) {
				values[y][x] = values[y+1][x];
				indexes[y][x] = indexes[y+1][x];
			}
			
			values[6][x] = nextRaisingPiece();
			indexes[6][x] = -raisingIndex;
		}
	}
	
	private static ArrayList<Integer> readIntegers(File file) throws FileNotFoundException {
		ArrayList<Integer> list = new ArrayList<Integer>();

		Scanner scanner = new Scanner(file);
		while (scanner.hasNextInt())
			list.add(scanner.nextInt());
		
		return list;
	}

	private int nextFallingPiece() {
		--piecesLeft;
		
		return lastFallingPiece = getPiece(fallingPieces, fallingIndex++);
	}
	
	private int nextRaisingPiece() {
		return getPiece(raisingPieces, raisingIndex++);
	}
	
	private static int getPiece(ArrayList<Integer> list, int index) {
		if (index >= list.size()) return 24;
		
		return list.get(index);
	}
}
