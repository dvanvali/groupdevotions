package com.groupdevotions.server.util;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class VerifyRecaptcha {

    public static final String url = "https://www.google.com/recaptcha/api/siteverify";
    private final static String USER_AGENT = "Mozilla/5.0";

    public static boolean verify(String gRecaptchaResponse, String recaptchaSecret) throws IOException {
        if (gRecaptchaResponse == null || "".equals(gRecaptchaResponse)) {
            return false;
        }

        try{
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String postParams = "secret=" + recaptchaSecret + "&response="
                    + gRecaptchaResponse;

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //parse JSON response and return 'success' value
            JsonReader jsonReader = new JsonReader(new StringReader(response.toString()));
            jsonReader.beginObject();
            String name = jsonReader.nextName();
            Boolean success = jsonReader.nextBoolean();
            jsonReader.close();

            return success;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    static class Response {
        public boolean success;
    }
}