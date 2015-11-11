package com.feilong.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.feilong.CommonListActivity;
import com.feilong.MusicPlayerActivity;
import com.feilong.MyMusicActivity;
import com.feilong.R;
import com.feilong.thread.DownLoadThread;
import com.feilong.util.StaticField;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MusicService extends Service implements Runnable, OnCompletionListener {
	private static final int PLAYING_NOTIFY_ID = 667667;
	
	MediaPlayer mediaPlayer;
	public boolean isRun = false;
	NotificationManager nNotificationManager;
	TelephonyManager mTelephonyManager;
	PhoneStateListener mPhoneStateListener;

	@Override
	public void onCreate() {
		super.onCreate();
		isRun = false;
		setPlaystate(STOP);
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setWakeMode(this.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		mediaPlayer.setOnCompletionListener(this);
		String ns = Context.NOTIFICATION_SERVICE;
		nNotificationManager = (NotificationManager) getSystemService(ns);

		mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (state == TelephonyManager.CALL_STATE_IDLE) {

				} else {
					if (mediaPlayer != null) {
						mediaPlayer.pause();
					}
				}
			}
		};
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		if (!isRun) {
			isRun = true;
			new Thread(this).start();
		}
	}

	public void setPlaystate(int playstate) {
		MusicService.playstate = playstate;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		int playstate = STOP;
		try {
			playstate = intent.getIntExtra(PLAYSTATE, STOP);
		} catch (Exception e) {
			System.out.println("��������");
			e.printStackTrace();
		}

		switch (playstate) {
		case PLAY:
			StartPlay(position);
			break;
		case REPLAY:
			RestartPlay();
			break;
		case PAUSE:
			PausePlay();
			nNotificationManager.cancel(1);
			break;
		case NEXT:
			NextPlay();
			break;
		case PREV:
			PrevPlay();
			break;
		case SEEK:
			SeekPlay();
			break;
		case STOP:
			break;
		}

	}

	/**
	 * ���͸�����Ϣ��������
	 */
	public void sendNotification() {
		Notification notification = new Notification(R.drawable.icon, "���ֲ�����", System.currentTimeMillis());
		Context context = getApplicationContext();
		CharSequence contentTitle = MusicService.playinfo.get("title");
		CharSequence contentText = MusicService.playinfo.get("article");
		Intent noti = new Intent(Intent.ACTION_MAIN);
		noti.addCategory(Intent.CATEGORY_LAUNCHER);
		noti.setClass(this, MyMusicActivity.class);
		noti.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, noti, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		//Finals�޸�
		startForeground(PLAYING_NOTIFY_ID, notification);
		//nNotificationManager.notify(1, notification);
	}

	private void PrevPlay() {
		switch (playmode) {
		case 0:
			position--;
			StartPlay(position);
			break;
		}
	}

	private void NextPlay() {
		switch (playmode) {
		case 0:
			position++;
			StartPlay(position);
			break;
		}
	}

	@Override
	public void onDestroy() {
		playtime = 0;
		secondTime = 0;
		current = 0;
		DestoryPlay();
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		stopForeground(true);
		super.onDestroy();
	}

	public void DestoryPlay() {
		isRun = false;
		mediaPlayer.stop();
		mediaPlayer.release();
		if (downloadThread != null) {
			downloadThread.Cancel();
		}
		setPlaystate(STOP);
	}

	public void StopPlay() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			playtime = 0;
			secondTime = 0;
			current = 0;
			setPlaystate(STOP);
			nNotificationManager.cancel(1);
		}
	}

	private void PausePlay() {
		if (playstate == PLAY) {
			mediaPlayer.pause();
			setPlaystate(PAUSE);
		}
	}

	public void SeekPlay() {

		if (current >= secondTime - 2) {
			mediaPlayer.seekTo((secondTime - 2) * 1000);
		} else {
			mediaPlayer.seekTo(current * 1000);
		}

	}

	public void RestartPlay() {
		if (playstate == PAUSE) {
			mediaPlayer.start();
			setPlaystate(PLAY);
			sendNotification();
		}
	}

	public void StartPlay(int position) {
		String url = MusicService.playinfo.get("download");
		if (url == null) {
			if (downloadThread != null) {
				downloadThread.Cancel();
			}
			current = 0;
			secondTime = 0;
			playtime = 0;
			Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST }, MediaStore.Audio.Media.MIME_TYPE + "=?", new String[] { "audio/mpeg" }, null);
			int maxCount = cursor.getCount();
			if (position >= maxCount) {
				position = 0;
			}
			if (position < 0) {
				position = maxCount - 1;
			}
			MusicService.position = position;
			int count = 0;
			String path = null;
			if (cursor.moveToFirst()) {
				do {
					if (count == position) {
						path = cursor.getString(0);
						MusicService.playinfo = new HashMap<String, String>();
						MusicService.playinfo.put("title", cursor.getString(1));
						MusicService.playinfo.put("article", cursor.getString(2));
						break;
					}
					count++;
				} while (cursor.moveToNext());
			}
			cursor.close();
			if (path != null) {
				mediaPlayer.reset();
				try {
					mediaPlayer.setDataSource(path);
					mediaPlayer.prepare();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return;
				} catch (IllegalStateException e) {
					e.printStackTrace();
					return;
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
				mediaPlayer.start();
				playtime = (int) (mediaPlayer.getDuration() / 1000);
				secondTime = playtime;
				// �߳̿���

			}
			setPlaystate(PLAY);
			sendNotification();
		} else if (url != null) {
			//url = url + ".mp3";
			if (downloadThread != null) {
				downloadThread.Cancel();
			}
			StopPlay();
			/*
			String lrc = MusicService.playinfo.get("lrc");
			int start = lrc.indexOf("href=\"");
			int end = lrc.indexOf("\" ");
			if (start != -1) {
				lrc = lrc.substring(start + 6, end);
			} else {
				lrc = null;
			}
			*/

			downloadThread = new DownLoadThread(this, url, null);
			new Thread(downloadThread).start();
		}
	}

	DownLoadThread downloadThread;

	public void StartPlay(String path) {
		mediaPlayer.reset();
		try {
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		mediaPlayer.start();
	}

	@Override
	public void run() {
		while (isRun) {
			try {
				Thread.sleep(500);
				if (playstate == PLAY) {
					current = (int) (mediaPlayer.getCurrentPosition() / 1000);
					if (secondTime < playtime) {
						if (current >= secondTime - 2) {
							mediaPlayer.pause();
						} else {
							mediaPlayer.start();
						}
					}
				} else if (playstate == PAUSE) {

				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		switch (playmode) {
		case 0:
			NextPlay();
			break;
		}
	}

	public static final String MUSICSERVICE = "com.feilong.service.MusicService";
	public static final String PLAYSTATE = "playstate";
	// ����״̬����
	public static final int STOP = 0;
	public static final int PLAY = 1;
	public static final int PAUSE = 2;
	public static final int REPLAY = 3;
	public static final int SEEK = 4;
	public static final int NEXT = 5;
	public static final int PREV = 6;

	// ��ǰ���ŵĸ���
	public static int position = 0;
	public static int playtime = 0;
	public static int secondTime = 0;
	public static int current = 0;
	// ����״̬
	public static int playstate = 0;
	// ����ģʽ
	public static int playmode = 0;
	// ����Դ
	// public static int playsource = 0;

	/**
	 * �洢����
	 */
	public static Map<String, String> playinfo;
}
