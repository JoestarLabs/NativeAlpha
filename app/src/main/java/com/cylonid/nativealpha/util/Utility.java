package com.cylonid.nativealpha.util;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utility {

	public static void setViewAndChildrenEnabled(View view, boolean enabled) {

		view.setClickable(enabled);
		if (enabled) {
			view.setAlpha(1.0f);
		} else {
			view.setAlpha(0.75f);
		}

		if (view instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) view;
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				View child = viewGroup.getChildAt(i);
				setViewAndChildrenEnabled(child, enabled);
			}
		}
	}

	public static void Assert(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	public static String getFileNameFromDownload(String url, String content_disposition, String mime_type) {
		String file_name = null;
		if (content_disposition != null && !content_disposition.equals("")) {
			Pattern pattern = Pattern.compile("attachment; filename=\"(.*)\"; filename\\*=UTF-8''(.*)",
					Pattern.CASE_INSENSITIVE);
			Matcher m = pattern.matcher(content_disposition);
			file_name = m.matches() ? m.group(2) : null;
		}
		if (file_name == null) {
			file_name = URLUtil.guessFileName(url, content_disposition, mime_type);
		}

		return file_name;
	}

}
