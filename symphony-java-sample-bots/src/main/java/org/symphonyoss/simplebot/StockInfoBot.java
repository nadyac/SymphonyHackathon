/*
 *
 * Copyright 2016 The Symphony Software Foundation
 *
 * Licensed to The Symphony Software Foundation (SSF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.symphonyoss.simplebot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.Room;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.RoomListener;
import org.symphonyoss.client.services.RoomService;
import org.symphonyoss.client.services.RoomServiceListener;
import org.symphonyoss.exceptions.AuthorizationException;
import org.symphonyoss.exceptions.InitException;
import org.symphonyoss.exceptions.MessagesException;
import org.symphonyoss.exceptions.RoomException;
import org.symphonyoss.exceptions.SymException;
import org.symphonyoss.symphony.agent.model.*;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.User;
import org.symphonyoss.symphony.pod.model.Stream;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import java.net.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.json.simple.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.exceptions.AttachmentsException;
import org.symphonyoss.symphony.clients.model.SymAttachmentInfo;



/**
 *
 *
 * Simple example of the RoomService.
 *
 * It will send a message to a room through from a stream (property: room.stream)
 * This will create a Room object, which is populated with all room attributes and
 * membership.  Adding a listener, will provide callbacks.
 *
 *
 *
 * REQUIRED VM Arguments or System Properties:
 *
 *        -Dsessionauth.url=https://pod_fqdn:port/sessionauth
 *        -Dkeyauth.url=https://pod_fqdn:port/keyauth
 *        -Dsymphony.agent.pod.url=https://agent_fqdn:port/pod
 *        -Dsymphony.agent.agent.url=https://agent_fqdn:port/agent
 *        -Dcerts.dir=/dev/certs/
 *        -Dkeystore.password=(Pass)
 *        -Dtruststore.file=/dev/certs/server.truststore
 *        -Dtruststore.password=(Pass)
 *        -Dbot.user=bot.user1
 *        -Dbot.domain=@domain.com
 *        -Duser.call.home=frank.tarsillo@markit.com
 *        -Droom.stream=(Stream)
 *
 *
 *
 *
 * Created by Frank Tarsillo on 5/15/2016.
 */
public class StockInfoBot implements RoomServiceListener, RoomListener {


    private final static Logger logger = LoggerFactory.getLogger(StockInfoBot.class);
    private RoomService roomService;

    private final static Pattern    CASHTAG_REGEX  = Pattern.compile("<cash tag=\"([^\"]+)\"/>");
    private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    private SymphonyClient     symClient;
    private Map<String,String> initParams = new HashMap<>();
    //private Chat               chat;

    private static Set<String> initParamNames = new HashSet<>();

  static
    {
        initParamNames.add("sessionauth.url");
        initParamNames.add("keyauth.url");
        initParamNames.add("pod.url");
        initParamNames.add("agent.url");
        initParamNames.add("truststore.file");
        initParamNames.add("truststore.password");
        initParamNames.add("bot.user.cert.file");
        initParamNames.add("bot.user.cert.password");
        initParamNames.add("bot.user.email");
        initParamNames.add("receiver.user.email");
    }


 public StockInfoBot()
        throws Exception
    {
        initParams();
        initAuth();
        //initChat();
    }

    public static void main(String[] args) {
        int returnCode = 0;

        try
        {
            StockInfoBot bot = new StockInfoBot();
            bot.start();
        }
        catch (Exception e)
        {
            returnCode = -1;
            logger.error("Unexpected exception.", e);
        }

        System.exit(returnCode);
    }

