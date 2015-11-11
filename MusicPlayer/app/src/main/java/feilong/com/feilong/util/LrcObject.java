package com.feilong.util;

import java.util.List;
import java.util.Map;

public class LrcObject {
	public String title;
	public String art;
	public String al;
	public String offset;

	public List<Integer[]> times;
	public List<String> lrcs;

	public int GetTime(String time) {

		time = time.replace(".", ":");
		String[] times = time.split(":");
		int lTime = 0;
		if (times.length == 3) {
			try {
				lTime = Integer.parseInt(times[0]) * 60 + Integer.parseInt(times[1]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				lTime = 0;
			}
		} else if (times.length == 2) {
			if (times[0].equals("ti")) {
				this.title = times[1];
				return -1;
			} else if (times[0].equals("ar")) {
				this.art = times[1];
				return -1;
			} else if (times[0].equals("al")) {
				this.al = times[1];
				return -1;
			} else if (times[0].equals("offset")) {
				this.offset = times[1];
				return -1;
			}
		}
		return lTime;
	}
}
