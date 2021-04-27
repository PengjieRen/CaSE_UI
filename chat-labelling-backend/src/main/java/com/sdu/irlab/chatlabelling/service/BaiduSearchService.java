package com.sdu.irlab.chatlabelling.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdu.irlab.chatlabelling.utils.CustomHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;

@Service
public class BaiduSearchService extends AbstractSearchService {

    private ObjectMapper objectMapper = new ObjectMapper();

    public String parseSearch(String html, String suggestJson) {
        Map<String, Object> results = new LinkedHashMap<>();//普通hashmap会导致最终json的顺序改变
        int count = 0;
        List<Map<String, String>> suggest = new LinkedList<>();
        List<Map<String, String>> answer = new ArrayList<>();

        if(html!=null&&html.length()>0) {
            Document doc = Jsoup.parse(html);
            Elements resultItems = doc.select("#content_left div.result-op.c-container, #content_left div.result.c-container");
            for (Element resultItem : resultItems) {
                Element link = resultItem.select("h3.t a").first();
                if (link == null)
                    continue;
                link.setBaseUri("https://www.baidu.com/");
                Element content = resultItem.select(".c-abstract").first();
                Map<String, String> map = new HashMap<>();
                map.put("title", link.text());
                map.put("content", content == null ? "Empty abstract" : content.text());
                map.put("link", link.absUrl("href"));
                map.put("id", "answer-" + count);
                map.put("from", "search result");


                //专门针对百度百科的解析
                String resultmu = resultItem.attr("mu");
                if (resultmu.startsWith("https://baike.baidu.com/item/") || resultmu.startsWith("http://baike.baidu.com/item/")) {
                    map.put("content", resultItem.select("div.c-span18.c-span-last p").first().text());
                }
                count++;
                answer.add(map);
            }

            Elements relatedSearchItems = doc.select("#rs th a");
            for (Element relatedSearchItem : relatedSearchItems) {
                Map<String, String> map = new HashMap<>();
                map.put("title", relatedSearchItem.text());
                map.put("id", "suggest-" + count);
                map.put("from", "related search");
                count++;
                suggest.add(map);
            }
        }

        if(suggestJson!=null&&suggestJson.length()>0) {
            try {
                JsonNode node = objectMapper.readTree(suggestJson).get("g");
                for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
                    JsonNode sugNode = it.next();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", sugNode.get("q").asText());
                    map.put("id", "suggest-" + count);
                    map.put("from", "search suggestion");
                    count++;
                    suggest.add(map);
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        results.put("Suggest", suggest);
        results.put("Answer", answer);
        try {
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String searchStates(List<String> states) {

        if (states.size() > 0) {
            String query = String.join(" ", states);
            Map<String, Object> headers = new HashMap<>();

            Map<String, Object> params = new HashMap<>();
            params.put("word", query);

            try {
                String response = CustomHttpClient.httpRequest("http://www.baidu.com/s", CustomHttpClient.HTTP_GET, params, headers, false);
                String sug = getSuggestions(query);
                return parseSearch(response, sug);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";

    }

    public String getSuggestions(String query) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Host", "www.baidu.com");

        Map<String, Object> params = new HashMap<>();
        params.put("ie", "utf-8");
        params.put("pre", "1");
        params.put("p", "3");
        params.put("json", "1");
        params.put("prod", "pc");
        params.put("from", "pc_web");
        params.put("wd", query);
        String response = null;
        try {
            response = CustomHttpClient.httpRequest("https://www.baidu.com/sugrec", CustomHttpClient.HTTP_GET, params, headers, false);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return response;

    }

//    public static void main(String[] args) {
//        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
//        List<String> list = Arrays.asList("红烧肉", "好吃");
//        BaiduSearchService baiduSearchService = new BaiduSearchService();
//        for (int i = 0; i < 1; i++) {
//            System.out.println(baiduSearchService.searchStates(list));
//        }
//    }
}
