/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    public void download(Calendar date) throws IOException {
        scrapePage(date, getScrapeURL(date));
    }

    protected void scrapePage(Calendar date, String url) throws IOException {
        String scrapedPage = downloadUrlToString(url);

        List<String> puzzleUrls = getPuzzleURLs(scrapedPage);
        puzzleUrls.addAll(getPuzzleRelativeURLs(url, scrapedPage));

        LOG.info("Found puzzles: " + puzzleUrls);

        PuzzleDatabaseHelper dbHelper = WordsWithCrossesApplication.getDatabaseHelper();
        for (String puzzleUrl : puzzleUrls) {
            if (!dbHelper.puzzleURLExists(puzzleUrl)) {
                // TODO: Support scraping more than one puzzle per page
                super.download(date, puzzleUrl);
                return;
            } else {
                LOG.info("Skipping download, already exists in database: " + puzzleUrl);
            }
        }

        if (puzzleUrls.isEmpty()) {
            throw new IOException("No puzzles to scrape");
        } else {
            throw new IOException("No new puzzles to scrape");
        }
    }

    public static List<String> getPuzzleRelativeURLs(String baseUrl, String input)
            throws MalformedURLException {
        URL base = new URL(baseUrl);
        ArrayList<String> result = new ArrayList<>();
        Matcher matcher = REL_PAT.matcher(input);

        while (matcher.find()) {
            result.add(new URL(base, matcher.group(1)).toString());
        }

        return result;
    }

    public static List<String> getPuzzleURLs(String input) {
        ArrayList<String> result = new ArrayList<>();
        Matcher matcher = PAT.matcher(input);

        while (matcher.find()) {
            result.add(matcher.group());
        }

        return result;
    }
}
