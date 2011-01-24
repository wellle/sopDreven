package main;

import java.io.IOException;
import java.util.Random;

public class SequenceImprover {
	private static boolean printScoresOnRead = true;
	private static boolean printImprovements = true;
	
	public static void main(String[] args) throws IOException {
		Random random = new Random();
		Board board;
		SequenceList sequenceList = new SequenceList();
		sequenceList.readFromFile();
		
		if (printScoresOnRead)
			sequenceList.printScores();
		
		while (true) {
			for (int i = 0; i < sequenceList.getSize(); ++i) {
				Sequence sequence = sequenceList.getSequence(i);
				
				for (int j = 0; j < 100; ++j) {
					board = new Board();
					board.drop(sequence.getDrops());
					
					while (!board.isGameOver()) {
						board.drop(random.nextInt(7) + 1);
						sequenceList.add(board.getSequence());
					}
				}
			}
			sequenceList.writeToFile(printImprovements);
		}
	}
}
