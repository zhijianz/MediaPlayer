package com.feilong.download.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.feilong.download.DownLoadAPI;

public class SogouDownAPI implements DownLoadAPI {

	@Override
	public String getFileUri(String path, boolean isCancel) {
		path = "http://mp3.sogou.com/" + path;
		System.out.println(path);
		// ����URL����
		URL mUrl = null;
		try {
			mUrl = new URL(path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}

		URLConnection conn = null;
		try {
			conn = mUrl.openConnection();
		} catch (IOException e) {
			System.out.println("URL�����쳣");
			return null;
		}

		InputStream is = null;
		try {
			is = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int len = 0;

			ByteArrayOutputStream fos = new ByteArrayOutputStream();

			// �ҵ���ʼ����
			String tableStart = new String("'position=0&url=h");
			int startCount = 0;
			// �ҵ���������
			String tableEnd = new String("')");
			int endCount = 0;
			boolean isStart = false;

			byte[] tableStartBytes = tableStart.getBytes();
			byte[] tableEndBytes = tableEnd.getBytes();

			// ��ʼ������������
			int startCountMax = tableStartBytes.length;
			int endCountMax = tableEndBytes.length;

			int startOffset;
			int endOffset;
			while ((len = is.read(buffer)) != -1) {
				if (isCancel) {
					fos.close();
					is.close();
					return null;
				}
				startOffset = 0;
				endOffset = len;// ����
				for (int i = 0; i < len; i++) {
					if (!isStart && (buffer[i] == tableStartBytes[startCount])) {
						startCount++;
						if (startCount == startCountMax) {
							isStart = true;

							startOffset = i;
							endOffset = len - startOffset;

							startCount = 0;
							System.out.println("��ʼ");

							// fos.write(tableStartBytes,0,startCountMax - 1);
						}
					} else if (isStart && (buffer[i] == tableEndBytes[endCount])) {

						endCount++;
						if (endCount == endCountMax) {
							isStart = false;

							endOffset = i - startOffset + 1;

							endCount = 0;

							System.out.println("����" + endOffset);
							fos.write(buffer, startOffset, endOffset);
							fos.close();
							return new String(fos.toByteArray());
						}
					} else {
						startCount = 0;
						endCount = 0;
					}
				}
				if (isStart) {
					fos.write(buffer, startOffset, endOffset);
				}
			}

			is.close();
		} catch (IOException e) {
			System.out.println("����������");
		}

		return null;
	}
	
	
	@Override
	public String getLrcUri(String path, boolean isCancel) {
		path = "http://mp3.sogou.com" + path;
		// ����URL����
		URL mUrl = null;
		try {
			mUrl = new URL(path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}

		URLConnection conn = null;
		try {
			conn = mUrl.openConnection();
		} catch (IOException e) {
			System.out.println("URL�����쳣");
			return null;
		}

		InputStream is = null;
		try {
			is = conn.getInputStream();
			byte[] buffer = new byte[1024];
			int len = 0;

			ByteArrayOutputStream fos = new ByteArrayOutputStream();

			// �ҵ���ʼ����
			String tableStart = new String("href=\"d");
			int startCount = 0;
			// �ҵ���������
			String tableEnd = new String("\" ");
			int endCount = 0;
			boolean isStart = false;

			byte[] tableStartBytes = tableStart.getBytes();
			byte[] tableEndBytes = tableEnd.getBytes();

			// ��ʼ������������
			int startCountMax = tableStartBytes.length;
			int endCountMax = tableEndBytes.length;

			int startOffset;
			int endOffset;
			while ((len = is.read(buffer)) != -1) {
				if (isCancel) {
					fos.close();
					is.close();
					return null;
				}
				startOffset = 0;
				endOffset = len;// ����
				for (int i = 0; i < len; i++) {
					if (!isStart && (buffer[i] == tableStartBytes[startCount])) {
						startCount++;
						if (startCount == startCountMax) {
							isStart = true;

							startOffset = i;
							endOffset = len - startOffset;

							startCount = 0;
							System.out.println("��ʼ");

							// fos.write(tableStartBytes,0,startCountMax - 1);
						}
					} else if (isStart && (buffer[i] == tableEndBytes[endCount])) {

						endCount++;
						if (endCount == endCountMax) {
							isStart = false;

							endOffset = i - startOffset + 1;

							endCount = 0;

							System.out.println("����" + endOffset);
							fos.write(buffer, startOffset, endOffset);
							fos.close();
							return new String(fos.toByteArray());
						}
					} else {
						startCount = 0;
						endCount = 0;
					}
				}
				if (isStart) {
					fos.write(buffer, startOffset, endOffset);
				}
			}

			is.close();
		} catch (IOException e) {
			System.out.println("����������");
		}

		return null;
	}
}
