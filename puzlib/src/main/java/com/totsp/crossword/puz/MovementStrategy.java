package com.totsp.crossword.puz;

import java.util.Arrays;

import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;

public interface MovementStrategy {

	public static final MovementStrategy MOVE_NEXT_ON_AXIS = new MovementStrategy() {

		public Word move(Playboard board, boolean skipCompletedLetters) {
			if (board.isAcross()) {
				return board.moveRight(skipCompletedLetters);
			} else {
				return board.moveDown(skipCompletedLetters);
			}
		}

		public Word back(Playboard board) {
	        if (board.isAcross()) {
	            return board.moveLeft();
	        } else {
	            return board.moveUp(false);
	        }
		}
	};

	public static final MovementStrategy STOP_ON_END = new MovementStrategy() {

		public Word move(Playboard board, boolean skipCompletedLetters) {
			// This is overly complex, but I am trying to save calls to heavy
			// methods on the board.

			Position p = board.getHighlightLetter();
			Word w = board.getCurrentWord();
			if ((w.across && p.across == w.start.across + w.length - 1)
					|| (!w.across && p.down == w.start.down + w.length - 1)) {
				return w;
			} else {
				MOVE_NEXT_ON_AXIS.move(board,skipCompletedLetters);
				Word newWord = board.getCurrentWord();
				if (newWord.equals(w)) {
					return w;
				} else {
					board.setHighlightLetter(p);
					return w;
				}
			}
		}

		public Word back(Playboard board) {
			Word w = board.getCurrentWord();
			Position p = board.getHighlightLetter();
			if(!p.equals(w.start)){
				MOVE_NEXT_ON_AXIS.back(board);
			}
			return w;
		}

	};

	public static final MovementStrategy MOVE_NEXT_CLUE = new MovementStrategy() {

		public Word move(Playboard board, boolean skipCompletedLetters) {
			Position p = board.getHighlightLetter();
			Word w = board.getCurrentWord();
			
			if ((w.across && p.across == w.start.across + w.length - 1)
					|| (!w.across && p.down == w.start.down + w.length - 1)) {
				try {
					int currentClueNumber = board.getBoxes()[w.start.across][w.start.down]
							.getClueNumber();
					if (w.across) {
						int nextClueIndex = Arrays.binarySearch(board
								.getPuzzle().getAcrossCluesLookup(),
								currentClueNumber);
						System.out.println("Current Clue Index: "
								+ nextClueIndex++ + " " + w.across);
						board.jumpTo(nextClueIndex, w.across);
					} else {
						int nextClueIndex = Arrays.binarySearch(board
								.getPuzzle().getDownCluesLookup(),
								currentClueNumber);
						System.out.println("Current Clue Index: "
								+ nextClueIndex++ + " " + w.across);

						board.jumpTo(nextClueIndex, w.across);
					}
					return w;

				} catch (Exception e) {
					board.setHighlightLetter(p);
					return w;
				}
			} else {
				MOVE_NEXT_ON_AXIS.move(board,
						skipCompletedLetters);
				Word newWord = board.getCurrentWord();
				if (newWord.equals(w)) {
					return w;
				} else {
					Position end = new Position(w.start.across + (w.across ? w.length -1: 0),
							w.start.down + (w.across? 0 : w.length -1));
					board.setHighlightLetter(end);
					this.move(board, skipCompletedLetters);
					return w;
				}
			}
		}

		public Word back(Playboard board) {
			Position p = board.getHighlightLetter();
			Word w = board.getCurrentWord();
			if ((w.across && p.across == w.start.across)
					|| (!w.across && p.down == w.start.down)) {
				try {
					int currentClueNumber = board.getBoxes()[w.start.across][w.start.down]
							.getClueNumber();
					if (w.across) {
						int nextClueIndex = Arrays.binarySearch(board
								.getPuzzle().getAcrossCluesLookup(),
								currentClueNumber);
						System.out.println("Current Clue Index: "
								+ nextClueIndex-- + " " + w.across);
						board.jumpTo(nextClueIndex, w.across);
						Word newWord = board.getCurrentWord();
						Position newPos = board.getHighlightLetter();
						newPos.across = newWord.start.across + newWord.length -1;
					} else {
						int nextClueIndex = Arrays.binarySearch(board
								.getPuzzle().getDownCluesLookup(),
								currentClueNumber);
						System.out.println("Current Clue Index: "
								+ nextClueIndex-- + " " + w.across);

						board.jumpTo(nextClueIndex, w.across);
						Word newWord = board.getCurrentWord();
						Position newPos = board.getHighlightLetter();
						newPos.down = newWord.start.down + newWord.length -1;
					}
					
					return w;

				} catch (Exception e) {
					board.setHighlightLetter(p);
					return w;
				}
			} else {
				Word newWord = MOVE_NEXT_ON_AXIS.back(board);
				if (newWord.equals(w)) {
					return w;
				} else {
					return board.setHighlightLetter(p);
				}
			}
		}
		
	};

