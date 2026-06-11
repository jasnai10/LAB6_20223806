# Laboratorio 6 — App Android Mundial de Fútbol

## Contexto del proyecto

Aplicación Android para gestionar pronósticos del Mundial de Fútbol, con autenticación Firebase y persistencia en Firestore. El proyecto ya está creado en Android Studio y Firebase está completamente configurado (Authentication con 3 providers habilitados, Firestore creado con reglas publicadas, `google-services.json` en su sitio).

**Tu trabajo**: generar todo el código Java + XML del proyecto.

---

## Stack y configuración (YA HECHO, no modifiques)

- **Lenguaje**: Java + XML
- **Build**: Gradle Groovy
- **Package**: `com.example.lab6_20223806`
- **minSdk**: 34 / **targetSdk** y **compileSdk**: 36
- **Java**: 11
- **ViewBinding**: habilitado

**Dependencias ya instaladas** (no agregar nada más sin avisar):

```groovy
implementation platform('com.google.firebase:firebase-bom:34.14.0')
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.google.android.gms:play-services-auth:21.2.0'
implementation 'androidx.navigation:navigation-fragment:2.8.4'
implementation 'androidx.navigation:navigation-ui:2.8.4'
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
```

---

## Backend: Firestore

**Colección única**: `pronosticos`

Cada documento (ID autogenerado) contiene:

| Campo | Tipo Firestore | Tipo Java | Notas |
|---|---|---|---|
| `userId` | string | `String` | uid del usuario autenticado |
| `seleccionA` | string | `String` | de lista fija |
| `seleccionB` | string | `String` | ≠ seleccionA |
| `fechaPartido` | timestamp | `Date` | DatePicker |
| `golesA` | int64 (number) | `long` | entero ≥ 0 |
| `golesB` | int64 (number) | `long` | entero ≥ 0 |
| `estado` | string | `String` | `"PENDIENTE"` / `"ACERTADO"` / `"FALLADO"` |
| `fechaCreacion` | timestamp | `Date` | `FieldValue.serverTimestamp()` al crear |

**NO se usa colección de usuarios** — Firebase Auth gestiona los datos del usuario.

**Reglas de Firestore** (ya publicadas en consola):

```
allow read, write: if request.auth != null;
```

→ La lógica de filtrado por `userId` y bloqueo por estado se implementa en el código Android, no en las reglas.

---

## Autenticación

**Login y registro 100% personalizados** (NO usar FirebaseUI / AuthUI). Todo en español, con logo propio.

Tres métodos a implementar:

1. **Email + Password** (con pantalla de Registro separada usando `createUserWithEmailAndPassword`)
2. **Google Sign-In** (con `GoogleSignInClient` y `signInWithCredential`)
3. **GitHub Sign-In** (con `OAuthProvider.newBuilder("github.com")` y `startActivityForSignInWithProvider`)

---

## Estructura de archivos

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/lab6_20223806/
│   ├── MainActivity.java                       (host con BottomNavigation + NavController)
│   ├── auth/
│   │   ├── LoginActivity.java                  (email + Google + GitHub)
│   │   └── RegisterActivity.java               (registro email/password)
│   ├── ui/
│   │   ├── pronosticos/
│   │   │   ├── PronosticosFragment.java        (lista con RecyclerView + FAB)
│   │   │   ├── PronosticoAdapter.java
│   │   │   ├── RegistrarPronosticoActivity.java
│   │   │   └── EditarPronosticoActivity.java
│   │   └── estadisticas/
│   │       └── EstadisticasFragment.java       (gráfico MPAndroidChart)
│   ├── model/
│   │   ├── Pronostico.java                     (POJO Firestore)
│   │   └── EstadoPronostico.java               (constantes)
│   └── data/
│       └── PronosticoRepository.java           (opcional)
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── activity_login.xml
    │   ├── activity_register.xml
    │   ├── activity_registrar_pronostico.xml
    │   ├── activity_editar_pronostico.xml
    │   ├── fragment_pronosticos.xml
    │   ├── fragment_estadisticas.xml
    │   └── item_pronostico.xml
    ├── menu/
    │   └── bottom_nav_menu.xml
    ├── navigation/
    │   └── nav_graph.xml
    ├── drawable/
    │   ├── logo.xml                            (logo vectorial simple)
    │   ├── ic_google.xml
    │   └── ic_github.xml
    └── values/
        ├── colors.xml
        ├── strings.xml                         (incluye string-array selecciones_mundial)
        └── themes.xml
