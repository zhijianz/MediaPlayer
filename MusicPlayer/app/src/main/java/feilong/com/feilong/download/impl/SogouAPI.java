package com.feilong.download.impl;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.feilong.download.SearchAPI;

public class SogouAPI implements SearchAPI {
	public String cacheFile;
	public static final String path = "http://mp3.sogou.com/music.so?class=1&pf=mp3&w=&st=&ac=1&query=";
	public static final String debugPath = "http://172.17.5.100/a.html?class=";

	public boolean LoadUrlFile(String query, String cacheFile, boolean isCancel) {
		this.cacheFile = cacheFile;
		// 搜狗API
		StringBuilder ApiUrl = new StringBuilder(path);
		try {
			ApiUrl.append(URLEncoder.encode(query, "GB2312"));
		} catch (UnsupportedEncodingException e1) {
			System.out.println("编码不支持");
			e1.printStackTrace();
		}
		String result = ApiUrl.toString();
		System.out.println(result);
		// 创建URL连接
		URL mUrl = null;
		try {
			mUrl = new URL(result);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}
		URLConnection conn = null;
		try {
			conn = mUrl.openConnection();
		} catch (IOException e) {
			System.out.println("URL连接异常");
			return false;
		}
		// 保存文件
		InputStream is = null;
		try {
			is = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int len = 0;

			FileOutputStream fos = new FileOutputStream(cacheFile);

			// 找到开始部分
			String tableStart = new String("<table border=\"0\" id=\"otherTable\"");
			int startCount = 0;
			// 找到结束部分
			String tableEnd = new String("</table>");
			int endCount = 0;
			boolean isStart = false;

			byte[] tableStartBytes = tableStart.getBytes();
			byte[] tableEndBytes = tableEnd.getBytes();

			// 开始结束最大计数器
			int startCountMax = tableStartBytes.length;
			int endCountMax = tableEndBytes.length;

			int startOffset;
			int endOffset;
			while ((len = is.read(buffer)) != -1) {
				if (isCancel) {
					is.close();
					fos.close();
					return false;
				}
				startOffset = 0;
				endOffset = len;// 结束
				for (int i = 0; i < len; i++) {
					if (!isStart && (buffer[i] == tableStartBytes[startCount])) {
						startCount++;
						if (startCount == startCountMax) {
							isStart = true;

							startOffset = i;
							endOffset = len - startOffset;

							startCount = 0;
							System.out.println("开始");

							fos.write(tableStartBytes);
						}
					} else if (isStart && (buffer[i] == tableEndBytes[endCount])) {

						endCount++;
						if (endCount == endCountMax) {
							isStart = false;

							endOffset = i - startOffset + 1;

							endCount = 0;

							System.out.println("结束");
							fos.write(buffer, startOffset, endOffset);

						}
					} else {
						startCount = 0;
						endCount = 0;
					}
				}
				if (isStart) {
					fos.write(buffer, startOffset, endOffset);
					fos.flush();
				}
			}

			is.close();
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("连接流出错");
			return false;
		}
	}

	@Override
	public List<Map<String, String>> ParseFile(boolean isCancel) {
		List<Map<String, String>> mList = null;
		byte[] buffer = new byte[10240];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(cacheFile);
			String[] tableStarts = new String[5];
			String[] tableEnds = new String[5];
			tableStarts[0] = "entitle=\"";
			tableEnds[0] = "\"";

			tableStarts[1] = "entitle=\"";
			tableEnds[1] = "\"";

			tableStarts[2] = "entitle=\"";
			tableEnds[2] = "\"";

			tableStarts[3] = "/down.so?";
			tableEnds[3] = "')";

			tableStarts[4] = "<td>";
			tableEnds[4] = "</td>";

			mList = FindByte(fis, buffer, tableStarts, tableEnds, isCancel);

			fis.close();
		} catch (Exception e) {
			System.out.println("文件找不到");
		}
		return mList;
	}

	public List<Map<String, String>> FindByte(InputStream is, byte[] buffer, String[] tableStarts, String[] tableEnds, boolean isCancel) {
		List<Map<String, String>> listView = new ArrayList<Map<String, String>>();
		Map<String, String> mMap = new HashMap<String, String>();
		int len = 0;

		int count = 0;
		int countMax = tableStarts.length;

		byte[] tableStartBytes = tableStarts[count].getBytes();
		byte[] tableEndBytes = tableEnds[count].getBytes();

		// 开始结束起始位置
		int startOffset = 0;
		int endOffset = 0;

		// 开始结束计数器
		int startCount = 0;
		int endCount = 0;

		// 开始结束最大计数器
		int startCountMax = tableStartBytes.length;
		int endCountMax = tableEndBytes.length;

		boolean isStart = false;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			while ((len = is.read(buffer)) != -1) {
				if (isCancel) {
					bos.close();
					return null;
				}
				startOffset = 0;
				endOffset = len;// 结束
				for (int i = 0; i < len; i++) {
					if (!isStart && (buffer[i] == tableStartBytes[startCount])) {
						startCount++;
						if (startCount == startCountMax) {
							isStart = true;

							startOffset = i;
							endOffset = len - startOffset;

							startCount = 0;
							System.out.println("开始");

							switch (count) {
							case 0:

								break;
							case 1:

								break;
							case 2:

								break;
							case 3:
								bos.write(tableStartBytes, 0, startCountMax - 1);
								break;
							case 4:
								break;
							default:
								break;
							}

						}
					} else if (isStart && (buffer[i] == tableEndBytes[endCount])) {

						endCount++;
						if (endCount == endCountMax) {
							isStart = false;

							endOffset = i - startOffset + 1;

							endCount = 0;

							System.out.println("结束" + endOffset);
							bos.write(buffer, startOffset, endOffset);
							bos.close();
							byte[] result = bos.toByteArray();
							int length = result.length;
							switch (count) {
							case 0:
								mMap.put("title", URLDecoder.decode(new String(result, 1, length - 2), "GB2312"));
								count++;
								break;
							case 1:
								mMap.put("article", URLDecoder.decode(new String(result, 1, length - 2), "GB2312"));
								count++;
								break;
							case 2:
								mMap.put("singgers", URLDecoder.decode(new String(result, 1, length - 2), "GB2312"));
								count++;
								break;
							case 3:
								mMap.put("download", new String(bos.toByteArray()));
								count++;
								break;
							case 4:
								mMap.put("lrc", new String(result, 1, length - 2));
								count = 0;
								listView.add(mMap);
								mMap = new HashMap<String, String>();
								break;
							default:
								break;
							}

							// 再次初始化数据
							{
								bos = new ByteArrayOutputStream();

								tableStartBytes = tableStarts[count].getBytes();
								tableEndBytes = tableEnds[count].getBytes();

								startCountMax = tableStartBytes.length;
								endCountMax = tableEndBytes.length;
							}

						}
					} else {
						startCount = 0;
						endCount = 0;
					}
				}
				if (isStart) {
					System.out.println(startOffset + "   " + endOffset);
					bos.write(buffer, startOffset, endOffset);
				}
			}
			bos.close();

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO出现异常");
		}
		return listView;
	}

}
