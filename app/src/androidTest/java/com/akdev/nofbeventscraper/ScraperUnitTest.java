package com.akdev.nofbeventscraper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ScraperUnitTest {

    @Test
    public void TestLocation() {

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
    public void TestTimezone() {

        FbScraper scraper = new FbScraper(null, "");

        String exp = "2020-10-23T05:00:00+02:00";
        String in = "2020-10-23T05:00:00+0200";
        String act = scraper.fixTimezone(in);
        assertEquals(exp, act);


        exp = "";
        in = "";
        act = scraper.fixTimezone(in);
        assertEquals(exp, act);

    }

    @Test
    public void TestLinks() {

        FbScraper scraper = new FbScraper(null, "");

        String in = "foo @[152580919265:274:MagentaMusik 360] bar";
        String exp = "foo m.facebook.com/152580919265 (MagentaMusik 360) bar";
        String act = scraper.fixLinks(in);
        assertEquals(exp, act);

        in = "foo @[152580919265:274:MagentaMusik 360] bar @[666666666666:274:NoOfTheBeast]";
        exp = "foo m.facebook.com/152580919265 (MagentaMusik 360) bar m.facebook.com/666666666666 (NoOfTheBeast)";
        act = scraper.fixLinks(in);
        assertEquals(exp, act);

    }
}
