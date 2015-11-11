package com.finale;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;

public class DownLoad implements Runnable {

	InputStream is;
	FileOutputStream fos;
	MediaPlayer mp;

	public DownLoad(InputStream is, FileOutputStream fos, MediaPlayer mp) {
		this.is = is;
		this.fos = fos;
		this.mp = mp;
	}

	boolean isFirst = true;

	int currentPosition = 0;
	int updateCount = 0;

	@Override
	public void run() {
		byte[] buffer = new byte[960000];
		int len = 0;
		try {

			while ((len = is.read(buffer)) != -1) {

				Thread.sleep(200);

				fos.write(buffer, 0, len);
				fos.flush();
				System.out.println("Êý¾Ý" + currentPosition);

				if (isFirst) {
					mp.setDataSource("/sdcard/a.mp3");
					try {
						mp.prepare();
						mp.start();
					} catch (Exception e) {
						continue;
					}
					mp.seekTo(currentPosition);
					isFirst = false;
				}
				if ((mp.getDuration() - mp.getCurrentPosition() < 200)) {
					currentPosition = mp.getCurrentPosition();
					mp.reset();
					isFirst = true;
				} else {
                    updateCount++;
                    if(updateCount>=3){
                    	System.out.println("eeeee");
                    	updateCount=0;
                    	currentPosition = mp.getCurrentPosition();
    					try {
    						mp.prepare();
    						mp.seekTo(currentPosition);
    						mp.start();
    					} catch (Exception e) {
    						continue;
    					}
                    }
				}

			}
			is.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
