package br.gov.sp.cps.fatec.pdv.model.strategy;

import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Porte;
import br.gov.sp.cps.fatec.pdv.model.entity.Servico;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementação da Strategy que ajusta o preço de banho e tosa de acordo com o porte do pet.
 */
public class CalculadoraPorPorteStrategy implements CalculadoraPrecoServico {

    @Override
    public BigDecimal calcularPreco(Servico servico, Pet pet) {
        if (servico == null) {
            throw new IllegalArgumentException("Serviço não pode ser nulo.");
        }
        BigDecimal precoBase = servico.getPrecoBase();
        if (!servico.isPermiteMultiplicadorPorte() || pet == null || pet.getPorte() == null) {
            return precoBase;
        }

        Porte porte = pet.getPorte();
        return precoBase.multiply(porte.getMultiplicador()).setScale(2, RoundingMode.HALF_UP);
    }
}
