package com.feilong;

import java.util.HashMap;
import java.util.Map;

import com.feilong.service.MusicService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.RelativeLayout;

public class MyMusicActivity extends com.feilong.MiniActivity {
	RelativeLayout localmusic;
	RelativeLayout netmusic;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		localmusic = (RelativeLayout) findViewById(R.id.localmusic);
		netmusic = (RelativeLayout) findViewById(R.id.netmusic);
		localmusic.setOnClickListener(this);
		netmusic.setOnClickListener(this);
	}

	protected void queryMusic() {
		if(MusicService.playinfo==null){
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
		if (v.equals(localmusic)) {
			Intent intent = new Intent(MyMusicActivity.this, com.feilong.CommonListActivity.class);
			startActivity(intent);
		} else if (v.equals(netmusic)) {
			MusicService.playinfo.put("download","http://m5.file.xiami.com/177/2177/11958/147263_16769739_l.mp3?auth_key=54c292927e22c2f9bd693b677d2513da-1446768000-0-null");
			Intent intent = new Intent(MusicService.MUSICSERVICE);
			intent.putExtra(MusicService.PLAYSTATE, MusicService.PLAY);
			startService(intent);
		//	Intent intent = new Intent(MyMusicActivity.this, SearchActivity.class);
		//	startActivity(intent);
		}
		super.onClick(v);
	}

	@Override
	protected void BasicActivitySetContentView() {
		setContentView(R.layout.my_music_activity);
	}

}
