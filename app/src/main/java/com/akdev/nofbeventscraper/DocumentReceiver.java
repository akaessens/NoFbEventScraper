package com.akdev.nofbeventscraper;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentReceiver {

    public static org.jsoup.nodes.Document getDocument(String url) {

        org.jsoup.nodes.Document document;

        try {
            // use default android user agent
            String user_agent = "Mozilla/5.0 (X11; Linux x86_64)";

            Log.d("scraperLog", "DocumentReceiver: " + url);

            Connection connection = Jsoup.connect(url).userAgent(user_agent).followRedirects(true);

            Connection.Response response = connection.execute();

            document = response.parse();

            Log.d("scraperLog", "Document title: " + document.title());

            try {
                // accept cookies needed?
                Element form = document.select("form[method=post]").first();
                String action = form.attr("action");

                List<String> names = form.select("input").eachAttr("name");
                List<String> values = form.select("input").eachAttr("value");

                Map<String, String> data = new HashMap<String, String>();

                for (int i = 0; i < names.size(); i++) {
                    data.put(names.get(i), values.get(i));
                }

                document = connection.url("https://mbasic.facebook.com" + action)
                        .cookies(response.cookies())
                        .method(Connection.Method.POST)
                        .data(data)
                        .post();

            } catch (Exception ignore) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return document;
    }
}
