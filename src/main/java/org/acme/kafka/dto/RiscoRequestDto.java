package org.acme.kafka.dto;

import org.acme.kafka.quarkus.Risco;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiscoRequestDto {
 
  String cadastro;
  String documento;
  String valor; 

  public Risco toAvro() {
    return Risco.newBuilder()
    .setCadastro(this.cadastro)
    .setDocumento(this.documento)
    .setValor(this.valor)
    .build();
  }
}


