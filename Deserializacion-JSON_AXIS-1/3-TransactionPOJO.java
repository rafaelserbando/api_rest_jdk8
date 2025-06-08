// 3. TransactionPOJO.java (ejemplo de uso)
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Calendar;

public class TransactionPOJO {
    
    // Campo con anotaciones Jackson para Calendar
    @JsonDeserialize(using = CalendarDeserializer.class)
    @JsonSerialize(using = CalendarSerializer.class)
    private Calendar fechaTransaccion;
    
    @JsonDeserialize(using = CalendarDeserializer.class)
    @JsonSerialize(using = CalendarSerializer.class)
    private Calendar fechaCreacion;
    
    // Otros campos sin cambios
    private String id;
    private Double monto;
    private String descripcion;
    
    // Constructores
    public TransactionPOJO() {}
    
    public TransactionPOJO(String id, Double monto, Calendar fechaTransaccion) {
        this.id = id;
        this.monto = monto;
        this.fechaTransaccion = fechaTransaccion;
    }
    
    // Getters y Setters
    public Calendar getFechaTransaccion() {
        return fechaTransaccion;
    }
    
    public void setFechaTransaccion(Calendar fechaTransaccion) {
        this.fechaTransaccion = fechaTransaccion;
    }
    
    public Calendar getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(Calendar fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Double getMonto() {
        return monto;
    }
    
    public void setMonto(Double monto) {
        this.monto = monto;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    @Override
    public String toString() {
        return "TransactionPOJO{" +
                "id='" + id + '\'' +
                ", monto=" + monto +
                ", fechaTransaccion=" + (fechaTransaccion != null ? fechaTransaccion.getTime() : null) +
                ", fechaCreacion=" + (fechaCreacion != null ? fechaCreacion.getTime() : null) +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
