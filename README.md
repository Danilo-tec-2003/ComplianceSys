# ComplianceSys - Sistema de Controle de Jornada (FMS)

![Java](https://img.shields.io/badge/Java-8%2B-orange?style=flat-square&logo=java)
![Tomcat](https://img.shields.io/badge/Tomcat-8%2B-yellow?style=flat-square&logo=apache-tomcat)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12%2B-blue?style=flat-square&logo=postgresql)
![JUnit](https://img.shields.io/badge/JUnit-5-green?style=flat-square&logo=junit5)
![Jacoco](https://img.shields.io/badge/Jacoco-100%25-brightgreen?style=flat-square)

---

## 📋 Sobre o Projeto

Sistema de gerenciamento e monitoramento de jornada de trabalho para motoristas profissionais, desenvolvido com foco em **conformidade legal** com a Lei do Caminhoneiro (Lei nº 13.103/2015).

**Objetivo:** Garantir segurança viária através do controle rigoroso de tempo de direção, pausas obrigatórias e limites de jornada.

---

## ✨ Funcionalidades

* **Registro de Ponto:** Marcação de início/fim de jornada e direção com validação em tempo real.
* **Validação Automática:** Sistema analisa todas as regras da Lei do Caminhoneiro e emite alertas críticos.
* **Cálculos Precisos:** Utiliza `java.time` para garantir precisão em durações e intervalos.
* **API REST:** Endpoints para registro e consulta de pontos (JSON com Gson).
* **Persistência:** Armazenamento seguro em PostgreSQL com índices otimizados.
* **Consultas Flexíveis:** Busca por motorista ou por data específica.
* **Alertas Inteligentes:**
  * ✅ **OK:** Jornada em conformidade
  * ⚠️ **ALERTA:** Horas extras dentro do limite legal (8h-10h)
  * 🚨 **ALERTA CRÍTICO:** Violação de regras (direção contínua, pausas, limite de 10h)

---

## 🎯 Regras de Negócio Implementadas

O sistema valida **todas** as exigências da Lei do Caminhoneiro:

| Regra | Limite Legal | Validação |
|-------|--------------|-----------|
| **Jornada Diária Máxima** | 8h (10h com extras) | 🚨 Bloqueia acima de 10h |
| **Direção Contínua** | 5h30min sem pausa | 🚨 Exige pausa após limite |
| **Pausa Obrigatória** | Mínimo 30min | 🚨 Valida intervalo entre direções |
| **Intervalo Intrajornada** | Mínimo 1h (almoço) | 🚨 Garante descanso no meio da jornada |
| **Descanso Diário (Interjornada)** | Mínimo 11h | 🚨 Impede jornadas sem descanso adequado |

**Tipos de Ponto Suportados:**
* `INICIO_JORNADA` - Início do dia de trabalho
* `FIM_JORNADA` - Fim do dia de trabalho
* `INICIO_DIRECAO` - Início de período dirigindo
* `FIM_DIRECAO` - Fim de período dirigindo

---

## 🏗️ Stack Técnica

| Tecnologia | Versão | Finalidade |
|-----------|--------|------------|
| **Java** | 8+ | Linguagem base com API `java.time` |
| **Apache Tomcat** | 8+ | Servidor de aplicação (Servlet Container) |
| **PostgreSQL** | 12+ | Banco de dados relacional |
| **Gson** | 2.10.1 | Serialização/Desserialização JSON |
| **JUnit 5** | 5.9.3 | Framework de testes unitários |
| **Jacoco** | 0.8.8 | Cobertura de código (100% RN críticas) |
| **Gradle** | 7.x | Build automation e gerenciamento de dependências |

---

## 🚀 Guia de Instalação e Setup

### 1. Pré-requisitos

* ☕ **JDK 8+** instalado
* 🐘 **PostgreSQL 12+** rodando
* 🐱 **Apache Tomcat 8+** configurado
* 📦 **Gradle** (wrapper incluído)

### 2. Clone o Repositório

```bash
git clone https://github.com/danilogw-dev-2025/Danilo_ComplianceSys.git
cd Danilo_ComplianceSys
```

### 3. Configuração do Banco de Dados

**3.1. Criar o Banco**

```sql
CREATE DATABASE compliancesys;
```

**3.2. Executar o Schema SQL**

Localize o script em `src/main/resources/database.sql` e execute:

```sql
CREATE TABLE ponto (
    id BIGSERIAL PRIMARY KEY,
    motorista_id BIGINT NOT NULL,
    registro TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    data_criacao TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    mensagem_conformidade VARCHAR(1000)
);

CREATE INDEX idx_ponto_motorista_registro
ON ponto (motorista_id, registro);
```

**3.3. Configurar Credenciais**

Edite o arquivo `src/main/java/DatabaseConfig/ConnectionFactory.java`:

```java
private static final String URL = "jdbc:postgresql://localhost:5432/compliancesys";
private static final String USER = "seu_usuario";
private static final String PASSWORD = "sua_senha";
```

### 4. Build do Projeto

```bash
./gradlew clean build
```

✅ O arquivo `.war` será gerado em: `build/libs/ComplianceSys.war`

### 5. Deploy no Apache Tomcat

**Opção A: Interface Web (Tomcat Manager)**

1. Acesse `http://localhost:8080/manager`
2. Seção **"Arquivo WAR a ser implantado"**
3. Selecione `ComplianceSys.war`
4. Clique em **Implantar**

**Opção B: Cópia Direta**

```bash
cp build/libs/ComplianceSys.war $CATALINA_HOME/webapps/
```

**Opção C: Via Gradle**

```bash
./gradlew tomcatDeploy
```

---

## 🌍 Acesso à Aplicação

Após iniciar o Tomcat, a API estará disponível em:

```
http://localhost:8080/compliancesys/api/jornada
```

---

## 📡 API REST - Endpoints

### 🔹 1. Registrar Ponto (POST)

**Endpoint:** `POST /api/jornada`

**Body (JSON):**
```json
{
  "motoristaId": 209,
  "registro": "2025-12-10T08:00:00",
  "tipo": "INICIO_JORNADA"
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/compliancesys/api/jornada \
  -H "Content-Type: application/json" \
  -d '{
    "motoristaId": 209,
    "registro": "2025-12-10T08:00:00",
    "tipo": "INICIO_JORNADA"
  }'
```

**Resposta (200 OK):**
```json
{
  "id": 1,
  "motoristaId": 209,
  "registro": "2025-12-10T08:00:00",
  "tipo": "INICIO_JORNADA",
  "mensagemConformidade": "OK: Jornada em conformidade. Total: 0h 0min.",
  "dataCriacao": "2025-12-11T15:31:06.736"
}
```

**Resposta com Alerta Crítico:**
```json
{
  "mensagemConformidade": "ALERTA CRÍTICO: Limite legal de Horas Extras excedido! Ultrapassou 10h em 0h e 1min."
}
```

---

### 🔹 2. Consultar Pontos por Motorista (GET)

**Endpoint:** `GET /api/jornada?motoristaId={id}`

**Exemplo:**
```bash
curl http://localhost:8080/compliancesys/api/jornada?motoristaId=209
```

**Resposta:**
```json
[
  {
    "id": 1,
    "motoristaId": 209,
    "registro": "2025-12-10T08:00:00",
    "tipo": "INICIO_JORNADA",
    "mensagemConformidade": "ALERTA CRÍTICO: ..."
  },
  {
    "id": 2,
    "motoristaId": 209,
    "registro": "2025-12-10T12:00:00",
    "tipo": "FIM_DIRECAO",
    "mensagemConformidade": "ALERTA CRÍTICO: ..."
  }
]
```

---

### 🔹 3. Consultar Pontos por Data (GET)

**Endpoint:** `GET /api/jornada?data={YYYY-MM-DD}`

**Exemplo:**
```bash
curl http://localhost:8080/compliancesys/api/jornada?data=2025-12-10
```

**Resposta:** Retorna todos os pontos de todos os motoristas naquela data.

---

## 📸 Capturas de Tela

### 🔸 Teste POST no Postman

![Teste POST - Registro de Jornada](docs/images/postID100.png)

*Registro de pontos para motorista ID 100 com jornada em conformidade*

---

### 🔸 Teste GET - DATA no Postman

![Teste POST - Registro de Jornada](docs/images/buscandoPorDia12.png)

*Método de busca usando Data como filtro*

---

### 🔸 Teste GET - ID do motorista no Postman

![Teste POST - Registro de Jornada](docs/images/getID100.png)

*Método de busca usando ID do motorista como filtro*


### 🔸 Dados Persistidos no PostgreSQL

![Query PostgreSQL](docs/images/buscaGeralBanco.png)

*Consulta `SELECT * FROM ponto;` mostrando registros com mensagens de conformidade*

---

## 🧪 Testes Unitários

### Executar Testes

```bash
./gradlew test
```

### Gerar Relatório de Cobertura (Jacoco)

```bash
./gradlew jacocoTestReport
```

📊 **Relatório HTML:** `build/reports/jacoco/test/html/index.html`

### Métricas de Cobertura

* ✅ **JornadaService:** 100% (métodos críticos)
* ✅ **Casos de Teste:** 12+ cenários
* ✅ **Linhas Cobertas:** 100% das regras de negócio

### Cenários Testados

**✅ Conformidade (OK):**
* Jornada padrão 8h com intervalo de 1h
* Horas extras legais (9h30min)
* Direção contínua no limite (5h30min)
* Pausa maior que mínima (1h30min)
* Descanso diário exato (11h)

**❌ Infrações (ALERTA CRÍTICO):**
* Direção contínua excedida (5h31min)
* Pausa insuficiente (29min)
* Intervalo intrajornada curto (59min)
* Horas extras ilegais (10h01min)
* Descanso diário insuficiente (<11h)

---

## 🔄 Git Flow

### Estrutura de Branches

```
main (produção)
  └── develop (integração)
       ├── feature/modulo_1_rns
       ├── feature/modulo_2_servlet_gson
       └── feature/modulo_3_dao_tests
```

### Pull Requests

✅ **PR #1:** Módulo 1 - API java.time e regras de jornada  
✅ **PR #2:** Módulo 2 - Serialização Gson e API REST  
✅ **PR #3:** Módulo 3 - Testes JUnit + Jacoco 100%

---

## 📁 Estrutura do Projeto

```
ComplianceSys/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── model/
│   │   │   │   └── Ponto.java
│   │   │   ├── service/
│   │   │   │   └── JornadaService.java
│   │   │   ├── servlet/
│   │   │   │   └── JornadaServlet.java
│   │   │   ├── DAO/
│   │   │   │   └── PontoDAO.java
│   │   │   └── DatabaseConfig/
|   |   |       └── ConnectionFactory.java 
|   |   |       └── DataBaseConfig.java
│   │   │       └── MainDB.java
│   │   └── resources/
│   │       └── squema.sql
│   └── test/
│       └── java/
│           └── service/
│               └── JornadaServiceTest.java
├── docs/
│   └── images/
│       ├── buscaGeralBanco.png
│       ├── buscandoPorDia12.png
│       └── postID100.png
|       └── psqlID100.png
|       └── getID100.png
|
├── build.gradle
└── README.md
```

---

## 📊 Diferenciais do Projeto

* 🎯 **Além do Requisito Base:** Implementei **todas** as regras da Lei do Caminhoneiro, não apenas as 8h básicas
* 🧪 **Qualidade Garantida:** 100% de cobertura de testes nas regras críticas
* 📡 **API Completa:** GET por motorista/data + POST com validação em tempo real
* 🔒 **Segurança:** Validações impedem registros que violem a legislação
* 📈 **Escalabilidade:** Índices otimizados e arquitetura em camadas

---

## 📚 Referências

* [Lei nº 13.103/2015 (Lei do Caminhoneiro)](http://www.planalto.gov.br/ccivil_03/_ato2015-2018/2015/lei/l13103.htm)
* [Java Time API Documentation](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html)
* [Gson User Guide](https://github.com/google/gson/blob/master/UserGuide.md)
* [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

---

## 👨‍💻 Autor

**Danilo GW**  
🔗 GitHub: [@danilogw-dev-2025](https://github.com/danilogw-dev-2025)

---

## 📄 Licença

Projeto desenvolvido como parte da **Trilha de Aceleração GW Sistemas - Nível Trainee**.

---

<div align="center">

**⭐ Se este projeto foi útil, deixe uma estrela no repositório!**

[![GitHub](https://img.shields.io/badge/GitHub-Danilo__ComplianceSys-181717?style=for-the-badge&logo=github)](https://github.com/danilogw-dev-2025/Danilo_ComplianceSys)

</div>