    public void start()
        throws Exception
    {
        Thread.sleep(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
    }

     private void initParams()
    {
        for (String initParam : initParamNames)
        {
            String initParamValue = System.getProperty(initParam);

            if (initParamValue == null)
            {
                throw new IllegalArgumentException("Cannot find required property; make sure you're using -D" + initParam + " to run HelloWorldBot");
            }
            else
            {
                initParams.put(initParam, initParamValue);
            }
        }
    }


    public void initAuth() {

        logger.info("Room Example starting...");

        try {

            symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);

            logger.debug("{} {}", System.getProperty("sessionauth.url"),
                               System.getProperty("keyauth.url"));

            AuthorizationClient authClient = new AuthorizationClient(
                    initParams.get("sessionauth.url"),
                    initParams.get("keyauth.url"));

            authClient.setKeystores(
                    initParams.get("truststore.file"),
                    initParams.get("truststore.password"),
                    initParams.get("bot.user.cert.file"),
                    initParams.get("bot.user.cert.password"));

            SymAuth symAuth = authClient.authenticate();

            symClient.init(
                    symAuth,
                    initParams.get("bot.user.email"),
                    initParams.get("agent.url"),
                    initParams.get("pod.url")
            );

            logger.info("Here...");

            //A message to send when the BOT comes online.
            SymMessage aMessage = new SymMessage();
            aMessage.setFormat(SymMessage.Format.TEXT);
            aMessage.setMessage("Hello master, I'm alive again in this room....");

            logger.info("2...");


            Stream stream = new Stream();
            stream.setId(System.getProperty("room.stream"));
            //stream.setId("Bmbt45BgO2mr2Q3recUzCH___qhufjq0dA");

             roomService = new RoomService(symClient);
             roomService.addRoomServiceListener(this);

            Room room = new Room();
            room.setStream(stream);
            room.setId(stream.getId());
            room.setRoomListener(this);

            roomService.joinRoom(room);

            logger.info("3...");

            //Send a message to the room.
            symClient.getMessageService().sendMessage(room, aMessage);


        } catch (RoomException e) {
           logger.error("error",e);
        } catch (MessagesException e) {
            logger.error("error",e);
        } catch (InitException e) {
            logger.error("error",e);
        } catch (AuthorizationException e) {
            logger.error("error",e);
        }

    }


    //Chat sessions callback method.
    public void onChatMessage(Message message) {
        if (message == null)
            return;

        logger.debug("TS: {}\nFrom ID: {}\nSymMessage: {}\nSymMessage Type: {}",
                message.getTimestamp(),
                message.getFromUserId(),
                message.getMessage(),
                message.getMessageType());

    }








    private String[] parseCashTags(String messageText)
    {
        String[]     result = null;
        List<String> temp   = new ArrayList<>();

        if (messageText != null)
        {
            Matcher matcher = CASHTAG_REGEX.matcher(messageText);

            while (matcher.find())
            {
                temp.add(matcher.group(1));
            }
        }

        result = new String[temp.size()];
        result = temp.toArray(result);

        return(result);
    }

    private StringBuffer sendGet() throws Exception {

        //String url = "https://plus-uit.credit-suisse.com/";
        String url =  "https://csplus-nadyac.c9users.io/amazonData.json";

        String USER_AGENT = "Mozilla/5.0";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

        return response;

    }



    private JSONObject jsonParse(String path) throws IOException, ParseException{
        JSONObject resultObject = new JSONObject();
        FileReader reader = new FileReader (path);
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
//            JSONArray result = (JSONArray) jsonObject.get("results");
        resultObject = (JSONObject) jsonObject.get("results");
        return resultObject;
    }
    
  
    private String buildStockMessage(Stock stock)
        throws Exception
    {
        
         TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {

                        public java.security.cert.X509Certificate[] getAcceptedIssuers()
                        {
                            return null;
                        }
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                        {
                            //No need to implement.
                        }
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                        {
                            //No need to implement.
                        }
                    }
            };

            // Install the all-trusting trust manager
            try 
            {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } 
            catch (Exception e) 
            {
                System.out.println(e);
            }
        
        
        
                 StringBuilder result = new StringBuilder();

//       final String path = "/Users/macbookpro/Documents/Symphony/"
//            + "symphony-java-sample-bots/src/main/java/org/symphonyoss/simplebot/test.json";
       String path2 = "https://csplus-nadyac.c9users.io/amazonData.json";
       logger.debug(path2);
       