	public static final MovementStrategy MOVE_PARALLEL_WORD = new MovementStrategy() {

		public Word move(Playboard board, boolean skipCompletedLetters) {
			Word w = board.getCurrentWord();
			Position p = board.getHighlightLetter();
			

			if ((w.across && p.across == w.start.across + w.length - 1)
					|| (!w.across && p.down == w.start.down + w.length - 1)) {
				//board.setHighlightLetter(w.start);
				Word newWord;
				if (w.across) {
					board.moveDown();
					while(board.getClue().hint == null && board.getHighlightLetter().down < board.getBoxes()[0].length){
						board.moveDown();
					}
					if(board.getClue().hint == null){
						board.toggleDirection();
					}
					newWord = board.getCurrentWord();
				} else {
					board.moveRight();
					while(board.getClue().hint == null && board.getHighlightLetter().across < board.getBoxes().length){
						board.moveRight();
					}
					if(board.getClue().hint == null){
						board.toggleDirection();
					}
					newWord = board.getCurrentWord();
					
				}
				if (!newWord.equals(w)) {
					board.setHighlightLetter(newWord.start);
					board.setAcross(w.across);
				}
			
			} else {
				MOVE_NEXT_ON_AXIS.move(board,
						skipCompletedLetters);
				Word newWord = board.getCurrentWord();
				if (!newWord.equals(w)) {
					Position end = new Position(w.start.across + (w.across ? w.length -1: 0),
							w.start.down + (w.across? 0 : w.length -1));
					board.setHighlightLetter(end);
					this.move(board, skipCompletedLetters);
				}
			}

			return w;
		}

		public Word back(Playboard board) {
			Word w = board.getCurrentWord();
			Position p = board.getHighlightLetter();
			if ((w.across && p.across == w.start.across)
					|| (!w.across && p.down == w.start.down)) {
				//board.setHighlightLetter(w.start);
				Word newWord;
				Position lastPos = null;
				if (w.across) {
					board.moveUp();
					while(!board.getHighlightLetter().equals(lastPos) && board.getClue().hint == null && board.getHighlightLetter().down < board.getBoxes()[0].length){
						lastPos = board.getHighlightLetter();
						board.moveUp();
						
					}
					if(board.getClue().hint == null){
						board.toggleDirection();
					}
					newWord = board.getCurrentWord();
				} else {
					board.moveLeft();
					while(!board.getHighlightLetter().equals(lastPos) && board.getClue().hint == null && board.getHighlightLetter().across < board.getBoxes().length){
						lastPos = board.getHighlightLetter();
						board.moveLeft();
					}
					if(board.getClue().hint == null){
						board.toggleDirection();
					}
					newWord = board.getCurrentWord();
					
				}
				if (!newWord.equals(w)) {
					
					Position newPos = new Position(newWord.start.across, newWord.start.down);
					newPos.across+= newWord.across ? newWord.length -1 : 0;
					newPos.down+= newWord.across ? 0 : newWord.length -1;
						
					
					board.setHighlightLetter(newPos);
					board.setAcross(w.across);
				}
			
			} else {
				Word newWord = MOVE_NEXT_ON_AXIS.back(board);
				if (!newWord.equals(w)) {
					board.setHighlightLetter(newWord.start);
				}
			}

			return w;
		}

	};

	Word move(Playboard board, boolean skipCompletedLetters);
	
	Word back(Playboard board);

}
