## Run this project:
- Install java8 and mysql5.7
- Prepare your actions, backgrounds and instructions, demo files are in `/resources`

  **For json files, please do not change the structure or the json keys**
- Run `java -jar path_to_war_file.war --dbHost=${your_mysqlhost} --dbPort=${your_mysqlport} --dbUser=${your_mysqluser} --dbPass=${your_mysqlpass} --actionFile=${absolutePath_to_actionFile} --backgroundFile=${absolutePath_to_backgroundFile} --serverPort=${server_port} --instructionFile=${instruction_file}` in your command line

  **This command will connect to your mysql server and automatically create a database called `chat_labelling`** 
- Login your mysql client, use database `chat_labelling`, add users to the system. For sys user, role='sys', for normal user, role='cus' 

After steps above, just open your browser, type in `http：//{your_server_ip}:{your_server_port}`, and the system is already to use.

**Notice: if you login as a 'sys' user, first run the proxy jar file. This is for collecting data from search engine.**


## Run the proxy:
For users of whose role is 'sys', not only a web browser, but also a proxy application is needed. This application is used to collecting data from search engine.

- first install java8 
- run `java -jar path_to_proxy_file.jar [search_engine] [bing_search_key] [bing_suggest_key]` 
- `search_engine` can only choose from 'bing' or 'baidu'
- if you are using bing search engine, please first get bing search api key and bing suggest api key, and add the params when running the jar file
- if you are using baidu search engine, `bing_search_key` and `bing_suggest_key` are not required


**Notice: the proxy uses port 9191, please make sure this is available.**
