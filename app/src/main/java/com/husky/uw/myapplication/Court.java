package com.husky.uw.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lybar_000 on 5/22/2015.
 */
public class Court {

    public Bitmap bitmap;
    private Paint paint;

    private static int HOOP_DIAMETER_INCHES = 18;
    private static int COURT_WIDTH_FEET = 50;
    private static int HOOP_OFFSET_INCHES = 63;
    private static int LINE_WIDTH_INCHES = 2;
    private static int BOUNDARY_WIDTH = 50;
    private static int BOUNDARY_HEIGHT = 10;
    private static int BACKBOARD_OFFSET_FEET = 4;
    private static int BACKBOARD_WIDTH_FEET = 6;
    private static int KEY_WIDTH_FEET = 12;
    private static int KEY_HEIGHT_FEET = 19;
    private static int KEY_TICK_SPACE_FEET = 3;
    private static int KEY_BLOCK_WIDTH_FEET = 1;
    private static int KEY_TICK_LENGTH_INCHES = 8;
    private static int KEY_BLOCK_OFFSET_FEET = 7;
    private float lineWidthPixels = 4;


    private float pixelsPerFoot;
    private float pixelsPerInch;
    private float hoopRadiusPixels;
    private float hoopOffsetPixels;
    private float backboardOffsetPixels;
    private float backboardWidthPixels;
    private float keyWidthPixels;
    private float keyHeightPixels;
    private float keyTickSpacePixels;
    private float keyTickLengthPixels;
    private float threePointPixels;
    private Context context;

    public float courtWidthPixels;
    public float courtHeightPixels;


