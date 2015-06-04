package com.husky.uw.myapplication;

/**
 * Created by lybar_000 on 5/22/2015.
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by lybar_000 on 5/9/2015.
 */
public class Hoop {

    public float X;
    public float Y;
    private static int LINEWIDTH = 4;
    private static int ICON_SIZE = 40;
    private Paint paint;
    private static Context context;

    public Bitmap icon;

    public Hoop(Context mcontext, Court court){
        if (context == null) {
            context = mcontext;
        }
        X = court.getHoopPositionPixels()[0];
        Y = court.getHoopPositionPixels()[1];

        //createIcon();

    }


    public void createIcon() {
        //Define circle format
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINEWIDTH);
        paint.setColor(context.getResources().getColor(R.color.husky_black));

        icon = Bitmap.createBitmap(ICON_SIZE + LINEWIDTH * 2,
                ICON_SIZE + LINEWIDTH * 2,
                Bitmap.Config.ARGB_8888);

        // Add canvas to each bitmap in array
        Canvas temporaryCanvas = new Canvas(icon);

        // Draw circle on canvas, for player icon
        float midpoint = (float) icon.getWidth() / 2;
        float radius = midpoint - LINEWIDTH;
        temporaryCanvas.drawCircle(midpoint, midpoint, radius, paint);
    }

    // X coordinates for insertion (accounts for width of icon)
    public float getX(){
        // Determine insertion point in view that will center icon at player position
        return X-(float)ICON_SIZE / 2;

    }

    // Y coordinate for insertion (accounts for height of icon)
    public float getY(){
        // Determine insertion point in view that will center icon at player position
        return Y-(float)ICON_SIZE / 2;
    }
}
