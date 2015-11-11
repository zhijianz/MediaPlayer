package com.feilong;

import java.io.File;

import com.feilong.util.StaticField;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

public class MusicPlayerActivity extends Activity implements AnimationListener {

	RelativeLayout appStarter;

	/**
	 * ��������
	 * */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_starter_activity);
		appStarter = (RelativeLayout) findViewById(R.id.appStarter);
		File dir = new File(StaticField.FILEPATH);
		if(!dir.exists()){
			dir.mkdir();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		animation.setDuration(500);
		animation.setFillAfter(true);
		animation.setAnimationListener(this);
		appStarter.startAnimation(animation);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		Intent intent = new Intent(MusicPlayerActivity.this, com.feilong.MyMusicActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onAnimationStart(Animation animation) {

	}
}