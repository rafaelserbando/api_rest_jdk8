// 1. CONFIGURACIÓN HTTP - HttpClientConfigManager.java
package org.example.config;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpClientConfigManager {
    
    private static final Logger logger = Logger.getLogger(HttpClientConfigManager.class);
    private static volatile CloseableHttpClient httpClient;
    private static volatile CloseableHttpClient sslHttpClient;
    private static final Object LOCK = new Object();
    
    public static CloseableHttpClient getHttpClient(boolean useSSL) {
        if (useSSL) {
            return getSSLHttpClient();
        } else {
            return getRegularHttpClient();
        }
    }
    
    private static CloseableHttpClient getRegularHttpClient() {
        if (httpClient == null) {
            synchronized (LOCK) {
                if (httpClient == null) {
                    httpClient = createHttpClient(false);
                    logger.debug("HttpClient regular creado y configurado");
                }
            }
        }
        return httpClient;
    }
    
    private static CloseableHttpClient getSSLHttpClient() {
        if (sslHttpClient == null) {
            synchronized (LOCK) {
                if (sslHttpClient == null) {
                    sslHttpClient = createHttpClient(true);
                    logger.debug("HttpClient SSL creado y configurado");
                }
            }
        }
        return sslHttpClient;
    }
    
    private static CloseableHttpClient createHttpClient(boolean useSSL) {
        // Configurar connection manager
        PoolingHttpClientConnectionManager connectionManager = 
            new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(20);
        connectionManager.setDefaultMaxPerRoute(10);
        
        // Configurar keep-alive strategy
        ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator(
                    response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return 30 * 1000; // 30 segundos por defecto
            }
        };
        
        // Configurar timeouts
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(5000)
            .setSocketTimeout(10000)
            .setConnectionRequestTimeout(3000)
            .build();
        
        // Crear cliente
        CloseableHttpClient client = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setKeepAliveStrategy(keepAliveStrategy)
            .setDefaultRequestConfig(requestConfig)
            .build();
        
        // Configurar limpieza de conexiones inactivas
        scheduleConnectionCleanup(connectionManager);
        
        return client;
    }
    
    private static void scheduleConnectionCleanup(PoolingHttpClientConnectionManager connectionManager) {
        // En una aplicación real, usarías un ScheduledExecutorService
        // Por simplicidad, aquí solo documentamos la funcionalidad
        logger.debug("Connection cleanup configurado para el connection manager");
    }
    
    public static void shutdown() {
        if (httpClient != null) {
            try {
                httpClient.close();
                logger.debug("HttpClient regular cerrado");
            } catch (IOException e) {
                logger.warn("Error cerrando HttpClient regular: " + e.getMessage());
            }
        }
        
        if (sslHttpClient != null) {
            try {
                sslHttpClient.close();
                logger.debug("HttpClient SSL cerrado");
            } catch (IOException e) {
                logger.warn("Error cerrando HttpClient SSL: " + e.getMessage());
            }
        }
    }
}

// 2. CONFIGURACIÓN DE LOGGING - LoggerConfigManager.java
package org.example.config;

import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerConfigManager {
    
    public static Logger setupLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz);
        
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
            System.err.println("Error al configurar logger para " + clazz.getSimpleName() + ": " + e.getMessage());
        }
        
        return logger;
    }
}

// 3. SERVICIO API - APIRestService.java
package org.example.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.example.config.HttpClientConfigManager;
import org.example.config.LoggerConfigManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

public class APIRestService {
    
    private static final Logger logger = LoggerConfigManager.setupLogger(APIRestService.class);
    private final ObjectMapper objectMapper;
    
    public APIRestService() {
        this.objectMapper = createObjectMapper();
    }
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
        return mapper;
    }
    
    public <T> T invokeRestService(String url, Object request, boolean useSSL, Class<T> responseClass) throws IOException {
        CloseableHttpClient httpClient = HttpClientConfigManager.getHttpClient(useSSL);
        CloseableHttpResponse httpResponse = null;
        
        try {
            if (useSSL) {
                logger.debug("Usando conexión SSL/TLS");
            } else {
                logger.debug("Usando conexión HTTP normal");
            }
            
            HttpPost httpPost = createHttpPost(url, request);
            httpResponse = httpClient.execute(httpPost);
            
            return processResponse(httpResponse, responseClass);
            
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    logger.warn("Error cerrando HttpResponse: " + e.getMessage());
                }
            }
            // NO cerrar httpClient aquí - se reutiliza
        }
    }
    
    private HttpPost createHttpPost(String url, Object request) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        
        // Configurar headers
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
        
        // Configurar body
        String jsonRequest = objectMapper.writeValueAsString(request);
        logger.debug("JSON Request: " + jsonRequest);
        
        StringEntity entity = new StringEntity(jsonRequest, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        
        return httpPost;
    }
    
    private <T> T processResponse(CloseableHttpResponse httpResponse, Class<T> responseClass) throws IOException {
        HttpEntity responseEntity = httpResponse.getEntity();
        
        try {
            String jsonResponse = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
            logger.debug("JSON Response: " + jsonResponse);
            
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                String errorMsg = "HTTP Error: " + statusCode + " - " + httpResponse.getStatusLine().getReasonPhrase();
                logger.error(errorMsg);
                throw new IOException(errorMsg + ". Response: " + jsonResponse);
            }
            
            return objectMapper.readValue(jsonResponse, responseClass);
            
        } finally {
            // Consumir completamente la respuesta para liberar la conexión
            EntityUtils.consume(responseEntity);
        }
    }
}

