import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;

public class BaiduSearch {
    private ObjectMapper objectMapper = new ObjectMapper();

    public String searchStates(String query) {
        String msg = "";
        if (query != null) {
            try {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("wd", query);
                String search = CustomHttpClient.httpRequest("http://www.baidu.com/s", CustomHttpClient.HTTP_GET, paramMap, new HashMap<String, Object>(), false);
                paramMap.clear();
                paramMap.put("json", "1");
                paramMap.put("prod", "pc");
                paramMap.put("from", "pc_web");
                paramMap.put("ie", "utf-8");
                paramMap.put("wd", query);
                String suggest = CustomHttpClient.httpRequest("http://www.baidu.com/sugrec", CustomHttpClient.HTTP_GET, paramMap, new HashMap<String, Object>(), false);
                msg = parseSearch(search, suggest);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        return msg;
    }

    public String parseSearch(String html, String suggestJson) {
        Map<String, Object> results = new LinkedHashMap<>();//普通hashmap会导致最终json的顺序改变
        int count = 0;
        List<Map<String, String>> suggest = new LinkedList<>();
        List<Map<String, String>> answer = new ArrayList<>();

        if (html != null && html.length() > 0) {
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
                map.put("from", "baidu search result");


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
                map.put("from", "baidu related search");
                count++;
                suggest.add(map);
            }
        }

        if (suggestJson != null && suggestJson.length() > 0) {
            try {
                JsonNode node = objectMapper.readTree(suggestJson).get("g");
                if (node != null)
                    for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
                        JsonNode sugNode = it.next();
                        Map<String, String> map = new HashMap<>();
                        map.put("title", sugNode.get("q").asText());
                        map.put("id", "suggest-" + count);
                        map.put("from", "baidu search suggestion");
                        count++;
                        suggest.add(map);
                    }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        results.put("Suggest", suggest);
        results.put("Answer", answer);
        System.out.println(suggest);
        System.out.println(answer);
        try {
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
