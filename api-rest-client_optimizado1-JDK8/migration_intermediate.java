// OPCIÓN 2: Migración Intermedia - Mejor control y estadísticas
public class MiProcesadorMejorado {
    
    private final APIRestClient<MiRespuesta> client;
    private int exitosos = 0;
    private int fallidos = 0;
    
    public MiProcesadorMejorado() {
        APIRestManager.initialize();
        this.client = new APIRestClient<>();
    }
    
    public void procesarRegistros(List<MiObjeto> listaObjetos, String url, boolean useSSL) {
        System.out.println("Iniciando procesamiento de " + listaObjetos.size() + " registros");
        exitosos = 0;
        fallidos = 0;
        
        for (MiObjeto objeto : listaObjetos) {
            procesarRegistroIndividual(objeto, url, useSSL);
        }
        
        mostrarEstadisticas();
    }
    
    private void procesarRegistroIndividual(MiObjeto objeto, String url, boolean useSSL) {
        try {
            // ✅ Reutilizar conexión HTTP
            MiRespuesta response = client.invokeRestServices(url, objeto, useSSL, MiRespuesta.class);
            
            // Procesar respuesta exitosa
            procesarRespuestaExitosa(response, objeto);
            exitosos++;
            
        } catch (IOException e) {
            // Manejar error sin detener el procesamiento
            manejarErrorIndividual(objeto, e);
            fallidos++;
        }
    }
    
    private void procesarRespuestaExitosa(MiRespuesta response, MiObjeto objeto) {
        // Tu lógica de procesamiento actual
        System.out.println("✅ Procesado exitosamente: " + objeto.getId());
        
        // Ejemplo: guardar en base de datos, archivo, etc.
        // guardarResultado(objeto, response);
    }
    
    private void manejarErrorIndividual(MiObjeto objeto, IOException e) {
        System.err.println("❌ Error procesando " + objeto.getId() + ": " + e.getMessage());
        
        // Opcional: guardar para reintento posterior
        // guardarParaReintento(objeto, e);
    }
    
    private void mostrarEstadisticas() {
        int total = exitosos + fallidos;
        System.out.println("\n=== RESUMEN DE PROCESAMIENTO ===");
        System.out.println("Total procesados: " + total);
        System.out.println("Exitosos: " + exitosos + " (" + (exitosos * 100.0 / total) + "%)");
        System.out.println("Fallidos: " + fallidos + " (" + (fallidos * 100.0 / total) + "%)");
    }
    
    public void cerrarRecursos() {
        APIRestManager.shutdown();
    }
    
    // Método de conveniencia para uso simple
    public static void procesarLista(List<MiObjeto> lista, String url, boolean useSSL) {
        MiProcesadorMejorado procesador = new MiProcesadorMejorado();
        try {
            procesador.procesarRegistros(lista, url, useSSL);
        } finally {
            procesador.cerrarRecursos();
        }
    }
}