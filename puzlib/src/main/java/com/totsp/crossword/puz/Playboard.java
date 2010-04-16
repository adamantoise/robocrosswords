package com.totsp.crossword.puz;

import java.io.Serializable;
import java.util.HashMap;

public class Playboard implements Serializable {

    private Position highlightLetter = new Position(0, 0);
    private Puzzle puzzle;
    private Box[][] boxes;
    private boolean across = true;
    private boolean skipCompletedLetters;

    private String responder;

    public boolean isSkipCompletedLetters() {
        return skipCompletedLetters;
    }

    public void setSkipCompletedLetters(boolean skipCompletedLetters) {
        this.skipCompletedLetters = skipCompletedLetters;
    }
    private boolean showErrors;
    private HashMap<Integer, Position> acrossWordStarts = new HashMap<Integer, Position>();
    private HashMap<Integer, Position> downWordStarts = new HashMap<Integer, Position>();

    public Playboard(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.highlightLetter = new Position(0, 0);
        this.boxes = new Box[puzzle.getBoxes()[0].length][puzzle.getBoxes().length];

        for (int x = 0; x < puzzle.getBoxes().length; x++) {
            for (int y = 0; y < puzzle.getBoxes()[x].length; y++) {
                boxes[y][x] = puzzle.getBoxes()[x][y];
                if (boxes[y][x] != null && boxes[y][x].isAcross()) {
                    acrossWordStarts.put(boxes[y][x].getClueNumber(), new Position(
                            y, x));
                }
                if (boxes[y][x] != null && boxes[y][x].isDown()) {
                    downWordStarts.put(boxes[y][x].getClueNumber(), new Position(y,
                            x));
                }
            }
        }
    }

    public Puzzle getPuzzle() {
        return this.puzzle;
    }

    public void setAcross(boolean across) {
        this.across = across;
    }

    public boolean isAcross() {
        return across;
    }

    public Box[][] getBoxes() {
        return this.boxes;
    }

    public Clue getClue() {
        Clue c = new Clue();
        try {
            Position start = this.getCurrentWordStart();
            c.number = this.getBoxes()[start.across][start.down].getClueNumber();
            c.hint = this.across ? this.puzzle.findAcrossClue(c.number)
                    : this.puzzle.findDownClue(c.number);
        } catch (Exception e) {
        }
        return c;
    }

    public Word getCurrentWord() {
        Word w = new Word();
        w.start = this.getCurrentWordStart();
        w.across = this.across;
        w.length = this.getWordRange();

        return w;
    }

    public Box getCurrentBox() {
        return this.boxes[this.highlightLetter.across][this.highlightLetter.down];
    }

    public Position getCurrentWordStart() {
        if (this.isAcross()) {
            int col = this.highlightLetter.across;
            Box b = null;

            while (b == null) {
                try {
                    if ((boxes[col][this.highlightLetter.down] != null)
                            && boxes[col][this.highlightLetter.down].isAcross()) {

                        b = boxes[col][this.highlightLetter.down];

                    } else {
                        col--;
                    }
                } catch (Exception e) {
                    break;
                }
            }

            return new Position(col, this.highlightLetter.down);
        } else {
            int row = this.highlightLetter.down;
            Box b = null;

            while (b == null) {
                try {
                    if ((boxes[this.highlightLetter.across][row] != null)
                            && boxes[this.highlightLetter.across][row].isDown()) {
                        b = boxes[this.highlightLetter.across][row];
                    } else {
                        row--;
                    }
                } catch (Exception e) {
                    break;
                }
            }

            return new Position(this.highlightLetter.across, row);
        }
    }

    public Word setHighlightLetter(Position highlightLetter) {
        Word w = this.getCurrentWord();

        if (highlightLetter.equals(this.highlightLetter)) {
            this.toggleDirection();
        } else {
            if ((this.boxes.length > highlightLetter.across)
                    && (this.boxes[highlightLetter.across].length > highlightLetter.down)
                    && (this.boxes[highlightLetter.across][highlightLetter.down] != null)) {
                this.highlightLetter = highlightLetter;
            }
        }

        return w;
    }

    public Position getHighlightLetter() {
        return highlightLetter;
    }

    public boolean isShowErrors() {
        return this.showErrors;
    }

