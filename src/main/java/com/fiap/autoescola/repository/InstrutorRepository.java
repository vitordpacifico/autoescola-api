package com.fiap.autoescola.repository;

import com.fiap.autoescola.model.Instrutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InstrutorRepository extends JpaRepository<Instrutor, Long> {
    Page<Instrutor> findAllByOrderByNomeAsc(Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByCnh(String cnh);

    Optional<Instrutor> findByIdAndAtivoTrue(Long id);

    List<Instrutor> findAllByAtivoTrue();

    @org.springframework.data.jpa.repository.Query("""
            SELECT i FROM Instrutor i
            WHERE i.ativo = true
              AND i.id NOT IN (
                  SELECT ins.instrutor.id FROM Instrucao ins
                  WHERE ins.dataHora = :dataHora
                    AND ins.status <> com.fiap.autoescola.model.StatusInstrucao.CANCELADA
              )
            """)
    List<Instrutor> findDisponiveisEm(LocalDateTime dataHora);
}
