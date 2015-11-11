package com.feilong.download;

import java.util.List;
import java.util.Map;

public interface SearchAPI {
	/**
	 * �����ļ�
	 * 
	 * @param query
	 * @return
	 */
	public boolean LoadUrlFile(String query, String cacheFile,boolean isCancel);

	/**
	 * �����ļ�
	 */
	public List<Map<String, String>> ParseFile(boolean isCancel);
}