         URL obj = new URL(path2);
                logger.debug(obj.toString());

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
       con.setRequestMethod("GET");
       con.setRequestProperty("User-Agent", "Mozilla/5.0");
       int responseCode = con.getResponseCode();
              logger.debug("Response Code: "+ responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
              logger.debug(response.toString());

        String jsonString = response.toString();
       JSONParser parser = new JSONParser();
        JSONObject jsonResults = (JSONObject) parser.parse(jsonString);
       JSONObject jsonResultFinal= (JSONObject) jsonResults.get("results");
      
       
//        JSONObject results = jsonParse(path);
                  
       

//        result.append("\n--------------------------------\n");
//        result.append("Symbol: " + stock.getSymbol() + "\n");
//        result.append("Name: " + stock.getName() + "\n");
//        result.append("Currency: " + stock.getCurrency() + "\n");
//        result.append("Stock2 Exchange: " + stock.getStockExchange() + "\n");
//        result.append("Quote: " + String.valueOf(stock.getQuote()) + "\n");
//        result.append("Stats: " + String.valueOf(stock.getStats()) + "\n");
//        result.append("Dividend: " + String.valueOf(stock.getDividend()) + "\n");
        result.append("\n--------------------------------\n");
        result.append("Sector: " + jsonResultFinal.get("412|sector") + "\n");
        logger.debug(jsonResultFinal.get("412|sector").toString());
        result.append("Last Actual Year: " + jsonResultFinal.get("411|lastActualYear") + "\n");
        result.append("Currency: " + jsonResultFinal.get("412|currency") + "\n");
//        JSONObject jsonResultfAndVGrid= (JSONObject) jsonResults.get("411|fAndVGrid");
//                logger.debug(jsonResultfAndVGrid.toString());
//
//        result.append("fAndVGrid: " + jsonResultFinal.get("411|fAndVGrid") + "\n");

//        result.append("Number of Shares (m): " + jsonResultfAndVGrid.get("Number of shares (m)") + "\n");
//        result.append("BV/share (Next Qtr, US$)" + jsonResultfAndVGrid.get("BV/share (Next Qtr, US$)") + "\n");
//        result.append("Net debt (Next Qtr, US$)" + jsonResultfAndVGrid.get("Net debt (Next Qtr, US$)") + "\n");
//        result.append("Dividend yield (%):" + jsonResultfAndVGrid.get("Dividend yield (%)") + "\n");
//        result.append("Price/Sales (ttm) (x)" + jsonResultfAndVGrid.get("Price/Sales (ttm) (x)") + "\n");
//        result.append("Dividend Yield: " + jsonResults.get("482|dividendYield") + "\n");
//        result.append("Currency Abbr: " + jsonResults.get("482|currencyAbbr") + "\n");
//        result.append("Currency Display Scale: " + jsonResults.get("482|currencyDisplayScale") + "\n");
//        result.append("Dividend: " + jsonResults.get("482|dividend") + "\n");
//        result.append("Market Cap: " + jsonResults.get("482|marketCap") + "\n");
//        result.append("Last Actual Year: " + results.get("482|lastActualYear") + "\n" + "<html>Hello, <b>world</b></html>");
//       
        return(result.toString());
    }
    



