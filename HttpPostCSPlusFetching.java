import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class HttpPostCSPlusFetching {

    public static void main(String[] args) {
        UserLogInInfo userLogInInfo = new UserLogInInfo();

        String companySearchJSONRequest =
                "{\"formulas\" :"
                        + "{"
                        + "\"targetPrice\":"
                        + "{\"ric\":\"TCS.N\",\"field\":\"company.target.price\"},"
                        + "\"rating\":"
                        + "{\"field\":\"comapny.rating\",\"ric\":\"TCS.N\"}"
                        + "}"
                        +"}";

        HttpPostCSPlusFetching http = new HttpPostCSPlusFetching();
        System.out.println("Send Http POST request");
        try {
            http.sendPost();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // HTTP POST request
    private void sendPost() throws Exception {

        String url = "https://plus.credit-suisse.com/mobileauth/login.fcc?SMQUERYDATA=-SM-HM3jGoF6Pjohn1DJeAHOSSycnTg45ByXPQFFqN5Kw8ad8fSGSsE6PlBNMuLy1kU7ZET0CyzYElMEe5YII%2FN5JXQm%2Bk9kSV9Ta9J%2FWN3IvJnGirnqJ8Ui5bNV3jej%2B1npYQa4dvfBuH%2FVvMdQv%2BkzFK88%2BaCUUUvXoux2Lx6v7q63rea3C%2BXQF%2BvccQ4NxZ5yT6qA7hEwDmo%2BBGZ7OYepZ9UMULfF4J3dyoNvTeoj8mmltIsrbgy9%2FoHWtzYhS4qFYQNb%2F5xeADllkAnNPrFgrcSwJGLL%2FnwkIT06sgkIftI7E2H2SKH9s10mwiVlERabnfdvvpuj8z97wCdAuwD2TuUciKngtjiINDcFwFEcwvL2jmmXK76vC%2BnJuKUusdkO71JRnJDO9unGmZsX5rheE9x86KvAr4d5";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Username", "mayadi1");
        con.setRequestProperty("target", "https%3A%2F%2Fplus.credit-suisse.com%2Fmobileios6%2Fhome.html");
        con.setRequestProperty("smauthreason", "0");
        con.setRequestProperty("smauthreason", "");
        con.setRequestProperty("smquerydata", "HM3jGoF6Pjohn1DJeAHOSSycnTg45ByXPQFFqN5Kw8ad8fSGSsE6PlBNMuLy1kU7ZET0CyzYElMEe5YII%2FN5JXQm%2Bk9kSV9Ta9J%2FWN3IvJnGirnqJ8Ui5bNV3jej%2B1npYQa4dvfBuH%2FVvMdQv%2BkzFK88%2BaCUUUvXoux2Lx6v7q63rea3C%2BXQF%2BvccQ4NxZ5yT6qA7hEwDmo%2BBGZ7OYepZ9UMULfF4J3dyoNvTeoj8mmltIsrbgy9%2FoHWtzYhS4qFYQNb%2F5xeADllkAnNPrFgrcSwJGLL%2FnwkIT06sgkIftI7E2H2SKH9s10mwiVlERabnfdvvpuj8z97wCdAu wD2TuUciKngtjiINDcFwFEcwvL2jmmXK76vC%2BnJuKUusdkO71JRnJDO9unGmZsX5rheE9x86KvAr4d5");
        con.setRequestProperty("Password", "fnv4mvil");
        con.setRequestProperty("SUBMIT","submit");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
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