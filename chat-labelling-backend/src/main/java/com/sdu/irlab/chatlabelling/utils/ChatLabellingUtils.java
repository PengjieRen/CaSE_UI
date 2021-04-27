package com.sdu.irlab.chatlabelling.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ChatLabellingUtils {
    public static String SEARCH_DEMO = "{ \"Clarify\": [ { \"id\": \"Clarify-0\", \"title\": \"Malaysian Wildlife Law\", \"content\": \"\" }, { \"id\": \"Clarify-1\", \"title\": \"Strategies to protecting wildlife\", \"content\": \"\" }, { \"id\": \"Clarify-2\", \"title\": \"Wildlife habitat\", \"content\": \"\" } ], \"Recommend\": [ { \"id\": \"Recommend-0\", \"title\": \"Malaysian Wildlife Act 2010 \", \"content\": \"\" }, { \"id\": \"Recommend-1\", \"title\": \"Malaysian Wildlife Act 1972\", \"content\": \"\" }, { \"id\": \"Recommend-2\", \"title\": \"Wildlife Conservation Act\", \"content\": \"\" }, { \"id\": \"Recommend-3\", \"title\": \"Protecting the Malayan Tiger in Thailand\", \"content\": \"\" } ], \"Reveal\": [ { \"id\": \"Reveal-0\", \"link\": \"https://www.geography.org.uk/teaching-resources/singapore-malaysia/Can-Malaysia-protect-any-of-its-endangered-species-from-extinction\", \"title\": \"Can Malaysia protect any of its endangered species from ...\", \"content\": \"Should a country's development be held back by a few wild animals such as the Orangutan? Is Malaysia's forest cover declining? Examine the conflicting evidence ...\" }, { \"id\": \"Reveal-1\", \"title\": \"World Wildlife Day - WWF Malaysia\", \"content\": \"Our animals are going extinct and it is high time that we do something about it. ... Why is it so important to keep the elephants around or conserve what's ...\" }, { \"id\": \"Reveal-2\", \"title\": \"title-Reveal-2\", \"content\": \"This is content Reveal 2--Mon Mar 09 2020 16:18:31 GMT+0100 (中欧标准时间)\" }, { \"id\": \"Reveal-3\", \"title\": \"Sumatran rhinoceros now extinct in Malaysia, say zoologists ...\", \"content\": \"Last of the species in country, a female rhino named Iman, 'died sooner than expected' ... The Sumatran rhinoceros has become extinct in Malaysia, zoologists have announced. ... pain,” said Augustine Tuuga, the director of the Sabah wildlife department. ... Efforts to breed them have so far proved futile.\" }, { \"id\": \"Reveal-4\", \"title\": \"Malaysian wildlife trade hampering conservation efforts | Free ...\", \"content\": \"The lack of enforcement efforts point to a lack of political will among those responsible at national and international levels in protecting wildlife species. ... Malaysia, identified as one of the key transit countries with Port Klang being a ... of expression and without prejudice, FMT tries its best to share reliable ...\" } ] }";

    public static String readFileAsString(String filePath, String encoding) {
        File file = new File(filePath);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String content = new String(filecontent, encoding);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
