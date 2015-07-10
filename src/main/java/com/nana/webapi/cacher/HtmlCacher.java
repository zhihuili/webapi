package com.nana.webapi.cacher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HtmlCacher {
	public static Map<String,String> HTMLCACHE = new ConcurrentHashMap<String, String>();
			
}
