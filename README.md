# AndinaSalud 🩺

> Gestión de citas médicas para el paciente **P-0417 — Lucía Quispe Mamani**.
> Una sola base de código Kotlin, dos plataformas: **Android e iOS**.

---

## ✨ ¿Qué incluye?

- **5 pantallas** funcionales: Inicio, Citas, Detalle, Solicitud y Perfil.
- **5 reglas de negocio** (RN-01 … RN-05) implementadas en el dominio y cubiertas por pruebas.
- **Buscador inteligente**: filtra por especialidad u médico, sin que importen tildes ni mayúsculas.
- **Tema oscuro** activable desde el perfil.
- **76 pruebas automatizadas** pasando en verde.
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
./gradlew :shared:testAndroidHostTest     # 76 tests en verde
```

> Nota: la suite iOS (`iosSimulatorArm64Test`) solo se ejecuta en **macOS**.

## 📱 iOS (pendiente)

El proyecto está generado y listo, pero compilar requiere macOS con Xcode:

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator
```

---

Hecho con Kotlin Multiplatform ♥ por `pe.edu.upeu.andinasalud`.