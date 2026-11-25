<p align="center">
  <img src="cloudpad_logo.png" alt="CloudPad Logo" width="160"/>
</p>

<h1 align="center">CloudPad</h1>

<p align="center">
  <b>Sistema de Gestão de Notas em Tempo Real para Plataforma Android</b><br>
  Construído com Kotlin, Jetpack Compose, Arquitetura MVVM e Firebase (BaaS)
</p>

---

## 1. Visão Geral

O **CloudPad** é uma aplicação móvel nativa desenvolvida para a plataforma Android com foco em sincronização distribuída, persistência na nuvem em tempo real e experiência de utilizador reativa. O sistema adota o paradigma **Backend-as-a-Service (BaaS)** através do ecossistema Google Firebase, suportando autenticação com múltiplos fornecedores e modelação hierárquica NoSQL com Cloud Firestore.

---

## 2. Conformidade com os Requisitos Técnicos (Firebase BaaS)

A solução foi estruturada para cumprir integralmente as normas e especificações técnicas de integração com Firebase em aplicações Android:

| Requisito Técnico | Detalhes de Implementação no CloudPad |
| :--- | :--- |
| **Configuração do Projeto** | Integração de `google-services.json` no módulo `:app`, utilização do Firebase Bill of Materials (`firebase-bom`) e aplicação do plugin `com.google.gms.google-services`. |
| **Autenticação de Utilizadores** | Firebase Authentication via Email/Password e Google Sign-In; gestão de sessão autenticada e extração do identificador de utilizador através de `currentUser?.uid`. |
| **Modelação Cloud Firestore** | Modelação hierárquica baseada em **Coleções**, **Documentos** e **Subcoleções** (`users/{userId}/notes/{noteId}`). |
| **Operações de Leitura e Escrita** | Fluxo CRUD completo: criação de notas, listagem reativa, atualização pontual de campos com mecanismo de *debounce* de 500ms e eliminação de registos. |
| **Sincronização em Tempo Real** | Utilização de `addSnapshotListener` acoplado à subcoleção do utilizador, sincronizando instantaneamente o estado da UI entre múltiplos dispositivos. |
| **Associação de Dados por UID** | Atribuição do campo `userId` a cada documento e isolamento físico das notas na subcoleção associada ao `currentUser.uid`. |

---

## 3. Arquitetura de Modelação de Dados (Cloud Firestore)

A arquitetura de persistência baseia-se numa estrutura hierárquica de subcoleções NoSQL, garantindo integridade referencial, isolamento multi-inquilino (*multi-tenant*) e escalabilidade operacional.

### 3.1. Hierarquia de Dados

```text
[Coleção] users
   │
   └── [Documento] {userId}
          │
          └── [Subcoleção] notes
                 │
                 └── [Documento] {noteId}
                        ├── id: String
                        ├── userId: String
                        ├── title: String
                        ├── content: String
                        └── updatedAt: Timestamp
```

### 3.2. Dicionário de Dados do Documento (`Note`)

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | `String` | Identificador único alfanumérico gerado pelo Firestore para o documento. |
| `userId` | `String` | UID do utilizador proprietário autenticado via Firebase Auth. |
| `title` | `String` | Título da nota atribuído pelo utilizador. |
| `content` | `String` | Corpo textual e conteúdo integral da nota. |
| `updatedAt` | `Timestamp` | Carimbo de data/hora no servidor (`FieldValue.serverTimestamp()`) para ordenação temporal. |

### 3.3. Regras de Segurança e Controlo de Acesso

A estrutura em subcoleção permite restringir o acesso diretamente pelo caminho do documento, assegurando que nenhum utilizador consegue ler ou modificar dados pertencentes a terceiros:

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

## 4. Funcionalidades Principais

### Autenticação e Controlo de Acesso
* Registo e início de sessão com Email e Palavra-passe através do Firebase Auth.
* Suporte nativo para autenticação federada com Google Sign-In.
* Validação de campos no cliente (formato de email e requisitos mínimos de palavra-passe).
* Mapeamento completo de códigos de exceção do Firebase para mensagens amigáveis em português.
* Navegação reativa condicionada pelo estado de autenticação (`currentUser != null`).

