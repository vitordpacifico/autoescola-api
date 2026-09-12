package com.fiap.autoescola.service;

import com.fiap.autoescola.dto.usuario.AlterarSenhaRequest;
import com.fiap.autoescola.dto.usuario.AtualizarUsuarioRequest;
import com.fiap.autoescola.dto.usuario.CriarUsuarioRequest;
import com.fiap.autoescola.dto.usuario.UsuarioResponse;
import com.fiap.autoescola.exception.DuplicateResourceException;
import com.fiap.autoescola.exception.InvalidCredentialsException;
import com.fiap.autoescola.exception.ResourceNotFoundException;
import com.fiap.autoescola.model.Usuario;
import com.fiap.autoescola.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse cadastrar(CriarUsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Já existe um usuário com este username.");
        }

        Usuario usuario = Usuario.builder()
                .username(request.username())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .ativo(true)
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuário cadastrado: '{}' (perfil={})", salvo.getUsername(), salvo.getPerfil());
        return UsuarioResponse.from(salvo);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findAllByOrderByUsernameAsc(pageable).map(UsuarioResponse::from);
    }

    @Transactional
    public UsuarioResponse atualizarPerfil(Long id, AtualizarUsuarioRequest request) {
        Usuario usuario = buscarPorId(id);
        usuario.setPerfil(request.perfil());
        usuario.setAtivo(request.ativo());
        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuário '{}' atualizado (perfil={}, ativo={})", salvo.getUsername(), salvo.getPerfil(), salvo.isAtivo());
        return UsuarioResponse.from(salvo);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
        log.info("Usuário '{}' excluído.", usuario.getUsername());
    }

    @Transactional
    public void alterarPropriaSenha(String username, AlterarSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new InvalidCredentialsException("Senha atual incorreta.");
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);
        log.info("Usuário '{}' alterou a própria senha.", username);
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: id=" + id));
    }
}
