package org.acme.kafka;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.acme.kafka.dto.RiscoRequestDto;
import org.acme.kafka.quarkus.Risco;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;


@Path("/riscos")
public class RiscosResource {
    private static final Logger LOGGER = Logger.getLogger(RiscosResource.class);

    @Channel("riscos")
    Emitter<Risco> emitter;

    @POST
    public Response publicaRisco(RiscoRequestDto riscoDto) {
        LOGGER.infof("Enviando Mensagem %s para o kafka", riscoDto.getCadastro());
        emitter.send(riscoDto.toAvro());
        return Response.accepted().build();
    }
    
}
