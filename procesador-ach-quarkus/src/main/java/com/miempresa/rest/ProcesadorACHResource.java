
package com.miempresa.rest;

import com.miempresa.integrador.ProcesadorACH;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/ach")
public class ProcesadorACHResource {

    @GET
    @Path("/procesar")
    public Response procesar(@QueryParam("accion") String accion) {
        ProcesadorACH ach = new ProcesadorACH();
        try {
            if ("enviar".equalsIgnoreCase(accion)) {
                ach.enviarACH();
                return Response.ok("Envío ACH ejecutado.").build();
            } else if ("consultar".equalsIgnoreCase(accion)) {
                ach.consultarACH();
                return Response.ok("Consulta ACH ejecutada.").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                               .entity("Acción no válida. Use 'enviar' o 'consultar'.")
                               .build();
            }
        } catch (Exception e) {
            return Response.serverError().entity("Error procesando ACH: " + e.getMessage()).build();
        }
    }
}
