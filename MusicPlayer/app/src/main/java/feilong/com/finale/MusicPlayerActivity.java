package com.finale;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import com.feilong.R;

import android.app.Activity;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MusicPlayerActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	Button mButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mButton = (Button) findViewById(R.id.button);
		mButton.setOnClickListener(this);
	}

	MediaPlayer mp;

	@Override
	public void onClick(View v) {
		try {
			URL url = new URL("http://172.17.5.103/a.mp3");
			InputStream is = url.openStream();
			File tmp = new File("/sdcard/a.mp3");
			FileOutputStream fos = new FileOutputStream(tmp);

			mp = new MediaPlayer();
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

			new Thread(new DownLoad(is, fos, mp)).start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onDestroy() {
		mp.stop();
		mp.release();
		super.onDestroy();
	}

}