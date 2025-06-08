package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class APIRestComponentClient {

    private static final Logger logger;

    static {
        logger = Logger.getLogger(APIRestComponentClient.class);
        setupLogger();
    }

    private static void setupLogger() {
        try {
            String logDir = "./logs/";
            String logFileName = "api-rest-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";

            File dir = new File(logDir);
            if (!dir.exists()) dir.mkdirs();

            DailyRollingFileAppender appender = new DailyRollingFileAppender(
                    new PatternLayout("%d{ISO8601} [%t] %-5p %c - %m%n"),
                    logDir + logFileName,
                    "'.'yyyy-MM-dd"
            );

            logger.setLevel(Level.DEBUG);
            logger.addAppender(appender);

        } catch (IOException e) {
            System.err.println("Error al configurar logger: " + e.getMessage());
        }
    }

    public GBDRespIngresarACHRecibidosRest invokeRestServices(String url, Object request, boolean useSSL) throws IOException {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;

        try {
            if (useSSL) {
                httpClient = HttpClients.createDefault(); // Aquí iría createSSLHttpClient si aplica
                logger.debug("Usando conexión SSL/TLS");
            } else {
                httpClient = HttpClients.createDefault();
                logger.debug("Usando conexión HTTP normal");
            }

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("application", "MiAplicacion");
            httpPost.setHeader("username", "usuario123");
            httpPost.setHeader("token", "token123");
            httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
            httpPost.setHeader("Connection", "keep-alive");

            logger.debug("URL: " + url);
            logger.debug("HEADER del request:");
            Header[] requestHeaders = httpPost.getAllHeaders();
            for (Header header : requestHeaders) {
                logger.debug(header.getName() + ": " + header.getValue());
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));

            String jsonRequest = objectMapper.writeValueAsString(request);
            logger.debug("JSON Request: " + jsonRequest);

            StringEntity entity = new StringEntity(jsonRequest, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            httpResponse = httpClient.execute(httpPost);
            HttpEntity responseEntity = httpResponse.getEntity();
            String jsonResponse = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

            logger.debug("JSON Response: " + jsonResponse);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode > 300) {
                logger.error("HTTP Error: " + statusCode + " - " + httpResponse.getStatusLine().getReasonPhrase());
            }

            return objectMapper.readValue(jsonResponse, GBDRespIngresarACHRecibidosRest.class);

        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("Error cerrando HttpClient: " + e.getMessage());
                }
            }
        }
    }
}
