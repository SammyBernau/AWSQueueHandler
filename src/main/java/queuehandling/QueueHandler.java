package queuehandling;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import database.Query;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static mask.MaskPII.generateHash;

public class QueueHandler {
//    private static final String QUEUE_NAME = "testQueue" + new Date().getTime();
    private static final String QUEUE_URL = "http://localhost:4566/000000000000/login-queue";

    private static AmazonSQS getSqsClient(){
        AWSCredentials credentials = new BasicAWSCredentials(
                "<AWS accesskey>",
                "<AWS secretkey>"
        );

        return AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.US_EAST_1)
                .build();
    }
    public static void handleQueue() throws NoSuchAlgorithmException, InvalidKeySpecException, SQLException {
        String checkCol = Query.build()
                .checkCol("user_logins", "app_version");

        if(!checkCol.equals("varchar")){
            int alterCol =  Query.build()
                    .alterTable("user_logins")
                    .alterColumn("app_version","varchar(32)")
                    .executeUpdate();
        }
        //ALTER TABLE user_logins_new ALTER COLUMN app_version TYPE varchar(32);
        AmazonSQS sqs = getSqsClient();

        //These are updated but never accessed; however, future update should be made to save them
        HashMap<String, String> deviceIdList = new HashMap<>();
        HashMap<String, String> ipList = new HashMap<>();

        while (true) {

                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
                receiveMessageRequest.setQueueUrl(QUEUE_URL);
                receiveMessageRequest.setMaxNumberOfMessages(10);
                receiveMessageRequest.setWaitTimeSeconds(20);

                ReceiveMessageResult result = sqs.receiveMessage(receiveMessageRequest);
                List<Message> messages = result.getMessages();

                for (Message message : messages) {
                    // process the message as a JSON object
                    String jsonBody = message.getBody();
                    System.out.println(jsonBody);
                    JsonObject messageJsonObject = new Gson().fromJson(jsonBody, JsonObject.class);

                    if (messageJsonObject.has("user_id")) {

                        //Each column and its value will be trimmed to remove redundant quotes and then checked if it equals "ul" which means null
                        String userId = checkForNull(trimQuotes(String.valueOf(messageJsonObject.get("user_id"))));

                        String deviceType = checkForNull(trimQuotes(String.valueOf(messageJsonObject.get("device_type"))));

                        String appVersion = checkForNull(trimQuotes(String.valueOf(messageJsonObject.get("app_version"))));

                        String locale = checkForNull(trimQuotes(String.valueOf(messageJsonObject.get("locale"))));

                        String deviceId = checkForNull(trimQuotes(String.valueOf(messageJsonObject.get("device_id"))));

                        String ip = checkForNull(trimQuotes(String.valueOf(messageJsonObject.get("ip"))));

                        //convert the desired fields to hashes and store them in respective HashMaps that can be saved for later in a separate database or secure file
                        //Encryption could also be used here to fully revert the masked field
                        String maskedDeviceId = generateHash(deviceId);
                        deviceIdList.put(maskedDeviceId, deviceId);

                        String maskedIp = generateHash(ip);
                        ipList.put(maskedIp, ip);

                        //user_id, device_type, masked_ip, masked_device_id, locale, app_version, create_date
                        LocalDate localDate = LocalDate.now();
                        Query insert = (Query) Query.build()
                                .insert("user_logins",
                                        userId,
                                        deviceType,
                                        maskedIp,
                                        maskedDeviceId,
                                        locale,
                                        appVersion,
                                        localDate)
                                .execute();

                    } else {
                        continue;
                    }
                    sqs.deleteMessage(QUEUE_URL, message.getReceiptHandle());
                }
        }
    }

    //Removes redundant quotes
    public static String trimQuotes(String s){
        return s.substring(1, s.length()-1);
    }

    //Checks column value
    public static String checkForNull(String s){
        if (s.equals("ul"))
            s = "null";
        return s;
    }


}
