## requirements
- java8, mvn

## build 
- `mvn clean compile assembly:single`, to build the project
- target jar file is `chat-labelling-proxy-server\target\chat-labelling-proxy-server-1.0-jar-with-dependencies.jar`

## run (production mode)
- run `java -jar {path_jar_file}.jar [search_engine] [bing_search_key] [bing_suggest_key]`
- `search_engine` can only choose from 'bing' or 'baidu'
- if you are using bing search engine, please first get bing search api key and bing suggest api key, and add the params when running the jar file
- if you are using baidu search engine, `bing_search_key` and `bing_suggest_key` are not required

## run (dev mode)
- run main method in `App.java` with parameters `[search_engine] [bing_search_key] [bing_suggest_key]`


