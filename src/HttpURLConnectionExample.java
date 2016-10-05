import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpURLConnectionExample {

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {

        HttpURLConnectionExample http = new HttpURLConnectionExample();

        System.out.println("Testing 1 - Send Http GET request");
        http.sendGet();

        System.out.println("\nTesting 2 - Send Http POST request");
       // http.sendPost();

    }

    // HTTP GET request
    private void sendGet() throws Exception {

        //String url = "https://plus-uit.credit-suisse.com/";
        String url =  "https://csplus-nadyac.c9users.io/amazonData.json";

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

    }

    // HTTP POST request
    private void sendPost() throws Exception {

        String url = "https://plus-qa.credit-suisse.com/mobileauth/login.fcc?SMQUERYDATA=-SM-HM3jGoF6Pjohn1DJeAHOSSycnTg45ByXPQFFqN5Kw8ad8fSGSsE6PlBNMuLy1kU7ZET0CyzYElMEe5YII%2FN5JXQm%2Bk9kSV9Ta9J%2FWN3IvJnGirnqJ8Ui5bNV3jej%2B1npYQa4dvfBuH%2FVvMdQv%2BkzFK88%2BaCUUUvXoux2Lx6v7q63rea3C%2BXQF%2BvccQ4NxZ5yT6qA7hEwDmo%2BBGZ7OYepZ9UMULfF4J3dyoNvTeoj8mmltIsrbgy9%2FoHWtzYhS4qFYQNb%2F5xeADllkAnNPrFgrcSwJGLL%2FnwkIT06sgkIftI7E2H2SKH9s10mwiVlERabnfdvvpuj8z97wCdAuwD2TuUciKngtjiINDcFwFEcwvL2jmmXK76vC%2BnJuKUusdkO71JRnJDO9unGmZsX5rheE9x86KvAr4d5";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        String urlParameters = "";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
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

    }

}
