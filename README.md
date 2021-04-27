# This repo contains the code for the UI used in the following paper:
  @inproceedings{ren2021wizard,\
  title={Wizard of Search Engine: Access to Information Through Conversations with Search Engines},\
  author={Ren, Pengjie and Liu, Zhongkun and Song, Xiaomeng and Tian, Hongtao and Chen, Zhumin and Ren, Zhaochun and de Rijke, Maarten},\
  booktitle={Proceedings of the 44th International ACM SIGIR Conference on Research and Development in Information Retrieval},\
  year={2021}\
  }

## Frontend
+ Requirements: node.js, npm
+ Run: 
  - cd front-end directory
  - install: `npm install`
  - run dev mode: `npm run dev`
  - run build mode: `npm run build`

## Backend
+ Requirements: java8, mysql5.7, maven
+ Run (dev mode, use compiled frontend):
   1. Build front-end, then copy `chat-labelling-frontend/dist` to `chat-labelling-backend/src/main/resources/static`.
   2. Start your mysql server.
   3. Run `chat-labelling-backend/src/main/java/com/sdu/irlab/chatlabelling/ChatLabellingApplication.main` with arguments:
         `--dbHost=${your_mysqlhost} --dbPort=${your_mysqlport} --dbUser=${your_mysqluser} --dbPass=${your_mysqlpass} --actionFile=${absolutePath_to_actionFile}  --backgroundFile=${absolutePath_to_backgroundFile} --serverPort=${server_port} --instructionFile=${instruction_file}`
   4. The server will auto create the database (if not exists) and run on the server your've configured.

+ Run (dev mode, dev mode frontend):
   1. Configure your backend host and port in the proxy table of `chat-labelling-frontend/config/index.js`.
   2. Start mysql server and run main method follow step 2 and 3 in **run (dev mode, use compiled frontend)**.

+ Run in production mode:
   1. Build and copy the front end as step 1 in **run (dev mode, use compiled frontend)**.
   2. `cd chat-labelling-backend`.
   3. Compile the project, `mvn clean package -Dmaven.test.skip=true`, the argument `-Dmaven.test.skip=true` will skip all tests when compiling.
   4. Compiled *.war will be `chat-labelling-backend/target/chat-labelling-0.0.1-SNAPSHOT.war`.
   5. Start your mysql server.
   6. `java -jar path_to_war_file.war --dbHost=${your_mysqlhost} --dbPort=${your_mysqlport} --dbUser=${your_mysqluser} --dbPass=${your_mysqlpass} --actionFile=${absolutePath_to_actionFile}  --backgroundFile=${absolutePath_to_backgroundFile} --serverPort=${server_port} --instructionFile=${instruction_file}`
   
## Demo resource files
+ Demo action file and background file are in `/resources`.
+ You can change the actions and backgrounds as you like.
+ You can **not** modify the json keys or json structure of the file.
+ `instruction.html` is the instruction file shown on the user interface, you can change it as you like.

## Modify system status
+ Background index and labelling turn can be modified, and will be available when a new conversation is needed.
+ `Wait seconds` is the waiting duration when one of the conversation partner is offline. it can be modified and will be available immediately (users already waiting for partner will not be affected).
+ Delete all system status, and restart the system, the all values will be set to default.
 
 ## Proxy application
 + If you log in the system as a 'sys' user, please first start the proxy application, in order to get search results from the search engine automatically.
 
 
 ## User guide
 + Please follow `USER_GUIDE.md`.




   


   

