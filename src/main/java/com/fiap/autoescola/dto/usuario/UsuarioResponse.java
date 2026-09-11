package com.fiap.autoescola.dto.usuario;

import com.fiap.autoescola.model.Perfil;
import com.fiap.autoescola.model.Usuario;

import java.time.Instant;

/** Nunca inclui a senha/hash - protecao contra exposicao acidental. */
public record UsuarioResponse(Long id, String username, Perfil perfil, boolean ativo, Instant createdAt) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getPerfil(),
                usuario.isAtivo(), usuario.getCreatedAt());
    }
}
