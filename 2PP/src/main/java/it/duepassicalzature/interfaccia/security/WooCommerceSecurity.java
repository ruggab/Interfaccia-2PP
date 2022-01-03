package it.duepassicalzature.interfaccia.security;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
public class WooCommerceSecurity {

    static String usernameD = "ck_e61bd71ce2ced97bfc049a62f0260eb6350df85f";
    static String passwordD = "cs_82d455dfd8ebb642b4774dc1268b9e7a42b08cce";

    public static HttpHeaders createHeaders(){
        return new HttpHeaders() {{
            String auth = usernameD + ":" + passwordD;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")) );
            String authHeader = "Basic " + new String( encodedAuth );
            //Basic Y2tfZTYxYmQ3MWNlMmNlZDk3YmZjMDQ5YTYyZjAyNjBlYjYzNTBkZjg1Zjpjc184MmQ0NTVkZmQ4ZWJiNjQyYjQ3NzRkYzEyNjhiOWU3YTQyYjA4Y2Nl
            set( "Authorization", authHeader );
        }};
    }

}
