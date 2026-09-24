# ParcialSQA

API REST de inventario con Spring Boot y MySQL para el primer parcial de Aseguramiento de la Calidad del Software.

Proyecto base: [inventory-sp3 de jcbergman/SQA-2026](https://github.com/jcbergman/SQA-2026/tree/ff6dd5288b42400dd28ab88d8480af86bef8c7ce/inventory-sp3).

## Requisitos

- Java 17 configurado en `JAVA_HOME` y `PATH`.
- Docker Desktop iniciado y Docker disponible en `PATH`.

## Ejecutar

Copia `.env.example` a `.env` y completa `MYSQL_ROOT_PASSWORD` con la contraseña local de MySQL. Desde la carpeta del proyecto, en PowerShell:

```powershell
$env:MYSQL_ROOT_PASSWORD = (Get-Content .env | Where-Object { $_ -match '^MYSQL_ROOT_PASSWORD=' }) -replace '^MYSQL_ROOT_PASSWORD=', ''
docker compose up -d --wait
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.address=127.0.0.1'
```

API disponible en `http://localhost:8080/api/v1`. Puedes probar `GET /categories` y `GET /products` desde Postman.

## Pruebas

```powershell
.\mvnw.cmd verify
```

Reporte de cobertura: `target/site/jacoco/index.html`.
