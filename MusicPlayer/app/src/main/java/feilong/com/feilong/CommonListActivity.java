package com.feilong;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.feilong.service.MusicService;
import com.feilong.util.StaticField;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class CommonListActivity extends MiniActivity implements OnItemClickListener {

	/**
	 * 音乐列表
	 */
	ListView musicList;
	/**
	 * 顶部按钮
	 */
	ImageView topButton;
	/**
	 * 歌曲列表
	 */
	ArrayList<Map<String, String>> songs;

	@Override
	protected void BasicActivitySetContentView() {
		this.setContentView(R.layout.local_musiclist_activity);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		musicList = (ListView) findViewById(R.id.musicList);
		topButton = (ImageView) findViewById(R.id.topButton);
		queryMusic();
		SimpleAdapter mAdapter = new SimpleAdapter(this, songs, R.layout.play_list_item, new String[] { "title", "article" }, new int[] { R.id.line1, R.id.line3 });
		musicList.setAdapter(mAdapter);
		musicList.setOnItemClickListener(this);
		topButton.setOnClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {

		if (songs != null) {
			Map<String, String> result = songs.get(arg2);
			MusicService.position = arg2;
			MusicService.playinfo = result;
			currSongTextView.setText(result.get("title"));
			currSingerTextView.setText(result.get("article"));
			StartPlayMusic();
			pausebtn.setBackgroundResource(R.drawable.mini_pausebtn_xml);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.equals(topButton)) {
			finish();
		}
		super.onClick(v);

	}

	@Override
	protected void queryMusic() {
		songs = queryMusics();
		if (MusicService.playinfo == null) {
			MusicService.playinfo = songs.get(MusicService.position);
		}
	}

	private ArrayList<Map<String, String>> queryMusics() {
		Cursor cursor = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA }, MediaStore.Audio.Media.MIME_TYPE + "=?", new String[] { "audio/mpeg" }, null);
		ArrayList<Map<String, String>> songs = new ArrayList<Map<String, String>>();
		if (cursor.moveToFirst()) {
			do {
				Map<String, String> item = new HashMap<String, String>();
				item.put("title", cursor.getString(1));
				item.put("article", cursor.getString(2));
				String path = cursor.getString(3);
				if (path != null) {
					item.put("path", path);
				}
				songs.add(item);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return songs;
	}

}
