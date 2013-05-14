package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Map;

import com.adamrosenfield.wordswithcrosses.WordsWithCrossesApplication;
import com.adamrosenfield.wordswithcrosses.io.NewsdayPlaintextIO;

/**
 * Newsday Crossword
 * URL: http://www.brainsonly.com/servlets-newsday-crossword/newsdaycrossword?date=YYMMDD
 * Date = Daily
 */
public class NewsdayDownloader extends AbstractDownloader
{
    private static final String NAME = "Newsday";

    DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    NumberFormat nf = NumberFormat.getInstance();

    public NewsdayDownloader()
    {
        super("http://www.brainsonly.com/servlets-newsday-crossword/", NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates()
    {
        return DATE_DAILY;
    }

    @Override
    protected boolean download(Calendar date, String urlSuffix, Map<String, String> headers)
        throws IOException
    {
        URL url = new URL(this.baseUrl + urlSuffix);

        LOG.info("Downloading " + url);

        String filename = getFilename(date);
        File txtFile = new File(WordsWithCrossesApplication.TEMP_DIR, filename);
        if (!utils.downloadFile(url, headers, txtFile, true, getName()))
        {
            return false;
        }

        File destFile = new File(WordsWithCrossesApplication.CROSSWORDS_DIR, filename);
        try
        {
            NewsdayPlaintextIO.convertNewsdayPuzzle(txtFile, destFile, date);
        }
        finally
        {
            txtFile.delete();
        }

        return true;
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return ("newsdaycrossword?date=" +
                (date.get(Calendar.YEAR) % 100) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)));
    }
}
