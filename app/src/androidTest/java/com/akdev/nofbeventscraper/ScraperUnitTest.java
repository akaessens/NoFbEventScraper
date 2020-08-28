package com.akdev.nofbeventscraper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ScraperUnitTest {

    @Test
    public void testLocation() {


        FbScraper scraper = new FbScraper(null, "");

        String exp = "Deutschland";
        String json = "{'@type': 'Place', 'name': 'Deutschland'}";
        String act = scraper.fixLocation(json);
        assertEquals(exp, act);

        exp = "Example name, Example Street 1, 12345 Example city";
        json = "{'@type': 'Place', 'name': 'Example name', 'address': {'@type': 'PostalAddress', 'addressCountry': 'DE', 'addressLocality': 'Example city', 'postalCode': '12345', 'streetAddress': 'Example Street 1'}}";
        act = scraper.fixLocation(json);
        assertEquals(exp, act);

        exp = "";
        json = "";
        act = scraper.fixLocation(json);
        assertEquals(exp, act);
    }

    @Test
    public void testTimezone() {

        FbScraper scraper = new FbScraper(null, "");

        String exp = "2020-10-23T05:00+02:00";
        String in = "2020-10-23T05:00:00+0200";
        String act = scraper.toZonedDateTime(in).toString();
        assertEquals(exp, act);


        exp = null;
        in = "";
        ZonedDateTime act2 = scraper.toZonedDateTime(in);
        assertEquals(exp, act2);
    }

    @Test
    public void testDescriptionLinks() {

        FbScraper scraper = new FbScraper(null, "");

        String in = "foo @[152580919265:274:MagentaMusik 360] bar";
        String exp = "foo MagentaMusik 360 [m.facebook.com/152580919265] bar";
        String act = scraper.fixDescriptionLinks(in);
        assertEquals(exp, act);

        in = "foo @[152580919265:274:MagentaMusik 360] bar @[666666666666:274:NoOfTheBeast]";
        exp = "foo MagentaMusik 360 [m.facebook.com/152580919265] bar NoOfTheBeast [m.facebook.com/666666666666]";
        act = scraper.fixDescriptionLinks(in);
        assertEquals(exp, act);
    }

    @Test
    public void testURI() throws MalformedURLException, URISyntaxException {

        FbScraper scraper = new FbScraper(null, "");

        String in = "https://www.facebook.com/events/1234324522341432?refsomething";
        String exp = "https://m.facebook.com/events/1234324522341432";
        String act = scraper.fixURI(in);
        assertEquals(exp, act);

        in = "https://de-de.facebook.com/events/1234324522341432/?active_tab=discussion";
        act = scraper.fixURI(in);
        assertEquals(exp, act);
    }
}
