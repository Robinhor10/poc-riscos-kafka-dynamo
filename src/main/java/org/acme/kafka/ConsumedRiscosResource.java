package org.acme.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.kafka.dto.RiscoRequestDto;
import org.acme.kafka.quarkus.Risco;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestSseElementType;

import io.smallrye.mutiny.Multi;

@ApplicationScoped
@Path("/consumed-riscos")
public class ConsumedRiscosResource {
    private static final Logger LOGGER = Logger.getLogger(ConsumedRiscosResource.class);

    @Channel("riscos-from-kafka")
    Multi<Risco> riscos;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestSseElementType(MediaType.TEXT_PLAIN)
    public Multi<String> stream() {
        LOGGER.infof("Consumindo mensagem do kafka");
        return riscos.map(RiscoRequestDto -> String.format("%s %s %s %s", RiscoRequestDto.getCadastro(), RiscoRequestDto.getDocumento(),RiscoRequestDto.getProduto(), RiscoRequestDto.getValor()));
    }
}
