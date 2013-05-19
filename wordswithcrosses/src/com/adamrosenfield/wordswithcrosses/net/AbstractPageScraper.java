package com.adamrosenfield.wordswithcrosses.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adamrosenfield.wordswithcrosses.PuzzleDatabaseHelper;
import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;

public abstract class AbstractPageScraper extends AbstractDownloader {

	private static final String REGEX = "http://[^ ^']*\\.puz";
	private static final String REL_REGEX = "href=\"(.*\\.puz)\"";
	private static final Pattern PAT = Pattern.compile(REGEX);
	private static final Pattern REL_PAT = Pattern.compile(REL_REGEX);

	protected AbstractPageScraper(String downloaderName) {
	    super("", downloaderName);
	}

	@Override
    protected String createUrlSuffix(Calendar date) {
	    return "";
	}

	protected abstract String getScrapeURL(Calendar date);

	@Override
    public boolean download(Calendar date) throws IOException {
	    return scrapePage(date, getScrapeURL(date));
	}

	protected boolean scrapePage(Calendar date, String url) throws IOException {
	    String scrapedPage = downloadUrlToString(url);

        List<String> puzzleUrls = getPuzzleURLs(scrapedPage);
        puzzleUrls.addAll(getPuzzleRelativeURLs(url, scrapedPage));

        LOG.info("Found puzzles: " + puzzleUrls);

        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
        for (String puzzleUrl : puzzleUrls) {
            if (!dbHelper.puzzleUrlExists(puzzleUrl)) {
                // TODO: Support scraping more than one puzzle per page
                return super.download(date, puzzleUrl);
            } else {
                LOG.info("Skipping download, already exists in database: " + puzzleUrl);
            }
        }

        return false;
	}

	public static List<String> getPuzzleRelativeURLs(String baseUrl, String input)
			throws MalformedURLException {
		URL base = new URL(baseUrl);
		ArrayList<String> result = new ArrayList<String>();
		Matcher matcher = REL_PAT.matcher(input);

		while (matcher.find()) {
			result.add(new URL(base, matcher.group(1)).toString());
		}

		return result;
	}

	public static List<String> getPuzzleURLs(String input) {
		ArrayList<String> result = new ArrayList<String>();
		Matcher matcher = PAT.matcher(input);

		while (matcher.find()) {
			result.add(matcher.group());
		}

		return result;
	}
}
