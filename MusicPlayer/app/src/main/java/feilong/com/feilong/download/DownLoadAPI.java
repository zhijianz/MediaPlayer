package com.feilong.download;

public interface DownLoadAPI {

	public String getFileUri(String path,boolean isCancel);

	String getLrcUri(String path, boolean isCancel);

}