### Gestão e Sincronização de Notas
* Consulta contínua à subcoleção via `addSnapshotListener`, refletindo alterações em tempo real na interface sem necessidade de atualização manual (*pull-to-refresh*).
* Ordenação dinâmica das notas por ordem cronológica decrescente da última modificação.
* Eliminação de notas com atualização reativa e imediata da grelha.

### Mecanismo de Gravação Automática com Debounce
* Deteção contínua de alterações nos campos de texto do editor.
* Cancelamento e reescalonamento da rotina de gravação via Kotlin Coroutines com atraso de 500ms (*debounce*).
* Persistência na Cloud Firestore através do método `set` com a opção `SetOptions.merge()`, reduzindo acessos redundantes à rede.
* Notificação visual de estado na barra de navegação (`A guardar...`, `Guardado na nuvem`).

---

## 5. Arquitetura de Software

O projeto adota os princípios de arquitetura recomendados pela Google para desenvolvimento moderno em Android:

* **Padrão de Apresentação:** Model-View-ViewModel (MVVM).
* **Gestão de Estado:** Unidirectional Data Flow (UDF) com `StateFlow` e `MutableStateFlow`.
* **Programação Concorrente:** Kotlin Coroutines (`viewModelScope`) para tarefas assíncronas em segundo plano.
* **Interface Gráfica:** 100% declarativa em Jetpack Compose com Material Design 3.
* **Navegação:** Navigation Compose com tipagem de rotas e argumentos.

---

## 6. Estrutura do Código-Fonte

```text
app/src/main/java/upca/example/cloudpad/
│
├── MainActivity.kt                      # Ponto de entrada, controlo de sessão e rotas
│
├── models/
│   └── Note.kt                          # Entidade de dados da nota
│
├── ui/
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── AuthScreen.kt            # Interface de autenticação (Email e Google)
│   │   │   └── AuthViewModel.kt         # Lógica de negócio de autenticação
│   │   │
│   │   ├── noteList/
│   │   │   ├── NotesListScreen.kt       # Interface de listagem em grelha
│   │   │   └── NoteListViewModel.kt     # Listener em tempo real da subcoleção
│   │   │
│   │   └── noteEdit/
│   │       ├── NoteEditorScreen.kt      # Interface do editor com auto-save
│   │       └── NoteEditorViewModel.kt   # Lógica de gravação com debounce
│   │
│   ├── components/
│   │   ├── MyTopBar.kt                  # Barra superior modular com estado de gravação
│   │   └── NoteCard.kt                  # Componente de cartão para exibição na grelha
│   │
│   └── theme/
│       ├── Color.kt                     # Paleta de cores do sistema
│       ├── Theme.kt                     # Configuração do tema Material 3 (Claro/Escuro)
│       └── Type.kt                      # Tipografia do sistema
```

---

## 7. Instruções de Configuração e Instalação

### 7.1. Pré-requisitos
* Android Studio (versão Ladybug, Iguana ou superior recomendada).
* JDK 17 ou superior configurado.
* Dispositivo físico Android com modo de depuração ativado ou Emulador com Google Play Services (API 24 ou superior).

### 7.2. Configuração do Firebase
1. Crie um projeto na [Consola do Firebase](https://console.firebase.google.com/).
2. Registe uma aplicação Android com o nome de pacote:
   ```text
   upca.example.cloudpad
   ```
3. Obtenha a impressão digital SHA-1 do ambiente de desenvolvimento executando o comando:
   ```bash
   ./gradlew signingReport
   ```
4. Adicione a chave SHA-1 nas definições da aplicação na Consola do Firebase.
5. Ative os métodos de início de sessão em **Authentication**:
   * E-mail/Palavra-passe
   * Google
6. Inicialize a base de dados em **Cloud Firestore** e aplique as regras de segurança descritas na secção 3.3.
7. Descarregue o ficheiro de configuração `google-services.json` e coloque-o no diretório:
   ```text
   CloudPad/app/google-services.json
   ```

### 7.3. Compilação e Execução
1. Abra o diretório do projeto no Android Studio.
2. Execute a sincronização dos ficheiros de construção através do botão **Sync Project with Gradle Files**.
3. Selecione o dispositivo de destino e inicie a execução através da opção **Run** (`Shift + F10`).
