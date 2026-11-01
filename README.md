# FinanceFlow

Painel de controle financeiro pessoal com lançamentos, categorias, contas a vencer e alertas agendados.

## Stack

- **API:** Java 21, Spring Boot 4.0.9, Spring Security com JWT, Spring Data JPA, PostgreSQL, Flyway
- **Web:** Angular 21 (standalone), TypeScript, SCSS
- **Infra local:** Docker Compose

## Modelo de dados

```mermaid
erDiagram
    USUARIOS ||--o{ CATEGORIAS : possui
    USUARIOS ||--o{ LANCAMENTOS : registra
    CATEGORIAS ||--o{ LANCAMENTOS : classifica

    USUARIOS {
        uuid id PK
        string nome
        string email UK
        string senha_hash
    }
    CATEGORIAS {
        uuid id PK
        uuid usuario_id FK
        string nome
        string tipo
    }
    LANCAMENTOS {
        uuid id PK
        uuid usuario_id FK
        uuid categoria_id FK
        string descricao
        decimal valor
        string tipo
        date data_lancamento
        date data_vencimento
        boolean paga
        date data_pagamento
    }
```

## Testes

```bash
cd api
./mvnw verify
```

Os testes de persistência sobem um PostgreSQL real via Testcontainers, então o Docker precisa estar ativo.

## Autenticação

A API usa JWT (HS256) em modo stateless.

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| POST | `/api/auth/cadastro` | público | Cria um usuário |
| POST | `/api/auth/login` | público | Retorna o token de acesso |
| GET | `/api/auth/eu` | autenticado | Retorna o perfil do usuário do token |
| GET | `/actuator/health` | público | Health check |

Enviar o token no header `Authorization: Bearer <token>`.

### Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/financeflow` | URL do banco |
| `DB_USERNAME` | `financeflow` | Usuário do banco |
| `DB_PASSWORD` | `financeflow` | Senha do banco |
| `JWT_SEGREDO` | valor de desenvolvimento | Segredo do JWT, mínimo de 32 caracteres |
| `JWT_EXPIRACAO` | `2h` | Validade do token |
| `CORS_ORIGENS` | `http://localhost:4200` | Origens permitidas, separadas por vírgula |
| `FUSO_HORARIO` | `America/Sao_Paulo` | Fuso usado para calcular "hoje" e vencimentos |
| `AGENDAMENTO_HABILITADO` | `true` | Liga ou desliga os jobs agendados |
| `RECORRENCIAS_CRON` | `0 0 6 * * *` | Horário do job de geração de recorrências (no fuso configurado) |
| `NOTIFICACOES_CRON` | `0 30 7 * * *` | Horário do job de avisos de vencimento (no fuso configurado) |

Em produção, sempre defina `JWT_SEGREDO` com um valor forte e exclusivo.

## Categorias

Cada usuário recebe categorias padrão no cadastro e pode gerenciar as próprias. O tipo (`RECEITA` ou `DESPESA`) não muda depois de criado, e nomes são únicos por usuário e tipo, ignorando maiúsculas e minúsculas.

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/categorias?tipo=` | Lista as categorias, com filtro opcional por tipo |
| POST | `/api/categorias` | Cria uma categoria |
| PUT | `/api/categorias/{id}` | Renomeia |
| DELETE | `/api/categorias/{id}` | Exclui, desde que não tenha lançamentos |

## Lançamentos

O tipo (`RECEITA` ou `DESPESA`) vem da categoria escolhida. A `natureza` diferencia contas fixas (`FIXA`: aluguel, internet) de gastos avulsos (`VARIAVEL`, o padrão).

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/lancamentos` | Cria um lançamento (pode já nascer pago) |
| GET | `/api/lancamentos` | Lista com filtros, paginação e ordenação |
| GET | `/api/lancamentos/{id}` | Busca um lançamento |
| PUT | `/api/lancamentos/{id}` | Atualiza |
| DELETE | `/api/lancamentos/{id}` | Exclui |
| POST | `/api/lancamentos/{id}/pagamento` | Marca como pago (data opcional, padrão hoje) |
| DELETE | `/api/lancamentos/{id}/pagamento` | Desfaz o pagamento |

