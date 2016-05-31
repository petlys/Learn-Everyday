package com.petterlysne.s198579;


import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public final class ContentFactory { // Final hindrer klassen å bli extended

    public static ArrayList<Page> pages;
    private static ContentFactory contentFactory;
    private static String language;

    private ContentFactory() {
        // Hindrer klassen i å bli instansiert
        pages = new ArrayList<>();
        language = Locale.getDefault().getLanguage();

        if(language.equals("nb")) // Unntak for norsk bokmål
            language = "no";

    }

    public static ContentFactory getInstance() {
        if (contentFactory == null)
            contentFactory = new ContentFactory();

        return contentFactory;
    }

    /*
    Kjører API kall mot wikipedia for å finne en ny side med bilde.
    Fortsetter fram til den finner en artikkel med bilde
     */
    public static void addPage() {
        boolean funnetSideMedBilde = false;

        while (!funnetSideMedBilde) {

            Page page = new Page();

            try {

                /////////////////// Hente tittel, pageid og innhold ///////////////////

                URL url = new URL("https://" + language + ".wikipedia.org/w/api.php?action=query&prop=extracts&format=json&exintro=&explaintext=&generator=random&grnnamespace=0");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder s = new StringBuilder();
                for (String line = br.readLine(); line != null; line = br.readLine())
                    s.append(line);

                JsonElement jsonElement = new JsonParser().parse(s.toString()).getAsJsonObject().get("query").getAsJsonObject().get("pages");

                JsonObject jsonObject = jsonElement.getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    page.setContent(entry.getValue().getAsJsonObject().get("extract").toString());
                    page.setPageid(Integer.parseInt(entry.getValue().getAsJsonObject().get("pageid").toString()));
                    page.setTitle(entry.getValue().getAsJsonObject().get("title").toString());
                }

                page.setContent(page.getContent().replace("\\n", "\n\n"));
                page.setContent(page.getContent().replace("\\", ""));
                page.setContent(page.getContent().replace("\"", ""));
                page.setTitle(page.getTitle().replace("\"", ""));

                br.close();

                /////////////////// Hente bilde URL ///////////////////

                url = new URL("https://" + language + ".wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&piprop=original&pageids=" + page.getPageid());
                conn = (HttpURLConnection) url.openConnection();

                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                s = new StringBuilder();
                for (String line = br.readLine(); line != null; line = br.readLine())
                    s.append(line);

                jsonElement = new JsonParser().parse(s.toString()).getAsJsonObject().get("query").getAsJsonObject().get("pages");

                jsonObject = jsonElement.getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
                    if(entry.getValue().getAsJsonObject().has("thumbnail"))
                        page.setImageURL(entry.getValue().getAsJsonObject().get("thumbnail").getAsJsonObject().get("original").toString().replace("\"", ""));

                br.close();

                if (!page.getImageURL().equals("")) {
                    funnetSideMedBilde = true;
                    pages.add(page);
                }

            }

            catch (Exception e) {
                Log.e("Custom", e.toString());
                if(e instanceof ConnectException || e instanceof UnknownHostException) {

                }
            }
        }
    }
}
