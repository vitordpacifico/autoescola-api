# Auto-Escola API

API REST em Spring Boot para agendamento de instruções de uma auto-escola — **Checkpoint 5**, disciplina **SOA e Web Services** (Prof. Carlos Eduardo Machado de Oliveira).

## Integrantes

| Nome | RM |
|---|---|
| Guilherme Abe | RM554743 |
| Gustavo Ruiz Vieira Paulino | RM554779 |
| Victor Pacifico Dias | RM558017 |
| Beatriz | RM554746 |
| Mariana | RM550494 |

## O que o Checkpoint 5 acrescenta

| Demanda do enunciado | Onde está |
|---|---|
| Consumo de uma API/WebService externo | `integration/viacep/ViaCepClient` consome o **ViaCEP** via `RestClient`; exposto em `GET /api/v1/cep/{cep}` |
| Documentação automática com Swagger | `config/OpenApiConfig` + anotações `@Tag`/`@Operation` nos controllers; Swagger UI em `/swagger-ui.html` |
| Configuração de CORS | `config/CorsConfig` + `config/CorsProperties`, ligado ao Spring Security em `config/SecurityConfig` |
| Testes automatizados para cada entidade | `AlunoServiceTest`, `InstrutorServiceTest`, `UsuarioServiceTest`, `InstrucaoServiceTest` + testes de integração |

Além disso, a listagem paginada de instruções (`GET /api/v1/instrucoes`) fecha o CRUD de Instruções.

## O que já vinha dos checkpoints anteriores

Conforme os enunciados do CP3 e CP4:

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

### Variáveis de ambiente opcionais

| Variável | Padrão | Para que serve |
|---|---|---|
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173,http://localhost:4200` | Origens (front-ends) autorizadas a chamar a API |
| `VIACEP_BASE_URL` | `https://viacep.com.br/ws` | Endereço base do web service de CEP |
| `JWT_SECRET` / `JWT_EXPIRATION_MS` | valor de desenvolvimento / `7200000` | Assinatura e validade do token |
| `ADMIN_DEFAULT_USERNAME` / `ADMIN_DEFAULT_SENHA` | `admin` / `Admin@123` | Usuário criado no primeiro start |

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
| GET | `/api/v1/instrucoes?pagina=&tamanho=` | autenticado | Listar instruções (paginado, 10/página, por data e hora) |
| GET | `/api/v1/instrucoes/{id}` | autenticado | Detalhe de uma instrução |
| POST | `/api/v1/instrucoes/{id}/cancelamento` | autenticado | Cancelar instrução (motivo obrigatório) |
| GET | `/api/v1/cep/{cep}` | autenticado | Consultar endereço por CEP (consome o ViaCEP) |

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

## Consumo de API externa (ViaCEP)

O cadastro de alunos e instrutores exige endereço completo. Para não obrigar o operador a digitar tudo, a API consulta o web service público [ViaCEP](https://viacep.com.br) e devolve o endereço correspondente ao CEP informado.

```http
GET /api/v1/cep/01310-100
Authorization: Bearer <accessToken>
```

```json
{
  "cep": "01310-100",
  "logradouro": "Avenida Paulista",
  "complemento": "de 612 a 1510 - lado par",
  "bairro": "Bela Vista",
  "cidade": "São Paulo",
  "uf": "SP"
}
```

| Situação | Resposta |
|---|---|
| CEP encontrado | `200` com o endereço |
| CEP em formato inválido | `400` (a chamada externa nem é feita) |
| CEP inexistente | `404` |
| ViaCEP fora do ar ou lento (timeout de 3s para conectar, 5s para ler) | `502` |

A chamada é feita com o `RestClient` do Spring (`ViaCepClient`), e a resposta do serviço externo é convertida para um DTO próprio (`CepResponse`), de modo que o contrato da nossa API não depende do formato do ViaCEP.

## CORS

Navegadores bloqueiam chamadas feitas por um front-end hospedado em outra origem, a menos que a API autorize. A configuração fica em `CorsConfig` e é lida de `application.properties` (prefixo `autoescola.cors`):

- **Origens permitidas:** `http://localhost:3000`, `http://localhost:5173` e `http://localhost:4200` (React, Vite e Angular em desenvolvimento). Em produção, defina `CORS_ALLOWED_ORIGINS`.
- **Métodos:** `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`.
- **Cabeçalhos aceitos:** `Authorization`, `Content-Type`, `Accept`.

Como a API usa Spring Security, o CORS é habilitado na cadeia de filtros (`http.cors(...)`), para que a requisição de *preflight* (`OPTIONS`) seja respondida antes da verificação do token JWT. Origens fora da lista recebem `403`.

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
├── config/        # Security, CORS, Swagger, DataInitializer, regras de agendamento
├── controller/    # REST controllers
├── dto/           # Request/response (nunca expõe entidades JPA diretamente)
├── exception/     # Exceções de negócio + GlobalExceptionHandler
├── integration/   # Clientes de serviços externos (ViaCEP)
├── model/         # Entidades JPA
├── repository/    # Spring Data JPA
├── security/      # JWT (geração/validação/filtro) + UserDetailsService
├── service/       # Regras de negócio
└── util/          # CpfValidator, EnderecoMapper
```

## Testes automatizados

79 testes (unitários com JUnit 5 + Mockito e de integração com MockMvc + H2):

| Entidade / assunto | Classe de teste | O que cobre |
|---|---|---|
| Aluno | `service/AlunoServiceTest` | cadastro, CPF inválido, e-mail/CPF duplicados, listagem, atualização sem alterar e-mail/CPF, exclusão lógica |
| Instrutor | `service/InstrutorServiceTest` | cadastro, e-mail/CNH duplicados, listagem, atualização sem alterar e-mail/CNH/especialidade, exclusão lógica |
| Usuário | `service/UsuarioServiceTest` | senha criptografada, username duplicado, atualização de perfil, exclusão, troca da própria senha |
| Instrução | `service/InstrucaoServiceTest` | todas as regras de agendamento e cancelamento |
| Aluno e Instrutor (HTTP) | `controller/InstrutorAlunoIntegrationTest` | CRUD ponta a ponta |
| Usuário (HTTP) | `controller/AuthAndRbacIntegrationTest` | login, JWT e autorização por perfil |
| Instrução (HTTP) | `controller/InstrucaoIntegrationTest` | agendar, listar, cancelar e conflitos |
| ViaCEP | `integration/viacep/ViaCepClientTest`, `controller/CepIntegrationTest` | cliente HTTP com servidor simulado e endpoint de CEP |
| CORS | `config/CorsIntegrationTest` | preflight de origem permitida e bloqueio de origem não permitida |
| CPF | `util/CpfValidatorTest` | dígitos verificadores |

Os testes do ViaCEP não dependem de internet: o serviço externo é simulado, então a suíte roda igual em qualquer máquina.
