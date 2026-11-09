# Nexturing — Energia Consciente

Projeto acadêmico de extensão universitária relacionado à ODS 7 — Energia Limpa e Acessível.

## Identificação acadêmica

- **Projeto:** Nexturing — Energia Consciente
- **Componente curricular:** Itinerário Extensionista 2
- **Semestre:** 4º semestre · 2026/2
- **Professor:** Valter De Sales Santana

### Integrantes

| Integrante | Curso e turma |
|---|---|
| Tiago Alves de Souza | UCS · Ciência da Computação (Bacharelado) · 4N1 · 2026/2 |
| Ronald Nathan Nascimento Alves | UCS · Ciência da Computação (Bacharelado) · 4N1 · 2026/2 |
| Wendel de Sousa Nobrega | UCS · Ciência da Computação (Bacharelado) · 4N1 · 2026/2 |
| Matheus dos Santos Godoy | UCS · Ciência da Computação (Bacharelado) · 3N1 · 2026/2 |

API REST para cadastro e estimativa de consumo de aparelhos, configuração de tarifa e simulação de redução de uso. Os valores calculados são estimativas e não representam necessariamente o valor final da conta de energia.

## Requisitos

- JDK 21 (versão configurada no projeto).
- MySQL 8 para executar com persistência.
- O perfil `demo` usa H2 em memória para desenvolvimento e testes; os dados são descartados ao encerrar a aplicação.

## Executar com H2 (demonstração)

Na pasta `ODS7/`:

```powershell
.\mvnw.cmd spring-boot:run
```

O perfil `demo` é o padrão explícito. A aplicação usa `jdbc:h2:mem:energiaconsciente`; o console H2 fica em `http://localhost:8080/h2-console`, com JDBC URL `jdbc:h2:mem:energiaconsciente`, usuário `sa` e senha vazia.

## Executar com MySQL

Crie o banco `energia_consciente` no MySQL e defina as variáveis de ambiente antes de iniciar. Exemplo no PowerShell:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/energia_consciente"
$env:DB_USERNAME = "seu_usuario"
$env:DB_PASSWORD = "sua_senha"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

O perfil `mysql` exige as credenciais por ambiente, executa o schema inicial em `src/main/resources/db/mysql-schema.sql` e valida as entidades com Hibernate. Não coloque credenciais reais no repositório.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes usam o perfil `demo` e verificam os cálculos de consumo/custo, validações e fluxos HTTP dos endpoints.

## Interface web

Com a aplicação em execução, acesse `http://localhost:8080/`. A interface responsiva usa HTML, CSS, JavaScript modular, Bootstrap 5 e Chart.js; as bibliotecas visuais são carregadas por CDN e exigem conexão à internet. As telas usam a API da mesma aplicação para carregar dados, validar operações no servidor e atualizar os gráficos. Dicas e informações sobre a ODS 7 são conteúdo educativo, não indicadores de consumo.

## Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/aparelhos` | Lista aparelhos com consumo e custo estimado |
| `POST` | `/api/aparelhos` | Cadastra aparelho |
| `GET` | `/api/aparelhos/{id}` | Consulta aparelho |
| `PUT` | `/api/aparelhos/{id}` | Atualiza aparelho |
| `DELETE` | `/api/aparelhos/{id}` | Exclui aparelho |
| `GET` | `/api/configuracoes` | Consulta a tarifa; `null` indica tarifa ainda não configurada |
| `PUT` | `/api/configuracoes` | Define tarifa por kWh |
| `GET` | `/api/dashboard` | Retorna totais, maior consumidor e dados para gráficos |
| `POST` | `/api/consumo` | Calcula consumo para valores informados sem cadastrar aparelho |
| `POST` | `/api/simulacoes` | Simula redução de horas sem alterar o aparelho |
| `POST` | `/api/simulacoes/solar` | Compara conta atual e estimativa fotovoltaica a partir das premissas informadas |
| `POST` | `/api/quiz/tentativas` | Inicia uma rodada com 10 perguntas e alternativas, sem expor o gabarito |
| `POST` | `/api/quiz/tentativas/{tentativaId}/perguntas/{perguntaId}/respostas` | Registra uma resposta e retorna feedback educativo |
| `GET` | `/api/quiz/historico` | Lista as tentativas do perfil identificado |

O corpo de cadastro/atualização de aparelho aceita `nome`, `potenciaWatts`, `horasPorDia` e `diasPorMes`. Para configurar tarifa, envie `{"tarifaKwh":0.90}`. Para simular, envie `{"aparelhoId":1,"horasPorDiaSimuladas":3}`.

A calculadora aceita `{"potenciaWatts":100,"horasPorDia":5,"diasPorMes":30}` em `POST /api/consumo`. A interface web é servida pelo Spring Boot em `http://localhost:8080/`.

Os dados são associados a um único perfil de demonstração, sem autenticação ou dados pessoais neste MVP. A tarifa não recebe um valor padrão: enquanto não for configurada, os custos estimados são retornados como `null`.

### Quiz Energético

O Quiz Energético apresenta 30 perguntas iniciais divididas em cinco categorias. Cada rodada sorteia 10 perguntas; cada acerto vale 10 pontos. As perguntas, alternativas, respostas corretas e explicações são armazenadas no banco. O gabarito não é enviado ao iniciar a rodada: a API informa a alternativa correta e a explicação somente após registrar uma resposta. Tentativas e respostas ficam associadas ao perfil de demonstração; sem autenticação, o histórico não representa usuários individuais.

Para consulta e verificação dos temas, veja as fontes públicas da [ONU sobre a ODS 7](https://sdgs.un.org/goals/goal7), do [Inmetro sobre o Programa Brasileiro de Etiquetagem](https://www.gov.br/inmetro/pt-br/assuntos/avaliacao-da-conformidade/programa-brasileiro-de-etiquetagem) e da [EPE sobre energia](https://www.epe.gov.br/pt/abcdenergia).

### Simulador Solar

A página **Simulador Solar** usa `POST /api/simulacoes/solar`. Informe valor da conta, cidade/UF, tarifa, custo mensal não compensável e percentual da tarifa que a fonte do orçamento considera compensável. Consumo em kWh, geração estimada e investimento são opcionais. Sem consumo, o serviço aproxima `(valor da conta - custo não compensável) / tarifa` e marca o resultado como simplificado. Sem geração informada, não calcula compensação, economia, conta solar ou retorno. O retorno simples é apresentado em meses somente quando há investimento positivo e economia mensal positiva.

Não há valores de irradiação, tarifa, geração ou investimento presumidos. O percentual compensável e o custo não compensável são premissas explicitamente informadas pelo usuário; o simulador não determina enquadramento regulatório nem aplica automaticamente a regra de transição de 2026 do art. 27 da Lei nº 14.300/2022. Confirme a modalidade, os itens compensáveis e as regras vigentes da ANEEL/distribuidora com o responsável técnico. A estimativa não modela todos os componentes da fatura nem substitui proposta técnica ou análise de viabilidade.
