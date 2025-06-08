// OPCIÓN 3: Migración Completa - Usando la arquitectura empresarial
public class MiAplicacionCompleta {
    
    public static void main(String[] args) {
        MiAplicacionCompleta app = new MiAplicacionCompleta();
        
        // Configurar shutdown automático
        Runtime.getRuntime().addShutdownHook(new Thread(app::cerrarAplicacion));
        
        try {
            app.ejecutar();
        } finally {
            app.cerrarAplicacion();
        }
    }
    
    public void ejecutar() {
        // Inicializar sistema
        APIRestManager.initialize();
        
        // Obtener datos a procesar
        List<MiObjeto> registros = obtenerRegistrosAProcesar();
        
        if (registros.isEmpty()) {
            System.out.println("No hay registros para procesar");
            return;
        }
        
        // Configurar procesador
        APIRestProcessor<MiRespuesta> processor = APIRestManager.createProcessor();
        
        // Convertir a formato requerido por el processor
        List<APIRestProcessor.ProcessRequest> requests = convertirARequests(registros);
        
        // Procesar todos los registros
        String url = "https://mi-api.com/endpoint";
        boolean useSSL = false; // o true según tu configuración
        
        processor.processRecords(requests, url, useSSL, MiRespuesta.class);
    }
    
    private List<MiObjeto> obtenerRegistrosAProcesar() {
        // Tu lógica actual para obtener registros
        // Puede ser desde base de datos, archivo, servicio, etc.
        List<MiObjeto> registros = new ArrayList<>();
        
        // Ejemplo: cargar desde donde sea que los tengas
        // registros = miRepositorio.obtenerRegistrosPendientes();
        // o
        // registros = leerDesdeArchivo();
        
        return registros;
    }
    
    private List<APIRestProcessor.ProcessRequest> convertirARequests(List<MiObjeto> registros) {
        List<APIRestProcessor.ProcessRequest> requests = new ArrayList<>();
        
        for (MiObjeto objeto : registros) {
            requests.add(new APIRestProcessor.ProcessRequest(
                objeto.getId().toString(), // ID para tracking
                objeto                     // Los datos a enviar
            ));
        }
        
        return requests;
    }
    
    private void cerrarAplicacion() {
        System.out.println("Cerrando aplicación...");
        APIRestManager.shutdown();
        System.out.println("Aplicación cerrada correctamente");
    }
}

// Clase personalizada que extiende el processor para tu lógica específica
public class MiProcesadorPersonalizado extends APIRestProcessor<MiRespuesta> {
    
    @Override
    protected void handleSuccessfulProcess(ProcessRequest request, MiRespuesta result) {
        // Tu lógica específica para procesos exitosos
        System.out.println("✅ Registro " + request.getId() + " procesado exitosamente");
        
        // Ejemplo: actualizar base de datos
        // actualizarEstadoEnBD(request.getId(), "PROCESADO", result);
        
        // Ejemplo: enviar notificación
        // enviarNotificacionExito(request.getId(), result);
    }
    
    @Override
    protected void handleFailedProcess(ProcessRequest request, Exception e) {
        // Tu lógica específica para procesos fallidos
        System.err.println("❌ Error procesando registro " + request.getId() + ": " + e.getMessage());
        
        // Ejemplo: guardar para reintento
        // guardarParaReintento(request.getId(), e.getMessage());
        
        // Ejemplo: enviar alerta
        // enviarAlertaError(request.getId(), e);
    }
    
    // Métodos auxiliares para tu lógica de negocio
    private void actualizarEstadoEnBD(String id, String estado, MiRespuesta resultado) {
        // Tu lógica para actualizar base de datos
    }
    
    private void guardarParaReintento(String id, String error) {
        // Tu lógica para guardar registros fallidos
    }
}

// Uso del procesador personalizado
public class EjecutorConProcesadorPersonalizado {
    
    public void ejecutarProcesamiento() {
        APIRestManager.initialize();
        
        try {
            MiProcesadorPersonalizado processor = new MiProcesadorPersonalizado();
            
            List<MiObjeto> registros = obtenerRegistros();
            List<APIRestProcessor.ProcessRequest> requests = convertirRegistros(registros);
            
            processor.processRecords(requests, "https://mi-api.com/endpoint", false, MiRespuesta.class);
            
        } finally {
            APIRestManager.shutdown();
        }
    }
}