package com.feilong.view;

import com.feilong.R;
import com.feilong.util.LRCTest;
import com.feilong.util.LrcObject;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Toast;
import android.widget.RemoteViews.ActionException;

public class BasicView extends View {

	/**
	 * 画笔
	 */
	Paint mPaint;
	/**
	 * 背景
	 */
	Bitmap backgroundImg;
	/**
	 * 旋转角度
	 */
	public float degrees = 0;
	public int Alpha = 255;
	GestureDetector detector;
	Matrix albumMatrix;
	int albumWidth;
	int albumHeight;
	int srcID;
	int backgroundID;
	String text = "暂不支持歌词";

	int offsetX = 0;
	int offsetY = 0;

	LrcObject lrcObject;

	public int currentTime = 0;

	public int currentPosition = 0;

	int TEXTSIZE = 35;

	int textHeight;

	public BasicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 初始化画笔
		mPaint = new Paint();
		// 初始化资源ID
		srcID = attrs.getAttributeResourceValue(null, "src", R.drawable.default_mini_album);
		backgroundID = attrs.getAttributeResourceValue(null, "background", R.drawable.default_mini_album);
		// 专辑宽高
		albumWidth = attrs.getAttributeIntValue(null, "width", 200);
		albumHeight = attrs.getAttributeIntValue(null, "height", 200);
		float Scale = context.getResources().getDisplayMetrics().density;
		albumWidth = (int) (albumWidth * Scale + 0.5f);
		albumHeight = (int) (albumHeight * Scale + 0.5f);
		// 初始化专辑矩阵
		albumMatrix = new Matrix();
		// 创建专辑图片
		CreateImage(context, null);
		getTextHeight();
		setLrcObject(LRCTest.getLrcObject("/sdcard/feilong/tmp.lrc", "UTF-8"));
	}

	public void setLrcObject(LrcObject lrcObject) {
		this.lrcObject = lrcObject;
	}

	public void CreateImage(Context context, Bitmap src) {
		if (src == null) {
			src = BitmapFactory.decodeResource(context.getResources(), srcID);
		}
		Bitmap background = BitmapFactory.decodeResource(context.getResources(), backgroundID);

		int bgW = background.getWidth();
		int bgH = background.getHeight();

		int srcW = src.getWidth();
		int srcH = src.getHeight();

		int paddingLeft = this.getPaddingLeft();
		int paddingTop = this.getPaddingTop();
		backgroundImg = Bitmap.createBitmap(albumWidth, albumHeight, Bitmap.Config.ARGB_8888);
		Canvas mCanvas = new Canvas(backgroundImg);

		float newBW = (float) albumWidth / bgW;
		float newBH = (float) albumHeight / bgH;
		albumMatrix.setScale(newBW, newBH);
		mCanvas.drawBitmap(background, albumMatrix, mPaint);

		float newW = albumWidth > srcW ? (float) (albumWidth - paddingLeft * 2) / srcW : (float) (albumWidth - paddingLeft * 2) / srcW;
		float newH = albumHeight > srcH ? (float) (albumHeight - paddingTop * 2) / srcH : (float) (albumHeight - paddingTop * 2) / srcH;

		albumMatrix.setScale(newW, newH);
		albumMatrix.postTranslate(paddingLeft, paddingTop);
		mCanvas.drawBitmap(src, albumMatrix, mPaint);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		if (!src.isRecycled()) {
			src.recycle();
		}
		if (!background.isRecycled()) {
			background.recycle();
		}

	}

	public boolean setImageBitmap(Bitmap bp) {
		CreateImage(getContext(), bp);
		invalidate();
		postInvalidate();
		return true;
	}

	public boolean setText(String text) {
		this.text = text;
		return true;
	}

	public void getTextHeight() {
		mPaint.setTextSize(TEXTSIZE);
		FontMetrics fm = mPaint.getFontMetrics();
		textHeight = (int) (Math.ceil(fm.descent - fm.ascent) + 8);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (lrcObject == null) {
			mPaint.setColor(Color.BLACK);
			mPaint.setTextAlign(Align.CENTER);
			mPaint.setTextSize(24);
			mPaint.setAlpha(255 - Alpha);

		} else {

			int count = this.getHeight() / textHeight / 2;

			for (int i = -count; i < count; i++) {
				int currrent = i + currentTime;
				if (currrent < 0 || currrent >= lrcObject.times.size()) {

				} else {
					Integer[] item = lrcObject.times.get(currrent);

					for (int t = 0, j = lrcObject.times.size() - 1; t < j; t++) {
						Integer[] it1 = lrcObject.times.get(t);
						Integer[] it2 = lrcObject.times.get(t + 1);
						if (currentPosition >= it1[0] && currentPosition < it2[0]) {
							currentTime = t;
							break;
						}
					}

					if (i == 0) {
						mPaint.setColor(Color.RED);
						mPaint.setTextAlign(Align.CENTER);
						mPaint.setColor(Color.RED);
						mPaint.setAlpha(255 - Alpha);
						canvas.drawText(lrcObject.lrcs.get(item[1]), this.getWidth() / 2 + offsetX, this.getHeight() / 2 + offsetY + i * textHeight, mPaint);
					} else {
						mPaint.setColor(Color.BLACK);
						mPaint.setTextAlign(Align.CENTER);
						mPaint.setAlpha(255 - Alpha);
						canvas.drawText(lrcObject.lrcs.get(item[1]), this.getWidth() / 2 + offsetX, this.getHeight() / 2 + offsetY + i * textHeight, mPaint);
					}
				}
			}
		}

		albumMatrix.setRotate(degrees, (canvas.getWidth() - albumWidth) / 2, canvas.getHeight());
		albumMatrix.postTranslate((canvas.getWidth() - albumWidth) / 2, 20);
		mPaint.setAlpha(Alpha);
		canvas.drawBitmap(backgroundImg, albumMatrix, mPaint);
	}

	public void upDateUI() {
		invalidate();
		postInvalidate();
	}

	float firstPress;
	float endPress;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			firstPress = event.getX();

			break;
		case MotionEvent.ACTION_UP:
			if (degrees > Math.PI * 5) {
				degrees = (int) (Math.PI * 6);
				Alpha = 0;
				invalidate();
				postInvalidate();
			} else {
				degrees = 0;
				Alpha = 255;
				invalidate();
				postInvalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			System.out.println("角度" + degrees);
			if (event.getX() - firstPress > 20) {
				if (degrees == (int) (Math.PI * 6)) {
					break;
				}
				degrees = (float) (Math.PI / 36) * (event.getX() - firstPress);
				Alpha = (int) (255 * (getWidth() - Math.abs((event.getX() - firstPress))) / this.getWidth());
				invalidate();
				postInvalidate();

			} else if (event.getX() - firstPress < -20) {
				if (degrees == 0) {
					break;
				}
				degrees = (float) (Math.PI * 10 - (Math.PI / 36) * (firstPress - event.getX()));
				if (degrees < 0) {
					degrees = 0;
				}
				Alpha = (int) (255 * (getWidth() - Math.abs((event.getX() - firstPress))) / this.getWidth());
				invalidate();
				postInvalidate();

			}

			break;
		}
		return true;
	}
}
