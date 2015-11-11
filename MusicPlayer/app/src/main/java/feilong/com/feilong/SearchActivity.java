package com.feilong;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.feilong.service.MusicService;
import com.feilong.thread.SearchNet;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchActivity extends MiniActivity implements OnItemClickListener, Callback {

	/**
	 * 搜索按钮
	 */
	ImageButton searchButton;
	/**
	 * 搜索文本框
	 */
	EditText searchText;
	/**
	 * 搜索结果
	 */
	public List<Map<String, String>> searchResult;
	/**
	 * 列表框
	 */
	ListView mSongList;
	/**
	 * 设置是否可以点击界面
	 */
	boolean ClickAble = true;
	public boolean isCannel = false;
	/**
	 * 进度条视图
	 */
	RelativeLayout progressBar;

	/**
	 * 显示标题
	 */
	TextView titleTextView;

	/**
	 * 顶部按钮
	 */
	ImageView topButton;

	@Override
	protected void BasicActivitySetContentView() {
		setContentView(R.layout.search);
	}

	public Handler updateView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		searchButton = (ImageButton) findViewById(R.id.searchButton);
		searchText = (EditText) findViewById(R.id.searchText);
		mSongList = (ListView) findViewById(R.id.listSongs);
		titleTextView = (TextView) findViewById(R.id.titleTextView);
		topButton = (ImageView) findViewById(R.id.topButton);

		searchButton.setOnClickListener(this);
		topButton.setOnClickListener(this);

		updateView = new Handler(this);
		titleTextView.setText("网络搜索");
		CreateProgressBar();

	}

	@Override
	protected void onResume() {
		super.onResume();
		titleTextView.setText("网络搜索");
	}

	@Override
	public void onClick(View v) {
		if (ClickAble) {
			if (v.equals(searchButton)) {
				searchFuncton(v);
			} else if (v.equals(topButton)) {
                finish();
			}
			super.onClick(v);
		} else {
			isCannel = true;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 搜索歌曲
	 * 
	 * @param v
	 */
	void searchFuncton(View v) {
		String text = searchText.getText().toString();
		if (text.equals("")) {
			new AlertDialog.Builder(this).setTitle("提示").setMessage("请输入内容").setPositiveButton("确定", new AlertDialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			}).create().show();
		} else {
			showProgressBar();
			new Thread(new SearchNet(text, this)).start();
		}
	}

	public void showProgressBar() {
		progressBar.setVisibility(View.VISIBLE);
		ClickAble = false;
		InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

	}

	public void HideProgressBar() {
		progressBar.setVisibility(View.GONE);
		ClickAble = true;
	}

	public void CreateProgressBar() {
		progressBar = new RelativeLayout(this);
		progressBar.setVisibility(View.GONE);
		LayoutInflater inflater = getLayoutInflater();
		inflater.inflate(R.layout.progressbar, progressBar);
		this.addContentView(progressBar, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		progressBar.setOnClickListener(this);
	}

	/**
	 * 列出所有的歌曲条目
	 * 
	 * @param count
	 */
	public void ListResult() {
		if (searchResult == null && !isCannel) {
			new AlertDialog.Builder(this).setTitle("提示").setMessage("没有搜索到歌曲").setPositiveButton("确定", new AlertDialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			}).create().show();
		} else if (isCannel) {

		} else {
			SimpleAdapter mAdapter = new SimpleAdapter(this, searchResult, R.layout.play_list_item, new String[] { "title", "article" }, new int[] { R.id.line1, R.id.line3 });
			mSongList.setAdapter(mAdapter);
			mSongList.setOnItemClickListener(this);
		}
	}

	/**
	 * 搜索的条目点击
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		MusicService.playinfo = searchResult.get(arg2);
		StartPlayMusic();
	}

	@Override
	protected void queryMusic() {

	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == UPDATE_LIST) {
			ListResult();
			HideProgressBar();
			isCannel = false;
			return true;
		} else if (msg.what == CANCEL_SEARCH) {
			HideProgressBar();
			return true;
		}
		return false;
	}

	public static final int UPDATE_LIST = 0x123;
	public static final int CANCEL_SEARCH = 0x124;
}