    public Court(Context mcontext){

        if (context == null) {
            context = mcontext;
        }

        courtWidthPixels = context.getResources().getInteger(R.integer.court_width);
        courtHeightPixels = context.getResources().getInteger(R.integer.court_height);

        pixelsPerFoot = courtWidthPixels/(float)COURT_WIDTH_FEET;
        pixelsPerInch = pixelsPerFoot/12f;
        hoopRadiusPixels = HOOP_DIAMETER_INCHES*pixelsPerInch/2f;
        hoopOffsetPixels = HOOP_OFFSET_INCHES*pixelsPerInch;
        backboardOffsetPixels = BACKBOARD_OFFSET_FEET*pixelsPerFoot;
        backboardWidthPixels = BACKBOARD_WIDTH_FEET*pixelsPerFoot;
        keyWidthPixels = KEY_WIDTH_FEET*pixelsPerFoot;
        keyHeightPixels = KEY_HEIGHT_FEET*pixelsPerFoot;
        keyTickSpacePixels = KEY_TICK_SPACE_FEET*pixelsPerFoot;
        keyTickLengthPixels = KEY_TICK_LENGTH_INCHES*pixelsPerInch;
        threePointPixels = keyHeightPixels + keyWidthPixels/2f - hoopOffsetPixels;

        bitmap = Bitmap.createBitmap((int)courtWidthPixels, (int)courtHeightPixels, Bitmap.Config.ARGB_8888);

        // Add canvas to each bitmap in array
        Canvas canvas = new Canvas(bitmap);

        // Define paint
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(lineWidthPixels);

        float x1;
        float y1;
        float x2;
        float y2;
        float r;

        //Out of bounds
        //Left
        x1 = lineWidthPixels/2;
        y1 = 0;
        x2 = lineWidthPixels/2;
        y2 = courtHeightPixels;
        canvas.drawLine(x1, y1, x2, y2, paint);

        //Right
        x1 = courtWidthPixels - lineWidthPixels/2;
        x2 = courtWidthPixels - lineWidthPixels/2;
        canvas.drawLine(x1, y1, x2, y2, paint);

        //Upper
        x1 = lineWidthPixels/2;
        y1 = courtHeightPixels - lineWidthPixels/2;
        x2 = courtWidthPixels - lineWidthPixels/2;
        y2 = y1;
        canvas.drawLine(x1, y1, x2, y2, paint);

        //Lower
        x1 = lineWidthPixels/2;
        y1 = lineWidthPixels/2;
        x2 = courtWidthPixels - lineWidthPixels/2;
        y2 = lineWidthPixels/2;
        canvas.drawLine(x1, y1, x2, y2, paint);

        // Hoop and backboard
        x1 = courtWidthPixels/2f;
        y1 = courtHeightPixels - hoopOffsetPixels;
        // y1 = oobUpperPixels + hoopOffsetPixels;
        r = hoopRadiusPixels;
        canvas.drawCircle(x1, y1, r,paint);

        x1 = courtWidthPixels/2f-backboardWidthPixels/2f;
        y1 = courtHeightPixels - backboardOffsetPixels;
        x2 = courtWidthPixels/2f+backboardWidthPixels/2f;
        y2 = y1;
        canvas.drawLine(x1, y1, x2, y2, paint);

        // Key
        //Left
        x1 = courtWidthPixels/2f - keyWidthPixels/2f;
        y1 = courtHeightPixels;
        x2 = x1;
        y2 = courtHeightPixels - keyHeightPixels;
        canvas.drawLine(x1, y1, x2, y2, paint);
        //Left block
        y1 = courtHeightPixels - backboardOffsetPixels - keyTickSpacePixels;
        x2 = x1 - keyTickLengthPixels;
        y2 = y1;
        canvas.drawLine(x1, y1, x2, y2, paint);
        //Left tick 1
        y1 = y2 - keyTickSpacePixels;
        x2 = x1 - keyTickLengthPixels;
        y2 = y1;
        canvas.drawLine(x1, y1, x2, y2, paint);
        //Left tick 2
        y1 = y2 - keyTickSpacePixels;
        y2 = y1;
        canvas.drawLine(x1, y1, x2, y2, paint);
        //Left tick 3
        y1 = y2 - keyTickSpacePixels;
        y2 = y1;
        canvas.drawLine(x1, y1, x2, y2, paint);

        //Right
        x1 = courtWidthPixels/2f + keyWidthPixels/2f;
        y1 = courtHeightPixels;
        x2 = x1;
        y2 = courtHeightPixels - keyHeightPixels;
        canvas.drawLine(x1, y1, x2, y2, paint);
        //Right block
        y1 = courtHeightPixels - backboardOffsetPixels - keyTickSpacePixels;
        x2 = x1 + keyTickLengthPixels;
        y2 = y1;
        canvas.drawLine(x1, y1, x2, y2, paint);
        //Right tick 1
        y1 = y2 - keyTickSpacePixels;
        x2 = x1 + keyTickLengthPixels;
        y2 = y1;
        canvas.drawLine(x1, y1, x2, y2, paint);
        //Right tick 2
        y1 = y2 - keyTickSpacePixels;
        y2 = y1;
        canvas.drawLine(x1, y1, x2, y2, paint);
        //Right tick 3
        y1 = y2 - keyTickSpacePixels;
        y2 = y1;
        canvas.drawLine(x1, y1, x2, y2, paint);

        //Upper
        x1 = (courtWidthPixels - keyWidthPixels)/2f - lineWidthPixels/2f;
        y1 = courtHeightPixels - keyHeightPixels;
        x2 = (courtWidthPixels + keyWidthPixels)/2f + lineWidthPixels/2f;
        y2 = y1;
        canvas.drawLine(x1, y1, x2, y2, paint);

        //Arc at key
        x1 = (courtWidthPixels - keyWidthPixels)/2f;
        y1 = courtHeightPixels - keyHeightPixels - keyWidthPixels/2f;
        x2 = (courtWidthPixels + keyWidthPixels)/2f;
        y2 = courtHeightPixels - keyHeightPixels + keyWidthPixels/2f;
        RectF oval = new RectF();
        oval.set(x1, y1, x2, y2);
        canvas.drawArc(oval, 180, 180, false, paint);

        //3pt arc
        //Arc
        x1 = courtWidthPixels/2f-threePointPixels;
        y1 = courtHeightPixels - hoopOffsetPixels - threePointPixels;
        x2 = courtWidthPixels/2f+threePointPixels;
        y2 = courtHeightPixels - hoopOffsetPixels + threePointPixels;
        oval.set(x1, y1, x2, y2);
        canvas.drawArc(oval, 180, 180, false, paint);

        //Left
        x1 = courtWidthPixels/2f-threePointPixels;
        y1 = courtHeightPixels;
        x2 = x1;
        y2 = courtHeightPixels - hoopOffsetPixels;
        canvas.drawLine(x1, y1, x2, y2, paint);

        //Right
        x1 = courtWidthPixels/2f+threePointPixels;
        x2 = x1;
        canvas.drawLine(x1, y1, x2, y2, paint);



    }


    public List<float[]> getPlayerInitialPositions(){
        List<float[]> positions = new ArrayList<float[]>();
        float[] X = new float[5];
        float[] Y = new float[5];

        // Player 1
        X[0] = courtWidthPixels/2f;
        Y[0] = courtHeightPixels - hoopOffsetPixels - threePointPixels - hoopRadiusPixels*2;

        // Player 2
        X[1] = courtWidthPixels/2f - keyWidthPixels*3/2;
        Y[1] = courtHeightPixels - keyHeightPixels;

        // Player 3
        X[2] = courtWidthPixels/2f + keyWidthPixels*3/2;
        Y[2] = courtHeightPixels - keyHeightPixels;

        // Player 4
        X[3] = courtWidthPixels/2f - keyWidthPixels/2f - hoopRadiusPixels;
        Y[3] = courtHeightPixels - backboardOffsetPixels - keyTickSpacePixels;

        // Player 5
        X[4] = courtWidthPixels/2f + keyWidthPixels/2f + hoopRadiusPixels;
        Y[4] = courtHeightPixels - backboardOffsetPixels - keyTickSpacePixels;

        positions.add(X);
        positions.add(Y);
        System.out.println("SIZE" + Float.toString(positions.size()));
        return positions;
    }

    public float[] getHoopPositionNorm(){
        return new float[]{1/2f, 1 - hoopOffsetPixels/courtHeightPixels};
    }

    public float[] getHoopPositionPixels(){
        return new float[]{courtWidthPixels/2f, courtHeightPixels - hoopOffsetPixels};
    }
}
