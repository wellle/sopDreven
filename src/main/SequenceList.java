package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class SequenceList {
	private ArrayList<Sequence> sequences = new ArrayList<Sequence>(700);
	private int maxImprovement;
	private static Random random = new Random();
	
	public SequenceList() {
		add(new Sequence(new ArrayList<Integer>(), 0));
	}
	
	public void add(Sequence sequence) {
		int length = sequence.getDrops().size();
		
		if (length >= sequences.size()) {
			sequences.add(sequence);
			sequence.copyDrops();
			return;
		}
		
		int improvement = sequence.getScore() - sequences.get(length).getScore();
		if (improvement >= 0) {
			sequences.set(length, sequence);
			sequence.copyDrops();
			
			if (maxImprovement < improvement)
				maxImprovement = improvement;
		}
	}
	
	public int getSize() { return sequences.size(); }
	public Sequence getSequence(int index) { return sequences.get(index); }
	
	public void writeToFile(boolean printImprovements) throws IOException {
		if (maxImprovement == 0) {
			conditionalPrint(printImprovements, ".");
			return;
		} else {
			conditionalPrint(printImprovements, "\n" + getTime() + ": " + maxImprovement);
			maxImprovement = 0;
		}
		
		File seqFile = File.createTempFile("sequences/", ".txt", new File("sequences"));
		File curFile = new File("sequences", "current.txt");
		
		BufferedWriter seqWriter = new BufferedWriter(new FileWriter(seqFile));
		BufferedWriter curWriter = new BufferedWriter(new FileWriter(curFile));
		
		curWriter.write(seqFile.getName());
		curWriter.close();

		for (Sequence sequence : sequences) {
			for (int drop : sequence.getDrops())
				seqWriter.write(" " + drop);
			
			seqWriter.write("\n");
		}
		
		seqWriter.close();
	}
	
	public void readFromFile(String fileName) throws FileNotFoundException {
		Scanner fileScanner = new Scanner(new File("sequences", fileName)), lineScanner;
		
		Board board;
		while (fileScanner.hasNextLine()) {
			lineScanner = new Scanner(fileScanner.nextLine());
			
			board = new Board();
			while (lineScanner.hasNext()) {
				board.drop(lineScanner.nextInt());
				add(board.getSequence());
			}
		}
	}
	
	public void readFromFile() throws IOException {
		File file = new File("sequences", "current.txt");
		if (!file.exists()) {
			file.createNewFile();
			return;
		}
		
		Scanner scanner = new Scanner(file);
		while (scanner.hasNextLine())
			readFromFile("sequences/" + scanner.nextLine());
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("[\n");
		
		for (int i = 0; i < sequences.size(); ++i)
			builder.append("    " + i + " -> " + sequences.get(i) + "\n");
		
		builder.append("]");
		
		return builder.toString();
	}
	
	public void printScores() {
		System.out.println("scores:");
		
		for (Sequence sequence : sequences)
			System.out.print(sequence.getScore() + "\t");
		
		System.out.println();
	}
	
	private String getTime() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
	
	private void conditionalPrint(boolean condition, String string) {
		if (condition) {
			System.out.print(string);
		}
	}
}