### Filtros da listagem

| Parâmetro | Exemplo | Descrição |
|---|---|---|
| `inicio`, `fim` | `2026-10-01` | Período pela data do lançamento |
| `tipo` | `DESPESA` | Receita ou despesa |
| `natureza` | `FIXA` | Fixa ou variável |
| `categoriaId` | UUID | Uma categoria específica |
| `paga` | `true` | Pagos ou em aberto |
| `vencidos` | `true` | Em aberto com vencimento anterior a hoje |
| `busca` | `mercado` | Texto contido na descrição |
| `page`, `size` | `0`, `20` | Paginação (máximo de 100 por página) |
| `sort` | `valor,desc` | Ordenação por `dataLancamento`, `dataVencimento`, `valor`, `descricao` ou `criadoEm` |

## Painel

`GET /api/painel?mes=2026-10` devolve tudo o que a tela principal precisa em uma requisição. Sem `mes`, usa o mês atual.

| Bloco | Conteúdo |
|---|---|
| `previsto` | Entradas, saídas e saldo de todos os lançamentos do mês |
| `realizado` | Entradas, saídas e saldo apenas dos lançamentos pagos |
| `contasAVencer` | Despesas em aberto até hoje + 7 dias, atrasadas primeiro, com `vencido` e `diasParaVencer` |
| `gastosPorCategoria` | Despesas do mês por categoria, com total e percentual |
| `despesasPorNatureza` | Despesas do mês separadas em fixas e variáveis |

As somas são calculadas no banco, e a lista de contas a vencer traz no máximo 50 itens, com `quantidade` e `valorTotal` cobrindo todas.

## Contas fixas recorrentes

Cadastre aluguel, internet, luz ou um salário uma vez, e o sistema gera o lançamento de cada mês com o valor estimado. Quando a conta chega, basta ajustar o valor do lançamento.

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/recorrencias` | Cria e já gera o mês atual e o próximo |
| GET | `/api/recorrencias?ativa=` | Lista, com filtro opcional por status |
| GET | `/api/recorrencias/{id}` | Busca uma recorrência |
| PUT | `/api/recorrencias/{id}` | Atualiza (vale apenas para as próximas gerações) |
| POST | `/api/recorrencias/{id}/pausar` | Pausa a geração |
| POST | `/api/recorrencias/{id}/retomar` | Retoma sem gerar meses passados |
| DELETE | `/api/recorrencias/{id}` | Exclui, mantendo os lançamentos já gerados |

Regras de geração:

- Um job diário garante os lançamentos do mês atual e do próximo.
- Dia de vencimento 31 em meses curtos vira o último dia do mês.
- O job é idempotente: rodar duas vezes, ou em duas instâncias, não duplica lançamentos.
- Um lançamento excluído pelo usuário não é recriado.
- Os lançamentos gerados têm natureza `FIXA` e entram normalmente no painel e na lista de contas a vencer.

## Notificações

Um job diário avisa sobre despesas em aberto: três dias antes do vencimento, no dia e quando atrasam (até 30 dias). Cada conta gera no máximo um aviso de cada tipo, e rodar o job de novo não duplica nada.

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/notificacoes?naoLidas=` | Lista os avisos, mais recentes primeiro |
| GET | `/api/notificacoes/nao-lidas/total` | Total de avisos não lidos |
| POST | `/api/notificacoes/{id}/leitura` | Marca um aviso como lido |
| POST | `/api/notificacoes/leitura` | Marca todos como lidos |

Os avisos são entregues por canais plugáveis (`CanalDeNotificacao`). O canal atual escreve no log, sem o texto do aviso. Se um canal falhar, o aviso fica pendente e é reenviado na próxima execução. A entrega garante pelo menos uma vez, e o despacho assume uma única instância da API.

## Status

Em desenvolvimento.
