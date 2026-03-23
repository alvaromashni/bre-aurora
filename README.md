# Business Rule Engine — Motor de Regras de Negócio Configurável - BRE AURORA

Motor de regras de negócio desenvolvido em Java com Spring Boot, capaz de avaliar condições dinâmicas contra eventos recebidos via API REST. As regras são armazenadas em banco de dados e interpretadas em tempo de execução, eliminando a necessidade de alterações no código a cada mudança de lógica de negócio.

---

## Motivacao

Em sistemas tradicionais, regras de negócio como "bloquear transações acima de R$ 5.000" ficam hardcoded no código-fonte. Qualquer alteração exige modificação, revisão, testes e deploy — um ciclo que pode levar dias. Este projeto resolve esse problema separando dados de lógica: as regras vivem no banco e a engine as interpreta dinamicamente, permitindo que analistas alterem o comportamento do sistema sem intervenção de um desenvolvedor.

Esse padrão é amplamente utilizado em sistemas de antifraude, motores de crédito, triagem médica e plataformas de compliance regulatório.

---

## Funcionalidades

- Avaliação de regras dinâmicas definidas via API, sem necessidade de redeploy
- Suporte a condições compostas com operadores `and`, `or` e comparadores relacionais
- Sistema de ações extensível com os tipos `LOG`, `ALERT` e `BLOCK`
- Cache em memória das regras ativas com recarga agendada
- Registro de auditoria completo para cada evento avaliado
- Retorno de `HTTP 403` automático quando uma regra de bloqueio é disparada
- Proteção contra SpEL injection via `SimpleEvaluationContext`

---

## Tecnologias

| Camada | Tecnologia                           |
|---|--------------------------------------|
| Framework | Spring Boot 4.x                      |
| API REST | Spring Web                           |
| Avaliação de expressoes | Spring Expression Language (SpEL)    |
| Persistencia | Spring Data JPA + PostgreSQL         |
| Cache | ConcurrentHashMap + Spring Scheduler |
| Documentacao | SpringDoc OpenAPI (Swagger UI)       |
| Testes | JUnit 5 + Mockito + AssertJ          |
| Containerizacao | Docker + Docker Compose              |
| Utilitarios | Lombok                               |

---

## Arquitetura

O projeto segue organizacao por funcionalidade (package-by-feature), com separacao clara entre as camadas de entrada, processamento e persistencia.

```
src/main/java/com/seuusuario/ruleengine/
|
|-- event/         # Recepcao e orquestracao de eventos
|-- rule/          # Gerenciamento de regras (CRUD)
|-- engine/        # Avaliacao de expressoes e cache
|-- action/        # Execucao de acoes pos-avaliacao
|-- audit/         # Registro de auditoria
|-- config/        # Configuracoes globais (Swagger, ExceptionHandler)
```

### Fluxo de execucao

```
POST /events/evaluate
        |
        v
  EventController
        |
        v
  RuleEngine.evaluate(eventData)
        |
        |-- RuleCache.getActiveRules()
        |
        v
  Para cada Rule (ordenada por prioridade):
        |
        |-- SpelRuleEvaluator.evaluate(condition, eventData)
        |       |
        |       |-- true  --> ActionExecutorRegistry.execute(rule, eventData)
        |       |        --> AuditService.log(..., matched=true)
        |       |
        |       |-- false --> AuditService.log(..., matched=false)
        |
        v
  EvaluationResult (totalRulesEvaluated, matchedRules, isBlocked)
```

---

## Design patterns aplicados

### Strategy — avaliação de expressões

A interface `RuleEvaluator` define o contrato de avaliação. A implementação padrão utiliza SpEL, mas pode ser substituída por qualquer outra biblioteca (MVEL, Groovy, etc.) sem alteração na engine.

```java
public interface RuleEvaluator {
    boolean evaluate(String condition, Map<String, Object> eventData);
}
```

### Chain of Responsibility — encadeamento de regras

A `RuleEngine` itera sobre as regras em ordem de prioridade. Cada regra e avaliada de forma independente — o resultado de uma nao interfere na avaliacao da proxima, salvo quando a acao e `BLOCK`.

### Registry — mapeamento de acoes

O `ActionExecutorRegistry` mantem um `Map<String, ActionExecutor>` populado automaticamente pelo Spring a partir de todos os beans que implementam a interface `ActionExecutor`. Para adicionar um novo tipo de acao, basta criar um novo componente anotado com `@Component` — nenhum codigo existente precisa ser alterado.

