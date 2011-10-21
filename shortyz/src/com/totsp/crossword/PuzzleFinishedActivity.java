package com.totsp.crossword;

import java.text.DateFormat;
import java.text.NumberFormat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.shortyz.R;
import com.totsp.crossword.shortyz.ShortyzApplication;

public class PuzzleFinishedActivity extends ShortyzActivity{
	private static final long SECONDS = 1000;
    private static final long MINUTES = SECONDS * 60;
    private static final long HOURS = MINUTES * 60;
    private final NumberFormat two_int = NumberFormat.getIntegerInstance();
	private final DateFormat date = DateFormat.getDateInstance(DateFormat.SHORT);
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utils.holographic(this);
        setContentView(R.layout.completed);
        this.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        
        Puzzle puz = ShortyzApplication.BOARD.getPuzzle();
        
        two_int.setMinimumIntegerDigits(2);
        
        long elapsed = puz.getTime();
        
        long hours = elapsed / HOURS;
        elapsed = elapsed % HOURS;

        long minutes = elapsed / MINUTES;
        elapsed = elapsed % MINUTES;

        long seconds = elapsed / SECONDS;
        
        String elapsedString = (hours > 0 ? two_int.format(hours) + ":" : "") + 
        		two_int.format(minutes) + ":"+
                two_int.format(seconds);
        
        int totalClues = puz.getAcrossClues().length + puz.getDownClues().length;
        int totalBoxes = 0;
        int cheatedBoxes = 0;
        for(Box b : puz.getBoxesList()){
        	if(b == null){
        		continue;
        	}
        	if(b.isCheated()){
        		cheatedBoxes++;
        	}
        	totalBoxes++;
        }
        
        String cheatedString = cheatedBoxes +" ("+
        two_int.format( (double) cheatedBoxes  * 100D / (double) totalBoxes)+"%)";
        
        final String shareMessage;
        if(puz.getSource() != null && puz.getDate() != null){
        	shareMessage = "I finished the "+puz.getSource()+" crossword for "+ date.format(puz.getDate()) +" in "+
        		elapsedString +(cheatedBoxes > 0 ? " but cheated "+cheatedBoxes+ " boxes" : "")+" in Shortyz!";
        } else {
        	shareMessage = "I finished "+puz.getSource()+" in "+
            		elapsedString +(cheatedBoxes > 0 ? "but cheated "+cheatedBoxes : "")+" with Shortyz!";
        }
        
        TextView elapsedTime = (TextView) this.findViewById(R.id.elapsed);
        elapsedTime.setText(elapsedString);
        
        TextView totalCluesView = (TextView) this.findViewById(R.id.totalClues);
        totalCluesView.setText(Integer.toString(totalClues));
        
        TextView totalBoxesView = (TextView) this.findViewById(R.id.totalBoxes);
        totalBoxesView.setText(Integer.toString(totalBoxes));
        
        TextView cheatedBoxesView = (TextView) this.findViewById(R.id.cheatedBoxes);
        cheatedBoxesView.setText(cheatedString);
        
        Button share = (Button) this.findViewById(R.id.share);
        share.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
//				sendIntent.putExtra(Intent.EXTRA_SUBJECT,
//						"I finished a puzzle in Shortyz Crosswords!");
				sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, "Share your time"));
			}
        	
        });
        
        Button done = (Button) this.findViewById(R.id.done);
        done.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				finish();
			}
        	
        });
        
	}
}
