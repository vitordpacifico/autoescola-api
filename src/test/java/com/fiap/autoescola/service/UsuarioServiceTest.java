package com.fiap.autoescola.service;

import com.fiap.autoescola.dto.usuario.AlterarSenhaRequest;
import com.fiap.autoescola.dto.usuario.AtualizarUsuarioRequest;
import com.fiap.autoescola.dto.usuario.CriarUsuarioRequest;
import com.fiap.autoescola.dto.usuario.UsuarioResponse;
import com.fiap.autoescola.exception.DuplicateResourceException;
import com.fiap.autoescola.exception.InvalidCredentialsException;
import com.fiap.autoescola.exception.ResourceNotFoundException;
import com.fiap.autoescola.model.Perfil;
import com.fiap.autoescola.model.Usuario;
import com.fiap.autoescola.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioExistente() {
        return Usuario.builder()
                .id(1L).username("operador").senhaHash("hash-antigo").perfil(Perfil.USER).ativo(true).build();
    }

    @Test
    void cadastrar_criptografaASenha_eNuncaGuardaTextoPuro() {
        var request = new CriarUsuarioRequest("operador", "Senha@12345", Perfil.USER);
        when(usuarioRepository.existsByUsername("operador")).thenReturn(false);
        when(passwordEncoder.encode("Senha@12345")).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UsuarioResponse response = usuarioService.cadastrar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getSenhaHash()).isEqualTo("hash-bcrypt").isNotEqualTo("Senha@12345");
        assertThat(response.username()).isEqualTo("operador");
        assertThat(response.perfil()).isEqualTo(Perfil.USER);
        assertThat(response.ativo()).isTrue();
    }

    @Test
    void cadastrar_comUsernameDuplicado_lancaDuplicateResourceException() {
        var request = new CriarUsuarioRequest("operador", "Senha@12345", Perfil.USER);
        when(usuarioRepository.existsByUsername("operador")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void atualizarPerfil_alteraPerfilEStatus() {
        Usuario existente = usuarioExistente();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse response = usuarioService.atualizarPerfil(1L, new AtualizarUsuarioRequest(Perfil.ADMIN, false));

        assertThat(response.perfil()).isEqualTo(Perfil.ADMIN);
        assertThat(response.ativo()).isFalse();
    }

    @Test
    void atualizarPerfil_deUsuarioInexistente_lancaResourceNotFoundException() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.atualizarPerfil(99L, new AtualizarUsuarioRequest(Perfil.ADMIN, true)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void excluir_removeOUsuario() {
        Usuario existente = usuarioExistente();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        usuarioService.excluir(1L);

        verify(usuarioRepository).delete(existente);
    }

    @Test
    void alterarPropriaSenha_comSenhaAtualCorreta_gravaNovoHash() {
        Usuario existente = usuarioExistente();
        when(usuarioRepository.findByUsername("operador")).thenReturn(Optional.of(existente));
        when(passwordEncoder.matches("Senha@12345", "hash-antigo")).thenReturn(true);
        when(passwordEncoder.encode("NovaSenha@123")).thenReturn("hash-novo");

        usuarioService.alterarPropriaSenha("operador", new AlterarSenhaRequest("Senha@12345", "NovaSenha@123"));

        assertThat(existente.getSenhaHash()).isEqualTo("hash-novo");
        verify(usuarioRepository).save(existente);
    }

    @Test
    void alterarPropriaSenha_comSenhaAtualErrada_lancaInvalidCredentialsException() {
        Usuario existente = usuarioExistente();
        when(usuarioRepository.findByUsername("operador")).thenReturn(Optional.of(existente));
        when(passwordEncoder.matches("errada", "hash-antigo")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.alterarPropriaSenha("operador", new AlterarSenhaRequest("errada", "NovaSenha@123")))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(existente.getSenhaHash()).isEqualTo("hash-antigo");
        verify(usuarioRepository, never()).save(any());
    }
}
