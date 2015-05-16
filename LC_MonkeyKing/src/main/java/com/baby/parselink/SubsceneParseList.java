/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baby.parselink;

import android.os.AsyncTask;

import com.baby.policy.ActionCallback;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tuehm
 */
public class SubsceneParseList {

    private static final List<String> langs = Arrays
            .asList("Arabic|Brazillian Portuguese|Danish|Dutch|English|Farsi/Persian|Finnish|French|Indonesian|Italian|Norwegian|Spanish|Swedish|Vietnamese|Albanian|Azerbaijani|Bengali|Big 5 code|Bosnian|Bulgarian|Bulgarian/ English|Burmese|Catalan|Chinese BG code|Croatian|Czech|Dutch/ English|English/ German|Esperanto|Estonian|Georgian|German|Greek|Greenlandic|Hebrew|Hindi|Hungarian|Hungarian/ English|Icelandic|Japanese|Korean|Kurdish|Latvian|Lithuanian|Macedonian|Malay|Manipuri|Polish|Portuguese|Romanian|Russian|Serbian|Sinhala|Slovak|Slovenian|Tagalog|Tamil|Telugu|Thai|Turkish|Ukranian|Urdu"
                    .split("\\|"));
    private static final List<String> codes = Arrays
            .asList("ar|pt-BR|da|nl|en|fa|fi|fr|id|it|no|es|sv|vi|sq|az|bn|b5c|bs|bg|bg|my|ca|zh|hr|cs|nl|en|eo|et|ka|de|el|kl|he|hi|hu|hu|is|ja|ko|ku|lv|lt|mk|ms|mni|pl|pt|ro|ru|sr|si|sk|sl|tl|ta|te|th|tr|uk|ur"
                    .split("\\|"));

    public static class SubsceneObject {

        public String title;
        public String lang;
        public String link;

        public SubsceneObject(String link, String lang, String title) {
            if (!link.startsWith("http://")) {
                link = "http://subscene.com" + link;
            }
            this.link = link;
            this.lang = lang;
            this.title = title;
        }
    }

    public static void getSubsceneData(String link,
            final ActionCallback<Map<String, List<SubsceneObject>>> onComplete) {
        getTextFromLink(link, new ActionCallback<String>() {
            @Override
            public void onComplete(String data) {
                Map<String, List<SubsceneObject>> result = new HashMap<String, List<SubsceneObject>>();

                data = data.replaceAll(".*<tbody>(.*)</tbody>.*", "$1").replaceAll("\\s{1,}", " ")
                        .replaceAll("</tr>", "\n");

                try {
                    Pattern pattern = Pattern
                            .compile("<td class=\"a1\">.*<a[^>]*href=\"([^\"]*)\"[^>]*>.*<span[^>]*>(.*)</span>.*<span[^>]*>(.*)</span>.*</a>.*</td>");
                    Matcher matcher = pattern.matcher(data);
                    while (matcher.find()) {
                        String langLink = matcher.group(1).trim();
                        String language = matcher.group(2).trim();
                        String title = matcher.group(3).trim();
                        List<SubsceneObject> subs = result.get(language);
                        if (subs == null) {
                            subs = new ArrayList<SubsceneObject>();
                            result.put(language, subs);
                        }
                        subs.add(new SubsceneObject(langLink, language, title));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (onComplete != null) {
                    onComplete.onComplete(result);
                }
            }
        });
    }

    public static void getDownloadLink(String link, final ActionCallback<String> onComplete) {
        getTextFromLink(link, new ActionCallback<String>() {
            @Override
            public void onComplete(String data) {
                String downloadLink = data
                        .replaceAll(
                                ".*<div class=\"download\">.*<a[^>]*href=\"([^\"]*)\"[^>]*downloadButton[^>]*>.*",
                                "$1");
                if (!downloadLink.startsWith("http://")) {
                    downloadLink = "http://subscene.com" + downloadLink;
                }

                if (onComplete != null) {
                    onComplete.onComplete(downloadLink);
                }
            }
        });
    }

    public static void getTextFromLink(final String url, final ActionCallback<String> onComplete) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String result = "";
                try {
                    HttpURLConnection.setFollowRedirects(true);
                    URL u = new URL(url);
                    HttpURLConnection huc = (HttpURLConnection) u.openConnection();
                    huc.setRequestMethod("GET");
                    huc.setConnectTimeout(10000);
                    huc.setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
                    huc.connect();
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            huc.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    result = response.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (onComplete != null) {
                    onComplete.onComplete(s);
                }
            }
        }.execute();
    }

    public static String getCode(String lang) {
        int index = langs.indexOf(lang);
        if (index > -1) {
            return codes.get(index);
        }
        return "";
    }

    public static String readFile(String filename, String encoding) {
        StringBuilder sb = new StringBuilder();
        try {
            File fileDir = new File(filename);
            BufferedReader in;
            if (encoding == null || encoding.isEmpty()) {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir)));
            } else {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir),
                        encoding));
            }

            String str;

            while ((str = in.readLine()) != null) {
                sb.append(str).append("\n");
            }

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String stringFromSubFile(String fileName, String lang) {
        String encoding = null;
        try {

            byte[] buf = new byte[4096];
            java.io.FileInputStream fis = new java.io.FileInputStream(fileName);

            UniversalDetector detector = new UniversalDetector(null);

            int nread;
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            encoding = detector.getDetectedCharset();
            detector.reset();
            if (encoding != null) {
                return readFile(fileName, encoding);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String code = getCode(lang);
        switch (code) {
            case "ar":
                encoding = "Windows-1256";
                break;
            case "vi":
                encoding = "Windows-1258";
                break;
            case "nl":
            case "it":
            case "de":
            case "eo":
                encoding = "ISO 8859-3";
                break;
            case "da":
            case "fi":
            case "no":
            case "et":
            case "kl":
            case "lv":
            case "lt":
                encoding = "ISO 8859-4 ";
                break;
            case "ru":
            case "bg":
                encoding = "ISO 8859-5";
                break;
            case "el":
                encoding = "ISO 8859-7";
                break;
            case "fr":
            case "es":
            case "sv":
            case "pt":
                encoding = "ISO 8859-9";
                break;
            case "bn":
                encoding = "";// kCFStringEncodingMacBengali
                break;
            case "is":
            case "pt-br":
                encoding = "ISO 8859-15";
                break;
            case "ro":
            case "sg":
            case "cs":
            case "hu":
            case "pl":
            case "sr":
            case "sk":
            case "sl":
                encoding = "Windows-1250";
                break;
            case "mk":
                encoding = "Windows-1251";
                break;
            case "tr":
                encoding = "Windows-1254";
                break;
            case "he":
                encoding = "Windows-1255";
                break;
            case "ja":
                encoding = "Windows-932";
                break;
            case "ko":
                encoding = "Windows-949";
                break;
            case "th":
                encoding = "ISO 8859-11";
                break;

        }
        return readFile(fileName, encoding);
    }
}