```

---

## Especificaciones por pantalla

### 1. LoginActivity

- Logo arriba (drawable propio, NO el ícono de Android).
- `TextInputLayout` para email y password.
- Botón "Iniciar sesión" → `signInWithEmailAndPassword`.
- Link "¿No tienes cuenta? Regístrate" → abre `RegisterActivity`.
- Divisor "o continúa con".
- Botón "Continuar con Google" (con ícono `ic_google`).
- Botón "Continuar con GitHub" (con ícono `ic_github`).
- Todo en español.
- Si ya hay sesión activa al iniciar (`getCurrentUser() != null`) → ir directo a `MainActivity` cerrando esta activity.

### 2. RegisterActivity

- Logo arriba.
- `TextInputLayout` para email, password, confirmar password.
- Validaciones: email válido, password ≥ 6 caracteres, passwords coinciden.
- Botón "Registrarse" → `createUserWithEmailAndPassword` → al éxito ir a `MainActivity`.
- Link "¿Ya tienes cuenta? Inicia sesión" → `finish()`.

### 3. MainActivity

- Host con `FragmentContainerView` + `BottomNavigationView` abajo.
- `NavController` con `nav_graph.xml`.
- 3 ítems en bottom nav:
  - **Mis Pronósticos** → `PronosticosFragment`
  - **Estadísticas** → `EstadisticasFragment`
  - **Cerrar Sesión** → no es destino navegable; al hacer click ejecuta `FirebaseAuth.getInstance().signOut()`, también `GoogleSignInClient.signOut()`, y navega de vuelta a `LoginActivity` cerrando el back stack.

### 4. PronosticosFragment

- `RecyclerView` con la lista de pronósticos del usuario actual.
- Query: `db.collection("pronosticos").whereEqualTo("userId", uid).orderBy("fechaPartido", DESCENDING)` con `addSnapshotListener`.
- La primera vez Firestore pedirá un índice compuesto → mostrar el error con `Log.e` y el link directo en el mensaje.
- `FloatingActionButton` abajo a la derecha → abre `RegistrarPronosticoActivity`.
- Mensaje "No tienes pronósticos aún" si la lista está vacía.

### 5. PronosticoAdapter (item_pronostico.xml)

Cada item muestra:

- Selección A vs Selección B
- Fecha del partido (formato `dd/MM/yyyy`)
- Resultado pronosticado: `golesA - golesB`
- **Chip de estado con color**:
  - 🟡 PENDIENTE → fondo ámbar
  - 🟢 ACERTADO → fondo verde
  - 🔴 FALLADO → fondo rojo
- Botones **Editar** y **Eliminar** visibles SOLO si `estado == PENDIENTE` (ocultos con `View.GONE` en otros estados).
- Click en editar → `EditarPronosticoActivity` pasando el ID del documento.
- Click en eliminar → `AlertDialog` de confirmación → si acepta, `db.collection("pronosticos").document(id).delete()`.

### 6. RegistrarPronosticoActivity

Formulario con:

- **Selección A**: `AutoCompleteTextView` dentro de `TextInputLayout` con estilo `Widget.Material3.TextInputLayout.OutlinedBox.ExposedDropdownMenu`. Adapter desde `R.array.selecciones_mundial`.
- **Selección B**: igual que A.
- **Fecha del partido**: `TextInputEditText` solo-lectura que al hacer click abre `DatePickerDialog`.
- **Goles A**: `TextInputEditText` con `inputType="number"`.
- **Goles B**: igual.
- Botón "Registrar".

Validaciones:

- Ambas selecciones no vacías.
- `seleccionA.equalsIgnoreCase(seleccionB)` → error: "Las selecciones no pueden ser iguales".
- Fecha seleccionada.
- Goles parseable a `long` ≥ 0.

Al guardar:

- `estado = "PENDIENTE"` automático.
- `userId = FirebaseAuth.getInstance().getCurrentUser().getUid()`.
- `fechaCreacion = FieldValue.serverTimestamp()` (usar `@ServerTimestamp` en el POJO o `Map<String,Object>` directo).
- Snackbar "Pronóstico registrado correctamente" → `finish()`.

### 7. EditarPronosticoActivity

- Recibe el ID del documento por Intent extra.
- Carga el documento al iniciar.
- Los mismos campos que el registro **más** un `AutoCompleteTextView` para **Estado** con opciones: "Pendiente", "Acertado", "Fallado".
- Antes de aplicar el update, verifica que el estado actual en Firestore sigue siendo "PENDIENTE" (re-fetch); si no, mostrar Toast "Este pronóstico ya fue cerrado" y `finish()`.
- Mismas validaciones que el registro.
- Snackbar "Pronóstico actualizado correctamente" → `finish()`.

### 8. EstadisticasFragment

- `addSnapshotListener` sobre la query del usuario actual.
- Cuenta total, acertados, fallados, pendientes.
- Muestra:
  - 4 `TextView` con los números (total, acertados, fallados, pendientes).
  - **Gráfico circular** con `PieChart` de MPAndroidChart con las 3 categorías (Acertados verde, Fallados rojo, Pendientes ámbar).
- Se actualiza automáticamente al haber cambios.

---

## Clases del modelo

### Pronostico.java

```java
public class Pronostico {
    @Exclude private String id;
    private String userId;
    private String seleccionA;
    private String seleccionB;
    private Date fechaPartido;
    private long golesA;
    private long golesB;
    private String estado;
    private Date fechaCreacion;

