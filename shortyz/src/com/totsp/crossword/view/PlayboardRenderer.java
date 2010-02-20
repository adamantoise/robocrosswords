package com.totsp.crossword.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.graphics.Paint.Align;

import android.graphics.Rect;
import android.graphics.Typeface;

import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Playboard;
import com.totsp.crossword.puz.Playboard.Position;
import com.totsp.crossword.puz.Playboard.Word;
import com.totsp.crossword.view.ScrollingImageView.Point;

import java.util.logging.Logger;

public class PlayboardRenderer {
	private static final int BOX_SIZE = 30;
	private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
	Paint blackBox = new Paint();
	Paint blackLine = new Paint();
	Paint cheated = new Paint();
	Paint currentLetterBox = new Paint();
	Paint currentLetterHighlight = new Paint();
	Paint currentWordHighlight = new Paint();
	Paint letterText = new Paint();
	Paint numberText = new Paint();
	Paint white = new Paint();
	Paint red = new Paint();
	private Bitmap bitmap;
	private Playboard board;
	private float scale = 1.0F;
	private float logicalDensity;

	public PlayboardRenderer(Playboard board, float logicalDensity) {
		this.scale = scale * logicalDensity;
		this.logicalDensity = logicalDensity;
		this.board = board;
		blackLine.setColor(Color.BLACK);
		blackLine.setStrokeWidth(2.0F);

		numberText.setTextAlign(Align.LEFT);
		numberText.setColor(Color.BLACK);
		numberText.setAntiAlias(true);
		numberText.setTypeface(Typeface.MONOSPACE);

		letterText.setTextAlign(Align.CENTER);
		letterText.setColor(Color.BLACK);
		letterText.setAntiAlias(true);
		letterText.setTypeface(Typeface.SANS_SERIF);

		blackBox.setColor(Color.BLACK);

		currentWordHighlight.setColor(Color.parseColor("#FFFFC0"));
		currentLetterHighlight.setColor(Color.parseColor("#FFFF00"));
		currentLetterBox.setColor(Color.parseColor("#FF6633"));
		currentLetterBox.setStrokeWidth(2.0F);

		white.setColor(Color.WHITE);
		red.setColor(Color.RED);

		this.cheated.setColor(Color.parseColor("#FFE0E0"));
	}

	public Bitmap drawWord() {
		Box[] word = this.board.getCurrentWordBoxes();
		int boxSize = (int)(BOX_SIZE * this.logicalDensity);
		Bitmap bitmap = Bitmap.createBitmap(
				(int) (word.length
						* boxSize)
						,(int) (boxSize), Bitmap.Config.RGB_565);
		bitmap.eraseColor(Color.WHITE);
		Canvas canvas = new Canvas(bitmap);
		// scale paints
		numberText.setTextSize(this.logicalDensity * 8F);
		letterText.setTextSize(this.logicalDensity * 20F);
		for (int col = 0; col < word.length; col++) {
			int numberTextSize = (int) (scale * 8F);
			int letterTextSize = (int) (scale * 20);
			int startX = (int) (col * boxSize);
			int startY = 0;
			Paint boxColor = this.blackLine;
			
			Rect r = new Rect(startX + 1, startY + 1,
					(startX + boxSize) - 1, (startY + boxSize) - 1);
			
			
			if(this.board.getCurrentBox() == word[col]){
				canvas.drawRect(r, this.currentLetterHighlight);
			}
			

			// Draw left

			canvas
					.drawLine(startX, startY, startX, startY + boxSize,
							boxColor);

			// Draw top
			canvas
					.drawLine(startX, startY, startX + boxSize, startY,
							boxColor);

			// Draw right
			canvas.drawLine(startX + boxSize, startY, startX + boxSize,
					startY + boxSize, boxColor);

			// Draw bottom
			canvas.drawLine(startX, startY + boxSize, startX + boxSize,
					startY + boxSize, boxColor);

			if (word[col].across | word[col].down) {
				canvas.drawText(Integer.toString(word[col].clueNumber),
						startX + 2, startY + numberTextSize + 2,
						this.numberText);
			}

			canvas.drawText(Character.toString(word[col].response), startX
					+ (boxSize / 2), startY + (int) (letterTextSize * 1.25),
					this.letterText);
		}

		return bitmap;

	}

