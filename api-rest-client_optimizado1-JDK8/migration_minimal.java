// OPCIÓN 1: Migración Mínima - Solo cambiar la instanciación del cliente
public class MiProcesador {

    public void procesarRegistros() {
        // ✅ Crear cliente UNA sola vez antes del for
        APIRestClient<MiRespuesta> client = new APIRestClient<>();
        
        try {
            for (MiObjeto objeto : listaObjetos) {
                try {
                    // ✅ Reutilizar el mismo cliente
                    MiRespuesta response = client.invokeRestServices(url, objeto, false, MiRespuesta.class);
                    
                    // Tu lógica de procesamiento actual
                    procesarRespuesta(response, objeto);
                    
                } catch (IOException e) {
                    // Tu manejo de errores actual
                    manejarError(objeto, e);
                }
            }
        } finally {
            // ✅ Cerrar recursos al final
            APIRestManager.shutdown();
        }
    }
    
    private void procesarRespuesta(MiRespuesta response, MiObjeto objeto) {
        // Tu lógica actual de procesamiento
        System.out.println("Procesado: " + objeto.getId() + " -> " + response.toString());
    }
    
    private void manejarError(MiObjeto objeto, IOException e) {
        // Tu lógica actual de manejo de errores
        System.err.println("Error procesando " + objeto.getId() + ": " + e.getMessage());
    }
}