// 4. CLIENTE API (FACADE) - APIRestClient.java
package org.example.client;

import org.apache.log4j.Logger;
import org.example.config.LoggerConfigManager;
import org.example.service.APIRestService;

import java.io.IOException;

public class APIRestClient<T> {
    
    private static final Logger logger = LoggerConfigManager.setupLogger(APIRestClient.class);
    private final APIRestService apiRestService;
    
    public APIRestClient() {
        this.apiRestService = new APIRestService();
    }
    
    public T invokeRestServices(String url, Object request, boolean useSSL, Class<T> responseClass) throws IOException {
        try {
            logger.info("Iniciando llamada REST a: " + url);
            T response = apiRestService.invokeRestService(url, request, useSSL, responseClass);
            logger.info("Llamada REST completada exitosamente");
            return response;
            
        } catch (IOException e) {
            logger.error("Error en llamada REST: " + e.getMessage(), e);
            throw e;
        }
    }
}

// 5. PROCESADOR DE REGISTROS - APIRestProcessor.java
package org.example.processor;

import org.apache.log4j.Logger;
import org.example.client.APIRestClient;
import org.example.config.LoggerConfigManager;

import java.io.IOException;
import java.util.List;

public class APIRestProcessor<T> {
    
    private static final Logger logger = LoggerConfigManager.setupLogger(APIRestProcessor.class);
    private final APIRestClient<T> apiClient;
    
    public APIRestProcessor() {
        this.apiClient = new APIRestClient<>();
    }
    
    public void processRecords(List<ProcessRequest> requests, String url, boolean useSSL, Class<T> responseClass) {
        int successful = 0;
        int failed = 0;
        
        logger.info("Iniciando procesamiento de " + requests.size() + " registros");
        
        for (ProcessRequest request : requests) {
            try {
                T result = apiClient.invokeRestServices(url, request.getData(), useSSL, responseClass);
                successful++;
                
                logger.debug("Registro procesado exitosamente: " + request.getId());
                handleSuccessfulProcess(request, result);
                
            } catch (IOException e) {
                failed++;
                logger.error("Error procesando registro " + request.getId() + ": " + e.getMessage());
                handleFailedProcess(request, e);
            }
        }
        
        logger.info("Procesamiento completado. Exitosos: " + successful + ", Fallidos: " + failed);
    }
    
    public T processSingleRecord(Object requestData, String url, boolean useSSL, Class<T> responseClass) throws IOException {
        return apiClient.invokeRestServices(url, requestData, useSSL, responseClass);
    }
    
    private void handleSuccessfulProcess(ProcessRequest request, T result) {
        // Implementar lógica post-procesamiento exitoso
        logger.debug("Procesamiento exitoso para: " + request.getId());
    }
    
    private void handleFailedProcess(ProcessRequest request, Exception e) {
        // Implementar lógica de manejo de errores
        logger.warn("Manejando fallo para registro: " + request.getId());
    }
    
    // Clase interna para encapsular requests
    public static class ProcessRequest {
        private String id;
        private Object data;
        
        public ProcessRequest(String id, Object data) {
            this.id = id;
            this.data = data;
        }
        
        public String getId() { return id; }
        public Object getData() { return data; }
    }
}

// 6. CLASE PRINCIPAL DE GESTIÓN - APIRestManager.java
package org.example;

import org.apache.log4j.Logger;
import org.example.config.HttpClientConfigManager;
import org.example.config.LoggerConfigManager;
import org.example.processor.APIRestProcessor;

public class APIRestManager {
    
    private static final Logger logger = LoggerConfigManager.setupLogger(APIRestManager.class);
    
    public static void initialize() {
        logger.info("Inicializando APIRestManager");
        // Aquí puedes agregar otras inicializaciones si las necesitas
    }
    
    public static void shutdown() {
        logger.info("Cerrando APIRestManager");
        HttpClientConfigManager.shutdown();
        logger.info("APIRestManager cerrado correctamente");
    }
    
    // Método de conveniencia para uso simple
    public static <T> APIRestProcessor<T> createProcessor() {
        return new APIRestProcessor<>();
    }
    
    // Configurar shutdown hook automático
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Ejecutando shutdown hook");
            shutdown();
        }));
    }
}