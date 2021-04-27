## This repo contains the code for the UI used in the following paper:
  @inproceedings{ren2021wizard,\
  title={Wizard of Search Engine: Access to Information Through Conversations with Search Engines},\
  author={Ren, Pengjie and Liu, Zhongkun and Song, Xiaomeng and Tian, Hongtao and Chen, Zhumin and Ren, Zhaochun and de Rijke, Maarten},\
  booktitle={Proceedings of the 44th International ACM SIGIR Conference on Research and Development in Information Retrieval},\
  year={2021}\
  }

## frontend
+ requirements: node.js, npm
+ run: 
  - cd front-end directory
  - install: `npm install`
  - run dev mode: `npm run dev`
  - run build mode: `npm run build`

## backend
+ requirements: java8, mysql5.7, maven
+ run (dev mode, use compiled frontend):
   1. build front-end, then copy `chat-labelling-frontend/dist` to `chat-labelling-backend/src/main/resources/static`
   2. start your mysql server
   3. run `chat-labelling-backend/src/main/java/com/sdu/irlab/chatlabelling/ChatLabellingApplication.main` with arguments:
         `--dbHost=${your_mysqlhost} --dbPort=${your_mysqlport} --dbUser=${your_mysqluser} --dbPass=${your_mysqlpass} --actionFile=${absolutePath_to_actionFile}  --backgroundFile=${absolutePath_to_backgroundFile} --serverPort=${server_port} --instructionFile=${instruction_file}`
   4. the server will auto create the database (if not exists) and run on the server your've configured

+ run (dev mode, dev mode frontend):
   1. configure your backend host and port in the proxy table of `chat-labelling-frontend/config/index.js`
   2. start mysql server and run main method follow step 2 and 3 in **run (dev mode, use compiled frontend)**

+ run in production mode:
   1. build and copy the front end as step 1 in **run (dev mode, use compiled frontend)**
   2. `cd chat-labelling-backend`
   3. compile the project, `mvn clean package -Dmaven.test.skip=true`, the argument `-Dmaven.test.skip=true` will skip all tests when compiling
   4. compiled *.war will is `chat-labelling-backend/target/chat-labelling-0.0.1-SNAPSHOT.war`
   5. start your mysql server
   6. `java -jar path_to_war_file.war --dbHost=${your_mysqlhost} --dbPort=${your_mysqlport} --dbUser=${your_mysqluser} --dbPass=${your_mysqlpass} --actionFile=${absolutePath_to_actionFile}  --backgroundFile=${absolutePath_to_backgroundFile} --serverPort=${server_port} --instructionFile=${instruction_file} --searchResultConigFile=${search_result_conig_file}`
   
## demo resource files
+ demo action file and background file are in `/resources`
+ you can change the actions and backgrounds as you like
+ you can **not** modify the json keys or json structure of the file.
+ `instruction.html` is the instruction file shown on the user interface, you can change it as you like.
+ `searchResultConfig.json` is for configure which results are available for which actions.

## modify system status
+ background index and labelling turn can be modified, and will be available when a new conversation is needed.
+ wait seconds is the waiting duration when one of the conversation partner is offline. it can be modified and will be available immediately. (users already waiting for partner will not be affected)
+ delete all system status, and restart the system, the all values will be set to default

 ## proxy application
 + if you will log in the system as a 'sys' user, please first start the proxy application, in order to get search results from the search engine automatically


 ## user guide
 + please follow `USER_GUIDE.MD` 







   

