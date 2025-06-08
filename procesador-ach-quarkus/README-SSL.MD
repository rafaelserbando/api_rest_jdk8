## Para activar SSL en Quarkus, necesitas configurar las propiedades de seguridad en tu archivo application.properties. Te muestro las configuraciones principales:

### Configuración básica de SSL
# Habilitar HTTPS
quarkus.http.ssl-port=8443
quarkus.http.ssl.certificate.key-store-file=keystore.jks
quarkus.http.ssl.certificate.key-store-password=password

# Opcional: Redireccionar HTTP a HTTPS
quarkus.http.insecure-requests=redirect

### Configuración más detallada

# Puerto HTTPS (por defecto 8443)
quarkus.http.ssl-port=8443

# Configuración del keystore
quarkus.http.ssl.certificate.key-store-file=META-INF/keystore.jks
quarkus.http.ssl.certificate.key-store-password=tu_password
quarkus.http.ssl.certificate.key-store-type=JKS

# Configuración del truststore (si es necesario)
quarkus.http.ssl.certificate.trust-store-file=META-INF/truststore.jks
quarkus.http.ssl.certificate.trust-store-password=trust_password

# Opciones adicionales
quarkus.http.ssl.cipher-suites=TLS_AES_256_GCM_SHA384,TLS_AES_128_GCM_SHA256
quarkus.http.ssl.protocols=TLSv1.3,TLSv1.2

# Deshabilitar HTTP completamente (opcional)
quarkus.http.port=-1

### Generar un keystore para desarrollo
# Si no tienes un certificado, puedes generar uno para desarrollo:

keytool -genkeypair -storepass password -keypass password -keyalg RSA -keysize 2048 -dname "CN=localhost" -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -validity 365 -keystore keystore.jks

### Ubicación del keystore
Coloca el archivo keystore.jks en:

src/main/resources/ para que esté disponible en el classpath
O especifica una ruta absoluta en la configuración

Tu clase no necesita cambios
Tus endpoints REST no requieren modificaciones. Una vez configurado SSL, estarán disponibles en:

https://localhost:8443/tu-endpoint

### Configuración para producción
Para producción, considera usar:

# Certificados separados en lugar de keystore
quarkus.http.ssl.certificate.files=cert.pem
quarkus.http.ssl.certificate.key-files=key.pem

# O variables de entorno
quarkus.http.ssl.certificate.key-store-password=${SSL_KEYSTORE_PASSWORD}

Con estas configuraciones, tu aplicación Quarkus se ejecutará con SSL habilitado y tus endpoints estarán protegidos con HTTPS.

