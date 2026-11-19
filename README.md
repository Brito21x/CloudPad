<p align="center">
  <img src="cloudpad_logo.png" alt="CloudPad Logo" width="180"/>
</p>

<h1 align="center">CloudPad</h1>

<p align="center">
  <b>Aplicação Android para gestão de notas sincronizadas na nuvem em tempo real</b>
</p>

<p align="center">
  Desenvolvida em <b>Kotlin</b> com <b>Jetpack Compose</b>, arquitetura <b>MVVM</b> e <b>Google Firebase</b>.
</p>

---

## 🚀 Funcionalidades

* 🔐 **Autenticação Completa:**
  * Início de sessão e criação de conta com Email e Palavra-passe.
  * Início de sessão rápido com a conta **Google (Google Sign-In)**.
  * Botão de alternância para ver/ocultar a palavra-passe.
  * Tratamento de erros detalhado na interface.
* 📝 **Gestão de Notas em Tempo Real:**
  * Lista sincronizada instantaneamente com a Cloud Firestore (`addSnapshotListener`).
  * Cada utilizador tem acesso apenas às suas próprias notas.
  * Eliminação de notas com atualização imediata da interface.
* ⚡ **Auto-Save Inteligente:**
  * Ao escrever no editor (título ou conteúdo), as alterações são **guardadas automaticamente** na nuvem com um mecanismo de *debounce* (500ms).
  * Feedback visual dinâmico na barra superior (*"A guardar..."* / *"Guardado na nuvem"*).
* 🎨 **Interface Moderna e Modular:**
  * Construída com **Jetpack Compose** e **Material 3**.
  * Componentes reutilizáveis na pasta `components/` (`MyTopBar`, `NoteCard`).
  * Separação rigorosa entre componentes *Stateful* e *Stateless* com suporte total a **Previews** no Android Studio.

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** [Kotlin](https://kotlinlang.org/)
* **Interface:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Navegação:** [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
* **Arquitetura:** MVVM (*Model-View-ViewModel*) com `StateFlow` e Coroutines
* **Backend:** 
  * [Firebase Authentication](https://firebase.google.com/docs/auth) (Email/Password & Google Sign-In)
  * [Cloud Firestore](https://firebase.google.com/docs/firestore) (Base de dados NoSQL em tempo real)
* **Build System:** Gradle (Kotlin DSL) com Version Catalog (`libs.versions.toml`)

---

## 📂 Estrutura do Projeto

```text
app/src/main/java/upca/example/cloudpad/
│
├── MainActivity.kt               # Ponto de entrada e configuração do NavHost
│
├── models/
│   └── Note.kt                   # Modelo de dados da nota (id, userId, title, content, updatedAt)
│
├── ui/
│   ├── auth/
│   │   ├── AuthScreen.kt         # Ecrã de Login e Registo (com Google Sign-In)
│   │   └── AuthViewModel.kt      # Lógica de autenticação com Firebase Auth
│   │
│   ├── notes/
│   │   ├── NotesListScreen.kt    # Ecrã de listagem das notas do utilizador
│   │   ├── NoteListViewModel.kt  # ViewModel para listar, ordenar e apagar notas
│   │   ├── NoteEditorScreen.kt   # Ecrã de escrita com Auto-Save
│   │   └── NoteEditorViewModel.kt# ViewModel com debounce e gravação na Firestore
│   │
│   ├── components/
│   │   ├── MyTopBar.kt           # Barra superior reutilizável com navegação automática
│   │   └── NoteCard.kt           # Cartão visual da nota para a grelha
│   │
│   └── theme/                    # Definições de cores, tipografia e tema Material 3
```

---

## ⚙️ Configuração e Execução

### 1. Clonar ou Abrir o Projeto
Abra a pasta do projeto no **Android Studio** (versão Ladybug / Iguana ou superior recomendada).

### 2. Configurar o Firebase
1. Aceda ao [Firebase Console](https://console.firebase.google.com/) e crie um projeto (ou utilize um existente).
2. Adicione uma aplicação Android com o pacote:
   ```text
   upca.example.cloudpad
   ```
3. Descarregue o ficheiro **`google-services.json`** e coloque-o na pasta `app/`:
   ```text
   CloudPad/app/google-services.json
   ```
4. Na consola do Firebase:
   * **Authentication:** Ative os fornecedores **E-mail/palavra-passe** e **Google**.
   * **Cloud Firestore:** Crie a base de dados em modo de teste ou configure as regras de segurança adequadas.
5. **Para o Google Sign-In funcionar:**
   * Obtenha a impressão digital **SHA-1** da sua máquina executando no terminal:
     ```bash
     ./gradlew signingReport
     ```
   * No Firebase Console > **Definições do projeto** > **Seus aplicativos** > Adicione a impressão digital **SHA-1**.

### 3. Executar
1. Sincronize o Gradle (**Sync Project with Gradle Files**).
2. Conecte um telemóvel Android físico (via USB/Wi-Fi) ou inicie um emulador.
3. Clique em **Run** ▶️ no Android Studio.
