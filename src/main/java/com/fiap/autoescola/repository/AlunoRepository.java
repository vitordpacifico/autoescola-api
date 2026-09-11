package com.fiap.autoescola.repository;

import com.fiap.autoescola.model.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Page<Aluno> findAllByOrderByNomeAsc(Pageable pageable);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    Optional<Aluno> findByIdAndAtivoTrue(Long id);
}
