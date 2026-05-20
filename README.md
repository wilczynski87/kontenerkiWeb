This is a Kotlin Multiplatform project targeting Android, Web.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Web Application

To build and run the development version of the web app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
  ```

### Docker (production web build)

From the repository root:

```shell
docker compose up --build
```

The app is served on [http://localhost:8080](http://localhost:8080) (override with `PORT=3000 docker compose up`).

**Common build failures**

- **OutOfMemoryError / Gradle daemon exited** — increase Docker memory to at least 4 GB (Docker Desktop → Settings → Resources).
- **`path "../KONTENERKIAPI" not found`** — use root `docker-compose.yml`, or start only the web service: `docker compose -f docker/docker-compose.yml up --build kotlin-wasm-app` (do not enable the `api` profile unless that repo exists next to this project).
- **`COPY ... productionExecutable: not found`** — the Wasm build failed in the builder stage; scroll up in the build log for the first Gradle error.

Optional API container (sibling repo required):

```shell
docker compose -f docker/docker-compose.yml --profile api up --build
```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).