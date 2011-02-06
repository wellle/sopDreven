package main;

import java.io.IOException;
import java.util.Random;

public class SequenceImprover {
	private static boolean printScoresOnRead = true;
	private static boolean printImprovements = true;
	
	/**
	 * @param args[0] read/write file
	 * @param args[1..] read files
	 */
	public static void main(String[] args) throws IOException {
		Random random = new Random();
		Board board;
		SequenceList sequenceList = new SequenceList();
		for (String fileName : args)
			sequenceList.readIndirect(fileName);
		
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
			if (sequenceList.printImprovements(printImprovements)) {
				for (int i = 1; i < args.length; ++i)
					sequenceList.readIndirect(args[i]);
				
				sequenceList.writeToFile(args[0], printImprovements);
			}
		}
	}
}
