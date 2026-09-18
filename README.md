# Agent OS (Android)

Autonomous enterprise AI assistants and workflow execution platform built with Kotlin and Jetpack Compose.

## Getting Started

Follow these steps to set up and run the project locally:

1. **Clone the repository:**
   ```bash
   git clone <repository_url>
   cd <repository_directory>
   ```

2. **Configure environment variables:**
   Copy the example environment file to `.env`:
   ```bash
   cp .env.example .env
   ```

3. **Add your API credentials locally (optional):**
   Open `.env` and uncomment the `GEMINI_API_KEY` line, providing your Gemini API key:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
   *(Note: The app works out of the box with the built-in autonomous execution engine even without a live API key.)*

4. **Build and run the application:**
   Open the project in Android Studio (Giraffe / Hedgehog / Ladybug or later) or run from the command line:
   ```bash
   ./gradlew assembleDebug
   ```

## Security & Privacy Guardrails

- **Zero Hardcoded Secrets**: All private credentials and environment configurations are strictly read via Gradle Secrets Plugin (`.env`) and injected via `BuildConfig` at compile time.
- **Git Protection**: `.env`, debug and upload keystores (`*.jks`, `*.keystore`), and service account files are explicitly excluded in `.gitignore`.
- **Human-in-the-Loop Safeguards**: High-impact actions require explicit user approval before execution.
