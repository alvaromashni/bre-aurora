# BRE Aurora

Motor de regras de negocio (Business Rule Engine) desenvolvido com Spring Boot. O sistema permite cadastrar regras com condicoes dinamicas avaliadas via **Spring Expression Language (SpEL)**, executar acoes automaticas e manter um log de auditoria completo.

---

## Indice

- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pre-requisitos](#pre-requisitos)
- [Configuracao](#configuracao)
- [Como Executar](#como-executar)

---

## Tecnologias

| Tecnologia | Versao | Finalidade |
|---|---|---|
| **Java** | 21 | Linguagem principal |
| **Spring Boot** | 4.0.4 | Framework base da aplicacao |
| **Spring Data JPA** | - | Persistencia e repositorios |
| **Spring Expression Language (SpEL)** | - | Avaliacao dinamica de condicoes das regras |
| **Hibernate** | - | ORM e gerenciamento de entidades |
| **PostgreSQL** | - | Banco de dados relacional |
| **Flyway** | - | Versionamento e migracao do banco de dados |
| **Lombok** | - | Reducao de boilerplate (getters, builders, etc.) |
| **Maven** | - | Gerenciamento de dependencias e build |

---

## Arquitetura

O projeto segue uma arquitetura modular organizada por dominio:

```
Evento (Map<String, Object>)
        |
        v
   [RuleEngine] --> busca regras no [RuleCache]
        |
        v
   [SpelRuleEvaluator] --> avalia condicao SpEL contra os dados do evento
        |
        v
   Match? --sim--> [ActionExecutorRegistry] --> delega para o executor correto
        |                                        (BLOCK, ALERT, LOG)
        v
   [AuditService] --> registra resultado no banco (audit_logs)
```

**Componentes principais:**

- **RuleEngine** — orquestra a avaliacao de todas as regras ativas contra um evento recebido.
- **RuleCache** — cache em memoria das regras ativas, com refresh periodico via `@Scheduled`.
- **SpelRuleEvaluator** — avalia expressoes SpEL de forma segura usando `StandardEvaluationContext`.
- **ActionExecutorRegistry** — registry de executores de acao (`BLOCK`, `ALERT`, `LOG`), resolvidos automaticamente via injecao de dependencia.
- **AuditService** — persiste logs de auditoria com dados do evento, regra avaliada e resultado.

---

## Estrutura do Projeto

```
src/main/java/dev/mashni/breaurora/
├── action/
│   ├── ActionExecutor.java           # Interface de executor de acao
│   ├── ActionExecutorRegistry.java   # Registry que mapeia tipo -> executor
│   ├── AlertActionExecutor.java      # Executor: ALERT
│   ├── BlockActionExecutor.java      # Executor: BLOCK
│   └── LogActionExecutor.java        # Executor: LOG
├── audit/
│   ├── AuditLog.java                 # Entidade JPA (audit_logs)
│   ├── AuditRepository.java          # Repository Spring Data
│   └── AuditService.java             # Servico de auditoria
├── engine/
│   ├── dto/
│   │   ├── EvaluationResult.java     # DTO com resultado da avaliacao
│   │   └── MatchedRule.java          # DTO de regra que deu match
│   ├── RuleCache.java                # Cache em memoria com refresh periodico
│   ├── RuleEngine.java               # Orquestrador principal
│   ├── RuleEvaluator.java            # Interface do avaliador de regras
│   └── SpelRuleEvaluator.java        # Implementacao SpEL
└── rule/
    ├── Rule.java                     # Entidade JPA (rules)
    ├── RuleRepository.java           # Repository Spring Data
    └── RuleService.java              # Servico de regras
```

---

## Pre-requisitos

- **Java 21+**
- **Maven 3.9+**
- **PostgreSQL** rodando na porta `5433` (ou configurar via variaveis de ambiente)

---

## Configuracao

As configuracoes ficam em `src/main/resources/application.properties`:

| Propriedade | Padrao | Descricao |
|---|---|---|
| `DB_HOST` | `localhost` | Host do banco PostgreSQL |
| `DB_PORT` | `5433` | Porta do banco |
| `DB_NAME` | `bre-aurora` | Nome do banco |
| `rule.cache.refresh-rate-ms` | `300000` (5min) | Intervalo de refresh do cache de regras |

---

## Como Executar

```bash
# clonar o repositorio
git clone https://github.com/seu-usuario/bre-aurora.git
cd bre-aurora

# executar
./mvnw spring-boot:run
```
