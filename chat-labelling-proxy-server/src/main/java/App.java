import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.iki.elonen.NanoHTTPD;

public class App extends NanoHTTPD {
    private ObjectMapper objectMapper = new ObjectMapper();
    private String searchEngine;
    private BaiduSearch baiduSearch = null;
    private BingSearch bingSearch = null;

    public static void main(String[] args) {
        try {
            new App(9191, args[0], args[0].equals("bing") ? args[1] : null, args[0].equals("bing") ? args[2] : null);
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        } catch (ArrayIndexOutOfBoundsException ioe) {
            System.err.println("Please add parameters!");
        }
    }

    public App(int port, String searchEngine, String bingSearchKey, String bingSuggestKey) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Running proxy server on http://localhost:" + port + "/ \n");
        if (searchEngine.equals("baidu")) {
            baiduSearch = new BaiduSearch();
        }
        if (searchEngine.equals("bing")) {
            if (bingSearchKey == null || bingSuggestKey == null) {
                System.out.println("Available bing api keys is needed!");
            }
            bingSearch = new BingSearch(bingSearchKey, bingSuggestKey);
        }
        this.searchEngine = searchEngine;
        if (baiduSearch == null && bingSearch == null) {
            System.out.println("Search engine config is needed!");
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "{}";
        Map<String, String> parms = session.getParms();
        String query = parms.get("query");
        if (searchEngine.equals("baidu")) {
            msg = baiduSearch.searchStates(query);
        }
        if (searchEngine.equals("bing")) {
            msg = bingSearch.searchStates(query);
        }
        if (parms.get("callback") != null) {
            msg = parms.get("callback") + "(" + msg + ")";
        }
        return newFixedLengthResponse(Response.Status.OK, "text/javascript; charset=UTF-8", msg);
    }

}