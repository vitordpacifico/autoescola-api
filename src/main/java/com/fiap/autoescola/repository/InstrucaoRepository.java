package com.fiap.autoescola.repository;

import com.fiap.autoescola.model.Instrucao;
import com.fiap.autoescola.model.StatusInstrucao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface InstrucaoRepository extends JpaRepository<Instrucao, Long> {
    @Query("""
            SELECT COUNT(i) FROM Instrucao i
            WHERE i.aluno.id = :alunoId
              AND i.status <> com.fiap.autoescola.model.StatusInstrucao.CANCELADA
              AND i.dataHora >= :inicioDoDia AND i.dataHora < :fimDoDia
            """)
    long countNaoCanceladasDoAlunoNoDia(@Param("alunoId") Long alunoId,
                                         @Param("inicioDoDia") LocalDateTime inicioDoDia,
                                         @Param("fimDoDia") LocalDateTime fimDoDia);

    boolean existsByInstrutorIdAndDataHoraAndStatusNot(Long instrutorId, LocalDateTime dataHora, StatusInstrucao statusExcluido);
}
