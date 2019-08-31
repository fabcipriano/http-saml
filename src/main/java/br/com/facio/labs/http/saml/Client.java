package br.com.facio.labs.http.saml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fabianocp
 */
public class Client {

    private static final Logger LOG = LogManager.getLogger(Client.class);
    private static final AtomicLong TID = new AtomicLong(0);

    public static void main(String[] args) {
        for (int i = 0; i < 64; i++) {
            createClientThread().start();
        }
    }

    private static Thread createClientThread() {
        LOG.info("Creating client...");
        Thread t = new Thread(() -> {
            while (true) {
                connectAndLogResponse();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    LOG.error("Interrupted", ex);
                }
            }
        }, "ClientTest-" + TID.getAndIncrement());
        return t;
    }

    private static void connectAndLogResponse() {
        HttpURLConnection con = null;
        try {
            LOG.info("connecting ...");
            URL obj = new URL("http://localhost:8181/hello/blocking/");

            CookieHandler.setDefault(new CookieManager());
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(15000);
            con.setInstanceFollowRedirects(true);
            Object content = con.getContent();
            LOG.info("content = " + content);
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                LOG.info("response.: {}", response.toString());
            } else {
                LOG.warn("not closing http...");
            }
        } catch (IOException ioe) {
            LOG.error("CLOSE - Original exception ioe", ioe);
            
            /*uncomment this line to FIX this Connection Leak, see:
            https://docs.oracle.com/javase/6/docs/technotes/guides/net/http-keepalive.html */
            treatErrorAndClose(con);
        } catch (Exception e) {
            LOG.error("Unexpected Exception", e);
        } finally {
            LOG.info("connected.");
        }
    }

    private static void treatErrorAndClose(HttpURLConnection con) {
        try {
            int respCode = con.getResponseCode();
            BufferedReader es = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            
            while ((inputLine = es.readLine()) != null) {
                response.append(inputLine);
            }
            es.close();
            LOG.warn("errorCode.: {}, errorResponse.: {}", respCode, response.toString());
        } catch (IOException iOException) {
            LOG.error("Failed to close httpconnection", iOException);
        }
    }
}
