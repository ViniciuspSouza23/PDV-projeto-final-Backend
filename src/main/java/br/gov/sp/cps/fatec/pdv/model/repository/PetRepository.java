package br.gov.sp.cps.fatec.pdv.model.repository;

import br.gov.sp.cps.fatec.pdv.model.entity.Pet;

import java.util.List;
import java.util.Optional;

public interface PetRepository {
    Pet salvar(Pet pet);

    Pet atualizar(Pet pet);

    void excluir(Long id);

    Optional<Pet> buscarPorId(Long id);

    List<Pet> listarPorTutor(Long tutorId);
}
