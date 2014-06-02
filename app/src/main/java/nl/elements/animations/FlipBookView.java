package nl.elements.animations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergi on 18/03/14.
 */
public class FlipBookView extends ImageView{

    private String pattern;
    private List<Integer> pages =new ArrayList<Integer>();
    private long flipDuration=4;
    private long lastFlipDuration=4;
    private int currentPage;
    private boolean animating;

    Handler handler = new Handler();
    private OnFlipFinishedListener onFlipFinishedListener;
    private long startingTime;
    private long deltaTime;
    private int realImageWidth;
    private int realImageHeight;
    private int previousCurrentPage=-1;
    Bitmap output;
    private long measuringTime;
    Canvas outputCanvas;

    Paint paint;
    Paint xpaint;
    RectF outerRect;


    public FlipBookView(Context context) {
        super(context);
        init();
    }

    public FlipBookView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlipBookView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

        paint = new Paint();//Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        xpaint = new Paint();//Paint.ANTI_ALIAS_FLAG);
        xpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        boolean finding=true;
        String pageName;
        this.pattern = pattern;
        pages.clear();

        for(int i=0;finding;i++) {
            pageName=String.format(pattern.toLowerCase(),i);
            int id = getResources().getIdentifier(pageName,"drawable",getContext().getPackageName());
            if (id!=0) {
                pages.add(id);
            }else{
                finding=false;
            }


        }
        invalidate();
    }

    public void setImage(int resourceId) {
        previousCurrentPage=-1;
        pages.clear();
        currentPage=0;
        pages.add(resourceId);
        invalidate();
    }

    public long getFlipDuration() {
        return flipDuration;
    }

    public void setFlipDuration(long flipDuration) {
        this.flipDuration = flipDuration;
    }

    public long getLastFlipDuration() {
        return lastFlipDuration;
    }

    public void setLastFlipDuration(long lastFlipDuration) {
        this.lastFlipDuration = lastFlipDuration;
    }

    public void flip() {
        if (pages.size()<1) {
            return;
        }
        currentPage=0;
        animating =true;
        startingTime=System.currentTimeMillis();
        measuringTime=System.currentTimeMillis();
        Log.d("timing", "starting:" + measuringTime);
        invalidate();
    }

    /***
     * Returns the correct size of the control when needed (Basically maintaining the
     * ratio)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int measuredHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        calculateRealSize (measuredWidth, measuredHeight);
        output = Bitmap.createBitmap(realImageWidth, realImageHeight, Bitmap.Config.ARGB_8888);
        outputCanvas = new Canvas(output);

        setMeasuredDimension(realImageWidth, realImageHeight);


    }

    /***
     * Starting from an initial size, calculates the real size of the View to be
     * used in order to fit in the assigned space and maintain image ratio
     *
     * @param initialWidth
     * @param initialHeight
     */
    protected void calculateRealSize (int initialWidth, int initialHeight) {
        int proposedHeight = initialWidth;
        int proposedWidth = initialHeight;

        if (proposedHeight>initialHeight) {
            realImageHeight=initialHeight;
            realImageWidth=proposedWidth;
        } else {
            realImageHeight=proposedHeight;
            realImageWidth=initialWidth;
        }

        outerRect = new RectF(0, 0, realImageWidth, realImageHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawFrame(canvas);
        if (animating) {
            deltaTime=System.currentTimeMillis()-startingTime;
            if (deltaTime>flipDuration) {

                currentPage++;
                if (currentPage<pages.size()) {

                    startingTime=System.currentTimeMillis();
                }else {
                    currentPage=pages.size()-1;
                    animating=false;
                    Handler h = new Handler();

                    h.postDelayed(new FinishRunnable(), lastFlipDuration);
                }

            }
        }

        invalidate();
    }

    private void drawFrame(Canvas outerCanvas) {


        if (previousCurrentPage!=currentPage || output==null) {


            try {

                //Generat a rectangle with the size calculated

                outputCanvas.drawCircle(realImageWidth / 2, realImageHeight / 2, Math.min(realImageHeight, realImageWidth) / 2, paint);


                // We save the current layer. Not sure why.
                outputCanvas.saveLayer(outerRect, xpaint, Canvas.ALL_SAVE_FLAG);


                // We got the appopiate drawable, image or placeholder
                Drawable framebase = getResources().getDrawable(pages.get(currentPage));


                // We set the bounds to be the same than the bitmap
                framebase.setBounds(0, 0, realImageWidth, realImageHeight);


                // And draw it in the canvas
                framebase.draw(outputCanvas);


                // That restores the compositing mode to normal and do some other stuff
                outputCanvas.restore();


                previousCurrentPage = currentPage;
            }catch (Exception ex){}

        }

        outerCanvas.drawBitmap(output, 0, 0, null);

    }

    private Bitmap loadResource(int id, int width, int height) {
        Bitmap temp=BitmapFactory.decodeResource(getResources(),id, null);

        return Bitmap.createScaledBitmap(temp,width,height,false);
    }

    public OnFlipFinishedListener getOnFlipFinishedListener() {
        return onFlipFinishedListener;
    }

    public void setOnFlipFinishedListener(OnFlipFinishedListener onFlipFinishedListener) {
        this.onFlipFinishedListener = onFlipFinishedListener;
    }

    private class FinishRunnable implements Runnable {
        @Override
        public void run() {

            setImageResource(pages.get(pages.size()-1));
            if (onFlipFinishedListener!=null) {
                onFlipFinishedListener.onFlipFinished();
            }
            currentPage=0;
            Log.d("timing", "end:" + (System.currentTimeMillis()));
            Log.d("timing", "duration:" + (System.currentTimeMillis()-measuringTime));
            invalidate();
        }
    }

    public interface OnFlipFinishedListener {
        public void onFlipFinished();
    }
}
