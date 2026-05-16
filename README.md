<div align="center">
  <h1>⚽ Sports Media Bias Analyzer</h1>
  <p>
    <strong>Desarrollo de Aplicaciones para Ciencia de Datos</strong><br>
    <em>Universidad de Las Palmas de Gran Canaria (ULPGC)</em>
  </p>
  <p>
    <b>Aythami Lorenzo Padilla</b> &nbsp;&bull;&nbsp; <b>Alejandro Delgado Valera</b>
  </p>
</div>

---

## 1. Descripción y Propuesta de Valor
**Sports Media Bias Analyzer** es una aplicación diseñada para visualizar y analizar empíricamente el sesgo deportivo en la prensa española. 

El objetivo principal de este proyecto es responder a preguntas clave mediante datos objetivos: ¿Qué periódicos castigan más a ciertos equipos? ¿Qué clubes tienen mayor repercusión mediática? ¿Cómo afecta la racha de victorias o derrotas de un equipo al sentimiento de las noticias que se publican sobre él? A través de la recolección de datos deportivos y noticias, la aplicación cruza el rendimiento en el campo con el trato periodístico para revelar posibles tendencias o favoritismos.

## 2. Arquitectura del Sistema
El proyecto sigue una arquitectura orientada a eventos basada en el modelo **Kappa**. Se ha elegido esta arquitectura porque todo el procesamiento de datos se realiza a través de un único flujo continuo de eventos (stream processing), prescindiendo de una capa *batch* separada. El estado del sistema se construye procesando los eventos en tiempo real, y si es necesario regenerar la información, se re-procesan los eventos históricos almacenados.

![Arquitectura del Sistema](ruta-a-la-imagen-arquitectura.png)

## 3. Estructura del Proyecto
Para mantener el código desacoplado y organizado siguiendo las especificaciones de los Sprints, el repositorio se divide en los siguientes directorios principales:

```text
sports-media-bias-analyzer/
├── football-api-client/    # Feeder: Ingesta de datos de resultados deportivos
├── rss-news-scraper/       # Feeder: Ingesta y análisis de sentimiento de noticias
├── event-store-builder/    # Suscriptor: Almacenamiento histórico de eventos en crudo
├── business-unit/          # Lógica de negocio y servidor web (Dashboard API)
├── datamart/               # Datamart, archivo SQLite .db
├── eventstore/             # Almacenamiento de todos los .events crudos para ambos feeders
├── state/                  # Alamacena las fechas de las últimas noticias/partidos obtenidos para no volver a almacenarlos (no incluido en git)
├── documentation/          # Archivos de diagramas (UML) y recursos de documentación
├── application.properties  # Archivo centralizado de configuración y tokens (no incluido en git)
└── README.md
````

## 4. Módulos y Directorios del proyecto
A continuación, se detalla la lógica interna de cada módulo de la arquitectura y su correspondiente diseño de clases; así como también la función de cada directorio adicional.

### 4.1. Feeders (Publishers)
Son los módulos encargados de la recolección continua de datos desde fuentes externas para publicarlos en el broker de mensajería (ActiveMQ). Se dividen en dos proyectos independientes:

- `football-api-client`: Extrae datos sobre jornadas, partidos y resultados.

![Diagrama de clases](ruta-a-la-imagen-arquitectura.png)

- `rss-news-scraper`: Lee los canales RSS, procesa el texto e interactúa con el modelo de Hugging Face para determinar el análisis de sentimiento antes de publicar el evento.

![Diagrama de clases](ruta-a-la-imagen-arquitectura.png)

(...)

### 4.2. Event Store Builder (Subscriber histórico)
Este módulo se suscribe a todos los tópicos del broker y persiste los eventos en formato crudo (`JSON`) dentro de un almacenamiento local. Actúa como nuestra fuente de verdad absoluta (Event Sourcing) en caso de que sea necesario regenerar el estado de la aplicación.

![Diagrama de clases](ruta-a-la-imagen-arquitectura.png)

### 4.3. Business Unit (Datamart y API REST)
El corazón del análisis. Este módulo consume los eventos (en tiempo real o en diferido), los procesa y construye un Datamart optimizado en SQLite. Además, expone la información a través de una API REST que alimenta el Dashboard visual.

![Diagrama de clases](ruta-a-la-imagen-arquitectura.png)


## 5. Fuentes de Datos
- **Deportivas (API-Football)**: Elegida por su fiabilidad, accesibilidad gratuita y por proporcionar todos los datos necesarios sobre resultados y jornadas de LaLiga.

- **Noticias (RSS Scraping)**: Se han extraído los canales RSS de *Marca*, *AS* y *Mundo Deportivo* al ser los tres medios deportivos más influyentes de España. El formato XML de los RSS facilita una extracción limpia.

- **Análisis de Sentimiento (Hugging Face API)**: Se ha integrado el modelo multilingüe preentrenado `cardiffnlp/twitter-xlm-roberta-base-sentiment` a través de la API de Hugging Face.
  - Por defecto, este modelo procesa el texto y devuelve tres valores de probabilidad independientes (negativo, neutro y positivo) en un rango de 0 a 1. Para poder comparar empíricamente las noticias en el Datamart, normalizamos estas salidas obteniendo una única puntuación ponderada en el rango de [-1, 1], usando la fórmula: $Puntuación = (P_{pos} \times 1) + (P_{neu} \times 0) + (P_{neg} \times (-1))$.

## 6. Almacenamiento
### 6.1. Estructura del event-store
El módulo `event-store-builder` persiste los mensajes organizados jerárquicamente por tópico y fecha de ingesta (`YYYYMMDD`). Este almacenamiento actúa como nuestra fuente de datos reales en crudo, permitiendo a la `business-unit` regenerar el Datamart en diferido (re-play) sin necesidad de volver a consumir las APIs externas o realizar scraping.

#### Estructura de directorios:
```text
eventstore/
├── football-matches/
│   └──  football-api
│        └── YYYYMMDD.events
└── news/
    └──  rss-scraper
         └── YYYYMMDD.events
