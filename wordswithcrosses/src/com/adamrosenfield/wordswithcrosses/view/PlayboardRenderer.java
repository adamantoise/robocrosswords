package com.adamrosenfield.wordswithcrosses.view;

import java.util.logging.Logger;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.adamrosenfield.wordswithcrosses.puz.Box;
import com.adamrosenfield.wordswithcrosses.puz.Playboard;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Position;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Word;
import com.adamrosenfield.wordswithcrosses.view.ScrollingImageView.Point;

public class PlayboardRenderer {
    public static final int BOX_SIZE = 30;

    private static final Logger LOG = Logger.getLogger("com.adamrosenfield.wordswithcrosses");

    private final Paint blackBox = new Paint();
    private final Paint blackCircle = new Paint();
    private final Paint blackLine = new Paint();
    private final Paint cheated = new Paint();
    private final Paint currentLetterBox = new Paint();
    private final Paint currentLetterHighlight = new Paint();
    private final Paint currentWordHighlight = new Paint();
    private final Paint letterText = new Paint();
    private final Paint numberText = new Paint();
    private final Paint red = new Paint();
    private final Paint white = new Paint();
    private final Rect boxRect = new Rect();

    private Bitmap bitmap;
    private Playboard board;
    private float logicalDensity;
    private float lastScale = -1.0f;

    private boolean hintHighlight;

    public PlayboardRenderer(Playboard board, float logicalDensity, boolean hintHighlight) {
        this.logicalDensity = logicalDensity;
        this.board = board;
        this.hintHighlight = hintHighlight;

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

        blackCircle.setColor(Color.BLACK);
        blackCircle.setAntiAlias(true);
        blackCircle.setStyle(Style.STROKE);

        currentWordHighlight.setColor(Color.parseColor("#FFAE57"));
        currentLetterHighlight.setColor(Color.parseColor("#EB6000"));
        currentLetterBox.setColor(Color.parseColor("#FFFFFF"));
        currentLetterBox.setStrokeWidth(2.0F);

        white.setTextAlign(Align.CENTER);
        white.setColor(Color.WHITE);
        white.setAntiAlias(true);
        white.setTypeface(Typeface.SANS_SERIF);

        red.setTextAlign(Align.CENTER);
        red.setColor(Color.RED);
        red.setAntiAlias(true);
        red.setTypeface(Typeface.SANS_SERIF);

        this.cheated.setColor(Color.parseColor("#FFE0E0"));
    }

    public Bitmap draw(Word reset, float scale) {
        try {
            Box[][] boxes = this.board.getBoxes();
            boolean renderAll = (reset == null);

            if (bitmap == null || scale != lastScale) {
                LOG.info("New bitmap");
                bitmap = Bitmap.createBitmap(
                    (int)(boxes[0].length * BOX_SIZE * scale),
                    (int)(boxes.length * BOX_SIZE * scale),
                    Bitmap.Config.RGB_565);
                bitmap.eraseColor(Color.BLACK);
                renderAll = true;
                lastScale = scale;
            }

            Canvas canvas = new Canvas(bitmap);

            // board data
            int boxSize = (int)(BOX_SIZE * scale);
            Word currentWord = this.board.getCurrentWord();

            for (int row = 0; row < boxes.length; row++) {
                for (int col = 0; col < boxes[row].length; col++) {
                    if (!renderAll &&
                        reset != null &&
                        !currentWord.checkInWord(col, row) &&
                        !reset.checkInWord(col, row))
                    {
                        continue;
                    }

                    int x = col * boxSize;
                    int y = row * boxSize;
                    this.drawBox(canvas, x, y, row, col, scale, boxes[row][col], currentWord);
                }
            }

            return bitmap;
        } catch (OutOfMemoryError e) {
            LOG.severe("Out of memory!");
            return bitmap;
        }
    }

