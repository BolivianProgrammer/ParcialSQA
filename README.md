# ParcialSQA — Inventory API

Proyecto base de Spring Boot para el primer parcial de Aseguramiento de la Calidad del Software.

## Procedencia

Importado desde [jcbergman/SQA-2026, carpeta inventory-sp3](https://github.com/jcbergman/SQA-2026/tree/ff6dd5288b42400dd28ab88d8480af86bef8c7ce/inventory-sp3), commit `ff6dd5288b42400dd28ab88d8480af86bef8c7ce`.
El código Java y las pruebas existentes se conservan sin cambios. La contraseña de MySQL se obtiene de una variable de entorno. Estas pruebas pertenecen al proyecto base; no representan trabajo nuevo del equipo. Se agregan la configuración local de Docker y el script de arranque preparados durante la instalación.

La colección Postman del proyecto original no se incluye: el equipo elaborará sus propios casos, solicitudes y scripts de prueba.

## Requisitos

- JDK 17 (el proyecto usa Spring Boot 3.1.0).
- Docker Desktop iniciado.
- Maven se descarga mediante el wrapper incluido; no requiere instalación global.
- Postman para probar la API. DBeaver es opcional para consultar MySQL.

## Ejecución en el equipo de Tyler

En PowerShell, desde la carpeta del repositorio:

```powershell
.\run-local.ps1
```

La instalación local ya dispone de un archivo `.env` ignorado por Git. El script carga la contraseña desde ese archivo y busca Java 17 en `%LOCALAPPDATA%\Programs\Eclipse Adoptium`, inicia MySQL y ejecuta la API. También añade temporalmente al PATH la instalación local de Docker Desktop. Si PowerShell bloquea el script, puede ejecutarse para este proceso con `powershell -NoProfile -ExecutionPolicy Bypass -File .\run-local.ps1`.

## Ejecución en otros equipos

Copiar `.env.example` a `.env` y completar `MYSQL_ROOT_PASSWORD` con una contraseña local. Si se reutiliza un volumen existente, debe coincidir con la contraseña con la que se creó. No subir `.env` al repositorio.

Configurar `JAVA_HOME` con la ruta del JDK 17 y añadir su carpeta `bin` al PATH. Docker debe estar disponible en el PATH. Desde la raíz del repositorio:

```powershell
$env:MYSQL_ROOT_PASSWORD = (Get-Content .env | Where-Object { $_ -match '^MYSQL_ROOT_PASSWORD=' }) -replace '^MYSQL_ROOT_PASSWORD=', ''
docker compose up -d --wait
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.address=127.0.0.1'
```

En macOS/Linux exportar `MYSQL_ROOT_PASSWORD` en el entorno y usar `./mvnw` en lugar de `.\mvnw.cmd`.

## Pruebas manuales

La aplicación es una API REST, sin interfaz web. En Postman configurar `baseUrl` como `http://127.0.0.1:8080`.

- `GET {{baseUrl}}/api/v1/categories`
- `GET {{baseUrl}}/api/v1/products`
- `GET {{baseUrl}}/api/v1/categories/export/excel`
- `GET {{baseUrl}}/api/v1/products/export/excel`

Los controladores en `src/main/java/com/company/inventory/controller` contienen las rutas y parámetros. Los resultados esperados deben definirse a partir de los requisitos acordados; el comportamiento actual no constituye por sí solo un criterio de aceptación.

## Base de datos local

| Campo | Valor |
|---|---|
| Host | 127.0.0.1 |
| Puerto | 3306 |
| Base de datos | db_inventory |
| Usuario | root |
| Contraseña | Valor local de MYSQL_ROOT_PASSWORD en .env |
| Contenedor | inventory-sp3-mysql |

La contraseña se guarda únicamente en el archivo local `.env`, excluido de Git. MySQL se publica únicamente en localhost. `compose.yaml` fija la imagen descargada durante la preparación y conserva los datos en el volumen `inventory-sp3_mysql-data`. Esta copia comparte el contenedor y los datos con la preparación anterior del proyecto; ejecutar una sola instancia de la API en el puerto 8080.

En DBeaver, usar la conexión existente o crear una conexión MySQL con los valores anteriores. Si el controlador lo requiere, establecer `allowPublicKeyRetrieval=true` y `useSSL=false` para esta conexión local.

## Pruebas existentes y cobertura

Con Java 17 activo:

```powershell
.\mvnw.cmd -B -ntp clean verify
```

- Resultados: `target/surefire-reports`.
- Cobertura JaCoCo: `target/site/jacoco/index.html`.
- Las pruebas de persistencia existentes usan H2; no sustituyen las pruebas manuales con MySQL.

## Detener los servicios

Si se inició con `run-local.ps1`, detener la API con Ctrl+C. Después, para detener MySQL conservando los datos:

```powershell
docker compose stop
```

Si la API se inició en segundo plano durante la preparación, el PID está en `target/application-local.pid` y los registros en `target/application-local.log` y `target/application-local.err.log`. Detener esa instancia antes de ejecutar `clean` o iniciar otra API.