```

#### Estructura de los eventos (`.events`):
Cada fichero contiene eventos independientes por línea en formato JSON. Se han diseñado dos esquemas de eventos distintos dependiendo del Feeder, incluyendo siempre metadatos de trazabilidad como ss (Source System) y ts (Timestamp):

1. Evento de Noticias (Tópico: `news`):

```JSON
{
  "title": "(Titular de la noticia)",
  "summary": "(Resumen de la noticia)",
  "link": "(URL)",
  "pubDate": "(Fecha de publicación)",
  "source": "(Periódico del que ha sido obtenido)",
  "team": "(Equipo sobre el que trata)",
  "sentimentScore": "(Puntuación en rango (-1 = neg, 1 = pos) [Double])",
  "ss": "rss-scraper",
  "ts": "(timestamp [format: ISO 8601])"
}
```
2. Evento de Resultados de Partidos (Tópico: `football.matches`):

```JSON
{
  "date": "(Fecha en la que se ha disputado el partido)",
  "matchday": "(Jornada [Integer])",
  "homeTeam": "(Equipo local)",
  "awayTeam": "(Equipo visitante)",
  "homeGoals": "(Goles del equipo local [Integer])",
  "awayGoals": "(Goles del equipo visitante [Integer])",
  "homeRankAfterMatchday": "(Puesto en la clasificación del equipo local tras terminar el partido [Integer])",
  "awayRankAfterMatchday": "(Puesto en la clasificación del equipo visitante tras terminar el partido [Integer])",
  "ss": "football-api",
  "ts": "(timestamp [format: ISO 8601])"
}
```

### 6.2. Estructura del Datamart
El sistema centraliza la información en una base de datos **SQLite** implementada a través de la API estándar **JDBC** de Java. Se ha elegido esta tecnología por ser un motor de base de datos ligero, transaccional y *serverless* (no requiere la instalación de un servidor independiente). Esto facilita enormemente el despliegue del proyecto y su portabilidad, manteniendo un alto rendimiento en las consultas analíticas de la interfaz.

La base de datos está modelada para optimizar la respuesta visual del Dashboard y consta de tres tablas principales:

#### 6.2.1. Tabla `processed_news` (Protección anti-duplicados)
Garantiza la idempotencia del sistema y el desacoplamiento entre módulos. Almacena las URLs de las noticias ya procesadas en la base de datos para evitar que duplicados de ActiveMQ o recargas del historial corrompan las estadísticas. Aunque ya evitamos duplicados al guardar en los .events, delegar esta validación final a la base de datos (con claves *UNIQUE*) permite una comprobación instantánea y evita que este módulo tenga que acoplarse a leer y parsear pesados archivos de texto secuenciales para saber si una noticia ya fue procesada.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| **url** | TEXT (PK) | Enlace único de la noticia. Actúa como Clave Primaria. |
| **team** | TEXT | Equipo al que hace referencia la noticia. |

#### 6.2.2. Tabla `daily_sentiment` (Agregación de Noticias)
Centraliza las métricas de sentimiento ya calculadas. En lugar de almacenar cada noticia de manera individual, esta tabla guarda únicamente la media ponderada del día para simplificar el acceso a los datos y optimizar el renderizado de las gráficas en la interfaz. Esta agregación diaria se calcula de forma continua para todas las noticias recolectadas, independientemente de si ese día se ha disputado un partido de liga o no.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| **date** | TEXT (PK) | Fecha del día en el que se publican las noticias (YYYY-MM-DD). |
| **source** | TEXT (PK) | Periódico deportivo (Marca, AS, Mundo Deportivo). |
| **team** | TEXT (PK) | Nombre del equipo analizado. |
| **average_sentiment** | REAL | Nota media ponderada del sentimiento en el rango `[-1, 1]`. |
| **news_count** | INTEGER | Volumen total de noticias publicadas ese día. |

#### 6.2.3. Tabla `matches` (Resultados Deportivos)
Guarda el histórico de partidos de LaLiga. Nos permite acceder tanto a todos los resultados como al histórico de la clasificación de cada equipo.

| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| **date** | TEXT | Fecha exacta del encuentro (Formato ISO 8601). |
| **matchday** | INTEGER | Número de la jornada de competición. |
| **home_team** | TEXT | Nombre del equipo local. |
| **away_team** | TEXT | Nombre del equipo visitante. |
| **home_goals** | INTEGER | Goles anotados por el equipo local. |
| **away_goals** | INTEGER | Goles anotados por el equipo visitante. |
| **home_rank** | INTEGER | Posición en la clasificación del local tras el partido. |
| **away_rank** | INTEGER | Posición en la clasificación del visitante tras el partido. |


## 7. Principios y Patrones de Diseño
El desarrollo se ha guiado por los principios SOLID, buscando un código limpio, modular y mantenible. Destacan los siguientes patrones de diseño:

- **Publisher/Subscriber**: Implementado transversalmente con ActiveMQ y JMS para desacoplar la recolección de la lógica de negocio.

- **Patrón Repository / DAO**: Visible en clases como SqlRepository, NewsRepository y EventRepository. Abstrae la lógica de acceso a datos (SQLite), aislando la capa de dominio de los detalles de persistencia.

- **Separation of Concerns (SoC)** en Queries: Las consultas a la base de datos están encapsuladas en clases individuales (TeamEvolutionQuery, ThermometerQuery, etc.), respetando el Principio de Responsabilidad Única (SRP) y facilitando la creación de las gráficas.

- **MVC / API Controller**: Separación clara en la Business Unit entre las rutas de la API (DashboardApi), los controladores lógicos y las vistas (archivos estáticos HTML/JS).

- **Monitorización y Trazabilidad (Logging):** Como pilar fundamental de la monitorización del sistema, se ha implementado un registro de *Logs* unificado en todos los módulos. Todos los mensajes están estandarizados y redactados en inglés. Esta práctica responde a la necesidad crítica de saber "¿qué ha ocurrido?" en tiempo de ejecución, permitiéndonos detectar fallos, entender el estado de la aplicación y facilitar enormemente la depuración ante posibles problemas en producción. Se han utilizado diferentes niveles de severidad (`INFO`, `WARN`, `ERROR`, etc.) siguiendo las mejores prácticas para no saturar la salida y registrar solo la información histórica relevante.

## 8. Instrucciones de Ejecución
### 8.1. Requisitos Previos
```text
Java 21
Apache Maven
ActiveMQ (Servicio local)
```

### 8.2. Configuración
Las claves de los servicios externos deben configurarse en un archivo centralizado llamado `application.properties` situado en el directorio raíz del proyecto. Este enfoque es seguro y evita incrustar credenciales directamente en el código fuente.
El contenido debe seguir esta estructura exacta:

```properties
huggingface.api.token=TOKEN_DE_HUGGINGFACE
football.api.token=TOKEN_DE_API_FOOTBALL
```
### 8.3. Orden de Ejecución
Para desplegar el proyecto desde cero, los módulos deben levantarse en el siguiente orden estricto:

1. **Arrancar ActiveMQ**: Iniciar el broker de mensajería en el entorno local.

2. **Ejecutar Event Store Builder**: Lanzar la clase `Main` del módulo *event-store-builder* para que empiece a escuchar y almacenar los eventos históricos.

3. **Ejecutar Feeders**: Lanzar las clases `Main` de los módulos *football-api-client* y *rss-news-scraper* para iniciar la ingesta y publicación de datos.

4. **Ejecutar Business Unit**: Lanzar la clase `Main` de *business-unit* para iniciar la creación del Datamart y el servidor web.

5. **Acceso a la Interfaz**: Abrir cualquier navegador web y acceder a la URL: `http://localhost:8080/index.html`

## 9. Ejemplos de Uso
<img width="1918" height="1075" alt="image" src="https://github.com/user-attachments/assets/c90b2000-997c-4198-9947-09d5232022da" />
<img width="1918" height="1078" alt="Captura de pantalla 2026-05-16 131731" src="https://github.com/user-attachments/assets/445a0a5e-0a47-4cb8-aadd-26476df0ea03" />

---

<p align="center">
  <img width="50%" alt="image" src="https://github.com/user-attachments/assets/b4c47d04-6ee6-4bc7-af93-7d05c473e2d6" />
</p>
