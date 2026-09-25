# Pruebas funcionales con Postman

Colección de pruebas de extremo a extremo contra la API real (Spring Boot + MySQL). Complementa las pruebas unitarias de JUnit y Mockito, que no usan la base de datos.

## Archivos

- `ParcialSQA-pruebas-funcionales.postman_collection.json`: 46 solicitudes en 6 carpetas, cada una con sus scripts de verificación.
- `producto-inicial.png` y `producto-actualizado.png`: imágenes que envían las solicitudes de productos en `form-data`. No cambiarlas; las comprobaciones comparan su contenido.

## Ejecutar en Postman

1. Levantar MySQL y la API desde la raíz del proyecto (ver el README principal).
2. En Postman, **Import** del archivo JSON.
3. En **Settings > General > Working directory**, seleccionar esta carpeta `postman/` para que Postman encuentre los PNG.
4. Usar **No environment**: `baseUrl` (`http://127.0.0.1:8080/api/v1`) ya está en las variables de la colección.
5. **Run collection** con 1 iteración y las carpetas en su orden original.

## Ejecutar con Newman

Desde esta carpeta:

```powershell
npx newman run ParcialSQA-pruebas-funcionales.postman_collection.json --working-dir .
```

## Notas

- La colección crea sus propios registros `SQA-<fecha>-<aleatorio>` y los elimina en la carpeta **06 Limpieza y cierre**. No borra datos ajenos.
- Si una ejecución se interrumpe, ejecutar la carpeta 06 antes de empezar otra.
- La última solicitud de limpieza condicional aparece omitida cuando la categoría temporal ya se eliminó; es lo esperado.
