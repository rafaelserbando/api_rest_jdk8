package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
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

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * APIRestComponentClient:
 *  - invokeRestServices(url, requestObj, useSSL):    tu método de siempre, sin cambios.
 *  - processDetalles(...):                            método nuevo que gestiona multihilos.
 */
public class APIRestComponentClient {

    private static final Logger logger;
    private static final Properties config;

    static {
        // 1. Configuración del logger (log4j con DailyRollingFileAppender)
        logger = Logger.getLogger(APIRestComponentClient.class);
        setupLogger();

        // 2. Cargar config.properties
        config = new Properties();
        try (InputStream in = new FileInputStream("src/main/resources/config.properties")) {
            config.load(in);
        } catch (IOException e) {
            logger.error("No se pudo cargar config.properties: " + e.getMessage(), e);
        }
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

    /**
     * Método “original” que envía un único request JSON al API REST y mapea la respuesta.
     * No lo alteramos: lo usamos tanto para envío individual como para batch.
     */
    public GBDRespIngresarACHRecibidosRest invokeRestServices(String url, Object request, boolean useSSL) throws IOException {
        CloseableHttpClient httpClient = null;
        try {
            if (useSSL) {
                httpClient = HttpClients.createDefault(); // placeholder para cliente SSL
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
            for (Header header : httpPost.getAllHeaders()) {
                logger.debug(header.getName() + ": " + header.getValue());
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));

            // Convierte el objeto request (ya sea un Detalle único o una lista de Detalle) a JSON:
            String jsonRequest = objectMapper.writeValueAsString(request);
            logger.debug("JSON Request: " + jsonRequest);

            StringEntity entity = new StringEntity(jsonRequest, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity responseEntity = httpResponse.getEntity();
            String jsonResponse = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

            logger.debug("JSON Response: " + jsonResponse);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode > 300) {
                logger.error("HTTP Error: " + statusCode + " - " + httpResponse.getStatusLine().getReasonPhrase());
            }

            // Mapea la respuesta JSON al POJO
            return objectMapper.readValue(jsonResponse, GBDRespIngresarACHRecibidosRest.class);

        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.warn("Error cerrando HttpClient: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Méthodo que optimiza el envío de la lista de detalles al API.
     *
     * @param detalles     Lista completa de Detalle (parsedos y validados).
     * @param idEncabezado ID obtenido tras invocar el API con el encabezado.
     * @param urlDetalle   URL base del endpoint para envío de detalles.
     * @param useSSL       True si la URL es HTTPS, false si es HTTP.
     */
    public void processDetalles(List<Detalle> detalles,
                                String idEncabezado, 
                                String urlDetalle, 
                                boolean useSSL) throws InterruptedException {

        // 1. Leer configuración
        boolean enabled   = Boolean.parseBoolean(config.getProperty("multithread.enabled", "false"));
        int maxThreads    = Integer.parseInt(config.getProperty("multithread.maxThreads", "4"));
        int chunkSize     = Integer.parseInt(config.getProperty("multithread.chunkSize", "500"));
        boolean sendBatch = Boolean.parseBoolean(config.getProperty("multithread.sendBatch", "false"));

        logger.info("=== processDetalles: enabled=" + enabled 
                    + ", maxThreads=" + maxThreads 
                    + ", chunkSize=" + chunkSize 
                    + ", sendBatch=" + sendBatch 
                    + " ===");

        // Si multithread está deshabilitado, simplemente recorre uno a uno:
        if (!enabled) {
            for (Detalle d : detalles) {
                // Construir el objeto request para cada detalle: 
                // por ejemplo, un Map o un DTO con idEncabezado + datos de 'd'
                Map<String, Object> payload = buildDetallePayload(idEncabezado, d);
                try {
                    invokeRestServices(urlDetalle, payload, useSSL);
                } catch (IOException e) {
                    logger.error("Error enviando detalle ID=" + d.getId() + ": " + e.getMessage(), e);
                }
            }
            return;
        }

        // 2. Configurar el pool de hilos
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        List<Future<Void>> futures = new ArrayList<>();

        // 3. Dividir la lista de detalles en “chunks” de tamaño chunkSize
        List<List<Detalle>> listaChunks = new ArrayList<>();
        for (int i = 0; i < detalles.size(); i += chunkSize) {
            listaChunks.add(detalles.subList(i, Math.min(i + chunkSize, detalles.size())));
        }

        // 4. Para cada chunk, creamos una tarea:
        for (List<Detalle> chunk : listaChunks) {
            Callable<Void> task = () -> {
                if (sendBatch) {
                    // Enviar el chunk completo como un array JSON:
                    List<Map<String, Object>> batchPayload = new ArrayList<>();
                    for (Detalle d : chunk) {
                        batchPayload.add(buildDetallePayload(idEncabezado, d));
                    }
                    logger.debug("[" + Thread.currentThread().getName() + "] Enviando batch de " 
                                 + chunk.size() + " detalles.");
                    try {
                        invokeRestServices(urlDetalle, batchPayload, useSSL);
                    } catch (IOException e) {
                        logger.error("Error enviando batch en hilo " 
                                     + Thread.currentThread().getName() 
                                     + ": " + e.getMessage(), e);
                    }

                } else {
                    // Enviar uno a uno en paralelo:
                    for (Detalle d : chunk) {
                        Map<String, Object> payload = buildDetallePayload(idEncabezado, d);
                        logger.debug("[" + Thread.currentThread().getName()
                                     + "] Enviando detalle ID=" + d.getId());
                        try {
                            invokeRestServices(urlDetalle, payload, useSSL);
                        } catch (IOException e) {
                            logger.error("Error enviando detalle ID="
                                         + d.getId() + " en hilo "
                                         + Thread.currentThread().getName()
                                         + ": " + e.getMessage(), e);
                        }
                    }
                }
                return null;
            };

            futures.add(executor.submit(task));
        }

        // 5. Esperar a que todos los hilos terminen
        for (Future<Void> f : futures) {
            try {
                f.get(); // aquí esperamos el fin de cada tarea
            } catch (ExecutionException ex) {
                logger.error("Tarea devolvió excepción: " + ex.getMessage(), ex);
            }
        }

        // 6. Shutdown del executor
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS); // espera hasta 1h (puedes ajustar)
        logger.info("processDetalles finalizado.");
    }

    /**
     * Construye el payload (Map) que se enviará al API para cada Detalle.
     * Aquí un ejemplo genérico; adáptalo a tu estructura real.
     */
    private Map<String, Object> buildDetallePayload(String idEncabezado, Detalle d) {
        Map<String, Object> map = new HashMap<>();
        map.put("idEncabezado", idEncabezado);
        map.put("detalleId", d.getId());
        map.put("campo1", d.getCampo1());
        map.put("campo2", d.getCampo2());
        // Agrega aquí los 25 campos que necesites extraer de tu Detalle real.
        return map;
    }
}
