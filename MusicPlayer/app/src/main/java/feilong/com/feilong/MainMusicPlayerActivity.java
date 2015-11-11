package com.feilong;

import java.util.HashMap;
import java.util.Map;

import com.feilong.service.MusicService;
import com.feilong.view.BasicView;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainMusicPlayerActivity extends MiniActivity {

	/**
	 * 顶部按钮
	 */
	ImageButton nowPlaying;
	/**
	 * 声音控制
	 */
	ImageButton volumnButton;
	/**
	 * 进度条
	 */
	SeekBar volumeprogressSeekBar;
	/**
	 * 进度条界面
	 */
	RelativeLayout playerView;
	/**
	 * 进度条界面
	 */
	RelativeLayout volume_progress_bar;
	/**
	 * 声音管理服务
	 */
	AudioManager audioManager;
	/**
	 * 最大声音
	 */
	int maxVolume;
	/**
	 * 当前声音
	 */
	int currentVolume;
	/**
	 * 显示声音控制倒计时
	 */
	public int EndTime = -1;

	@Override
	protected void BasicActivitySetContentView() {
		setContentView(R.layout.music_player_panel);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		nowPlaying = (ImageButton) findViewById(R.id.nowPlaying);
		volumnButton = (ImageButton) findViewById(R.id.volumnButton);
		volumeprogressSeekBar = (SeekBar) findViewById(R.id.volumeprogressSeekBar);
		playerView = (RelativeLayout) findViewById(R.id.playerView);

		nowPlaying.setOnClickListener(this);
		volumnButton.setOnClickListener(this);

		// 初始化音频服务
		initVolume();
		// 创建控制播放进度
		createVolumeBar();

	}

	@Override
	protected void queryMusic() {
		if (MusicService.playinfo == null) {
			Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA }, MediaStore.Audio.Media.MIME_TYPE + "=?", new String[] { "audio/mpeg" }, null);
			Map<String, String> item = new HashMap<String, String>();
			cursor.moveToPosition(MusicService.position);
			item.put("title", cursor.getString(1));
			item.put("article", cursor.getString(2));
			String path = cursor.getString(3);
			if (path != null) {
				item.put("path", path);
			}
			cursor.close();
			MusicService.playinfo = item;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.equals(nowPlaying)) {
			finish();
		} else if (v.equals(volumnButton)) {
			if (EndTime < 0) {
				EndTime = 2000;
				volume_progress_bar.setVisibility(View.VISIBLE);
				mHander.post(upDateVol);
				Animation mAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
				volume_progress_bar.startAnimation(mAnimation);
			} else {
				EndTime = 2000;
			}
		}
		super.onClick(v);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		if (seekBar.equals(volumeprogressSeekBar)) {
			mHander.removeCallbacks(upDateVol);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			if (seekBar.equals(volumeprogressSeekBar)) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, seekBar.getProgress(), AudioManager.VIBRATE_SETTING_OFF);
			}
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (seekBar.equals(volumeprogressSeekBar)) {
			EndTime = 2000;
			mHander.post(upDateVol);
			return;
		}
		super.onStopTrackingTouch(seekBar);
	}

	Runnable upDateVol = new Runnable() {

		@Override
		public void run() {
			if (EndTime >= 0) {
				EndTime -= 200;
				mHander.postDelayed(upDateVol, 200);
			} else {
				Animation mAnimation = AnimationUtils.loadAnimation(MainMusicPlayerActivity.this, R.anim.fade_out);
				volume_progress_bar.startAnimation(mAnimation);
				volume_progress_bar.setVisibility(View.GONE);
			}

		}
	};

	/**
	 * 创建声音条
	 */
	private void createVolumeBar() {
		LayoutInflater inflater = this.getLayoutInflater();
		volume_progress_bar = (RelativeLayout) inflater.inflate(R.layout.volume_progress_bar, null);
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		layout.addRule(RelativeLayout.ABOVE, R.id.bottomArea);
		playerView.addView(volume_progress_bar, layout);

		volume_progress_bar.setVisibility(View.GONE);
		volumeprogressSeekBar = (SeekBar) findViewById(R.id.volumeprogressSeekBar);
		volumeprogressSeekBar.setOnSeekBarChangeListener(this);
		volumeprogressSeekBar.setMax(maxVolume);
		volumeprogressSeekBar.setProgress(currentVolume);
	}

	/**
	 * 初始化声音对象
	 */
	public void initVolume() {
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}
}
