<p align="center">
  <img src="cloudpad_logo.png" alt="CloudPad Logo" width="180"/>
</p>

<h1 align="center">CloudPad</h1>

<p align="center">
  <b>Aplicação Android para gestão de notas sincronizadas na nuvem em tempo real</b>
</p>

<p align="center">
  Desenvolvida em <b>Kotlin</b> com <b>Jetpack Compose</b>, arquitetura <b>MVVM</b> e <b>Google Firebase (BaaS)</b>.
</p>

---

## 🎯 Conformidade com os Requisitos Firebase BaaS

Esta aplicação implementa integralmente os requisitos para utilização do **Firebase como Backend-as-a-Service (BaaS)** em Android:

| Requisito | Implementação no CloudPad |
| :--- | :--- |
| **Configuração do projeto** | `google-services.json` em `app/`, Firebase BOM (`platform(libs.firebase.bom)`), plugin Gradle `google-services`. |
| **Autenticação de Utilizadores** | Firebase Auth com Email/Password e Google Sign-In; gestão de sessão e obtenção de `currentUser?.uid`. |
| **Modelação Cloud Firestore** | Arquitetura hierárquica completa com **Coleções**, **Documentos** e **Subcoleções** (`users/{userId}/notes/{noteId}`). |
| **Leitura e Escrita de Dados** | Criação, listagem em tempo real, atualização reativa de campos (com *debounce* de auto-save) e eliminação. |
| **Sincronização em Tempo Real** | `addSnapshotListener` na subcoleção de notas para sincronização instantânea de UI entre dispositivos. |
| **Associação de Dados por UID** | Cada documento armazena `userId` correspondente ao `currentUser.uid` e localiza-se na subcoleção do próprio utilizador. |

---

## 🚀 Funcionalidades

* 🔐 **Autenticação Completa:**
  * Início de sessão e criação de conta com Email e Palavra-passe via Firebase Auth.
  * Início de sessão rápido com a conta **Google (Google Sign-In)**.
  * Validações de formulário e mapeamento amigável de erros do Firebase em português.
  * Proteção de rotas no `MainActivity` consoante `currentUser != null`.
* 📝 **Gestão de Notas em Tempo Real:**
  * Subcoleção de notas escutada ativamente com `addSnapshotListener`.
  * Isolamento de dados por utilizador (cada utilizador gere as suas próprias notas).
  * Ordenação dinâmica das notas pela data da última edição (`updatedAt`).
  * Eliminação instantânea com reflexo reativo na interface.
* ⚡ **Auto-Save Inteligente com Debounce:**
  * Ao digitar o título ou conteúdo no editor, as modificações são salvas de forma automática no Firestore via `SetOptions.merge()`.
  * Mecanismo de debounce de 500ms utilizando Kotlin Coroutines (`Job.cancel()` e `delay(500)`).
  * Indicador de estado em tempo real no TopBar (*"A guardar..."* / *"Guardado na nuvem"*).
* 🎨 **Interface Moderna e Modular:**
  * Desenvolvida 100% em **Jetpack Compose** e **Material 3**.
  * Componentes modulares e reutilizáveis (`MyTopBar`, `NoteCard`).
  * Padrão *Stateful vs Stateless* com suporte a Android Studio Previews.

---

## 🗄️ Modelação de Dados no Cloud Firestore (Subcoleções)

A estrutura de dados foi desenhada aproveitando a capacidade de **Subcoleções** do Cloud Firestore para garantir segurança e isolamento por utilizador:

```text
📁 users (Collection)
 └── 📄 {userId} (Document)
      └── 📁 notes (Subcollection)
           └── 📄 {noteId} (Document)
                ├── id: String ("...")
                ├── userId: String ("<uid_do_utilizador>")
                ├── title: String ("Exemplo de Título")
                ├── content: String ("Conteúdo da nota...")
                └── updatedAt: Timestamp (FieldValue.serverTimestamp())
```

### Vantagens desta Arquitetura:
1. **Subcoleção Dedicada:** As notas pertencem diretamente ao caminho do utilizador autenticado (`/users/{userId}/notes/{noteId}`), eliminando a necessidade de consultas complexas e reduzindo custos de leitura.
2. **Associação Explícita:** Cada documento mantém o campo `userId` com o UID do criador para verificação e integridade dos dados.
3. **Segurança Reforçada:** Permite regras de segurança Firestore simples e à prova de fugas:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/notes/{noteId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem:** [Kotlin](https://kotlinlang.org/)
* **Interface:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Navegação:** [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
* **Arquitetura:** MVVM (*Model-View-ViewModel*) com `StateFlow` e Coroutines
* **Backend (BaaS):** 
  * [Firebase Authentication](https://firebase.google.com/docs/auth) (Email/Password & Google Sign-In)
  * [Cloud Firestore](https://firebase.google.com/docs/firestore) (Base de dados NoSQL com subcoleções e listeners em tempo real)
* **Build System:** Gradle (Kotlin DSL) com Version Catalog (`libs.versions.toml`)

---

## 📂 Estrutura do Projeto

```text
app/src/main/java/upca/example/cloudpad/
│
├── MainActivity.kt               # Ponto de entrada, verificação de auth e NavHost
│
├── models/
│   └── Note.kt                   # Modelo de dados da nota (id, userId, title, content, updatedAt)
│
├── ui/
│   ├── auth/
│   │   ├── AuthScreen.kt         # Ecrã de Login e Registo (com Google Sign-In)
│   │   └── AuthViewModel.kt      # Lógica de autenticação com Firebase Auth
│   │
│   ├── screens/
│   │   ├── noteList/
│   │   │   ├── NotesListScreen.kt    # Ecrã de listagem das notas do utilizador
│   │   │   └── NoteListViewModel.kt  # Listener em tempo real (addSnapshotListener) na subcoleção
│   │   │
│   │   └── noteEdit/
│   │       ├── NoteEditorScreen.kt   # Ecrã de escrita com Auto-Save
│   │       └── NoteEditorViewModel.kt# ViewModel com auto-save debounce na subcoleção do utilizador
│   │
│   ├── components/
│   │   ├── MyTopBar.kt           # Barra superior reutilizável com status de auto-save
│   │   └── NoteCard.kt           # Cartão visual da nota para a grelha
│   │
│   └── theme/                    # Cores, tipografia e tema Material 3
```

---

## ⚙️ Configuração e Execução

### 1. Clonar ou Abrir o Projeto
Abra a pasta do projeto no **Android Studio** (versão Ladybug / Iguana ou superior recomendada).

### 2. Configurar o Firebase
1. Aceda ao [Firebase Console](https://console.firebase.google.com/) e crie um projeto.
2. Adicione uma aplicação Android com o package:
   ```text
   upca.example.cloudpad
   ```
3. Descarregue o ficheiro **`google-services.json`** e coloque-o na pasta `app/`:
   ```text
   CloudPad/app/google-services.json
   ```
4. Na consola do Firebase:
   * **Authentication:** Ative os fornecedores **E-mail/palavra-passe** e **Google**.
   * **Cloud Firestore:** Crie a base de dados e configure as regras de segurança para a subcoleção (`users/{userId}/notes/{noteId}`).
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
