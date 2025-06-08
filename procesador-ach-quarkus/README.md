
# Procesador ACH Quarkus

Este proyecto implementa un microservicio REST con Quarkus para procesar operaciones ACH.

## Endpoints

- `GET /ach/procesar?accion=enviar` – Ejecuta el envío ACH.
- `GET /ach/procesar?accion=consultar` – Ejecuta la consulta ACH.

## Construcción del proyecto

```
./mvnw clean package
```

## Ejecución en modo desarrollo

```
./mvnw quarkus:dev
```

## Producción

```
java -jar target/quarkus-app/quarkus-run.jar
```

## Docker

1. Agrega extensión:
```
./mvnw quarkus:add-extension -Dextensions="container-image-docker"
```

2. Construye la imagen:
```
./mvnw package -Dquarkus.container-image.build=true
```

3. Ejecuta:
```
docker run -i --rm -p 8080:8080 com.miempresa/procesador-ach-quarkus
```
#############################################################

¡Claro! A continuación, te guiaré paso a paso para crear una versión de tu aplicación ProcesadorACH utilizando Quarkus, el framework Java optimizado para entornos en la nube y contenedores.
🛠️ 1. Crear el proyecto base con Quarkus

Puedes generar un nuevo proyecto Quarkus de dos maneras:
Opción A: Usando la CLI de Quarkus

Si tienes instalada la CLI de Quarkus, ejecuta:

quarkus create app com.miempresa:procesador-ach-quarkus \
  --extension='resteasy-reactive, resteasy-reactive-jackson' \
  --no-code
cd procesador-ach-quarkus

Opción B: Usando Maven

Si prefieres usar Maven directamente:

mvn io.quarkus.platform:quarkus-maven-plugin:3.23.0:create \
  -DprojectGroupId=com.miempresa \
  -DprojectArtifactId=procesador-ach-quarkus \
  -Dextensions='resteasy-reactive, resteasy-reactive-jackson' \
  -DnoCode
  
  
POWERSHELL:
mvn io.quarkus.platform:quarkus-maven-plugin:3.23.0:create `
  "-DprojectGroupId=com.miempresa" `
  "-DprojectArtifactId=procesador-ach-quarkus" `
  "-Dextensions=resteasy-reactive, resteasy-reactive-jackson" `
  "-DnoCode"


cd procesador-ach-quarkus

Estos comandos generarán la estructura básica del proyecto Quarkus sin código adicional, permitiéndote agregar tus propias clases.
📁 2. Estructura del proyecto

La estructura generada será similar a:

procesador-ach-quarkus/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── miempresa/
│   │   │           └── rest/
│   │   │               └── ProcesadorACHResource.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/
│               └── miempresa/
│                   └── rest/
│                       └── ProcesadorACHResourceTest.java

✍️ 3. Implementar el endpoint REST

Crea la clase ProcesadorACHResource.java en src/main/java/com/miempresa/rest/ con el siguiente contenido:

package com.miempresa.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/ach")
public class ProcesadorACHResource {

    @GET
    @Path("/procesar")
    public Response procesar(@QueryParam("accion") String accion) {
        if ("enviar".equalsIgnoreCase(accion)) {
            // Lógica para enviar ACH
            return Response.ok("Envío ACH ejecutado.").build();
        } else if ("consultar".equalsIgnoreCase(accion)) {
            // Lógica para consultar ACH
            return Response.ok("Consulta ACH ejecutada.").build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Acción no válida. Use 'enviar' o 'consultar'.")
                           .build();
        }
    }
}

Este endpoint responderá a solicitudes GET en /ach/procesar, esperando un parámetro de consulta accion con valores enviar o consultar.
⚙️ 4. Configurar application.properties

En src/main/resources/application.properties, puedes definir propiedades de configuración, por ejemplo:

# Puerto de la aplicación
quarkus.http.port=8080

# Configuraciones específicas de tu aplicación
api.url.enviados=https://localhost:8443/api/enviados
api.url.recibidos=https://localhost:8443/api/recibidos

🚀 5. Ejecutar la aplicación en modo desarrollo

Para iniciar la aplicación en modo desarrollo con recarga en caliente:

./mvnw quarkus:dev

La aplicación estará disponible en http://localhost:8080.

Puedes probar el endpoint accediendo a:

    Envío ACH: http://localhost:8080/ach/procesar?accion=enviar

    Consulta ACH: http://localhost:8080/ach/procesar?accion=consultar

📦 6. Empaquetar la aplicación

Para empaquetar la aplicación como un JAR ejecutable:

./mvnw package

El archivo resultante estará en target/quarkus-app/quarkus-run.jar. Puedes ejecutarlo con:

java -jar target/quarkus-app/quarkus-run.jar

🐳 7. Crear un contenedor Docker (opcional)

Quarkus facilita la creación de contenedores Docker. Puedes generar un Dockerfile con:

./mvnw quarkus:add-extension -Dextensions="container-image-docker"

Luego, construye la imagen:

./mvnw package -Dquarkus.container-image.build=true

Y ejecuta el contenedor:

docker run -i --rm -p 8080:8080 com.miempresa/procesador-ach-quarkus

📚 Recursos adicionales

Para más información sobre Quarkus y sus capacidades, puedes consultar:

    Guía oficial de inicio rápido: Creando su primera aplicación - Quarkus

    Tutorial en video: Fundamentos de Quarkus: ¿Qué es y cómo crear un proyecto?
	