package me.kennydude.speaker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

class SmoothSeekBar extends View{
	public static final Integer height = 60;
	public static final Integer text_size = 15;
	
	public static abstract class OnValueChanged{
		public abstract void ValueChanged(SmoothSeekBar seekbar, Integer new_value);
	}
	
	public OnValueChanged onValueChanged = new OnValueChanged(){
		@Override
		public void ValueChanged(SmoothSeekBar seekbar, Integer new_value) {}
	};
	
	public String[] values = { "Option 1", "Option 2", "Option 3" };

	public SmoothSeekBar(Context context, AttributeSet as) {
		super(context, as);
	}
	
	boolean isDragging, isAnimating = false;
	int x = 0, curX = 0, newX = 0;
	int biggestWidth = 0;
	int currentValue = 0;
	
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		this.setMeasuredDimension(widthMeasureSpec, height);
	}
	
	public void HandleMove(MotionEvent e){
		if(!isDragging){ return; }
		isAnimating = false;
		//if(e.getX() > x - 10 && e.getX() < x + biggestWidth + 10){
			curX = Math.round( e.getX() );
			invalidate();
		//} else{
		//	isDragging = false;
		//}
	}
	
	public class BackgroundAnimator extends AsyncTask<Object, Object, Object>{

		public Object doInBackground(Object... args) {
			while(curX != newX){
				if(isAnimating == true){
					if( curX > newX )
						curX --;
					else
						curX ++;
					postInvalidate();
					try {
						Thread.sleep(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else{
					return null;
				}
			}
			isAnimating = false;
			return null;
		}
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			if(x < 0 || (event.getX() > x && event.getX() < x + biggestWidth)){
				isAnimating = false;
				getParent().requestDisallowInterceptTouchEvent(true);
				isDragging = true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			HandleMove(event);
			break;
		case MotionEvent.ACTION_UP:
			HandleMove(event);
		case MotionEvent.ACTION_CANCEL:
			if (isDragging == true){
				isAnimating = true;
				// Now we have to calculate a value
				if(curX == 0){
					curX = valBasedX();
				}
				int bw = (width() / 3);
				for(int i = 0; i != 3; i++){
					if( (bw * i) < event.getX() && event.getX() < (bw * (i+1)) ){
						currentValue = i;
					}
				}
				onValueChanged.ValueChanged(this, currentValue);
				// But, we don't request we do that right now
				newX = valBasedX();
				new BackgroundAnimator().execute();
				
				// Then reset everything
				getParent().requestDisallowInterceptTouchEvent(false);
				isDragging = false;
			}
			break;
		}
		return true;
	}
	
	void DrawString(int where, String str, Canvas c, Paint text){
		// Find out width
		Rect bounds = new Rect();
		text.getTextBounds(str, 0, str.length(), bounds);
		// Now draw into box
		int bw = (width() / 3);
		c.drawText(str, (bw * where) + ((bw / 2) - (bounds.width() / 2))
				, (height() / 2) - (text_size / 2), text);
	}
	
	Rect size;
	int width(){
		return size.width();
	}
	int height(){
		return size.height();
	}
	
	void Measure(int one, Paint text){
		Rect bounds = new Rect();
		text.getTextBounds(values[one], 0, values[one].length(), bounds);
		if(biggestWidth < bounds.width())
			biggestWidth = bounds.width();
	}
	
	@Override
	public void onDraw(Canvas c){
		size = c.getClipBounds();
		
		/*DisplayMetrics dm = new DisplayMetrics();
		((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(dm);
		int mul = dm.densityDpi;*/
		
		// Draw 3 values
		Paint text = new Paint();
		text.setTextSize(text_size);
		
		if(biggestWidth == 0){
			//biggestWidth = 0;
			Measure(0, text);
			Measure(1, text);
			Measure(2, text);
			biggestWidth += 20; // Padding
		}
		
		// Draw slider
		//x = 0;
		if(isDragging || isAnimating){
			x = curX;
		}
		else{
			x = valBasedX();
		}
		Paint box = new Paint();
		box.setColor(getResources().getColor(R.color.seek_color)); // TODO: Base off Holo
		
		c.drawRoundRect(new RectF( x, 0, x + biggestWidth, height() ), 5, 5, box);
		
		text.setColor(getResources().getColor(android.R.color.primary_text_dark));
		DrawString(0, values[0], c, text);
		DrawString(1, values[1], c, text);
		DrawString(2, values[2], c, text);
		
	}

	private int valBasedX() {
		int bw = (width() / 3);
		return (bw * currentValue) + ((bw / 2) - (biggestWidth / 2));
	}
}