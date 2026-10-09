package com.cylonid.nativealpha.util;

import com.cylonid.nativealpha.model.AdblockConfig;

import java.util.ArrayList;

public class Const {
	public static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:137.0) Gecko/20100101 Firefox/137.0";

	public static final String INTENT_WEBAPPID = "webappID";
	public static final String INTENT_BACKUP_RESTORED = "backup_restored";
	public static final String INTENT_WEBAPP_CHANGED = "webapp_changed";
	public static final String INTENT_REFRESH_NEW_THEME = "theme_changed";

	public static final int NO_CONTAINER = -1;

	public static final int PERMISSION_RC_LOCATION = 123;
	public static final int PERMISSION_RC_STORAGE = 132;
	public static final int PERMISSION_CAMERA = 100;
	public static final int PERMISSION_AUDIO = 101;

	public static final int CODE_OPEN_FILE = 512;
	public static final int CODE_WRITE_FILE = 4096;

	public static final int FAVICON_MIN_WIDTH = 16;

	public static ArrayList<AdblockConfig> getDefaultAdBlockConfig() {
		ArrayList<AdblockConfig> list = new ArrayList<>();
		list.add(new AdblockConfig("Fanboy Ultimate List", "https://fanboy.co.nz/r/fanboy-ultimate.txt"));
		return list;
	}
}
