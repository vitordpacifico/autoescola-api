package com.fiap.autoescola.controller;

import com.fiap.autoescola.dto.usuario.AlterarSenhaRequest;
import com.fiap.autoescola.dto.usuario.AtualizarUsuarioRequest;
import com.fiap.autoescola.dto.usuario.CriarUsuarioRequest;
import com.fiap.autoescola.dto.usuario.UsuarioResponse;
import com.fiap.autoescola.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestao de usuarios. Cadastrar/listar/atualizar/excluir sao restritos a
 * ADMIN (regra aplicada no SecurityConfig); /me/senha e liberado a qualquer
 * usuario autenticado, que so pode alterar a propria senha.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Cadastro e gestão de usuários da API (apenas ADMIN, exceto troca da própria senha)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CriarUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.cadastrar(request));
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {
        Pageable pageable = PageRequest.of(pagina, tamanho, Sort.by("username").ascending());
        return ResponseEntity.ok(usuarioService.listar(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizarPerfil(@PathVariable Long id,
                                                            @Valid @RequestBody AtualizarUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.atualizarPerfil(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** Qualquer usuário autenticado pode alterar a própria senha (nunca a de terceiros). */
    @PutMapping("/me/senha")
    public ResponseEntity<Void> alterarPropriaSenha(Authentication authentication,
                                                     @Valid @RequestBody AlterarSenhaRequest request) {
        usuarioService.alterarPropriaSenha(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
