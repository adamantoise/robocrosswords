package com.adamrosenfield.wordswithcrosses.net;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

public class PuzzleParsingHandler extends DefaultHandler {
	

	private static final Logger LOG = Logger.getLogger("PuzzleParsingHandler");

	private final DerStandardPuzzleMetadata pm;
	
	private ContentHandler currentDelegate;
	
	private PuzzleHandler cwtableHandler = new PuzzleHandler();
	private HintsHandler questHandler = new HintsHandler();

	public PuzzleParsingHandler(DerStandardPuzzleMetadata pm) {
		this.pm = pm;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (localName.equals("table") && hasClass(attributes, "cwtable")) {
			currentDelegate = cwtableHandler;
		} else if (localName.equals("div") && hasClass(attributes, "quest")) {
			currentDelegate = questHandler;
		} else if (currentDelegate != null) {
			currentDelegate.startElement(uri, localName, qName, attributes);
		}
	}


	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (currentDelegate != null) {
			currentDelegate.characters(ch, start, length);	
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (currentDelegate != null) {
			currentDelegate.endElement(uri, localName, qName);
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		Puzzle p = new Puzzle();
		
		p.setAuthor("derStandard.at");
		p.setCopyright("derStandard.at");
		p.setDate(pm.getDate());
		
		p.setTitle("Nr. " + pm.getId());
		
		cwtableHandler.saveTo(p);
		questHandler.saveTo(p);
		
		pm.setPuzzle(p);
	}

	private boolean hasClass(Attributes attributes, String clazz) {
		String aClass = attributes.getValue("class");
		if (aClass != null) {
			if (aClass.equals(clazz)) {
				return true;
			} else {
				String[] split = aClass.split("\\s");
				for (String s : split) {
					if (s.equals(clazz)) {
						return true;
					}
				}
			}
		} 

		return false;
	}
	
	
	
	private final class HintsHandler extends DefaultHandler {
		Map<Integer, String> horizontal = new HashMap<Integer, String>();
		Map<Integer, String> vertical = new HashMap<Integer, String>();
		Map<Integer, String> current;
		boolean inQuestItem = false;
		boolean inQuestItemText = false;
		StringBuilder questItemText = new StringBuilder();
		StringBuilder questItemNumber = new StringBuilder();

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (inQuestItem && inQuestItemText) {
				questItemText.append("<"+localName+">");
			} else if (localName.equals("div")) {
				if (hasClass(attributes, "hquest")) {
					current = horizontal;				
				} else if (hasClass(attributes, "vquest")) {
					current = vertical;				
				} else if (hasClass(attributes, "questitem")) {
					inQuestItem = true;
				}
			} 

		}

		public void saveTo(Puzzle p) {
			String[] acrossClues = new String[horizontal.size()];
			Integer[] acrossCluesLookup = new Integer[horizontal.size()];
			copyOver(horizontal, acrossClues, acrossCluesLookup);
			
			String[] downClues = new String[vertical.size()];
			Integer[] downCluesLookup = new Integer[vertical.size()];
			copyOver(vertical, downClues, downCluesLookup);

			p.setAcrossClues(acrossClues);
			p.setAcrossCluesLookup(acrossCluesLookup);
			p.setDownClues(downClues);
			p.setDownCluesLookup(downCluesLookup);
			
			p.setNumberOfClues(horizontal.size() + vertical.size());
			
			p.setRawClues(makeRawClues(p.getBoxes(), horizontal, vertical));
		}

		private String[] makeRawClues(Box[][] boxes, Map<Integer, String> horizontal,  Map<Integer, String> vertical) {
			String[] ret = new String[horizontal.size() + vertical.size()];
			int i=0;
			
			for (int r = 0; r < boxes.length; r++) {
         for (int c = 0; c < boxes[r].length; c++) {
             if (boxes[r][c] == null) {
                 continue;
             }

             Box box = boxes[r][c];
             int clueNumber = box.getClueNumber();
             
             if (clueNumber != 0) {
            	 if (box.isAcross()) {
            		 ret[i++] = horizontal.get(clueNumber);
            	 }

            	 if (box.isDown()) {
            		 ret[i++] = vertical.get(clueNumber);
            	 }
             }
         }
     }

			
			return ret;
		}

		private void copyOver(Map<Integer, String> map, String[] clues, Integer[] cluesLookup) {
			int count = 0;
			for (Integer key : new TreeSet<Integer>(map.keySet())) {
				String value = map.get(key);
				clues[count] = value.trim();
				cluesLookup[count] = key;
				++count;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("div")) {
				if (inQuestItem) {
					int number = Integer.parseInt(questItemNumber.toString().trim());
					String text = questItemText.toString();
					
					current.put(number, text);
					
					inQuestItem = false;
					inQuestItemText = false;
					questItemText.setLength(0);
					questItemNumber.setLength(0);
				}
			} else if (inQuestItem && !inQuestItemText && "em".equals(localName)) {
				inQuestItemText = true;
			} else if (inQuestItem && inQuestItemText) {
				questItemText.append("</"+localName+">");
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (inQuestItem) {
				if (inQuestItemText) {
					questItemText.append(ch, start, length);
				} else {
					questItemNumber.append(ch, start, length);
				}
			}
		}
	}
	
	
	private final class PuzzleHandler extends DefaultHandler {
		int width = 0;
		int height = 0;
		
		boolean[][] editable = new boolean[50][50];
		int[][] number = new int[50][50];
		
		int row = -1;
		int col = -1;
		
		boolean inWordNumber = false;
		StringBuilder wordNumber = new StringBuilder();
		
		public void saveTo(Puzzle p) {
			Box[] boxes = new Box[width * height];
			
			for (int y = 0; y<height; y++) {
				for (int x = 0; x<width; x++) {
					
					if (editable[x][y]) {
						Box b = new Box();
						boxes[y * width + x] =  b;

						int nr = number[x][y];
						if (nr != 0) {
							b.setClueNumber(nr);
						
							boolean across = !isEditable(x-1, y) && isEditable(x+1, y);
							boolean down = !isEditable(x, y-1) && isEditable(x, y+1);

							b.setAcross(across);
							b.setDown(down);
						}
					}
				}
			}
			
			p.setBoxesList(boxes);
			
			p.setWidth(width);
			p.setHeight(height);
		}
		
		private boolean isEditable(int x, int y) {
			if (x < 0 || y < 0)
				return false;
			
			if (x >= width) {
				return false;
			}
			
			if (y >= height) {
				return false;
			}

			return editable[x][y];
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("tr".equals(localName) && hasClass(attributes, "cwrow")) {
				row += 1;
				col = -1;
				
				if (height <= row) {
					height = row + 1;
				}
				
			} else if ("div".equals(localName) && hasClass(attributes, "cwwordcontainer")) {
				col += 1;
				
				if (width <= col) {
					width = col + 1;
				}
			}
			
			if ("div".equals(localName) && hasClass(attributes, "cweditable")) {
				editable[col][row] = true;
			} else if ("div".equals(localName) && hasClass(attributes, "cwwordnumber")) {
				inWordNumber = true;
				wordNumber.setLength(0);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("div".equals(localName) && inWordNumber) {
				int nr = Integer.parseInt(wordNumber.toString().trim());
				number[col][row] = nr;
			}
			inWordNumber = false;
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (inWordNumber) {
				wordNumber.append(ch, start, length);
			}
		}
		
	}
}