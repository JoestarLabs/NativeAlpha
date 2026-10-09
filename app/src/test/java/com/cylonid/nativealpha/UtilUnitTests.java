package com.cylonid.nativealpha;

import com.cylonid.nativealpha.model.WebApp;
import com.cylonid.nativealpha.util.ShortcutIconUtils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine
 * (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UtilUnitTests {

	@Test
	public void getWidthFromHTMLElementString() {
		assertEquals(192, ShortcutIconUtils.getWidthFromIcon("192x192"));
	}
	public void testShortcutHelper(String base_url, final String expected, final int result_index) {
		WebApp webapp = new WebApp(base_url, Integer.MAX_VALUE);
		ShortcutDialogFragment frag = ShortcutDialogFragment.newInstance(webapp);
		String[] result = frag.fetchWebappData();
		assertEquals(result[result_index], expected);
	}

	@Test
	public void faviconFromWebManifest() {
		WebApp webapp = new WebApp("https://xda-developers.com", Integer.MAX_VALUE);
		ShortcutDialogFragment frag = ShortcutDialogFragment.newInstance(webapp);
		String[] result = frag.fetchWebappData();
		String favicon = result[IconFetchResult.FAVICON.index];
		assertNotNull(favicon);
		assertTrue("Favicon URL should contain favicon and end with .png: " + favicon,
				favicon.contains("favicon") && favicon.endsWith(".png"));
	}

	@Test
	public void faviconWithoutManifest() {
		testShortcutHelper("https://orf.at",
				"https://orf.at/mojo/1_4_1/storyserver//common/images/favicons/favicon-128x128.png",
				IconFetchResult.FAVICON.index);
	}

	@Test
	public void faviconFromMoodleTum() {
		WebApp webapp = new WebApp("https://moodle.tum.de", Integer.MAX_VALUE);
		ShortcutDialogFragment frag = ShortcutDialogFragment.newInstance(webapp);
		String[] result = frag.fetchWebappData();
		String favicon = result[IconFetchResult.FAVICON.index];
		assertNotNull(favicon);
		assertTrue("Favicon URL should contain favicon: " + favicon, favicon.contains("favicon"));
	}

	@Test
	public void faviconNull() {
		testShortcutHelper("https://tugraz.at", null, IconFetchResult.FAVICON.index);
	}

	@Test
	public void faviconNonExistingSite() {
		testShortcutHelper("https://asdfasdfasdfasdf.asdfsdaf", null, IconFetchResult.FAVICON.index);
	}

	@Test
	public void getStartUrlFromWebManifest() {
		testShortcutHelper("https://online.tugraz.at",
				"https://online.tugraz.at/tug_online/ee/ui/ca2/app/desktop/#/login?pwa=1",
				IconFetchResult.NEW_BASEURL.index);
	}

	@Test
	public void getWebAppTitleFromManifest() {
		testShortcutHelper("https://online.tugraz.at", "TUGRAZonline Go", IconFetchResult.TITLE.index);
	}

}
