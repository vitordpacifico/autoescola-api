# Auto-Escola API

API REST em Spring Boot para agendamento de instruções de uma auto-escola — **Checkpoint 4**, disciplina **SOA e Web Services** (Prof. Carlos Eduardo Machado de Oliveira).

## Integrantes

| Nome | RM |
|---|---|
| Beatriz Vieira de Novais | RM554746 |
| Guilherme Abe | RM554743 |
| Gustavo Ruiz Vieira Paulino | RM554779 |
| Mariana Neugebauer Dourado | RM550494 |
| Victor Pacifico Dias | RM558017 |

## O que esta entrega implementa

Conforme o enunciado do CP4 (continuação do CRUD de Instrutores/Alunos do CP3):

- **CRUD de Instrutores** e **Alunos**, com listagem paginada (10 registros/página, ordenada por nome) e **exclusão lógica** (o registro é marcado como inativo, nunca removido).
- **Tabela de usuários** com **pelo menos 1 usuário cadastrado** automaticamente no startup (`admin` / senha configurável).
- **Autenticação JWT**: só usuários cadastrados e autenticados acessam a API.
- **Cadastro de usuários com senha criptografada** (BCrypt, nunca texto puro).
- **Autorização por perfil**: apenas `ADMIN` pode cadastrar, listar, atualizar o perfil e excluir usuários.
- **Troca da própria senha**: qualquer usuário autenticado pode alterar a própria senha (nunca a de terceiros).
- **Agendamento de instruções**, com todas as regras de negócio do documento anexo (horário de funcionamento, antecedência mínima, limite diário por aluno, conflito de horário do instrutor, escolha aleatória de instrutor).
- **Cancelamento de instruções**, com motivo obrigatório e antecedência mínima de 24 horas.

## Stack

Java 21 · Spring Boot 3.5 · Spring Security + JWT (jjwt) · Spring Data JPA · Bean Validation · H2 (padrão) / PostgreSQL (opcional) · springdoc-openapi (Swagger UI) · JUnit 5 + Mockito + MockMvc.

## Como rodar

Nenhuma infraestrutura externa é necessária — o perfil padrão usa **H2 em memória**.

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Documentação interativa (Swagger UI):
`http://localhost:8080/swagger-ui.html`.

No primeiro start, um usuário `ADMIN` é criado automaticamente:

| username | senha |
|---|---|
| `admin` | `Admin@123` |

(configuráveis via `ADMIN_DEFAULT_USERNAME` / `ADMIN_DEFAULT_SENHA`).

### Rodar com PostgreSQL (opcional)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
# variáveis: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
```

### Rodar os testes

```bash
./mvnw test
```

## Autenticação

```http
POST /api/v1/auth/login
Content-Type: application/json

{ "username": "admin", "senha": "Admin@123" }
```

Retorna `{ "accessToken": "...", "tipo": "Bearer", "expiraEmSegundos": 7200 }`.
Use o token nas demais chamadas: header `Authorization: Bearer <accessToken>`.
No Swagger UI, clique em **Authorize** e cole apenas o token (sem o prefixo `Bearer`).

## Endpoints

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/v1/auth/login` | público | Login, retorna o JWT |
| POST | `/api/v1/usuarios` | ADMIN | Cadastrar usuário (senha criptografada) |
| GET | `/api/v1/usuarios?pagina=&tamanho=` | ADMIN | Listar usuários |
| PUT | `/api/v1/usuarios/{id}` | ADMIN | Atualizar perfil/status de um usuário |
| DELETE | `/api/v1/usuarios/{id}` | ADMIN | Excluir usuário |
| PUT | `/api/v1/usuarios/me/senha` | autenticado | Trocar a própria senha |
| POST | `/api/v1/instrutores` | autenticado | Cadastrar instrutor |
| GET | `/api/v1/instrutores?pagina=&tamanho=` | autenticado | Listar instrutores (paginado, 10/página, por nome) |
| GET | `/api/v1/instrutores/{id}` | autenticado | Detalhe de um instrutor |
| PUT | `/api/v1/instrutores/{id}` | autenticado | Atualizar nome/telefone/endereço (e-mail, CNH e especialidade são imutáveis) |
| DELETE | `/api/v1/instrutores/{id}` | autenticado | Exclusão lógica (inativa) |
| POST | `/api/v1/alunos` | autenticado | Cadastrar aluno |
| GET | `/api/v1/alunos?pagina=&tamanho=` | autenticado | Listar alunos (paginado, 10/página, por nome) |
| GET | `/api/v1/alunos/{id}` | autenticado | Detalhe de um aluno |
| PUT | `/api/v1/alunos/{id}` | autenticado | Atualizar nome/telefone/endereço (e-mail e CPF são imutáveis) |
| DELETE | `/api/v1/alunos/{id}` | autenticado | Exclusão lógica (inativa) |
| POST | `/api/v1/instrucoes` | autenticado | Agendar instrução |
| GET | `/api/v1/instrucoes/{id}` | autenticado | Detalhe de uma instrução |
| POST | `/api/v1/instrucoes/{id}/cancelamento` | autenticado | Cancelar instrução (motivo obrigatório) |

### Exemplo — agendar instrução

```json
POST /api/v1/instrucoes
{
  "alunoId": 1,
  "instrutorId": 2,
  "dataHora": "2026-09-15T10:00:00"
}
```

`instrutorId` é **opcional** — se omitido, o sistema escolhe aleatoriamente um instrutor disponível no horário.

### Exemplo — cancelar instrução

```json
POST /api/v1/instrucoes/1/cancelamento
{ "motivo": "ALUNO_DESISTIU" }
```

Valores aceitos: `ALUNO_DESISTIU`, `INSTRUTOR_CANCELOU`, `OUTROS`.

## Regras de negócio implementadas

**Cadastro (Instrutor/Aluno)**
- Todos os campos obrigatórios, exceto número e complemento do endereço.
- E-mail (Instrutor/Aluno) e CNH (Instrutor) e CPF (Aluno, validado por dígito verificador) são únicos.
- Atualização não permite alterar e-mail/CNH/especialidade (instrutor) nem e-mail/CPF (aluno).
- Exclusão é sempre lógica (`ativo = false`).

**Agendamento**
- Funcionamento: segunda a sábado, 06:00–21:00 (instrução de 1h — início mais tardio: 20:00).
- Antecedência mínima de 30 minutos.
- Não agenda com aluno ou instrutor inativos.
- Máximo de 2 instruções por dia por aluno.
- Instrutor não pode ter duas instruções no mesmo horário.
- Instrutor não informado → escolhido aleatoriamente entre os disponíveis no horário.

**Cancelamento**
- Motivo obrigatório (enum fechado).
- Só permitido com antecedência mínima de 24 horas.

## Estrutura do projeto

```
src/main/java/com/fiap/autoescola/
├── config/        # Security, JWT, Swagger, DataInitializer, regras de agendamento
├── controller/    # REST controllers
├── dto/           # Request/response (nunca expõe entidades JPA diretamente)
├── exception/     # Exceções de negócio + GlobalExceptionHandler
├── model/         # Entidades JPA
├── repository/    # Spring Data JPA
├── security/      # JWT (geração/validação/filtro) + UserDetailsService
├── service/       # Regras de negócio
└── util/          # CpfValidator, EnderecoMapper
```

43 testes automatizados (unitários + integração com MockMvc/H2) cobrindo autenticação, RBAC, CRUDs e as regras de negócio de agendamento/cancelamento.
