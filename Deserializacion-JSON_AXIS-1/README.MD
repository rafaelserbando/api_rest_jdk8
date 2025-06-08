Características de esta implementación:
1. CalendarDeserializer robusto

Maneja múltiples formatos de fecha ISO
Con milisegundos: 2024-06-07T15:30:00.000Z
Sin milisegundos: 2024-06-07T15:30:00Z
Solo fecha: 2024-06-07
Maneja valores null/vacíos
Siempre convierte a UTC

2. CalendarSerializer consistente

Siempre serializa en formato ISO 8601 con milisegundos
Garantiza UTC en la salida
Maneja valores null correctamente

3. Uso transparente

Tu código de deserialización NO cambia
Solo agregas las anotaciones a los campos Calendar
Jackson se encarga automáticamente del resto

4. Tu código permanece igual