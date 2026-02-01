# 📚 Documentación de Endpoints - SoundTribe Users API

## 🎯 Introducción

Esta API REST maneja toda la gestión de usuarios de la plataforma SoundTribe, incluyendo autenticación, perfiles, seguimiento de usuarios y gestión de cuentas.

**Base URL**: `http://localhost:puerto` (configurar según tu deployment)

---

## 📑 Índice de Contenidos

1. [Autenticación (`/auth`)](#1-autenticación-auth)
2. [Gestión de Tokens JWT (`/api/jwt`)](#2-gestión-de-tokens-jwt-apijwt)
3. [Experiencia de Usuario (`/user`)](#3-experiencia-de-usuario-user)
4. [Gestión de Fotos (`/fotos`)](#4-gestión-de-fotos-fotos)
5. [Eliminación de Cuenta (`/eliminate/user`)](#5-eliminación-de-cuenta-eliminateuser)

---

## 1. Autenticación (`/auth`)

### 1.1 Registro de Usuario

**Endpoint**: `POST /auth/signup`

**Descripción**: Registra un nuevo usuario en la plataforma y envía un email de verificación.

**Content-Type**: `multipart/form-data`

**Parámetros**:
- `user` (JSON, required): Datos del usuario
  ```json
  {
    "email": "usuario@ejemplo.com",
    "username": "nombreusuario",
    "password": "contraseña123",
    "rol": "OYENTE" // OYENTE, ARTISTA, ADMIN
  }
  ```
- `file` (File, optional): Imagen de perfil (PNG de 98x98, máx 4MB)

**Respuestas**:
- `200 OK`: Registro exitoso
  ```
  "El registro del usuario fue exitoso. Por favor, chequear el email para verificar la cuenta..."
  ```
- `400 Bad Request`: Contraseña vacía
- `409 Conflict`: Usuario o email ya existe

**Ejemplo de uso**:
```javascript
const formData = new FormData();
formData.append('user', JSON.stringify({
    email: 'juan@ejemplo.com',
    username: 'juanperez',
    password: 'miPassword123',
    rol: 'OYENTE'
}));
formData.append('file', imagenFile);

fetch('/auth/signup', {
    method: 'POST',
    body: formData
});
```

---

### 1.2 Verificación de Cuenta

**Endpoint**: `GET /auth/accountVerification/{token}`

**Descripción**: Activa la cuenta del usuario mediante el token enviado por email.

**Parámetros**:
- `token` (path): Token de verificación recibido por email

**Respuestas**:
- `200 OK`: "La cuenta se ha activado Exitosamente"
- `400 Bad Request`: Token inválido

---

### 1.3 Login

**Endpoint**: `POST /auth/login`

**Descripción**: Autentica al usuario y devuelve un token JWT.

**Body**:
```json
{
  "email": "usuario@ejemplo.com",
  "password": "contraseña123"
}
```

**Respuestas**:
- `200 OK`:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "juanperez",
    "email": "juan@ejemplo.com"
  }
  ```
- `401 Unauthorized`: Credenciales incorrectas o cuenta no verificada

---

### 1.4 Verificar Existencia de Email

**Endpoint**: `GET /auth/email-exists?email={email}`

**Descripción**: Verifica si un email ya está registrado.

**Parámetros**:
- `email` (query): Email a verificar

**Respuestas**:
- `200 OK`: `true` o `false`

---

### 1.5 Verificar Existencia de Username

**Endpoint**: `GET /auth/username-exists?username={username}`

**Descripción**: Verifica si un username ya está en uso.

**Parámetros**:
- `username` (query): Username a verificar

**Respuestas**:
- `200 OK`: `true` o `false`

---

### 1.6 Obtener Usuarios Habilitados

**Endpoint**: `GET /auth/usuarios-habilitados`

**Descripción**: Lista todos los usuarios con cuenta verificada.

**Respuestas**:
- `200 OK`:
  ```json
  [
    {
      "username": "juanperez",
      "urlFoto": "url-de-la-imagen.png"
    }
  ]
  ```

---

### 1.7 Cambiar Contraseña (Autenticado)

**Endpoint**: `PUT /auth/change-password`

**Headers**:
- `Authorization`: `Bearer {token}`

**Body**:
```json
{
  "currentPassword": "contraseñaActual",
  "newPassword": "nuevaContraseña123"
}
```

**Respuestas**:
- `200 OK`: "Contraseña actualizada correctamente"
- `400 Bad Request`: Contraseña actual incorrecta

---

### 1.8 Verificar Contraseña

**Endpoint**: `POST /auth/is-my-password`

**Headers**:
- `Authorization`: `Bearer {token}`

**Body**:
```json
{
  "password": "contraseñaAVerificar"
}
```

**Respuestas**:
- `200 OK`: `true` o `false`

---

## 2. Gestión de Tokens JWT (`/api/jwt`)

### 2.1 Validar Token

**Endpoint**: `POST /api/jwt/validate`

**Descripción**: Valida un token JWT y devuelve información del usuario.

**Headers**:
- `Authorization`: `Bearer {token}`

**Respuestas**:
- `200 OK`:
  ```json
  {
    "valid": true,
    "userId": 123,
    "username": "juanperez",
    "email": "juan@ejemplo.com",
    "role": "OYENTE",
    "slug": "palabra1-palabra2-1234",
    "isAdmin": false,
    "isArtista": false,
    "isOyente": true
  }
  ```
- `401 Unauthorized`: Token inválido
- `404 Not Found`: Usuario no encontrado

---

## 3. Experiencia de Usuario (`/user`)

### 3.1 Seguir Usuario

**Endpoint**: `POST /user/follow/{id}`

**Descripción**: Sigue a otro usuario y envía notificación.

**Headers**:
- `Authorization`: `Bearer {token}`

**Parámetros**:
- `id` (path): ID del usuario a seguir

**Respuestas**:
- `200 OK`: "Seguido correctamente"
- `400 Bad Request`: Ya estás siguiendo a este usuario / No puedes seguirte a ti mismo

---

### 3.2 Dejar de Seguir

**Endpoint**: `DELETE /user/unfollow/{id}`

**Headers**:
- `Authorization`: `Bearer {token}`

**Parámetros**:
- `id` (path): ID del usuario a dejar de seguir

**Respuestas**:
- `200 OK`: "Has dejado de seguir al usuario"

---

### 3.3 Verificar si Sigues a un Usuario

**Endpoint**: `GET /user/is-following/{id}`

**Headers**:
- `Authorization`: `Bearer {token}`

**Parámetros**:
- `id` (path): ID del usuario a verificar

**Respuestas**:
- `200 OK`: `true` o `false`

---

### 3.4 Obtener Amigos Artistas Mutuos

**Endpoint**: `GET /user/mutual-artist-friends`

**Descripción**: Lista artistas que te siguen y a quienes sigues (amigos mutuos).

**Headers**:
- `Authorization`: `Bearer {token}`

**Respuestas**:
- `200 OK`: Array de objetos `UserGet`

---

### 3.5 Obtener Seguidores

**Endpoint**: `GET /user/followers`

**Headers**:
- `Authorization`: `Bearer {token}`

**Respuestas**:
- `200 OK`:
  ```json
  [
    {
      "id": 456,
      "username": "otroUsuario",
      "rol": "ARTISTA",
      "urlFoto": "imagen.png",
      "slug": "palabra-palabra-1234",
      "followersCount": 100
    }
  ]
  ```

---

### 3.6 Recuperar Contraseña

**Endpoint**: `POST /user/recuperar-password`

**Descripción**: Envía un email con link para recuperar contraseña.

**Body**: `"usuario@ejemplo.com"` (como string simple)

**Respuestas**:
- `200 OK`: "Se ha enviado una nueva contraseña al correo electrónico..."
- `400 Bad Request`: Error al enviar correo

---

### 3.7 Cambiar Contraseña (Recuperación)

**Endpoint**: `PUT /user/cambiar-password`

**Body**:
```json
{
  "newPassword": "nuevaContraseña123",
  "slugRecovery": "slug-de-recuperacion-1234"
}
```

**Respuestas**:
- `200 OK`: "Contraseña cambiada correctamente"
- `400 Bad Request`: Slug inválido o expirado

---

### 3.8 Validar Slug de Recuperación

**Endpoint**: `GET /user/validar-slug/{slugRecovery}`

**Parámetros**:
- `slugRecovery` (path): Slug de recuperación

**Respuestas**:
- `200 OK`: Devuelve el username del usuario
- `400 Bad Request`: Slug no válido o expirado

---

### 3.9 Verificar Validez de Slug

**Endpoint**: `GET /user/recuperar-password/validar/{slugRecovery}`

**Descripción**: Verifica si un slug de recuperación está vigente (30 min de validez).

**Respuestas**:
- `200 OK`: `true` o `false`

---

### 3.10 Obtener Todos los Usuarios

**Endpoint**: `GET /user/all`

**Descripción**: Lista todos los usuarios habilitados.

**Respuestas**:
- `200 OK`:
  ```json
  {
    "usuarios": [
      {
        "id": 1,
        "username": "usuario1",
        "rol": "OYENTE",
        "urlFoto": "foto.png",
        "slug": "palabra-palabra-1234",
        "followersCount": 50
      }
    ]
  }
  ```

---

### 3.11 Obtener Todos los Usuarios (Excepto Tú)

**Endpoint**: `GET /user/all/jwt`

**Headers**:
- `Authorization`: `Bearer {token}`

**Descripción**: Lista todos los usuarios excepto el autenticado.

**Respuestas**:
- `200 OK`: Objeto `GetAll` con array de usuarios

---

### 3.12 Obtener Perfil por Slug

**Endpoint**: `GET /user/perfil/slug/{slug}`

**Parámetros**:
- `slug` (path): Slug único del usuario

**Respuestas**:
- `200 OK`:
  ```json
  {
    "id": 123,
    "username": "juanperez",
    "description": "Descripción del perfil",
    "rol": "ARTISTA",
    "urlimage": "imagen.png",
    "createdAt": "2024-01-15T10:30:00",
    "artistasSeguidos": [...],
    "slug": "palabra-palabra-1234",
    "followersCount": 200,
    "followedsCount": 150
  }
  ```

---

### 3.13 Obtener Perfil por ID

**Endpoint**: `GET /user/perfil/{id}`

**Parámetros**:
- `id` (path): ID del usuario

**Respuestas**:
- `200 OK`: Objeto `UserDescription`

---

### 3.14 Obtener Mi Perfil

**Endpoint**: `GET /user/mi-perfil`

**Headers**:
- `Authorization`: `Bearer {token}`

**Respuestas**:
- `200 OK`: Objeto `UserDescription` del usuario autenticado

---

### 3.15 Obtener Información del Usuario Autenticado

**Endpoint**: `GET /user/me`

**Headers**:
- `Authorization`: `Bearer {token}`

**Respuestas**:
- `200 OK`: Objeto `UserGet`

---

### 3.16 Cambiar Descripción

**Endpoint**: `PUT /user/change-description`

**Headers**:
- `Authorization`: `Bearer {token}`

**Parámetros**:
- `newDescription` (query): Nueva descripción del perfil

**Respuestas**:
- `200 OK`: "Descripción actualizada correctamente"

---

### 3.17 Cambiar Foto de Perfil

**Endpoint**: `POST /user/cambiar-foto`

**Content-Type**: `multipart/form-data`

**Headers**:
- `Authorization`: `Bearer {token}`

**Parámetros**:
- `file` (File, required): Nueva imagen de perfil

**Respuestas**:
- `200 OK`: "Foto de perfil actualizada correctamente"

---

### 3.18 Generar Slug Personalizado

**Endpoint**: `POST /user/generate-slug`

**Headers**:
- `Authorization`: `Bearer {token}`

**Parámetros**:
- `firstWord` (query): Primera palabra (2-7 caracteres)
- `secondWord` (query): Segunda palabra (2-7 caracteres)
- `number` (query): Número (0-9999)

**Respuestas**:
- `200 OK`: Devuelve el slug generado: `"palabra1-palabra2-#1234"`
- `400 Bad Request`: Slug ya en uso

**Ejemplo**:
```
POST /user/generate-slug?firstWord=musica&secondWord=viva&number=4567
→ "musica-viva-#4567"
```

---

### 3.19 Verificar Primera Palabra del Slug

**Endpoint**: `GET /user/check-first-slug/{firstPart}`

**Descripción**: Verifica si existe algún slug con esa primera palabra.

**Respuestas**:
- `200 OK`: `true` o `false`

---

### 3.20 Verificar Segunda Palabra del Slug

**Endpoint**: `GET /user/check-second-slug/{secondPart}`

**Respuestas**:
- `200 OK`: `true` o `false`

---

### 3.21 Verificar Número del Slug

**Endpoint**: `GET /user/check-number-slug/{number}`

**Respuestas**:
- `200 OK`: `true` o `false`

---

## 4. Gestión de Fotos (`/fotos`)

### 4.1 Subir Foto

**Endpoint**: `POST /fotos`

**Content-Type**: `multipart/form-data`

**Parámetros**:
- `file` (File, required): Imagen PNG de 98x98, máx 4MB

**Respuestas**:
- `200 OK`:
  ```json
  {
    "id": 789,
    "fileName": "imagen-original.png",
    "fileType": "PNG_IMAGE",
    "fileUrl": "uuid-imagen-original.png"
  }
  ```

---

### 4.2 Obtener Foto por ID

**Endpoint**: `GET /fotos/{id}`

**Descripción**: Devuelve la imagen como stream binario.

**Respuestas**:
- `200 OK`: Imagen (Content-Type: image/png)
- `404 Not Found`: Foto no encontrada

---

### 4.3 Obtener Foto por Nombre de Archivo

**Endpoint**: `GET /fotos/image/{filename}`

**Descripción**: Devuelve la imagen directamente desde MinIO.

**Respuestas**:
- `200 OK`: Imagen (Content-Type: image/png)

---

## 5. Eliminación de Cuenta (`/eliminate/user`)

### 5.1 Eliminar Cuenta Completa

**Endpoint**: `DELETE /eliminate/user/delete-account`

**Descripción**: Elimina la cuenta del usuario y todos sus datos relacionados en todos los microservicios (música, donaciones, notificaciones). Este proceso es **asíncrono**.

**Headers**:
- `Authorization`: `Bearer {token}`

**⚠️ ADVERTENCIA**: Esta acción es irreversible.

**Proceso de eliminación**:
1. Elimina datos en microservicio de donaciones
2. Elimina datos en microservicio de notificaciones
3. Elimina datos en microservicio de música
4. Elimina relaciones de seguidores/seguidos
5. Elimina tokens de verificación
6. Elimina foto personalizada (si no es estándar)
7. Elimina la cuenta del usuario

**Respuestas**:
- `200 OK`: "✅ Se ha iniciado el proceso de eliminación de cuenta."
- `400 Bad Request`: Token no proporcionado correctamente

---

## 📊 Modelos de Datos

### UserGet
```json
{
  "id": 123,
  "username": "juanperez",
  "rol": "OYENTE",
  "urlFoto": "imagen.png",
  "slug": "palabra-palabra-1234",
  "followersCount": 100
}
```

### UserDescription
```json
{
  "id": 123,
  "username": "juanperez",
  "description": "Descripción del usuario",
  "rol": "ARTISTA",
  "urlimage": "imagen.png",
  "createdAt": "2024-01-15T10:30:00",
  "artistasSeguidos": [...],
  "slug": "palabra-palabra-1234",
  "followersCount": 200,
  "followedsCount": 150
}
```

### FotoModel
```json
{
  "id": 789,
  "fileName": "original.png",
  "fileType": "PNG_IMAGE",
  "fileUrl": "uuid-original.png"
}
```

---

## 🔐 Autenticación y Autorización

### Sistema de Roles
- **ADMIN**: Acceso completo al sistema
- **ARTISTA**: Puede subir música, recibir donaciones
- **OYENTE**: Puede escuchar música, seguir artistas, donar

### Headers de Autenticación
Todos los endpoints protegidos requieren:
```
Authorization: Bearer {jwt_token}
```

El token JWT contiene:
- `sub`: Username del usuario
- `role`: Rol del usuario
- `email`: Email del usuario
- Fecha de expiración

---

## 🎨 Características Especiales

### Sistema de Slugs
Cada usuario tiene un slug único con formato: `palabra1-palabra2-numero`
- Palabras: 2-7 caracteres cada una
- Número: 4 dígitos (0000-9999)
- Ejemplo: `musica-viva-#1234`

Los slugs pueden ser:
1. **Generados automáticamente**: Palabras aleatorias + número aleatorio
2. **Personalizados**: El usuario elige las palabras y el número

### Gestión de Imágenes
- Almacenamiento en MinIO (S3-compatible)
- Imágenes estándar por defecto: `perfilstandar.png`, `ADMIN.png`
- Las imágenes personalizadas se eliminan al cambiar/eliminar cuenta
- Las estándar se mantienen en el sistema

### Sistema de Notificaciones
Al seguir a un usuario, se envía automáticamente una notificación al microservicio de notificaciones.

### Recuperación de Contraseña
- Genera un slug único de recuperación
- Validez: 30 minutos
- Se envía por email con link al frontend
- Después de usar, el slug se invalida

---

## 🚀 Flujos Completos de Usuario

### Flujo de Registro
1. Usuario envía datos + foto opcional → `POST /auth/signup`
2. Sistema crea usuario con cuenta deshabilitada
3. Se envía email con token de verificación
4. Usuario hace click en link → `GET /auth/accountVerification/{token}`
5. Cuenta se activa

### Flujo de Login
1. Usuario envía credenciales → `POST /auth/login`
2. Sistema verifica contraseña y estado de cuenta
3. Se genera JWT
4. Cliente guarda token para requests futuros

### Flujo de Seguimiento
1. Usuario A sigue a Usuario B → `POST /user/follow/{idB}`
2. Se crea relación en base de datos
3. Se envía notificación a Usuario B (microservicio)
4. Usuario B puede ver que A lo sigue

### Flujo de Recuperación de Contraseña
1. Usuario solicita recuperación → `POST /user/recuperar-password`
2. Se genera slug único (30 min validez)
3. Se envía email con link
4. Usuario valida slug → `GET /user/validar-slug/{slug}`
5. Usuario establece nueva contraseña → `PUT /user/cambiar-password`
6. Slug se invalida automáticamente

---

## 📝 Notas Importantes

1. **Validación de imágenes**: Solo PNG de 98x98 píxeles, máximo 4MB
2. **Tokens de verificación**: No tienen expiración (solo para activación de cuenta)
3. **Slugs de recuperación**: Expiran en 30 minutos
4. **JWT**: El tiempo de expiración se configura en el JwtProvider
5. **Eliminación asíncrona**: El proceso de eliminación de cuenta continúa en background
6. **Cache**: Algunos endpoints usan cache para mejorar rendimiento (`@Cacheable`)

---

## 🐛 Manejo de Errores

### Errores Comunes

**400 Bad Request**
- Datos inválidos en el request
- Contraseña incorrecta
- Slug ya en uso

**401 Unauthorized**
- Token JWT inválido o expirado
- Usuario no autenticado

**404 Not Found**
- Usuario no encontrado
- Recurso no existe

**409 Conflict**
- Email o username ya registrado
- Usuario ya está siendo seguido

---

## 🎬 Ejemplos de Uso Completos

### Ejemplo 1: Registro y Login Completo
```javascript
// 1. Verificar si email existe
const emailExists = await fetch('/auth/email-exists?email=juan@ejemplo.com')
    .then(r => r.json());

if (!emailExists) {
    // 2. Registrar usuario
    const formData = new FormData();
    formData.append('user', JSON.stringify({
        email: 'juan@ejemplo.com',
        username: 'juanperez',
        password: 'MiPassword123!',
        rol: 'OYENTE'
    }));

    await fetch('/auth/signup', {
        method: 'POST',
        body: formData
    });

    // 3. Usuario verifica email (se hace desde el link del correo)
    // GET /auth/accountVerification/{token}

    // 4. Login
    const response = await fetch('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email: 'juan@ejemplo.com',
            password: 'MiPassword123!'
        })
    });

    const { token } = await response.json();
    localStorage.setItem('token', token);
}
```

### Ejemplo 2: Actualizar Perfil
```javascript
const token = localStorage.getItem('token');

// 1. Cambiar descripción
await fetch('/user/change-description?newDescription=Amante de la música indie', {
    method: 'PUT',
    headers: {
        'Authorization': `Bearer ${token}`
    }
});

// 2. Cambiar foto
const formData = new FormData();
formData.append('file', nuevaFoto);

await fetch('/user/cambiar-foto', {
    method: 'POST',
    headers: {
        'Authorization': `Bearer ${token}`
    },
    body: formData
});

// 3. Cambiar slug personalizado
const nuevoSlug = await fetch(
    '/user/generate-slug?firstWord=musica&secondWord=rock&number=2024',
    {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    }
).then(r => r.text());

console.log('Nuevo slug:', nuevoSlug); // "musica-rock-#2024"
```

### Ejemplo 3: Sistema de Seguimiento
```javascript
const token = localStorage.getItem('token');
const artistaId = 456;

// 1. Verificar si ya lo sigo
const yaLoSigo = await fetch(`/user/is-following/${artistaId}`, {
    headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());

if (!yaLoSigo) {
    // 2. Seguir al artista
    await fetch(`/user/follow/${artistaId}`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
    });
}

// 3. Obtener mis amigos artistas mutuos
const amigosMutuos = await fetch('/user/mutual-artist-friends', {
    headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());

console.log('Amigos artistas:', amigosMutuos);
```

---

## 🎓 Conclusión

Esta API proporciona todas las funcionalidades necesarias para:
- ✅ Gestión completa de usuarios
- ✅ Autenticación segura con JWT
- ✅ Sistema de seguimiento social
- ✅ Recuperación de contraseñas
- ✅ Gestión de imágenes con MinIO
- ✅ Slugs personalizables
- ✅ Integración con otros microservicios

**Arquitectura de microservicios integrados**:
- Users (este microservicio)
- Music
- Donations
- Notifications

---

**Versión**: 1.0  
**Última actualización**: Febrero 2026  
**Autor**: Gabriel Scipioni (SoundTribe)