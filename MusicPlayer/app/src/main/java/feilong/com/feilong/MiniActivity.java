package com.feilong;

import com.feilong.service.MusicService;
import com.feilong.view.BasicView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public abstract class MiniActivity extends Activity implements OnClickListener, OnSeekBarChangeListener {

	/**
	 * 前一个按钮
	 */
	ImageButton prevbtn;
	/**
	 * 暂停按钮
	 */
	ImageButton pausebtn;
	/**
	 * 下一个按钮
	 */
	ImageButton nextbtn;
	/**
	 * 进度条
	 */
	SeekBar playSeekBar;
	/**
	 * 歌曲歌曲
	 */
	TextView currSongTextView;
	/**
	 * 歌手
	 */
	TextView currSingerTextView;
	/**
	 * 专辑
	 */
	ImageView albumImageView;

	/**
	 * 更新进度条
	 */
	public Handler mHander;
	/**
	 * 最近播放
	 */
	int currentposition = 0;

	TextView currTimeTextView;
	TextView totalTimeTextView;

	BasicView album;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BasicActivitySetContentView();
		initComponent();
	}

	protected abstract void BasicActivitySetContentView();

	void initComponent() {
		prevbtn = (ImageButton) findViewById(R.id.prevbtn);
		pausebtn = (ImageButton) findViewById(R.id.pausebtn);
		nextbtn = (ImageButton) findViewById(R.id.nextbtn);
		currSongTextView = (TextView) findViewById(R.id.currSongTextView);
		currSingerTextView = (TextView) findViewById(R.id.currSingerTextView);
		playSeekBar = (SeekBar) findViewById(R.id.playSeekBar);
		albumImageView = (ImageView) findViewById(R.id.albumImageView);
		currTimeTextView = (TextView) findViewById(R.id.currTimeTextView);
		totalTimeTextView = (TextView) findViewById(R.id.totalTimeTextView);
		album = (BasicView) findViewById(R.id.album);

		playSeekBar.setProgress(0);
		playSeekBar.setMax(0);
		playSeekBar.setSecondaryProgress(0);
		playSeekBar.setOnSeekBarChangeListener(this);

		prevbtn.setOnClickListener(this);
		pausebtn.setOnClickListener(this);
		nextbtn.setOnClickListener(this);

		if (albumImageView != null) {
			albumImageView.setOnClickListener(this);
		}
		mHander = new Handler();
	}

	@Override
	protected void onResume() {
		queryMusic();
		mHander.post(updateTime);
		if (MusicService.playstate == MusicService.PLAY) {
			pausebtn.setBackgroundResource(R.drawable.mini_pausebtn_xml);
		} else if (MusicService.playstate == MusicService.STOP) {
			currSongTextView.setText(MusicService.playinfo.get("title"));
			currSingerTextView.setText(MusicService.playinfo.get("article"));
			pausebtn.setBackgroundResource(R.drawable.mini_playbtn_xml);

			playSeekBar.setMax(0);
			playSeekBar.setSecondaryProgress(0);
			playSeekBar.setProgress(0);
		}
		System.out.println("开始绑定时间更新");
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mHander.removeCallbacks(updateTime);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		mHander.removeCallbacks(updateTime);
		System.out.println("销毁时间更新");
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		if (v.equals(pausebtn)) {
			switch (MusicService.playstate) {
			case MusicService.PLAY:
				PausePlayMusic();
				break;
			case MusicService.PAUSE:
				RestartPlayMusic();
				break;
			case MusicService.STOP:
				StartPlayMusic();
				break;
			}
		} else if (v.equals(nextbtn)) {
			NextPlayMusic();
		} else if (v.equals(prevbtn)) {
			PrevPlayMusic();
		} else if (v.equals(albumImageView)) {
			Intent intent = new Intent(MiniActivity.this, MainMusicPlayerActivity.class);
			startActivity(intent);
		}
	}

	public void RestartPlayMusic() {
		mHander.removeCallbacks(updateTime);
		Intent intent = new Intent(MusicService.MUSICSERVICE);
		intent.putExtra(MusicService.PLAYSTATE, MusicService.REPLAY);
		startService(intent);
		mHander.post(updateTime);
	}

	public void StartPlayMusic() {
		mHander.removeCallbacks(updateTime);
		Intent intent = new Intent(MusicService.MUSICSERVICE);
		intent.putExtra(MusicService.PLAYSTATE, MusicService.PLAY);
		startService(intent);
		mHander.post(updateTime);
	}

	public void PausePlayMusic() {
		mHander.removeCallbacks(updateTime);
		Intent intent = new Intent(MusicService.MUSICSERVICE);
		intent.putExtra(MusicService.PLAYSTATE, MusicService.PAUSE);
		startService(intent);
		mHander.post(updateTime);
	}

	public void NextPlayMusic() {
		currentposition = MusicService.position;
		Intent intent = new Intent(MusicService.MUSICSERVICE);
		intent.putExtra(MusicService.PLAYSTATE, MusicService.NEXT);
		startService(intent);
	}

	public void PrevPlayMusic() {
		currentposition = MusicService.position;
		Intent intent = new Intent(MusicService.MUSICSERVICE);
		intent.putExtra(MusicService.PLAYSTATE, MusicService.PREV);
		startService(intent);
	}

	public void SeekPlayMusic(int currrent) {
		mHander.removeCallbacks(updateTime);
		MusicService.current = currrent;
		Intent intent = new Intent(MusicService.MUSICSERVICE);
		intent.putExtra(MusicService.PLAYSTATE, MusicService.SEEK);
		startService(intent);
		mHander.post(updateTime);
	}

	int tmpplaytime;
	int tmpsecondtime;
	int tmpcurrent;
	int tmpplaystate;
	Runnable updateTime = new Runnable() {

		@Override
		public void run() {
			if (currentposition != MusicService.position) {
				currentposition = MusicService.position;
				queryMusic();
				currSongTextView.setText(MusicService.playinfo.get("title"));
				currSingerTextView.setText(MusicService.playinfo.get("article"));
			}
			if (tmpplaytime != MusicService.playtime) {
				tmpplaytime = MusicService.playtime;
				playSeekBar.setMax(MusicService.playtime);
				if (totalTimeTextView != null) {
					totalTimeTextView.setText(getTime(MusicService.playtime));
				}
			}
			if (tmpsecondtime != MusicService.secondTime) {
				tmpsecondtime = MusicService.secondTime;
				playSeekBar.setSecondaryProgress(MusicService.secondTime);
			}
			if (tmpcurrent != MusicService.current) {
				tmpcurrent = MusicService.current;
				playSeekBar.setProgress(MusicService.current);
				if (currTimeTextView != null) {
					currTimeTextView.setText(getTime(MusicService.current));
				}
				if (album != null) {
					album.currentPosition = MusicService.current;
					album.upDateUI();
				}
			}
			if (tmpplaystate != MusicService.playstate) {
				tmpplaystate = MusicService.playstate;
				switch (MusicService.playstate) {
				case MusicService.PLAY:
					pausebtn.setBackgroundResource(R.drawable.mini_pausebtn_xml);
					currSongTextView.setText(MusicService.playinfo.get("title"));
					currSingerTextView.setText(MusicService.playinfo.get("article"));
					break;
				case MusicService.PAUSE:
					pausebtn.setBackgroundResource(R.drawable.mini_playbtn_xml);
					currSongTextView.setText(MusicService.playinfo.get("title"));
					currSingerTextView.setText(MusicService.playinfo.get("article"));
					break;
				case MusicService.REPLAY:
					pausebtn.setBackgroundResource(R.drawable.mini_pausebtn_xml);
					break;
				}
			}
			if (MusicService.playstate == MusicService.STOP) {
				pausebtn.setBackgroundResource(R.drawable.mini_playbtn_xml);
				currSongTextView.setText(MusicService.playinfo.get("title"));
				currSingerTextView.setText(MusicService.playinfo.get("article"));
			}
			mHander.postDelayed(updateTime, 500);
			System.out.println("开始更新" + MusicService.playstate);
		}
	};

	public void onProgressChanged(android.widget.SeekBar arg0, int arg1, boolean arg2) {

	}

	public void onStartTrackingTouch(android.widget.SeekBar arg0) {

	}

	public void onStopTrackingTouch(android.widget.SeekBar bar) {
		if (bar.equals(playSeekBar)) {
			SeekPlayMusic(bar.getProgress());
		}
	}

	public String getTime(int time) {
		int minute = (int) time / 60;
		int second = (int) time % 60;
		String minuteStr = minute >= 10 ? minute + "" : "0" + minute;
		String secondStr = second >= 10 ? second + "" : "0" + second;
		return minuteStr + ":" + secondStr;
	}

	protected abstract void queryMusic();
}
