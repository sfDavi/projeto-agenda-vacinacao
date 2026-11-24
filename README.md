# Agendamento de Vacinas

Projeto final da disciplina **Software para Persistência de Dados (INF0301)**.
Autor: **Davi de Souza Fernandes** (trabalho individual).

Sistema web para cadastrar vacinas, alergias e usuários, agendar vacinas com geração automática de todas as doses, dar baixa nas aplicações e acompanhar o cartão de vacinação de cada usuário.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 25 (`maven.compiler.release=25`) |
| Persistência | **Hibernate 6.6** (JPA 3.1 / Jakarta Persistence), `hbm2ddl.auto=validate` |
| Banco de dados | MySQL 8.0 ou superior (testado com 8.0.46) |
| Web | Servlets + JSP + **JSTL 3.0** (Jakarta EE 10), sem framework adicional |
| Servidor | Apache Tomcat 10.1 |
| Build | Maven 3.9 (`packaging=war`), testes com JUnit 5 |
| Interface | HTML + **Bootstrap 5.3** e **Bootstrap Icons** (via CDN) + JavaScript puro |

Tecnologias extras em relação ao mínimo pedido: **Bootstrap** (layout responsivo, modais e badges) e **JSTL** (laços e condicionais nas JSPs). As duas dependem de acesso à internet apenas para o CSS/JS do Bootstrap; o JSTL vai dentro do `.war`.

---

## Estrutura do repositório

```
README.md                  este arquivo
vacinacao/                 projeto Maven
├── pom.xml
├── deploy.bat             build + cópia do .war para o Tomcat
├── sql/
│   ├── schema.sql         criação do banco e das tabelas (MER + adequações)
│   ├── complementos.sql   contraindicações por alergia (tabela vacina_alergia) + índice de séries de doses
│   └── dados.sql          carga de exemplo
└── src/
    ├── main/java/br/ufg/vacinacao/
    │   ├── model/         entidades JPA (Vacina, Alergia, Usuario, Agenda) e enums
    │   ├── dao/           acesso a dados (GenericDAO + um DAO por entidade)
    │   ├── service/       regras de negócio (agendamento, doses, contraindicações)
    │   ├── servlet/       controladores (um servlet por área do sistema)
    │   ├── util/          JPAUtil, datas, mensagens, parâmetros
    │   └── web/           filtro de UTF-8 e listener do JPA
    ├── main/resources/META-INF/persistence.xml
    ├── main/webapp/       JSPs (em WEB-INF/jsp), CSS e JS
    └── test/java/         testes JUnit (regra de datas e controle de doses)
```

Arquitetura: **Servlet → Service → DAO (JPA/Hibernate) → MySQL**, com as JSPs em `WEB-INF/jsp` (não acessíveis diretamente). Após cada envio de formulário a aplicação redireciona (Post/Redirect/Get) e exibe a mensagem de resultado.

---

## Como executar

### 1. Pré-requisitos

- JDK 25
- Maven 3.9+ (`mvn -v`)
- MySQL 8.0+ em execução
- Apache Tomcat 10.1 (descompactado em qualquer pasta)

### 2. Criar o banco

Na pasta `vacinacao`, execute os três scripts **nesta ordem** (o primeiro cria o banco `vacinacao`):

Prompt de comando (cmd), Git Bash, Linux ou macOS:

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p vacinacao < sql/complementos.sql
mysql -u root -p vacinacao < sql/dados.sql
```

PowerShell (não aceita o redirecionamento `<`):

```powershell
mysql -u root -p --default-character-set=utf8mb4 -e "source sql/schema.sql"
mysql -u root -p --default-character-set=utf8mb4 -D vacinacao -e "source sql/complementos.sql"
mysql -u root -p --default-character-set=utf8mb4 -D vacinacao -e "source sql/dados.sql"
```

> Se o `mysql` não estiver no PATH, use o caminho completo (ex.: `& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"`). Se o MySQL estiver em outra máquina ou no WSL, acrescente `-h 127.0.0.1`.
>
> `schema.sql` apaga e recria as tabelas: use em um banco `vacinacao` sem dados importantes.
> `dados.sql` é opcional, mas recomendado para ver o sistema funcionando (as datas são relativas ao dia de hoje).

