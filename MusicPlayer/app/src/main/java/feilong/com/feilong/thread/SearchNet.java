package com.feilong.thread;

import java.util.List;
import java.util.Map;
import com.feilong.SearchActivity;
import com.feilong.download.SearchAPI;
import com.feilong.download.impl.SogouAPI;
import com.feilong.util.StaticField;

public class SearchNet implements Runnable {
	String url;
	SearchActivity searchActivity;

	public SearchNet(String url, SearchActivity searchActivity) {
		this.searchActivity = searchActivity;
		this.url = url;
	}

	@Override
	public void run() {
		List<Map<String, String>> mList = null;
		try {
			SearchAPI api = new SogouAPI();
			boolean isLoad = api.LoadUrlFile(url, StaticField.FILEPATH + "/a.txt",searchActivity.isCannel);
			if (isLoad) {
				mList = api.ParseFile(searchActivity.isCannel);
			}
		} catch (Exception e) {
			System.out.println("Õ¯¬Á¡¨Ω”“Ï≥£");
			e.printStackTrace();
		}
		searchActivity.searchResult = mList;
		searchActivity.updateView.sendEmptyMessage(SearchActivity.UPDATE_LIST);
	}
}
