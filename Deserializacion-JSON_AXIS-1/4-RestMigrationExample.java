// 4. Ejemplo de uso en tu código de migración
public class RestMigrationExample {
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    public TransactionPOJO callRestService(String jsonResponse) throws IOException {
        
        // Tu deserialización permanece IGUAL
        TransactionPOJO pojo = objectMapper.readValue(jsonResponse, TransactionPOJO.class);
        
        // Jackson automáticamente usa CalendarDeserializer para los campos anotados
        Calendar fecha = pojo.getFechaTransaccion(); // Ya convertida de JSON a Calendar
        
        System.out.println("Fecha deserializada: " + fecha.getTime());
        
        return pojo;
    }
    
    // Ejemplo de JSON que se mapea correctamente:
    /*
    {
        "id": "TXN001",
        "monto": 1500.50,
        "fechaTransaccion": "2024-06-07T15:30:00.000Z",
        "fechaCreacion": "2024-06-07T10:15:30Z",
        "descripcion": "Transferencia bancaria"
    }
    */
}