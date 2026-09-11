package com.fiap.autoescola.repository;

import com.fiap.autoescola.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<Usuario> findAllByOrderByUsernameAsc(Pageable pageable);
}