    @Override
    public void onRoomMessage(SymMessage roomMessage) {

        Room room = roomService.getRoom(roomMessage.getStreamId());

        if(room!=null && roomMessage.getMessage() != null)
            logger.debug("New room message detected from room: {} on stream: {} from: {} message: {}",
                    room.getRoomDetail().getRoomAttributes().getName(),
                    roomMessage.getStreamId(),
                    roomMessage.getFromUserId(),
                    roomMessage.getMessage()

                );

        /*SymMessage aMessage = new SymMessage();
        aMessage.setFormat(SymMessage.Format.TEXT);
        aMessage.setMessage("Don't worry, I got you");

        try {
            symClient.getMessageService().sendMessage(room, aMessage);
        } 
        catch (MessagesException e) {logger.error("error", e);} */

        try
        {
            String messageText = roomMessage.getMessage();

            if (messageText != null)
            {
                String[]           stocks       = parseCashTags(messageText);
                Map<String, Stock> stocksData   = YahooFinance.get(stocks);
                StringBuilder      stockMessage = new StringBuilder();

                for (String stock : stocksData.keySet())
                {
                    Stock  stockData    = stocksData.get(stock);
                    stockMessage.append(buildStockMessage(stockData));

                }

                //sendMessage(stockMessage.toString(), SymMessage.Format.TEXT);
                SymMessage messageSubmission = new SymMessage();
                messageSubmission.setFormat(SymMessage.Format.TEXT);
                messageSubmission.setMessage(stockMessage.toString());

                symClient.getMessageService().sendMessage(room, messageSubmission);
                
//                 String imageLocation =  
//                    "http://sstatic.net/stackoverflow/img/logo.png";
//                SymAttachmentInfo attachInfo = new SymAttachmentInfo();
//                attachInfo.setName("image");
//                SymMessage symMessage = new SymMessage();
//                symMessage.setMessage("Attachment");
//                symMessage.setStreamId(roomMessage.getStreamId());
//                symMessage.setFormat(SymMessage.Format.TEXT);
//                List<SymAttachmentInfo> attachmentList = new ArrayList<>();
//                    try{
//                        logger.debug("Getting image");
//                        attachInfo.setSize(83*1024L);
//                        attachInfo.setName("cs_logo.jpg");
//                        
//                       attachmentList.add(symClient.getAttachmentsClient().postAttachment(symMessage.getStreamId(), new File("/Users/macbookpro/Documents/Symphony/symphony-java-sample-bots/cs_logo.jpg")));
//                       logger.debug("AttachmentList"+attachmentList);
//
//                    }catch(AttachmentsException e){
//                        logger.error("Attachements Exception",e);
//                    }
//                
//                symMessage.setAttachments(attachmentList);
//                                       logger.debug("AttachmentList"+attachmentList);
//
//                Chat chat = symClient.getChatService().getChatByStream(roomMessage.getStreamId());
//                symClient.getMessageService().sendMessage(chat, symMessage);
                
            }
        }
        catch (Exception e)
        {
            logger.error("Unexpected exception.", e);
        }
    }





    @Override
    public void onRoomCreatedMessage(RoomCreatedMessage roomCreatedMessage) {

    }

    @Override
    public void onMessage(SymMessage symMessage) {

    }

    @Override
    public void onNewRoom(Room room) {
        logger.info("Created new room instance from incoming message..{} {}", room.getId(), room.getRoomDetail().getRoomAttributes().getName());
        room.setRoomListener(this);
    }

    @Override
    public void onRoomDeactivatedMessage(RoomDeactivatedMessage roomDeactivatedMessage) {

    }

    @Override
    public void onRoomMemberDemotedFromOwnerMessage(RoomMemberDemotedFromOwnerMessage roomMemberDemotedFromOwnerMessage) {

    }

    @Override
    public void onRoomMemberPromotedToOwnerMessage(RoomMemberPromotedToOwnerMessage roomMemberPromotedToOwnerMessage) {

    }

    @Override
    public void onRoomReactivatedMessage(RoomReactivatedMessage roomReactivatedMessage) {

    }

    @Override
    public void onRoomUpdatedMessage(RoomUpdatedMessage roomUpdatedMessage) {

    }

    @Override
    public void onUserJoinedRoomMessage(UserJoinedRoomMessage userJoinedRoomMessage) {

    }

    @Override
    public void onUserLeftRoomMessage(UserLeftRoomMessage userLeftRoomMessage) {

    }
}

