package com.feilong.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import android.R.integer;

public class LRCTest {

	public static LrcObject getLrcObject(String fileName, String encode) {
		// 创建文件
		File mFile = new File(fileName);
		// 设置缓冲区
		byte[] buffer = new byte[51200];
		// 打开输入流
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(mFile);
		} catch (FileNotFoundException e) {
			System.out.println("文件未找到");
			return null;
		}
		// 开始标签,结束标签

		byte[] startBytes = null;
		byte[] endBytes = null;

		// 缓冲区长度
		int len = 0;

		// 开始结束起始位置
		int startOffset = 0;
		int endOffset = 0;

		// 开始结束计数器
		int startCount = 0;
		int endCount = 0;

		// 开始结束最大计数器
		int startCountMax = 0;
		int endCountMax = 0;

		boolean isStart = false;

		LrcObject object = new LrcObject();
		object.times = new ArrayList<Integer[]>();
		object.lrcs = new ArrayList<String>();
		int item = 0;

		// 第一次判断编码
		boolean isFirst = true;
		// 初始化标题和内容
		ByteArrayOutputStream title = new ByteArrayOutputStream();
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		try {
			while ((len = fis.read(buffer)) != -1) {
				startOffset = 0;
				endOffset = len;// 结束
				if (isFirst) {
					if (buffer[0] == 0xFF && buffer[1] == 0xFE) {
						// UTF16-LE
						encode = "UTF-16LE";
					} else if (buffer[0] == 0xFE && buffer[1] == 0xFF) {
						// UTF16-BE
						encode = "UTF-16BE";
					} else if (buffer[0] == 0xEF && buffer[1] == 0xBB && buffer[2] == 0xBF) {
						// UTF-8
						encode = "UTF-8";
					} else {
						encode = "GB2312";
					}
					startBytes = "[".getBytes(encode);
					endBytes = "]".getBytes(encode);
					startCountMax = startBytes.length;
					endCountMax = endBytes.length;

					isFirst = false;
				}

				for (int i = 0; i < len; i++) {
					if (!isStart && (buffer[i] == startBytes[startCount])) {
						startCount++;
						if (startCount == startCountMax) {
							isStart = true;

							startOffset = i + 1;
							endOffset = len - startOffset;

							startCount = 0;
							// System.out.println("开始");

							// title.write(startBytes, 0, startCountMax - 1);
							// 初始化
							{
								content.close();

								if (content.size() != 0) {
									// System.out.print(new
									// String(content.toByteArray(), encode));
									object.lrcs.add(new String(content.toByteArray(), encode));
									content = new ByteArrayOutputStream();
									item++;
								}
							}
						}
					} else if (isStart && (buffer[i] == endBytes[endCount])) {
						endCount++;
						if (endCount == endCountMax) {
							isStart = false;

							endOffset = i - startOffset + 1;
							endCount = 0;

							// System.out.println("结束");
							title.write(buffer, startOffset, endOffset);
							title.close();
							// System.out.println(new
							// String(title.toByteArray(), encode));

							String mTime = new String(title.toByteArray(), encode);
							// System.out.println(mTime);
							mTime = mTime.substring(0, mTime.length() - 1);
							int time = object.GetTime(mTime);
							if (time >= 0) {
								object.times.add(new Integer[] { time, item });
							}

							// 再次初始化数据
							title = new ByteArrayOutputStream();
							continue;
						}
					} else {
						startCount = 0;
						endCount = 0;
					}
					if (!isStart) {
						content.write(buffer[i]);
					}
				}
				if (isStart) {
					title.write(buffer, startOffset, endOffset);
					System.out.println("内容结尾");
				}
				if (!isStart) {
					content.close();
					// System.out.println("标题结尾");
				}
			}
			if (!isStart) {
				// System.out.println("没有完成");
				object.lrcs.add("");
			}

			title.close();
			content.close();
			fis.close();

			int size = object.times.size();
			for (int i = 0; i < size; i++) {
				for (int j = i + 1; j < size; j++) {
					Integer[] items1 = object.times.get(i);
					Integer[] items2 = object.times.get(j);
					int[] item1 = new int[] { items1[0], items1[1] };
					int[] item2 = new int[] { items2[0], items2[1] };
					if (item1[0] > item2[0]) {
						object.times.set(i, new Integer[] { item2[0], item2[1] });
						object.times.set(j, new Integer[] { item1[0], item1[1] });
					}
				}
			}
			for (int i = 0; i < object.lrcs.size(); i++) {
				System.out.println(object.lrcs.get(i));
			}

			return object;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
