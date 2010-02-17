package com.totsp.crossword.puz;

import java.util.Arrays;
import java.util.Date;


public class Puzzle {
    String author;
    String copyright;
    String fileMagic;
    String notes;
    String title;
    String versionString; // 4bytes
    String[] acrossClues;
    Integer[] acrossCluesLookup;
    String[] downClues;
    Integer[] downCluesLookup;
    byte[] maskedHighChecksums; //4bytes
    byte[] maskedLowChecksums; //4bytes
    byte[] reserved20; //0xC bytes;
    int numberOfClues; //2 bytes;
    short cibChecksum;
    short fileChecksum;
    short reserved1C; //2bytes
    short unknown; //2bytes
    short unknown30; //2bytes
    short unknown32; //2bytes;
    private Box[][] boxes;
    private //one byte;
    int height;
    private int width; //one byte;
    String[] rawClues;
    String source;
    Date date = new Date();
   

    public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getPercentComplete() {
		int total = 0;
		int correct = 0;
		for(int x=0; x < boxes.length; x++){
			for(int y=0; y < boxes[x].length; y++){
				if(boxes[x][y] != null){
					total++;
					if(boxes[x][y].response == boxes[x][y].solution){
						correct++;
					}
				}
			}
		}
		return (correct * 100)/(total);
	}


	public void setBoxes(Box[][] boxes) {
        this.boxes = boxes;

        int clueCount = 1;

        for (int x = 0; x < boxes.length; x++) {
            boolean tickedClue = false;

            for (int y = 0; y < boxes[x].length; y++) {
                if (boxes[x][y] == null) {
                    continue;
                }

                if ((x == 0) || (boxes[x - 1][y] == null)) {
                    boxes[x][y].down = true;

                    if ((x == 0) || (boxes[x - 1][y] == null)) {
                        boxes[x][y].clueNumber = clueCount;
                        tickedClue = true;
                    }
                }

                if ((y == 0) || (boxes[x][y - 1] == null)) {
                    boxes[x][y].across = true;

                    if ((y == 0) || (boxes[x][y - 1] == null)) {
                        boxes[x][y].clueNumber = clueCount;
                        tickedClue = true;
                    }
                }

                if (tickedClue) {
                    clueCount++;
                    tickedClue = false;
                }
            }
        }
    }

    public Box[][] getBoxes() {
        return boxes;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Puzzle other = (Puzzle) obj;

        if (!Arrays.equals(acrossClues, other.acrossClues)) {
            System.out.println("acrossClues");

            return false;
        }

        if (!Arrays.equals(acrossCluesLookup, other.acrossCluesLookup)) {
        	System.out.println("acrossCluesLookup");
            return false;
        }

        if (author == null) {
            if (other.author != null) {
                return false;
            }
        } else if (!author.equals(other.author)) {
        	System.out.println("author");
            return false;
        }

        Box[][] b1 = boxes;
        Box[][] b2 = other.boxes;
        boolean boxEq = true;
        for(int x=0; x < b1.length; x++ ){
        	for(int y=0; y<b1[x].length; y++){
        		boxEq = boxEq ? (b1[x][y] == b2[x][y] || b1[x][y].equals(b2[x][y])) : boxEq;
        	}
        }
        if(!boxEq){
        	System.out.println("boxes");
        	return false;
        }

        if (cibChecksum != other.cibChecksum) {
            return false;
        }

        if (copyright == null) {
            if (other.copyright != null) {
                return false;
            }
        } else if (!copyright.equals(other.copyright)) {
            return false;
        }

        if (!Arrays.equals(downClues, other.downClues)) {
        	System.out.println("downClues");
            return false;
        }

        if (!Arrays.equals(downCluesLookup, other.downCluesLookup)) {
        	System.out.println("downCluesLookup");
            return false;
        }

        if (fileChecksum != other.fileChecksum) {
        	System.out.println("fileChecksum");
            return false;
        }

        if (fileMagic == null) {
            if (other.fileMagic != null) {
                return false;
            }
        } else if (!fileMagic.equals(other.fileMagic)) {
        	System.out.println("fileMagic");
            return false;
        }

        if (height != other.height) {
        	System.out.println("height");
            return false;
        }

       
        if (notes == null) {
            if (other.notes != null) {
                return false;
            }
        } else if (!notes.equals(other.notes)) {
        	System.out.println("notes");
            return false;
        }

        if (numberOfClues != other.numberOfClues) {
        	System.out.println("numberOfClues");
            return false;
        }

        if (reserved1C != other.reserved1C) {
            return false;
        }

        
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
        	System.out.println("title");
            return false;
        }

        if (unknown != other.unknown) {
            return false;
        }

        if (unknown30 != other.unknown30) {
            return false;
        }

        if (unknown32 != other.unknown32) {
            return false;
        }

        if (versionString == null) {
            if (other.versionString != null) {
                return false;
            }
        } else if (!versionString.equals(other.versionString)) {
            return false;
        }

        if (width != other.width) {
            return false;
        }

        return true;
    }

    public String findAcrossClue(int clueNumber) {
        return this.acrossClues[Arrays.binarySearch(this.acrossCluesLookup,
            clueNumber)];
    }

    public String findDownClue(int clueNumber) {
        return this.downClues[Arrays.binarySearch(this.downCluesLookup,
            clueNumber)];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Arrays.hashCode(acrossClues);
        result = (prime * result) + Arrays.hashCode(acrossCluesLookup);
        result = (prime * result) + ((author == null) ? 0 : author.hashCode());
        result = (prime * result) + Arrays.hashCode(boxes);
        result = (prime * result) + cibChecksum;
        result = (prime * result) +
            ((copyright == null) ? 0 : copyright.hashCode());
        result = (prime * result) + Arrays.hashCode(downClues);
        result = (prime * result) + Arrays.hashCode(downCluesLookup);
        result = (prime * result) + fileChecksum;
        result = (prime * result) +
            ((fileMagic == null) ? 0 : fileMagic.hashCode());
        result = (prime * result) + height;
        result = (prime * result) + Arrays.hashCode(maskedHighChecksums);
        result = (prime * result) + Arrays.hashCode(maskedLowChecksums);
        result = (prime * result) + ((notes == null) ? 0 : notes.hashCode());
        result = (prime * result) + numberOfClues;
        result = (prime * result) + reserved1C;
        result = (prime * result) + Arrays.hashCode(reserved20);
        result = (prime * result) + ((title == null) ? 0 : title.hashCode());
        result = (prime * result) + unknown;
        result = (prime * result) + unknown30;
        result = (prime * result) + unknown32;
        result = (prime * result) +
            ((versionString == null) ? 0 : versionString.hashCode());
        result = (prime * result) + width;

        return result;
    }
}
