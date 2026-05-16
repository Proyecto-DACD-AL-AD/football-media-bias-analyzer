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
├── business-unit/          # Datamart, lógica de negocio y servidor web (Dashboard API)
├── documentation/          # Archivos de diagramas (UML) y recursos de documentación
├── application.properties  # Archivo centralizado de configuración y tokens (no incluido en git)
└── README.md
````

## 4. Módulos y Diagramas de Clases
A continuación, se detalla la lógica interna de cada módulo de la arquitectura y su correspondiente diseño de clases.

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


## 5. Fuentes de Datos y Datamart
### 5.1. Justificación de Fuentes
- **Deportivas (API-Football)**: Elegida por su fiabilidad, accesibilidad gratuita y por proporcionar todos los datos necesarios sobre resultados y jornadas de LaLiga.

- **Noticias (RSS Scraping)**: Se han extraído los canales RSS de *Marca*, *AS* y *Mundo Deportivo* al ser los tres medios deportivos más influyentes de España. El formato XML de los RSS facilita una extracción limpia.

- **Análisis de Sentimiento (Hugging Face API)**: Se decidió calcular la nota de sentimiento de cada noticia directamente en la fase de captura (Feeder). Dado que la API tarda aproximadamente un segundo por petición, delegar esto al inicio del flujo evita cuellos de botella en la Business Unit, garantizando fluidez en la interfaz de usuario (UI) y un procesamiento de eventos constante.

### 5.2. Estructura del Datamart
El sistema centraliza la información en una base de datos SQLite estructurada para optimizar las consultas de la UI. Consta de tres tablas principales:

- **Registro de Noticias (Escudo anti-duplicados)**: Almacena el equipo y la URL de la noticia. Garantiza la idempotencia evitando procesar la misma noticia dos veces.

- **Agregación Diaria de Noticias**: Almacena la puntuación media de sentimiento por día, el número total de noticias diarias para cada periódico y el equipo correspondiente.

- **Resultados Deportivos**: Guarda los marcadores e historial de partidos por jornada para cada equipo.

![Diagrama de clases](ruta-a-la-imagen-arquitectura.png)


## 6. Principios y Patrones de Diseño
El desarrollo se ha guiado por los principios SOLID, buscando un código limpio, modular y mantenible. Destacan los siguientes patrones de diseño:

- **Publisher/Subscriber**: Implementado transversalmente con ActiveMQ y JMS para desacoplar la recolección de la lógica de negocio.

- **Patrón Repository / DAO**: Visible en clases como SqlRepository, NewsRepository y EventRepository. Abstrae la lógica de acceso a datos (SQLite), aislando la capa de dominio de los detalles de persistencia.

- **Separation of Concerns (SoC)** en Queries: Las consultas a la base de datos están encapsuladas en clases individuales (TeamEvolutionQuery, ThermometerQuery, etc.), respetando el Principio de Responsabilidad Única (SRP) y facilitando la creación de las gráficas.

- **MVC / API Controller**: Separación clara en la Business Unit entre las rutas de la API (DashboardApi), los controladores lógicos y las vistas (archivos estáticos HTML/JS).

## 7. Instrucciones de Ejecución
### 7.1. Requisitos Previos
```text
Java 21
Apache Maven
ActiveMQ (Servicio local)
```

### 7.2. Configuración
Las claves de los servicios externos deben configurarse en un archivo centralizado llamado `application.properties` situado en el directorio raíz del proyecto. Este enfoque es seguro y evita incrustar credenciales directamente en el código fuente.
El contenido debe seguir esta estructura exacta:

```properties
huggingface.api.token=TOKEN_DE_HUGGINGFACE
football.api.token=TOKEN_DE_API_FOOTBALL
```
### 7.3. Orden de Ejecución
Para desplegar el proyecto desde cero, los módulos deben levantarse en el siguiente orden estricto:

1. **Arrancar ActiveMQ**: Iniciar el broker de mensajería en el entorno local.

2. **Ejecutar Event Store Builder**: Lanzar la clase `Main` del módulo *event-store-builder* para que empiece a escuchar y almacenar los eventos históricos.

3. **Ejecutar Feeders**: Lanzar las clases `Main` de los módulos *football-api-client* y *rss-news-scraper* para iniciar la ingesta y publicación de datos.

4. **Ejecutar Business Unit**: Lanzar la clase `Main` de *business-unit* para iniciar la creación del Datamart y el servidor web.

5. **Acceso a la Interfaz**: Abrir cualquier navegador web y acceder a la URL: `http://localhost:8080/index.html`

## 8. Ejemplos de Uso
(...)

<p align="center">
  <img width="50%" alt="image" src="https://github.com/user-attachments/assets/b4c47d04-6ee6-4bc7-af93-7d05c473e2d6" />
</p>
