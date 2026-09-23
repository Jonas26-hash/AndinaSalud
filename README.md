# AndinaSalud 🩺

> Gestión de citas médicas para el paciente **P-0417 — Lucía Quispe Mamani**.
> Una sola base de código Kotlin, dos plataformas: **Android e iOS**.

---

## ✨ ¿Qué incluye?

- **5 pantallas** funcionales: Inicio, Citas, Detalle, Solicitud y Perfil.
- **5 reglas de negocio** (RN-01 … RN-05) implementadas en el dominio y cubiertas por pruebas.
- **Buscador inteligente**: filtra por especialidad u médico, sin que importen tildes ni mayúsculas.
- **Tema oscuro** activable desde el perfil.
- **82 pruebas automatizadas** pasando en verde.
- **Cero base de datos, cero red**: toda la información vive en memoria, de pura arquitectura.

## 🧱 Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.4.10 |
| UI compartida | Compose Multiplatform 1.11.1 + Material 3 |
| DI | Koin 4.2.2 |
| Async | coroutines 1.10.2 |
| Tiempo | kotlinx-datetime 0.7.1 |
| Navegación | navigation-compose 2.9.2 |
| Android | AGP 9.0.1 · minSdk 24 · target 36 |
| Gradle | 9.1.0 (JVM 21) |

## 🗂️ Estructura (Clean Architecture)

```
shared/
├── commonMain/…/domain          Modelos, repositorio (interfaz) y casos de uso
│   ├── model/                   Cita, EstadoCita, Paciente, Medico, Sede
│   ├── repository/              CitaRepository (interfaz)
│   └── usecase/                 ObtenerCitas · SolicitarCita · CancelarCita
├── commonMain/…/data            Implementación simulada en memoria
│   ├── local/                   CitasSimuladas (seed)
│   └── repository/              CitaRepositoryFake (con delays)
├── commonMain/…/presentation    Pantallas, ViewModels, navegación y tema
├── commonMain/…/di              AppModule (Koin)
└── commonTest/                  Fakes y suites de prueba
androidApp/                      App Android (MainActivity + MainApplication)
iosApp/                          App iOS (SwiftUI, embebe el framework Shared)
```

## 🧠 Reglas de negocio

| Regla | Descripción |
|---|---|
| **RN-01** | La fecha y hora de la cita deben ser futuras. |
| **RN-02** | Máximo 3 citas **Programadas** simultáneas. |
| **RN-03** | Cancelar solo una cita Programada y faltando más de 24 h. |
| **RN-04** | El motivo debe tener entre 10 y 200 caracteres. |
| **RN-05** | Sin duplicados de cita Programada en la misma fecha y hora. |

## 🚀 Cómo ejecutar en Android

```bash
./gradlew :androidApp:assembleDebug
# APK → androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

Instálalo en tu dispositivo:

```bash
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

## 🧪 Cómo correr los tests

```bash
./gradlew :shared:testAndroidHostTest     # 82 tests en verde
```

> Nota: la suite iOS (`iosSimulatorArm64Test`) solo se ejecuta en **macOS**.

## 🔁 Flujo de datos

```
Pantalla (Compose)
   │  UiState → recibe estado; Lambdas → emite intención
   ▼
ViewModel (StateFlow + MutableStateFlow privado)
   │  llama
   ▼
Caso de uso (reglas RN-01…RN-05 en el dominio)
   │  llama
   ▼
CitaRepository (interfaz, en domain) ──> CitaRepositoryFake (en data, en memoria)
```

- La UI **nunca** toca `CitasSimuladas` ni el repositorio: solo pasa por el ViewModel.
- El retardo (800 ms) se simula con `delay()` dentro del repositorio; no hay red ni base de datos.

## 💉 Inyección de dependencias (Koin)

`AppModule` registra, en `commonMain` y disponible para ambas plataformas:

| Registro | Tipo |
|---|---|
| `single<CitaRepository>` | `CitaRepositoryFake()` (en memoria) |
| `factory` | `ObtenerCitasUseCase`, `SolicitarCitaUseCase`, `CancelarCitaUseCase` |
| `viewModel` | `InicioViewModel`, `CitasViewModel`, `DetalleCitaViewModel`, `SolicitudViewModel`, `PerfilViewModel` |

- **Android**: se arranca en `MainApplication` con `androidLogger` + `androidContext`.
- **iOS**: se arranca con `KoinIosKt.doInitKoinIos()` en el `init()` de `iOSApp`.

## 📱 iOS

Configurado para dispositivos **ARM64** y simulador **ARM64** en el módulo `shared`
(targets `iosArm64` + `iosSimulatorArm64`, framework estático `Shared`). El `iosApp`
embebe el framework con `embedAndSignAppleFrameworkForXcode`.

### Compilación en macOS (local)

```bash
# 1. Compilar el framework compartido (KMP)
./gradlew :shared:embedAndSignAppleFrameworkForXcode

# 2. Abrir el proyecto en Xcode y compilar para simulador
cd iosApp
xcodebuild -project iosApp.xcodeproj \
  -scheme iosApp \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 17' \
  -configuration Debug \
  build \
  CODE_SIGNING_ALLOWED=NO \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO
```

### Compilación mediante GitHub Actions (CI)

El workflow `.github/workflows/ios-build.yml` se ejecuta automáticamente en cada push a `main` o `develop`:

1. Compila el framework `Shared` con Gradle (`:shared:embedAndSignAppleFrameworkForXcode`)
2. Detecta automáticamente un simulador iPhone ARM64 disponible en el runner macOS
3. Compila la app iOS con `xcodebuild` para simulador (sin firma: `CODE_SIGNING_ALLOWED=NO`)
4. Sube logs como artifacts (`gradle-build-log`, `ios-build-log`)

**Ver runs**: https://github.com/Jonas26-hash/AndinaSalud/actions/workflows/ios-build.yml

### Diferencia: Compilación vs Ejecución real

| Aspecto | Compilación (CI / `xcodebuild build`) | Ejecución real en dispositivo |
|---------|----------------------------------------|------------------------------|
| **Qué hace** | Verifica que el código compila y linkea correctamente | Instala y ejecuta la app en hardware |
| **Firma** | No requerida (`CODE_SIGNING_ALLOWED=NO`) | Requiere Apple Developer Program ($99/año) + certificado + provisioning profile |
| **Simulador** | Funciona en CI (macOS runners de GitHub) | Requiere macOS local + Xcode |
| **Dispositivo físico** | No | Sí, solo con certificado válido |
| **Estado actual** | ✅ **Verificado en CI** (simulador ARM64) | ❌ No verificado (requiere cuenta Apple Developer) |

> **Nota honesta**: El proyecto compila correctamente para simulador ARM64 (evidencia en GitHub Actions). La ejecución en dispositivo físico real no se ha verificado por requerir cuenta de desarrollador Apple de pago. Desde Windows no es posible compilar iOS.

---

Hecho con Kotlin Multiplatform ♥ por `pe.edu.upeu.andinasalud`.