### 3. Configurar usuário e senha do MySQL

Por padrão a aplicação usa `root` com **senha vazia** em `localhost:3306`. Se for diferente, defina as variáveis de ambiente **no mesmo terminal em que o Tomcat será iniciado**:

| Variável | Padrão |
|---|---|
| `VACINACAO_DB_USER` | `root` |
| `VACINACAO_DB_PASSWORD` | *(vazia)* |
| `VACINACAO_DB_URL` | `jdbc:mysql://localhost:3306/vacinacao?useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Sao_Paulo` |

Windows (cmd):
```bat
set VACINACAO_DB_PASSWORD=minhaSenha
```
PowerShell:
```powershell
$env:VACINACAO_DB_PASSWORD = "minhaSenha"
```
Linux/macOS:
```bash
export VACINACAO_DB_PASSWORD=minhaSenha
```

### 4. Compilar e publicar

Com o script (Windows), informando a pasta do Tomcat (a que contém `bin`, `conf` e `webapps`). No mesmo terminal em que definiu a senha do passo 3:

Prompt de comando (cmd):
```bat
cd vacinacao
set CATALINA_HOME=C:\caminho\apache-tomcat-10.1.x
deploy.bat start
```

PowerShell:
```powershell
cd vacinacao
$env:CATALINA_HOME = "C:\caminho\apache-tomcat-10.1.x"
.\deploy.bat start
```

O `deploy.bat` executa `mvn clean package` (incluindo os testes), copia `target\vacinacao.war` para `%CATALINA_HOME%\webapps` e, com `start`, inicia o Tomcat. Se o Maven não estiver no PATH, defina antes a variável `MVN` com o caminho do `mvn.cmd` (ex.: `$env:MVN = "C:\maven\bin\mvn.cmd"`).

Para parar o Tomcat: `%CATALINA_HOME%\bin\shutdown.bat` (no PowerShell, `& "$env:CATALINA_HOME\bin\shutdown.bat"`).

Manualmente (qualquer sistema):

```bash
cd vacinacao
mvn clean package
cp target/vacinacao.war <pasta-do-tomcat>/webapps/
<pasta-do-tomcat>/bin/startup.sh      # ou startup.bat no Windows
```

### 5. Acessar

**http://localhost:8080/vacinacao/**

Na inicialização o Hibernate confere se as tabelas batem com o mapeamento (`validate`). Se o MySQL estiver fora do ar ou os scripts não tiverem sido executados, a página inicial mostra um aviso explicando o problema; a aplicação volta a funcionar sozinha quando o banco estiver disponível.

> Se a porta 8080 estiver ocupada por outro programa, altere o `port="8080"` do `<Connector>` em `<pasta-do-tomcat>/conf/server.xml` (por exemplo para `8081`) e acesse a nova porta.

---

## Funcionalidades (requisitos do enunciado)

| Item | Requisito | Onde |
|---|---|---|
| 1 | Incluir, excluir e consultar vacinas, alergias, usuários e agendas | Menus **Vacinas**, **Alergias**, **Usuários** e **Agendas** (botões Novo, Detalhes e Excluir). Também há **edição** de vacinas, alergias e usuários |
| 2 | Listagens completas das quatro tabelas | `/vacinas`, `/alergias`, `/usuarios`, `/agendas` |
| 3 | Listar agendas Canceladas e Realizadas | `/agendas` com filtro de situação (atalhos "Realizados" e "Cancelados"), além de filtros por período, usuário e vacina |
| 4 | Agendas do dia ordenadas Agendado → Realizado → Cancelado | Menu **Hoje** (`/agendas/hoje`) e card "Agendas de hoje" na página inicial |
| 5 | Dar baixa (Realizada/Cancelada) com `data_situacao` = hoje | Botões **Realizada** e **Cancelar** (com confirmação e motivo opcional) na listagem, em Hoje, no detalhe e no cartão |
| 6 | Agendamentos do usuário (data, situação, vacina, dose) | **Cartão de vacinação** (`/usuarios/agendas?id=`), agrupado por vacina |
| Extra | Nova funcionalidade | **Alerta de contraindicação por alergia** (vacinas com alergias contraindicadas; alerta e confirmação ao agendar) e **controle de doses e próximas aplicações** (progresso, atrasos, recálculo e reagendamento de doses, página "Próximas doses"), descritos no PDF entregue à parte |
| Regra | Gerar as N agendas conforme doses, periodicidade e intervalo | Ao agendar, todas as doses são criadas numa única transação, com prévia das datas antes de salvar |