    public Pronostico() {}  // requerido por Firestore

    // getters y setters de todos los campos
    // getter+setter de id con @Exclude
}
```

### EstadoPronostico.java

```java
public final class EstadoPronostico {
    public static final String PENDIENTE = "PENDIENTE";
    public static final String ACERTADO  = "ACERTADO";
    public static final String FALLADO   = "FALLADO";
    private EstadoPronostico() {}

    public static String displayName(String estado) {
        switch (estado) {
            case PENDIENTE: return "Pendiente";
            case ACERTADO:  return "Acertado";
            case FALLADO:   return "Fallado";
            default: return estado;
        }
    }
}
```

---

## strings.xml — agregar este array

```xml
<string-array name="selecciones_mundial">
    <item>Argentina</item>
    <item>Brasil</item>
    <item>Perú</item>
    <item>Uruguay</item>
    <item>Colombia</item>
    <item>Ecuador</item>
    <item>Chile</item>
    <item>Paraguay</item>
    <item>Bolivia</item>
    <item>Venezuela</item>
    <item>España</item>
    <item>Francia</item>
    <item>Alemania</item>
    <item>Inglaterra</item>
    <item>Portugal</item>
    <item>Italia</item>
    <item>Países Bajos</item>
    <item>Bélgica</item>
    <item>Croacia</item>
    <item>México</item>
    <item>Estados Unidos</item>
    <item>Canadá</item>
    <item>Japón</item>
    <item>Corea del Sur</item>
    <item>Marruecos</item>
    <item>Senegal</item>
    <item>Australia</item>
    <item>Arabia Saudita</item>
</string-array>
```

---

## AndroidManifest.xml

- `LoginActivity` como launcher.
- `MainActivity`, `RegisterActivity`, `RegistrarPronosticoActivity`, `EditarPronosticoActivity` declaradas.
- Permiso de internet (`android.permission.INTERNET`).

---

## Reglas importantes

1. **Java**, no Kotlin.
2. Usa **ViewBinding** en todas las Activities y Fragments (ya está habilitado en Gradle).
3. NO uses FirebaseUI / AuthUI — login totalmente personalizado.
4. Todos los textos visibles al usuario en **español**.
5. Las constantes de estado siempre vía `EstadoPronostico.PENDIENTE`, etc. — no strings mágicos en el código.
6. Para fechas usa `java.util.Date` en el POJO; Firestore lo convierte a `Timestamp` automáticamente.
7. Maneja errores con `addOnFailureListener` y muestra `Toast` o `Snackbar` al usuario.
8. Para Google Sign-In: obtén el `default_web_client_id` automáticamente desde `R.string` (lo genera google-services.json).
9. Comenta el código en español cuando ayude a entender.
10. Importante: `golesA` y `golesB` son `long` en Java porque Firestore deserializa todos los números enteros como `Long` (int64). NO usar `int`.

---

## Orden de generación recomendado

1. `Pronostico.java` + `EstadoPronostico.java`
2. `strings.xml`, `colors.xml`, `themes.xml`
3. `LoginActivity` + `activity_login.xml`
4. `RegisterActivity` + `activity_register.xml`
5. `MainActivity` + `activity_main.xml` + `bottom_nav_menu.xml` + `nav_graph.xml`
6. `PronosticosFragment` + `PronosticoAdapter` + layouts (`fragment_pronosticos.xml`, `item_pronostico.xml`)
7. `RegistrarPronosticoActivity` + `activity_registrar_pronostico.xml`
8. `EditarPronosticoActivity` + `activity_editar_pronostico.xml`
9. `EstadisticasFragment` + `fragment_estadisticas.xml`
10. `PronosticoRepository` (opcional — puedes inyectar el código directo en activities/fragments)

Genera los archivos en orden, paso a paso, y avísame antes de continuar al siguiente bloque para que pueda revisar.

---

## Notas finales

- **Índice compuesto en Firestore**: la primera vez que se ejecute la query de la lista, Firestore lanzará un error con un link directo en Logcat para crear el índice `userId` (ASC) + `fechaPartido` (DESC). Es normal, se crea con un click.
- **GitHub Sign-In en emulador**: a veces falla la redirección OAuth en emulador; funciona bien en dispositivo físico.
- **SHA-1**: ya está registrado en Firebase Console para el debug keystore actual.
