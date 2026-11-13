# Adventure Book

Interactive adventure book: pick a book, read a section, choose what your character does next, and try to
reach an ending before you run out of health.

The repository has three modules:

| Module | Stack | Port |
|---|---|---|
| [`adventure-book-api`](adventure-book-api) | Java 21, Spring Boot 3.5, Maven | `8080` (context path `/api`) |
| [`adventure-book-front-angular`](adventure-book-front-angular) | Angular 18, Angular Material | `4200` |
| [`adventure-book-front-react`](adventure-book-front-react) | React 18, TypeScript, Fluent UI | `3000` |

Both frontends implement the same game and talk to the same API.

## Contents

- [How to play](#how-to-play)
- [Quick start (scripts)](#quick-start-scripts)
- [Run each module manually](#run-each-module-manually)
- [Production builds](#production-builds)
- [API reference](#api-reference)

## How to play

1. Open the Angular app (http://localhost:4200) or the React app (http://localhost:3000).
2. Select a book in the dropdown. Only valid books are listed.
3. You start at the book's beginning with **10 health points**.
4. Read the section and click an option to move to the next section.
5. Some options have a consequence that changes your health and shows what happened: losing or regaining health,
   a permanent boost or poison on your maximum health, or instant death (see
   [Consequence types](#consequence-types)).
6. When your health reaches **0**, you die and the game is over: click **Start a new game**.
7. When you reach an ending section, you win: click **Play again**.

**Restart** (top right) starts the current book again at any time. Picking another book starts a new game.

## Quick start (scripts)

Two scripts start the API and both frontends with one command:

### Windows

Run from the repository root, in Command Prompt or PowerShell:

```bat
run-windows.bat
```

- Each server opens in its **own console window**, so you can read its logs. The script returns once all servers
  are ready.
- Stop all servers with `run-windows.bat stop`, or close their windows.
- Only the frontends, when the API already runs in your IDE: `run-windows.bat start angular react`.
- Smoke test: `run-windows.bat check`.
- In PowerShell, prefix the script with `.\`: `.\run-windows.bat`.
- The script keeps the PID of each window in `%TEMP%\adventure-book\` so that `stop` only closes what it started.

### macOS

Run from the repository root, in Terminal:

```bash
chmod +x run-mac.sh   # once
./run-mac.sh
```

- All servers run **in this terminal**, each log line prefixed with the server name (`api |`, `angular |`, `react |`).
- **Ctrl+C** stops all servers.
- Only the frontends, when the API already runs in your IDE: `./run-mac.sh start angular react`.
- Smoke test: `./run-mac.sh check`.
- Without the execute bit, run it with `bash run-mac.sh`. The script also works on Linux and with the bash 3.2
  shipped with macOS.
- To use JDK 21 when another JDK is the default: `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`.

## Run each module manually

Start the API first, then one or both frontends, each in its own terminal.

### API

```bash
cd adventure-book-api
mvn spring-boot:run
```

Or run the built jar:

```bash
cd adventure-book-api
mvn package
java -jar target/adventure-book-api-0.0.1-SNAPSHOT.jar
```

Check that it answers: http://localhost:8080/api/books

### Angular app

```bash
cd adventure-book-front-angular
npm ci
npm start
```
Open http://localhost:4200

### React app

```bash
cd adventure-book-front-react
npm ci
npm start
```
Open http://localhost:3000.

## Production builds

| Module | Command | Output |
|---|---|---|
| API | `mvn package` in `adventure-book-api` | `adventure-book-api/target/adventure-book-api-0.0.1-SNAPSHOT.jar` |
| Angular | `npm run build` in `adventure-book-front-angular` | `adventure-book-front-angular/dist/adventure-book-front-angular/browser/` |
| React | `npm run build` in `adventure-book-front-react` | `adventure-book-front-react/build/` |

The frontends are static files after the build. Serve them from any web server, and add that server's origin to
the API's CORS config (see [Configuration](#configuration)).

## API reference

Base URL: `http://localhost:8080/api`

| Method | Path | Returns |
|---|---|---|
| `GET` | `/books` | The **valid** books: `[{ "id": 1, "title": "The Prisoner" }, { "id": 4, "title": "The Prisoner: The Long Night" }]` |
| `GET` | `/books/{id}` | The full book: id, title, author and sections with their options and consequences |
| `GET` | `/actuator/health` | `{ "status": "UP" }` |

`GET /books/{id}` returns any available book, even an invalid one; the list only shows valid books.
An unknown id returns `404`:

```json
{ "code": "404 NOT_FOUND", "message": "Book not found! ID: 99" }
```

Examples:

```bash
curl http://localhost:8080/api/books
```

```bash
curl http://localhost:8080/api/books/1
```
