# Sistema de Compras Internacionais

API REST para registrar compras internacionais em dólar americano (USD). A aplicação consulta a cotação de venda do dólar na [AwesomeAPI](https://docs.awesomeapi.com.br/api-de-moedas), converte o valor para reais (BRL) e aplica taxa de serviço, tributo didático e limite mensal por cliente.

Projeto do **Segundo Exercício — Pós-Graduação Backend II** (Programação Backend II).

**Autor(es):** [nome completo]

---

## Tecnologias

- Java 21
- Spring Boot 4 (Web MVC, Validation)
- Spring Data JPA (Hibernate)
- Spring Cloud OpenFeign
- Flyway (migrations)
- MySQL 8
- Lombok
- Maven (wrapper incluído)

## Arquitetura

Arquitetura MVC em camadas, com as responsabilidades separadas:

```
com.aula.pos.comprasinternacionais
 ├─ controller   → recebe requisições e devolve respostas (@Valid nos DTOs)
 ├─ service      → regras de negócio (cálculos, limite mensal, cotação)
 ├─ repository   → persistência (Spring Data JPA)
 ├─ entity       → domínio: Cliente e Compra
 ├─ dto          → contratos de entrada e saída da API
 ├─ mapper       → conversão entidade → DTO
 ├─ client       → cliente OpenFeign da AwesomeAPI e DTOs externos
 └─ exception    → exceções personalizadas e GlobalExceptionHandler
```

As entidades nunca são devolvidas pelos controllers: a API recebe e devolve apenas DTOs.

---

## Como executar

### 1. Pré-requisitos

- JDK 21 instalado
- MySQL 8 em execução na porta `3306`
- Acesso à internet (para consultar a AwesomeAPI)

### 2. Configuração do banco de dados

O projeto usa o MySQL com o banco `compras_db`. A URL já inclui `createDatabaseIfNotExist=true`, então o banco é criado automaticamente na primeira execução.

A configuração está em `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/compras_db?createDatabaseIfNotExist=true
    username: root
    password: ${DB_PASSWORD}
```

A **senha do banco não fica no código**: ela é lida da variável de ambiente `DB_PASSWORD`. Se o seu MySQL usa outro usuário, host ou porta, ajuste `username` e `url` no arquivo.

As tabelas (`tb_clientes` e `tb_compras`) e os clientes de exemplo são criados automaticamente pelo **Flyway** ao subir a aplicação, a partir de `src/main/resources/db/migration`:

| Migration | Conteúdo |
|-----------|----------|
| `V1__create_tables_clientes_compras.sql` | Cria as tabelas, a chave estrangeira e o índice |
| `V2__insert_clientes.sql` | Insere 3 clientes de exemplo |

**Clientes de exemplo** (não há endpoints de cadastro de cliente):

| Id | Nome |
|----|------|
| `11111111-1111-1111-1111-111111111111` | Maria Silva |
| `22222222-2222-2222-2222-222222222222` | Joao Souza |
| `33333333-3333-3333-3333-333333333333` | Ana Lima |

### 3. Configuração da integração externa

A cotação vem do endpoint público da AwesomeAPI:

```
GET https://economia.awesomeapi.com.br/json/last/USD-BRL
```

A URL base e os tempos limite ficam no `application.yaml`:

```yaml
integrations:
  awesomeapi:
    url: https://economia.awesomeapi.com.br

spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connect-timeout: 3000   # ms
            read-timeout: 5000      # ms
```

Da resposta, a aplicação usa o campo **`ask`** (cotação de venda), convertido de texto para `BigDecimal`, e o campo `create_date` (data/hora da cotação). O campo `bid` (compra) não é utilizado. A cotação nunca é recebida do cliente da API.

Nenhuma chave de API é necessária para executar o projeto com o endpoint acima.

### 4. Executando

Defina a variável de ambiente com a senha do MySQL e rode o projeto.

**Windows (PowerShell):**

```powershell
$env:DB_PASSWORD="sua_senha"
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
DB_PASSWORD=sua_senha ./mvnw spring-boot:run
```

**IntelliJ IDEA:** em *Run → Edit Configurations*, na configuração `ComprasinternacionaisApplication`, adicione `DB_PASSWORD=sua_senha` em *Environment variables* e execute a classe.

A aplicação sobe em `http://localhost:8080`.

---

## Endpoints

| Método | Rota | Descrição | Sucesso |
|--------|------|-----------|---------|
| POST | `/compras/simular` | Consulta a cotação e calcula os valores, sem gravar | 200 |
| POST | `/compras` | Valida, consulta a cotação, verifica o limite e grava a compra | 201 |
| GET | `/clientes/{id}/compras` | Histórico de compras do cliente (da mais recente para a mais antiga) | 200 |
| GET | `/clientes/{id}/limite` | Total utilizado e saldo do limite mensal | 200 |

### POST /compras/simular

Requisição:

```json
{ "valorUSD": 200.00 }
```

Resposta (valores ilustrativos; a cotação real varia):

```json
{
  "valorUSD": 200.00,
  "cotacaoUtilizada": 5.4000,
  "valorConvertido": 1080.00,
  "taxaServico": 21.60,
  "tributoSimulado": 108.00,
  "valorFinalBRL": 1209.60,
  "dataCotacao": "2026-10-21"
}
```

### POST /compras

Requisição:

```json
{
  "clienteId": "11111111-1111-1111-1111-111111111111",
  "valorUSD": 200.00
}
```

Resposta (201):

```json
{
  "id": "6c7d85e5-60ac-40f1-a5e9-0444bba1e1d3",
  "clienteId": "11111111-1111-1111-1111-111111111111",
  "valorUSD": 200.00,
  "cotacaoUtilizada": 5.4000,
  "valorConvertido": 1080.00,
  "taxaServico": 21.60,
  "tributoSimulado": 108.00,
  "valorFinalBRL": 1209.60,
  "dataCotacao": "2026-10-21",
  "dataCompra": "2026-10-21T10:15:30.123456"
}
```

### GET /clientes/{id}/limite

```json
{
  "clienteId": "11111111-1111-1111-1111-111111111111",
  "mesReferencia": "2026-10",
  "limiteMensal": 10000.00,
  "totalUtilizado": 1209.60,
  "saldoDisponivel": 8790.40
}
```

---

## Regras de negócio

| Regra | Descrição |
|-------|-----------|
| RN01 | A cotação é sempre o campo `ask` da AwesomeAPI. Cotação enviada pelo cliente não é aceita |
| RN02 | `valorConvertido = valorUSD × cotacaoUtilizada` |
| RN03 | Taxa de serviço: 5% do valor convertido se `valorUSD < 100`; 2% se `valorUSD ≥ 100` |
| RN04 | Tributo didático (fictício): 10% do valor convertido |
| RN05 | `valorFinalBRL = valorConvertido + taxaServico + tributoSimulado` |
| RN06 | A soma dos valores finais do cliente no mesmo mês-calendário, incluindo a nova compra, não pode exceder R$ 10.000,00 |
| RN07 | A compra guarda a cotação e os valores calculados no momento do registro; consultas posteriores não recalculam nada |
| RN08 | Em caso de erro de validação, cotação indisponível ou limite excedido, nada é gravado |

**Exemplo de conferência:** compra de US$ 200,00 com cotação de venda de R$ 5,40 → convertido R$ 1.080,00 + taxa de 2% (R$ 21,60) + tributo de 10% (R$ 108,00) = **R$ 1.209,60**.

**Decisões de implementação**

- Todos os cálculos usam `BigDecimal`, com `RoundingMode.HALF_UP`: valores em reais com 2 casas decimais e cotação com 4.
- A cotação é arredondada para 4 casas antes do cálculo, e é esse mesmo valor que é gravado na compra.
- O `valorUSD` aceita no máximo 2 casas decimais.
- Uma compra que leva o total do mês exatamente a R$ 10.000,00 é permitida (a regra diz que o limite não pode ser *excedido*).
- A simulação não verifica o limite mensal, pois recebe apenas o valor em dólar.

---

## Tratamento de erros

Um `@RestControllerAdvice` (`GlobalExceptionHandler`) devolve todos os erros no mesmo formato, sem expor stack trace ou detalhes internos:

```json
{
  "timestamp": "2026-10-21T10:15:30.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados de entrada inválidos",
  "details": [
    { "field": "valorUSD", "message": "Valor em dólar deve ser maior que zero" }
  ]
}
```

| Status | Situação |
|--------|----------|
| 400 | Entrada inválida: campo ausente ou inválido (`@Valid`), JSON mal formatado, id que não é UUID |
| 404 | Cliente inexistente |
| 422 | Limite mensal excedido; resposta da cotação inválida (campos ausentes, `ask` inválido ou menor ou igual a zero) |
| 503 | Serviço de cotação indisponível |
| 504 | Tempo esgotado ao consultar a cotação |
| 500 | Erro inesperado (mensagem genérica; o detalhe fica apenas no log do servidor) |

---

## Exemplos de uso (curl)

```bash
# Simular
curl -X POST http://localhost:8080/compras/simular \
  -H "Content-Type: application/json" \
  -d '{"valorUSD": 200}'

# Registrar compra
curl -X POST http://localhost:8080/compras \
  -H "Content-Type: application/json" \
  -d '{"clienteId": "11111111-1111-1111-1111-111111111111", "valorUSD": 200}'

# Histórico
curl http://localhost:8080/clientes/11111111-1111-1111-1111-111111111111/compras

# Limite mensal
curl http://localhost:8080/clientes/11111111-1111-1111-1111-111111111111/limite
```

No PowerShell do Windows, `curl` é um alias de outro comando; use `curl.exe` ou `Invoke-RestMethod`.

---

## Cenários para conferência

| Cenário | Resultado esperado |
|---------|--------------------|
| Simular com US$ 200 | 200, com taxa de 2% e tributo de 10% |
| Simular com US$ 50 | 200, com taxa de 5% e tributo de 10% |
| Registrar compra válida | 201 e compra gravada |
| Registrar com cliente inexistente | 404 |
| Registrar com `valorUSD` negativo ou sem `clienteId` | 400, com `details` |
| Registrar compra que passa de R$ 10.000,00 no mês | 422, e a compra não aparece no histórico |
| Consultar com id que não é UUID | 400 |
| Simular sem acesso à internet | 503 |
