package com.feilong.download;

import java.util.List;
import java.util.Map;

public interface SearchAPI {
	/**
	 * 加载文件
	 * 
	 * @param query
	 * @return
	 */
	public boolean LoadUrlFile(String query, String cacheFile,boolean isCancel);

	/**
	 * 解析文件
	 */
	public List<Map<String, String>> ParseFile(boolean isCancel);
}