    public int getWordRange() {
        Position start = this.getCurrentWordStart();

        if (this.isAcross()) {
            int col = start.across;
            Box b = null;

            do {
                b = null;

                int checkCol = col + 1;

                try {
                    col++;
                    b = this.getBoxes()[checkCol][start.down];
                } catch (RuntimeException e) {
                }
            } while (b != null);

            return col - start.across;
        } else {
            int row = start.down;
            Box b = null;

            do {
                b = null;

                int checkRow = row + 1;

                try {
                    row++;
                    b = this.getBoxes()[start.across][checkRow];
                } catch (RuntimeException e) {
                }
            } while (b != null);

            return row - start.down;
        }
    }

    public Word deleteLetter() {
        try {
            this.boxes[this.highlightLetter.across][this.highlightLetter.down].setResponse(' ');
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        return this.previousLetter();
    }

    public Word moveDown() {
        return this.moveDown(false);
    }

    public Word moveDown(boolean skipFilled) {
        Word w = this.getCurrentWord();
        Box b = null;
        int checkRow = this.highlightLetter.down;

        while ((b == null) && (checkRow < (this.getBoxes().length - 1))) {
            try {
                b = this.getBoxes()[this.highlightLetter.across][++checkRow];
                if (skipFilled && b != null && b.getResponse() != ' ' && checkRow < w.start.down + w.length) {
                    b = null;
                }
            } catch (RuntimeException e) {
            }
        }

        this.highlightLetter = new Position(this.highlightLetter.across,
                checkRow);

        return w;
    }

    public Word moveLeft() {
        Word w = this.getCurrentWord();
        Box b = null;
        int checkCol = this.highlightLetter.across;

        while ((b == null) && (checkCol > 0)) {
            try {
                b = this.getBoxes()[--checkCol][this.highlightLetter.down];
            } catch (RuntimeException e) {
                this.highlightLetter = new Position(checkCol++,
                        this.highlightLetter.down);
                return this.moveRight();
            }
        }

        this.highlightLetter = new Position(checkCol, this.highlightLetter.down);

        return w;
    }

    public Word moveRight() {
        return moveRight(false);
    }

    public Word moveRight(boolean skipFilled) {
        Word w = this.getCurrentWord();
        Box b = null;
        int checkCol = this.highlightLetter.across;

        while ((b == null)
                && (checkCol < (this.getBoxes()[this.highlightLetter.across].length - 1))) {
            try {
                b = this.getBoxes()[++checkCol][this.highlightLetter.down];
                if (skipFilled && b != null && b.getResponse() != ' ' && checkCol < w.start.across + w.length) {
                    b = null;
                }
            } catch (RuntimeException e) {
                return w;
            }
        }

        this.highlightLetter = new Position(checkCol, this.highlightLetter.down);

        return w;
    }

    public Word movieUp() {
        Word w = this.getCurrentWord();
        Box b = null;
        int checkRow = this.highlightLetter.down;

        while ((b == null) && (checkRow > 0)) {
            try {
                b = this.getBoxes()[this.highlightLetter.across][--checkRow];
            } catch (RuntimeException e) {
                this.highlightLetter = new Position(this.highlightLetter.across,
                        checkRow++);
                return this.moveDown();
            }
        }

        this.highlightLetter = new Position(this.highlightLetter.across,
                checkRow);

        return w;
    }

    public Word nextLetter() {
        if (across) {
            return this.moveRight(this.skipCompletedLetters);
        } else {
            return this.moveDown(this.skipCompletedLetters);
        }
    }

    public Word playLetter(char letter) {
        Box b = this.boxes[this.highlightLetter.across][this.highlightLetter.down];
        if(b == null){
        	return null;
        }
        b.setResponse(letter);
        b.setResponder(this.responder);
        return this.nextLetter();
    }

    public Word previousLetter() {
        if (across) {
            return this.moveLeft();
        } else {
            return this.movieUp();
        }
    }

    public void revealLetter() {
        Box b = this.boxes[this.highlightLetter.across][this.highlightLetter.down];

        if ((b != null) && (b.getSolution() != b.getResponse())) {
            b.setCheated(true);
            b.setResponse(b.getSolution());
        }
    }

    public void revealPuzzle() {
        for (Box[] row : this.boxes) {
            for (Box b : row) {
                if ((b != null) && (b.getSolution() != b.getResponse())) {
                    b.setCheated(true);
                    b.setResponse(b.getSolution());
                }
            }
        }
    }

    public void revealWord() {
        Position oldHighlight = this.highlightLetter;
        Word w = this.getCurrentWord();
        this.highlightLetter = w.start;

        for (int i = 0; i < w.length; i++) {
            revealLetter();
            nextLetter();
        }

        this.highlightLetter = oldHighlight;
    }

    public Word toggleDirection() {
        Word w = this.getCurrentWord();
        this.across = !across;

        return w;
    }

    public void toggleShowErrors() {
        this.showErrors = !showErrors;
    }

    public void jumpTo(int clueIndex, boolean across) {
        this.across = across;
        if (across) {
            this.highlightLetter = (this.acrossWordStarts.get(this.puzzle.getAcrossCluesLookup()[clueIndex]));
        } else {
            this.highlightLetter = (this.downWordStarts.get(this.puzzle.getDownCluesLookup()[clueIndex]));
        }
    }

    public Box[] getCurrentWordBoxes() {
        Word currentWord = this.getCurrentWord();
        Box[] result = new Box[currentWord.length];
        for (int i = 0; i < result.length; i++) {
            Position pos = new Position(currentWord.start.across,
                    currentWord.start.down);
            if (currentWord.across) {
                pos.across += i;
            } else {
                pos.down += i;
            }
            result[i] = this.boxes[pos.across][pos.down];
        }
        return result;
    }

    public Clue[] getDownClues() {
        Clue[] clues = new Clue[puzzle.getDownClues().length];
        for (int i = 0; i < clues.length; i++) {
            clues[i] = new Clue();
            clues[i].hint = puzzle.getDownClues()[i];
            clues[i].number = puzzle.getDownCluesLookup()[i];
        }
        return clues;
    }

    public Clue[] getAcrossClues() {
        Clue[] clues = new Clue[puzzle.getAcrossClues().length];
        for (int i = 0; i < clues.length; i++) {
            clues[i] = new Clue();
            clues[i].hint = puzzle.getAcrossClues()[i];
            clues[i].number = puzzle.getAcrossCluesLookup()[i];
        }
        return clues;
    }

    /**
     * @return the responder
     */
    public String getResponder() {
        return responder;
    }

    /**
     * @param responder the responder to set
     */
    public void setResponder(String responder) {
        this.responder = responder;
    }

    public static class Clue {

        public String hint;
        public int number;

        @Override
        public int hashCode() {
            return hint.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Clue other = (Clue) obj;
            if ((this.hint == null) ? (other.hint != null) : !this.hint.equals(other.hint)) {
                return false;
            }
            if (this.number != other.number) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return number + ". " + hint;
        }
    }

    public static class Position {

        public int across;
        public int down;

        public Position(int across, int down) {
            this.down = down;
            this.across = across;
        }

        @Override
        public boolean equals(Object o) {
            if (o.getClass() != this.getClass()) {
                return false;
            }

            Position p = (Position) o;

            return ((p.down == this.down) && (p.across == this.across));
        }

        @Override
        public int hashCode() {
            return this.across ^ this.down;
        }

        @Override
        public String toString() {
            return "[" + this.across + " x " + this.down + "]";
        }
    }

    public static class Word {

        public Position start;
        public boolean across;
        public int length;

        public boolean checkInWord(int across, int down) {
            int ranging = this.across ? across : down;
            boolean offRanging = this.across ? (down == start.down)
                    : (across == start.across);

            int startPos = this.across ? start.across : start.down;

            return (offRanging && (startPos <= ranging) && ((startPos + length) > ranging));
        }

        @Override
        public boolean equals(Object o) {
            if (o.getClass() != Word.class) {
                return false;
            }

            Word check = (Word) o;
            return check.start.equals(this.start) && check.across == this.across && check.length == this.length;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + (this.start != null ? this.start.hashCode() : 0);
            hash = 29 * hash + (this.across ? 1 : 0);
            hash = 29 * hash + this.length;
            return hash;
        }
    }
}
