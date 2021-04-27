import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.cognitiveservices.search.autosuggest.BingAutoSuggestSearchAPI;
import com.microsoft.azure.cognitiveservices.search.autosuggest.BingAutoSuggestSearchManager;
import com.microsoft.azure.cognitiveservices.search.autosuggest.models.SearchAction;
import com.microsoft.azure.cognitiveservices.search.autosuggest.models.Suggestions;
import com.microsoft.azure.cognitiveservices.search.websearch.BingWebSearchAPI;
import com.microsoft.azure.cognitiveservices.search.websearch.BingWebSearchManager;
import com.microsoft.azure.cognitiveservices.search.websearch.models.Query;
import com.microsoft.azure.cognitiveservices.search.websearch.models.SearchResponse;
import com.microsoft.azure.cognitiveservices.search.websearch.models.WebPage;

import java.util.*;

public class BingSearch {

    // Enter a valid subscription key.
    private String subscriptionSearchKey;
    private String subscriptionSuggestKey;
    private BingWebSearchAPI bingWebSearchAPI;
    private BingAutoSuggestSearchAPI bingAutoSuggestSearchAPI;
    private ObjectMapper objectMapper = new ObjectMapper();

    public BingSearch(String subscriptionSearchKey, String subscriptionSuggestKey) {
        this.subscriptionSearchKey = subscriptionSearchKey;
        this.subscriptionSuggestKey = subscriptionSuggestKey;
        this.bingWebSearchAPI = BingWebSearchManager.authenticate(subscriptionSearchKey);
        this.bingAutoSuggestSearchAPI = BingAutoSuggestSearchManager.authenticate(subscriptionSuggestKey);
    }


    public String searchStates(String query) {
        Map<String, Object> results = new LinkedHashMap<>();//普通hashmap会导致最终json的顺序改变
        int count = 0;
        SearchResponse webData = bingWebSearchAPI.bingWebs().search()
                .withQuery(query)
                .withCount(10)
                .execute();
        Suggestions suggestions = bingAutoSuggestSearchAPI.bingAutoSuggestSearch().autoSuggest()
                .withQuery(query).execute();


        List<Map<String, String>> suggest = new LinkedList<>();
        //相关搜索
        if (webData != null && webData.relatedSearches() != null && webData.relatedSearches().value() != null &&
                webData.relatedSearches().value().size() > 0) {
            for (Query r : webData.relatedSearches().value()) {
                Map<String, String> map = new HashMap<>();
                map.put("title", r.text());
                map.put("from", "bing related search");
                map.put("id", "suggest-" + count);
                count++;
                suggest.add(map);
            }
        }

        if (suggestions != null && suggestions.suggestionGroups() != null && suggestions.suggestionGroups().size() > 0) {
            for (SearchAction suggestion : suggestions.suggestionGroups().get(0).searchSuggestions()) {
                Map<String, String> map = new HashMap<>();
                map.put("title", suggestion.query());
                map.put("id", "suggest-" + count);
                map.put("from", "bing search suggestion");
                count++;
                suggest.add(map);
            }
        }

        results.put("Suggest", suggest);

        List<Map<String, String>> answer = new ArrayList<>();
        if (webData != null && webData.webPages() != null && webData.webPages().value() != null &&
                webData.webPages().value().size() > 0) {
            for (WebPage r : webData.webPages().value()) {
                Map<String, String> map = new HashMap<>();
                map.put("title", r.name());
                map.put("content", r.snippet());
                map.put("link", r.url());
                map.put("from", "bing search result");
                map.put("id", "answer-" + count);
                count++;
                answer.add(map);
            }
        }
        results.put("Answer", answer);

        try {
            return objectMapper.writeValueAsString(results);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