    public Bitmap drawWord(float scale) {
        LOG.warning("New bitmap (drawWord)");
        Position[] word = board.getCurrentWordPositions();
        Box[] boxes = board.getCurrentWordBoxes();
        int boxSize = (int)(BOX_SIZE * scale);
        Bitmap bitmap = Bitmap.createBitmap((int)(word.length * boxSize), (int)(boxSize), Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.BLACK);

        Canvas canvas = new Canvas(bitmap);

        for (int i = 0; i < word.length; i++) {
            int x = (int)(i * boxSize);
            int y = 0;
            drawBox(canvas, x, y, word[i].down, word[i].across, scale, boxes[i], null);
        }

        return bitmap;
    }

    public int findBoxNoScale(Point p) {
        int boxSize = (int) (BOX_SIZE * this.logicalDensity);

        return p.x / boxSize;
    }

    private void drawBox(Canvas canvas, int x, int y, int row, int col, float scale, Box box, Word currentWord) {
        int boxSize = (int)(BOX_SIZE * scale);
        int numberTextSize = (int)(scale * 8F);
        int letterTextSize = (int)(scale * 20);

        // scale paints
        numberText.setTextSize(scale * 8F);
        letterText.setTextSize(scale * 20F);
        red.setTextSize(scale * 20F);
        white.setTextSize(scale * 20F);

        boolean inCurrentWord = (currentWord != null) && currentWord.checkInWord(col, row);
        Position highlight = this.board.getHighlightLetter();

        Paint thisLetter = null;

        boxRect.set(x + 1, y + 1, (x + boxSize) - 1, (y + boxSize) - 1);

        if (box == null) {
            canvas.drawRect(boxRect, this.blackBox);
        } else {
            // Background colors
            if ((highlight.across == col) && (highlight.down == row)) {
                canvas.drawRect(boxRect, this.currentLetterHighlight);
            } else if ((currentWord != null) && currentWord.checkInWord(col, row)) {
                canvas.drawRect(boxRect, this.currentWordHighlight);
            } else if (this.hintHighlight && box.isCheated() && !(board.isShowErrors() && box.getResponse() != box.getSolution())) {
                canvas.drawRect(boxRect, this.cheated);
            } else if (this.board.isShowErrors() && (box.getResponse() != ' ') &&
                    (box.getSolution() != box.getResponse())) {
            	box.setCheated(true);
                canvas.drawRect(boxRect, this.red);
            } else {
                canvas.drawRect(boxRect, this.white);
            }

            if (box.isAcross() | box.isDown()) {
                canvas.drawText(Integer.toString(box.getClueNumber()), x + 2, y + numberTextSize + 2, this.numberText);
            }

            // Draw circle
            if (box.isCircled()) {
                canvas.drawCircle(x + (boxSize / 2) + 0.5F, y + (boxSize / 2) + 0.5F, (boxSize / 2) - 1.5F, blackCircle);
            }

            thisLetter = this.letterText;

            if (board.isShowErrors() && (box.getSolution() != box.getResponse())) {
                if ((highlight.across == col) && (highlight.down == row)) {
                    thisLetter = this.white;
                } else if (inCurrentWord) {
                    thisLetter = red;
                }
            }

            canvas.drawText(Character.toString(box.getResponse()), x + (boxSize / 2),
                y + (int) (letterTextSize * 1.25), thisLetter);
        }

        Paint boxColor = (((highlight.across == col) && (highlight.down == row)) && (currentWord != null))
            ? this.currentLetterBox : this.blackLine;

        // Draw left
        if ((col != (highlight.across + 1)) || (row != highlight.down)) {
            canvas.drawLine(x, y, x, y + boxSize, boxColor);
        }

        // Draw top
        if ((row != (highlight.down + 1)) || (col != highlight.across)) {
            canvas.drawLine(x, y, x + boxSize, y, boxColor);
        }

        // Draw right
        if ((col != (highlight.across - 1)) || (row != highlight.down)) {
            canvas.drawLine(x + boxSize, y, x + boxSize, y + boxSize, boxColor);
        }

        // Draw bottom
        if ((row != (highlight.down - 1)) || (col != highlight.across)) {
            canvas.drawLine(x, y + boxSize, x + boxSize, y + boxSize, boxColor);
        }
    }
}
