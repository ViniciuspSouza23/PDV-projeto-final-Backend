package br.gov.sp.cps.fatec.pdv.model.strategy;

import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Servico;

import java.math.BigDecimal;

/**
 * Padrão GoF Strategy para cálculo dinâmico do preço de serviços.
 */
public interface CalculadoraPrecoServico {

    /**
     * Calcula o preço final do serviço levando em conta as características do animal atendido.
     *
     * @param servico Serviço a ser prestado.
     * @param pet     Pet que receberá o serviço (pode ser nulo para serviços avulsos).
     * @return Valor monetário ajustado do serviço.
     */
    BigDecimal calcularPreco(Servico servico, Pet pet);
}
