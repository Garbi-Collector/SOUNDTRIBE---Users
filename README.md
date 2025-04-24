<p align="center">
  <img src="logo.png" alt="SoundTribe Logo" width="200"/>
</p>

<h1 align="center">SoundTribe - Microservicio de Usuarios</h1>
<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-Latest-green" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Docker-Available-blue" alt="Docker Available"/>
  <img src="https://img.shields.io/badge/Licencia-MIT-blue" alt="Licencia MIT"/>
  <br>
  <i>Conectando personas a través del sonido</i>
</p>

## 📌 Índice
- [¿Qué es SoundTribe?](#-qué-es-soundtribe)
- [Funcionalidades](#-funcionalidades)
- [Tecnologías](#-tecnologías)
- [Imagen Docker](#-imagen-docker)
- [Documentación API](#-documentación-api)
- [Roadmap](#-roadmap)
- [Contribuciones](#-contribuciones)
- [Licencia](#-licencia)

## 🎵 ¿Qué es SoundTribe?

**SoundTribe** es una plataforma de música social que nace como un proyecto personal para conectar a las personas a través de su pasión por la música. Este repositorio contiene el **microservicio de usuarios**, un componente crucial de la arquitectura completa de SoundTribe.

> 💡 **Visión:** Crear comunidades vibrantes alrededor de la música, permitiendo a cualquier persona compartir sus gustos musicales, descubrir nuevos artistas y conectar con personas afines.

Este microservicio representa mi compromiso con el aprendizaje continuo y mi pasión por crear tecnología que acerque a las personas.

## ✨ Funcionalidades

Este microservicio gestiona todo lo relacionado con los usuarios:

- **Registro y autenticación:**
  - Sistema completo de registro con validaciones
  - Login seguro con JWT
  - Refresh tokens

- **Gestión de perfiles:**
  - Carga y gestión de imágenes de perfil (integración con MinIO)
  - Generación y validación de slugs únicos
  - Edición de datos de perfil

- **Seguridad:**
  - Implementación robusta con Spring Security
  - Filtros personalizados
  - Encriptación de contraseñas

## 🛠️ Tecnologías

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Security"/>
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white" alt="JWT"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/MinIO-C72E49?style=for-the-badge&logo=minio&logoColor=white" alt="MinIO"/>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/H2-darkblue?style=for-the-badge" alt="H2"/>
  <img src="https://img.shields.io/badge/ModelMapper-blueviolet?style=for-the-badge" alt="ModelMapper"/>
</p>

## 🐳 Imagen Docker

El microservicio está disponible como imagen Docker en Docker Hub:

[![Docker Hub](https://img.shields.io/badge/Docker_Hub-SoundTribe_Users_API-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://hub.docker.com/r/garbi21/soundtribe-users-api)

Para usar la imagen:

```bash
docker pull garbi21/soundtribe-users-api
```

También puedes incluirla directamente en tu docker-compose.yml:

```yaml
services:
  users-api:
    image: garbi21/soundtribe-users-api:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      # Otras variables de entorno según necesites
```



## 📚 Documentación API

La documentación completa de la API está disponible a través de Swagger UI cuando el proyecto está en ejecución:

```
http://localhost:8080/swagger-ui.html
```


## 🗺️ Roadmap

- [x] Implementación de roles y permisos
- [ ] Sistema de recuperación de contraseñas


## 👥 Contribuciones

¿Quieres contribuir a SoundTribe? ¡Genial! Toda contribución es bienvenida. Sigue estos pasos:

1. Haz un fork del proyecto
2. Crea tu rama de características (`git checkout -b feature/los-mejores-cambios-panchito`)
3. Realiza tus cambios y haz commit (`git commit -m 'Add los cambios mas geniales de la vida'`)
4. Sube los cambios (`git push origin feature/los-mejores-cambios-panchito`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

---

<p align="center">
  Hecho con ❤️ por <a href="https://github.com/Garbi-Collector">Garbi-Collector</a>
</p>
