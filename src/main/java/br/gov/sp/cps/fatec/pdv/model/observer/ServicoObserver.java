package br.gov.sp.cps.fatec.pdv.model.observer;

import br.gov.sp.cps.fatec.pdv.model.entity.Servico;

import java.util.List;

@FunctionalInterface
public interface ServicoObserver {
    void onServicosChanged(List<Servico> servicos);
}
