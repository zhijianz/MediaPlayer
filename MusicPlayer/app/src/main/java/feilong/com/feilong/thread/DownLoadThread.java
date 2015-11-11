package com.feilong.thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;

import com.feilong.download.DownLoadAPI;
import com.feilong.download.impl.SogouDownAPI;
import com.feilong.service.MusicService;
import com.feilong.util.Frame;
import com.feilong.util.FrameUtil;
import com.feilong.util.StaticField;

public class DownLoadThread implements Runnable {
	MusicService finalsPlayer;
	String url;
	String lrc;
	Frame frame;
	boolean isCancel;

	public DownLoadThread(MusicService finalsPlayer, String url, String lrc) {
		this.finalsPlayer = finalsPlayer;
		this.url = url;
		this.lrc = lrc;
		isCancel = false;
	}

	boolean isFirst;

	@Override
	public void run() {
		try {
			isFirst = true;
			DownLoadAPI api = new SogouDownAPI();
			//url = api.getFileUri(url, isCancel);
			//lrc = api.getLrcUri(lrc, isCancel);
			if (lrc != null) {
				lrc = "http://mp3.sogou.com/" + lrc;
				lrc = new String(lrc.getBytes(), 0, lrc.getBytes().length - 2);
				DownLoadLrc(lrc);
			}
			if (url == null) {
				return;
			}
			//url = new String(url.getBytes(), 0, url.getBytes().length - 2);
			URL mUrl = new URL(url);
			URLConnection connection = mUrl.openConnection();
			int fileSize = connection.getContentLength();
			InputStream is = connection.getInputStream();

			String tmpFilePath = StaticField.FILEPATH + "/tmp.dat";
			File tmpFile = new File(tmpFilePath);
			File tmpFileImg = new File(tmpFilePath + ".jpg");
			if (!tmpFile.exists()) {
				tmpFile.createNewFile();
			}
			if (tmpFileImg.exists()) {
				tmpFileImg.delete();
			}
			byte[] buffer = new byte[81920];

//			FileOutputStream raf = new FileOutputStream(tmpFile);
//			int count = fileSize / buffer.length;
//			int end = fileSize % buffer.length;
//
//			for (int i = 0; i < count; i++) {
//				if (isCancel) {
//					raf.close();
//					is.close();
//					return;
//				}
//				raf.write(buffer);
//			}
//			raf.write(buffer, 0, end);
//			raf.close();

			RandomAccessFile fos = new RandomAccessFile(tmpFile, "rwd");
			fos.setLength(fileSize);

			int len = 0;

			int bufferCount = 0;
			while ((len = is.read(buffer)) != -1) {
				if (isCancel) {
					fos.close();
					is.close();
					finalsPlayer.StopPlay();
					return;
				}
				fos.write(buffer, 0, len);
				bufferCount += len;
				if (isFirst && (bufferCount <= 102400)) {
					continue;
				}
				if (isFirst && (bufferCount >= 102400)) {
					isFirst = false;
					frame = FrameUtil.CalcFrame(tmpFilePath, fileSize);
					MusicService.playtime = (int) frame.playTime;
					finalsPlayer.StartPlay(tmpFilePath);
					finalsPlayer.setPlaystate(MusicService.PLAY);
					finalsPlayer.sendNotification();
				}
				MusicService.secondTime = (int) frame.getDuration(bufferCount);
				Thread.sleep(800);
			}
			System.out.println("enddddddddddddddddddddd");
			fos.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean DownLoadLrc(String lrc) {
		URL mUrl = null;
		try {
			mUrl = new URL(lrc);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}
		URLConnection connection;
		try {
			connection = mUrl.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		try {
			InputStream is = connection.getInputStream();
			String tmpFilePath = StaticField.FILEPATH + "/tmp.lrc";
			File tmpFile = new File(tmpFilePath);
			FileOutputStream fos = new FileOutputStream(tmpFile);

			byte[] buffer = new byte[81920];
			int len = 0;
			while ((len = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			is.close();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void Cancel() {
		isCancel = true;
	}
}