```java
public ActionExecutorRegistry(List<ActionExecutor> executorList) {
    this.executors = executorList.stream()
            .collect(Collectors.toMap(ActionExecutor::getType, e -> e));
}
```

---

## Seguranca

### Protecao contra SpEL Injection

O Spring Expression Language (SpEL) e capaz de executar metodos Java arbitrarios se utilizado com `StandardEvaluationContext`. Uma expressao maliciosa armazenada no banco poderia executar comandos no servidor:

```
// expressao maliciosa
T(java.lang.Runtime).getRuntime().exec('rm -rf /')
```

Este projeto utiliza `SimpleEvaluationContext`, que restringe a avaliacao a operacoes de leitura sobre o objeto raiz, bloqueando acesso a classes e metodos do sistema:

```java
SimpleEvaluationContext.forReadOnlyDataBinding()
        .withInstanceMethods()
        .withRootObject(eventData)
        .build();
```

A cobertura de testes inclui um caso especifico que valida este comportamento:

```java
@Test
void deveBloquearSpelInjection() {
    boolean result = evaluator.evaluate(
            "T(java.lang.Runtime).getRuntime().exec('echo hack')", event);
    assertThat(result).isFalse();
}
```

---

## Estrutura das Regras

As regras são definidas em JSON e armazenadas no banco. A condição e uma expressão avaliada contra os dados do evento recebido.

```json
{
  "name": "Fraude internacional",
  "description": "Alerta para transacoes de valor elevado fora do Brasil",
  "condition": "['valor'] > 500 and ['pais'] != 'BR'",
  "actionType": "ALERT",
  "priority": 1,
  "active": true
}
```

### Tipos de ação disponíveis

| Tipo | Comportamento |
|---|---|
| `LOG` | Registra o disparo nos logs da aplicacao |
| `ALERT` | Registra alerta estruturado — ponto de extensao para webhook ou fila |
| `BLOCK` | Registra bloqueio e retorna `HTTP 403` na resposta da API |

---

## Como Executar

### Pre-requisitos

- Java 21
- Docker e Docker Compose

### Passos

**1. Clone o repositório**

```bash
git clone https://github.com/alvaromashni/rule-engine.git
cd rule-engine
```

**2. Configure as variáveis de ambiente**

```bash
cp .env.example .env
```

Edite o `.env` com os valores desejados:

```env
POSTGRES_DB=ruleengine
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
DB_PORT=5432
```

**3. Suba o banco de dados**

```bash
docker compose up -d
```

**4. Execute a aplicação**

```bash
./mvnw spring-boot:run
```

A aplicacao estara disponivel em `http://localhost:8080`.

A documentacao interativa da API estara disponivel em `http://localhost:8080/swagger-ui/index.html`.

---

## Exemplos de Uso

### Cadastrar uma regra

```http
POST /rules
Content-Type: application/json

{
  "name": "Bloqueio de cliente novo suspeito",
  "condition": "['clienteNovo'] == true and ['valor'] > 2000",
  "actionType": "BLOCK",
  "priority": 0,
  "active": true
}
```

### Avaliar um evento

```http
POST /events/evaluate
Content-Type: application/json

{
  "valor": 3000,
  "pais": "BR",
  "clienteNovo": true
}
```

Resposta com regra de bloqueio disparada (`HTTP 403`):

```json
{
  "totalRulesEvaluated": 3,
  "matchedRules": [
    {
      "ruleId": "a1b2c3d4-...",
      "ruleName": "Bloqueio de cliente novo suspeito",
      "actionTaken": "BLOCK"
    }
  ],
  "blocked": true
}
```

### Consultar auditoria

```http
GET /audit/matched
```

---

## Testes

O projeto possui cobertura completa com 34 testes distribuidos entre testes unitarios e de integracao.

```bash
./mvnw test
```

| Camada | Testes | Tipo |
|---|---|---|
| `SpelRuleEvaluator` | 7 | Unitario |
| `ActionExecutorRegistry` | 4 | Unitario |
| `RuleService` | 9 | Unitario |
| `AuditService` | 5 | Unitario |
| `RuleEngine` | 5 | Unitario |
| `RuleCache` | 4 | Unitario |
| `RuleController` | 6 | Integracao |
| `EventController` | 4 | Integracao |
| `AuditController` | 5 | Integracao |

---

## Possiveis Evolucoes

- Versionamento de regras com histórico de alterações
- Interface web para gerenciamento de regras sem uso da API
- Metricas de disparo por regra (frequência de ativação)
- Integração com Apache Kafka para ingestao de eventos em stream
- Suporte a grupos de condições com operadores lógicos em nível de banco
