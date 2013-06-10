package com.adamrosenfield.wordswithcrosses.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.puz.Playboard.Position;
import com.adamrosenfield.wordswithcrosses.view.CrosswordImageView.ClickListener;

public class ClueImageView extends TouchImageView
{
    private float renderScale = 1.0f;

    private ClickListener clickListener;

    public ClueImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setCanScale(false);
    }

    public void fitToHeight()
    {
        renderScale = (float)getHeight() / PlayboardRenderer.BOX_SIZE;
        render();
    }

    public void setClickListener(ClickListener listener)
    {
        clickListener = listener;
    }

    @Override
    protected void onClick(PointF pos)
    {
        if (clickListener == null)
        {
            return;
        }

        float boxSize = PlayboardRenderer.BOX_SIZE * renderScale;
        int x = (int)(pos.x / boxSize);
        int y = (int)(pos.y / boxSize);
        Position crosswordPos = new Position(x, y);

        clickListener.onClick(crosswordPos);
    }

    public void render()
    {
        Bitmap bitmap = WordsWithCrossesApplication.RENDERER.drawWord(renderScale);
        setImageBitmap(bitmap);
    }
}
