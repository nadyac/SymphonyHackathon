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
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.simple.*;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.SymAuth;
import org.symphonyoss.client.services.ChatListener;
import org.symphonyoss.exceptions.AuthorizationException;
import org.symphonyoss.exceptions.InitException;
import org.symphonyoss.exceptions.MessagesException;
import org.symphonyoss.exceptions.SymException;
import org.symphonyoss.symphony.agent.model.Message;
import org.symphonyoss.symphony.agent.model.MessageSubmission;
import org.symphonyoss.symphony.clients.AuthorizationClient;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymAttachmentInfo;
import org.symphonyoss.symphony.clients.model.SymUser;
import org.symphonyoss.symphony.pod.model.User;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import java.net.*;

 

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.symphonyoss.exceptions.AttachmentsException;

public class StockInfoBot
    implements ChatListener
{
    private final static Logger log = LoggerFactory.getLogger(StockInfoBot.class);

    private final static Pattern    CASHTAG_REGEX  = Pattern.compile("<cash tag=\"([^\"]+)\"/>");
    private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");


    private SymphonyClient     symClient;
    private Map<String,String> initParams = new HashMap<>();
    private Chat               chat;

    private static Set<String> initParamNames = new HashSet<>();
//    private TextOverlay textOverlayClass = new TextOverlay();
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

    public static void main(String[] args)
    {
        int returnCode = 0;

        try
        {
            StockInfoBot bot = new StockInfoBot();
            bot.start();
        }
        catch (Exception e)
        {
            returnCode = -1;
            log.error("Unexpected exception.", e);
        }

        System.exit(returnCode);
    }

    public StockInfoBot()
        throws Exception
    {
        initParams();
        initAuth();
        initChat();
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

    private void initAuth()
        throws Exception
    {
        symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC);

        log.debug("{} {}", System.getProperty("sessionauth.url"),
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
    }

    private void initChat()
        throws SymException
    {
        this.chat = new Chat();
        chat.setLocalUser(symClient.getLocalUser());
        Set<SymUser> remoteUsers = new HashSet<>();

        remoteUsers.add(symClient.getUsersClient().getUserFromEmail(initParams.get("receiver.user.email")));
        chat.setRemoteUsers(remoteUsers);
        chat.setStream(symClient.getStreamsClient().getStream(remoteUsers));

        chat.registerListener(this);
        symClient.getChatService().addChat(chat);
    }

    private void sendMessage(String message, SymMessage.Format messageFormat)
        throws MessagesException
    {
        SymMessage messageSubmission = new SymMessage();
        messageSubmission.setFormat(messageFormat);
        messageSubmission.setMessage(message);

        symClient.getMessageService().sendMessage(chat, messageSubmission);
    }
    private void sendImage(List <SymAttachmentInfo> attachment)
        throws MessagesException
    {
        SymMessage messageSubmission = new SymMessage();
        messageSubmission.setFormat(SymMessage.Format.MESSAGEML);
        messageSubmission.setAttachments(attachment);
        symClient.getMessageService().sendMessage(chat, messageSubmission);
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
         
       final String path = "/Users/macbookpro/Documents/Symphony/"
            + "symphony-java-sample-bots/src/main/java/org/symphonyoss/simplebot/test.json";
       URL path2 = new URL("http://csplus.nadyac.c9users.io/response.json");
       
        JSONObject results = jsonParse(path);
                  
       
        StringBuilder result = new StringBuilder();

//        result.append("\n--------------------------------\n");
//        result.append("Symbol: " + stock.getSymbol() + "\n");
//        result.append("Name: " + stock.getName() + "\n");
//        result.append("Currency: " + stock.getCurrency() + "\n");
//        result.append("Stock2 Exchange: " + stock.getStockExchange() + "\n");
//        result.append("Quote: " + String.valueOf(stock.getQuote()) + "\n");
//        result.append("Stats: " + String.valueOf(stock.getStats()) + "\n");
//        result.append("Dividend: " + String.valueOf(stock.getDividend()) + "\n");
        result.append("\n--------------------------------\n");
        result.append("Last Update: " + results.get("482|lastUpdateDate") + "\n");
        result.append("Last Price Date: " + results.get("482|lastPriceDate") + "\n");
        result.append("52 Week High: " + results.get("482|fiftyTwoWeekHigh") + "\n");
        result.append("Target Price: " + results.get("482|targetPrice") + "\n");
        result.append("52 Week Low: " + results.get("482|fiftyTwoWeekLow") + "\n");
        result.append("Currency Symbol: " + results.get("482|currencySymbol") + "\n");
        result.append("Current Price: " + results.get("482|currentPrice") + "\n");
        result.append("Market Cap Currency Symbol: " + results.get("482|marketCapCurrSymbol") + "\n");
        result.append("Dividend Yield: " + results.get("482|dividendYield") + "\n");
        result.append("Currency Abbr: " + results.get("482|currencyAbbr") + "\n");
        result.append("Currency Display Scale: " + results.get("482|currencyDisplayScale") + "\n");
        result.append("Dividend: " + results.get("482|dividend") + "\n");
        result.append("Market Cap: " + results.get("482|marketCap") + "\n");
        result.append("Last Actual Year: " + results.get("482|lastActualYear") + "\n" + "<html>Hello, <b>world</b></html>");
       
        return(result.toString());
    }
    

    @Override
    public void onChatMessage(SymMessage message)
    {
        try
        {
            String messageText = message.getMessage();

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

                sendMessage(stockMessage.toString(), SymMessage.Format.TEXT);
                
                
                String imageLocation =  
                    "http://sstatic.net/stackoverflow/img/logo.png";
                
                SymAttachmentInfo attachInfo = new SymAttachmentInfo();
                attachInfo.setName("image");
                SymMessage symMessage = new SymMessage();
                symMessage.setMessage("Attachment");
                symMessage.setStreamId(message.getStreamId());
                symMessage.setFormat(SymMessage.Format.TEXT);
                List<SymAttachmentInfo> attachmentList = new ArrayList<>();
                    try{
                        log.debug("Getting image");
                        attachInfo.setSize(83*1024L);
                        attachInfo.setName("cs_logo.jpg");
                        
                       attachmentList.add(symClient.getAttachmentsClient().postAttachment(symMessage.getStreamId(), new File("/Users/macbookpro/Documents/Symphony/symphony-java-sample-bots/cs_logo.jpg")));
                       log.debug("AttachmentList"+attachmentList);

                    }catch(AttachmentsException e){
                        log.error("Attachements Exception",e);
                    }
                
                symMessage.setAttachments(attachmentList);
                                       log.debug("AttachmentList"+attachmentList);

                Chat chat = symClient.getChatService().getChatByStream(message.getStreamId());
                symClient.getMessageService().sendMessage(chat, symMessage);
                
                
            }
        }
        catch (Exception e)
        {
            log.error("Unexpected exception.", e);
        }
    }

}
///**
// *
// * @author macbookpro
// */
//class TextOverlay extends JPanel {
//
//    private BufferedImage image;
//
//    public TextOverlay() {
//        try {
//            image = ImageIO.read(new URL(
//                    "http://sstatic.net/stackoverflow/img/logo.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        image = process(image);
//    }
//
//    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension(image.getWidth(), image.getHeight());
//    }
//
//    private BufferedImage process(BufferedImage old) {
//        int w = old.getWidth();
//        int h = old.getHeight();
//        BufferedImage img = new BufferedImage(
//                w, h, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2d = img.createGraphics();
//        g2d.drawImage(old, 0, 0, null);
//        g2d.setPaint(Color.red);
//        g2d.setFont(new Font("Serif", Font.BOLD, 20));
//        String s = "Hello, world!";
//        FontMetrics fm = g2d.getFontMetrics();
//        int x = img.getWidth() - fm.stringWidth(s) - 5;
//        int y = fm.getHeight();
//        g2d.drawString(s, x, y);
//        g2d.dispose();
//        return img;
//    }
//
//        protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        g.drawImage(image, 0, 0, null);
//    }
//
//    private static void create() {
//        JFrame f = new JFrame();
//        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        f.add(new TextOverlay());
//        f.pack();
//        f.setVisible(true);
//    }
//
//    public static void main(String[] args) {
//        EventQueue.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//                create();
//            }
//        });
//    }
//}