	public Bitmap draw(Word reset) {
		try {
			Box[][] boxes = this.board.getBoxes();
			boolean renderAll = reset == null;

			if (bitmap == null) {
				LOG.warning("New bitmap");
				bitmap = Bitmap.createBitmap(
						(int) (boxes.length * BOX_SIZE * scale),
						(int) (boxes[0].length * BOX_SIZE * scale),
						Bitmap.Config.RGB_565);
				bitmap.eraseColor(Color.WHITE);
				renderAll = true;
			}

			Canvas canvas = new Canvas(bitmap);

			// scale paints
			numberText.setTextSize(scale * 8F);
			letterText.setTextSize(scale * 20F);

			// board data
			Word currentWord = this.board.getCurrentWord();
			Position highlight = this.board.getHighlightLetter();

			for (int col = 0; col < boxes.length; col++) {
				for (int row = 0; row < boxes[col].length; row++) {
					int boxSize = (int) (BOX_SIZE * scale);
					int numberTextSize = (int) (scale * 8F);
					int letterTextSize = (int) (scale * 20);
					int startX = col * boxSize;
					int startY = row * boxSize;
					Paint boxColor = highlight.across == col
							&& highlight.down == row ? this.currentLetterBox
							: this.blackLine;

					// Draw left
					if (col != (highlight.across + 1)) {
						canvas.drawLine(startX, startY, startX, startY
								+ boxSize, boxColor);
					}

					// Draw top
					if (row != (highlight.down + 1)) {
						canvas.drawLine(startX, startY, startX + boxSize,
								startY, boxColor);
					}

					// Draw right
					if (col != (highlight.across + 1)) {
						canvas.drawLine(startX + boxSize, startY, startX
								+ boxSize, startY + boxSize, boxColor);
					}

					// Draw bottom
					if (row != (highlight.down - 1)) {
						canvas.drawLine(startX, startY + boxSize, startX
								+ boxSize, startY + boxSize, boxColor);
					}

					if (!renderAll) {
						if (!currentWord.checkInWord(col, row)
								&& (reset != null)
								&& !reset.checkInWord(col, row)) {
							continue;
						}
					}

					Rect r = new Rect(startX + 1, startY + 1,
							(startX + boxSize) - 1, (startY + boxSize) - 1);

					if (boxes[col][row] == null) {
						canvas.drawRect(r, this.blackBox);
					} else {
						// Background colors
						if ((highlight.across == col)
								&& (highlight.down == row)) {
							canvas.drawRect(r, this.currentLetterHighlight);
						} else if (currentWord.checkInWord(col, row)) {
							canvas.drawRect(r, this.currentWordHighlight);
						} else if (boxes[col][row].cheated) {
							canvas.drawRect(r, this.cheated);
						} else if (this.board.isShowErrors()
								&& boxes[col][row].solution != boxes[col][row].response) {
							canvas.drawRect(r, this.red);
						} else {
							canvas.drawRect(r, this.white);
						}

						if (boxes[col][row].across | boxes[col][row].down) {
							canvas.drawText(Integer
									.toString(boxes[col][row].clueNumber),
									startX + 2, startY + numberTextSize + 2,
									this.numberText);
						}

						canvas.drawText(Character
								.toString(boxes[col][row].response), startX
								+ (boxSize / 2), startY
								+ (int) (letterTextSize * 1.25),
								this.letterText);
					}

				}
			}

			return bitmap;
		} catch (OutOfMemoryError e) {
			return bitmap;
		}
	}

	public Position findBox(Point p) {
		int boxSize = (int) (BOX_SIZE * scale);
		int col = p.x / boxSize;
		int row = p.y / boxSize;

		return new Position(col, row);
	}

	public Point findPointBottomRight(Position p) {
		int boxSize = (int) (BOX_SIZE * scale);
		int x = (p.across * boxSize) + boxSize;
		int y = (p.down * boxSize) + boxSize;

		return new Point(x, y);
	}

	public Point findPointTopLeft(Position p) {
		int boxSize = (int) (BOX_SIZE * scale);
		int x = p.across * boxSize;
		int y = p.down * boxSize;

		return new Point(x, y);
	}

	public void zoomIn() {
		this.bitmap = null;
		this.scale = scale * 1.5F;
	}

	public void zoomOut() {
		this.bitmap = null;
		this.scale = scale / 1.5F;
	}

	public void zoomReset() {
		this.bitmap = null;
		this.scale = 1.0F;
	}
}