**Regra de geração das doses:** a dose *i* cai em `data inicial + i × intervalo` unidades da periodicidade (dias, semanas, meses ou anos), sempre calculada a partir da data inicial. Assim o "mesmo dia/mês" é mantido quando possível (31/01 mensal → 28/02 → 31/03; 29/02 anual → 28/02 nos anos não bissextos). Exemplo do enunciado: 5 doses, anual, intervalo 3 → hoje, +3, +6, +9 e +12 anos.

---

## Adequações ao modelo (MER)

O banco segue o MER fornecido, com as adequações abaixo (todas em `sql/schema.sql` e `sql/complementos.sql`):

| Adequação | Justificativa |
|---|---|
| Colunas `usuario_id` e `vacina_id` em `agendas`, com chaves estrangeiras | Materializam os relacionamentos 1:N do MER (Usuário–Agendas e Vacina–Agendas) |
| Tabela associativa `usuario_alergia` | Relacionamento N:N entre Usuários e Alergias do MER |
| Coluna `dose` (INT) em `agendas` | O Item 6 exige mostrar a dose de cada agenda. Sem a coluna, o número da dose teria de ser deduzido pela ordem das datas, o que falha quando uma dose é cancelada ou reagendada |
| `situacao` guarda `AGENDADO`, `REALIZADO` ou `CANCELADO` | Valor do `enum` Java gravado por nome (`@Enumerated(EnumType.STRING)`); cabe no `char(10)` do MER e é legível direto no banco |
| `hora` como `CHAR(5)` ("HH:mm") | O MER permite `time` ou `char(5)`; `char(5)` evita conversões de fuso horário no driver JDBC |
| `sexo` como `CHAR(1)` com `M`/`F` | Segue o MER; na tela aparece "Masculino"/"Feminino" |
| `periodicidade` como `INT` de 1 a 4 (1 dias, 2 semanas, 3 meses, 4 anos) | Segue o MER; no Java é um `enum` convertido por `AttributeConverter` |
| `periodicidade` e `intervalo` nulos em vacinas de dose única | Para uma dose só não existe intervalo entre doses |
| `CHECK` de coerência entre `situacao` e `data_situacao` | Garante no próprio banco a regra do enunciado: `data_situacao` só é preenchida quando a agenda é realizada ou cancelada |
| `CHECK`s de domínio (`doses >= 1`, `intervalo >= 1`, `sexo`, `situacao`) e `nome` único em `alergias` | Integridade dos dados também fora da aplicação |
| Tabela `vacina_alergia` e índice `idx_agendas_serie` (em `complementos.sql`) | Nova funcionalidade: contraindicações por alergia e consultas por série de doses |
| Banco em `utf8mb4` | Acentuação correta em nomes e observações |

**Exclusões e integridade:**

- Excluir um **usuário** remove também as suas agendas e os seus vínculos de alergia; a confirmação informa quantas agendas serão removidas.
- Uma **vacina** só pode ser excluída se não estiver em nenhuma agenda.
- Uma **alergia** só pode ser excluída se não estiver registrada em usuários nem como contraindicação de vacinas.
- **Agendas** podem ser excluídas individualmente ou junto com as doses seguintes ainda agendadas da mesma série.
- Ao editar uma vacina que já tem agendas, doses, periodicidade e intervalo ficam travados, pois as séries existentes foram geradas com esse esquema.

---

## Testes automatizados

```bash
cd vacinacao
mvn test
```

24 testes JUnit cobrem a regra de geração das datas (exemplo do enunciado, 31/01 mensal, 29/02 anual, dias e semanas), o formato da hora e o controle de doses (progresso, atraso, data mínima, reagendamento e separação de séries).
