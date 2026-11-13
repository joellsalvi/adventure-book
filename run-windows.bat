@echo off
rem ============================================================================
rem  Adventure Book - start the API, the Angular app and the React app (Windows)
rem
rem    run-windows.bat [start ^| check ^| stop] [api] [angular] [react]
rem
rem    start   (default) start the servers, each in its own console window
rem    check   start the servers, wait until they all answer, then stop them
rem    stop    stop the servers started by this script
rem
rem  Without service names, all three are used. Example, when the API already
rem  runs in your IDE:   run-windows.bat start angular react
rem ============================================================================
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0"
set "ROOT=%ROOT:~0,-1%"
set "API_DIR=%ROOT%\adventure-book-api"
set "ANGULAR_DIR=%ROOT%\adventure-book-front-angular"
set "REACT_DIR=%ROOT%\adventure-book-front-react"
set "API_JAR=%API_DIR%\target\adventure-book-api-0.0.1-SNAPSHOT.jar"
set "PID_DIR=%TEMP%\adventure-book"
if not defined WAIT_SECONDS set "WAIT_SECONDS=300"

rem ---------------------------------------------------------------- arguments
set "ACTION=start"
set "RUN_API="
set "RUN_ANGULAR="
set "RUN_REACT="
set "SHOW_HELP="
set "BAD_ARG="
for %%a in (%*) do (
  set "KNOWN="
  for %%k in (start check stop) do if /i "%%~a"=="%%k" (set "ACTION=%%k" & set "KNOWN=1")
  for %%k in (api angular react) do if /i "%%~a"=="%%k" (set "RUN_%%k=1" & set "KNOWN=1")
  for %%k in (help -h --help) do if /i "%%~a"=="%%k" (set "SHOW_HELP=1" & set "KNOWN=1")
  if not defined KNOWN set "BAD_ARG=%%~a"
)
if defined SHOW_HELP (call :usage & exit /b 0)
if defined BAD_ARG (
  echo [run] ERROR: unknown argument "!BAD_ARG!".
  echo.
  call :usage
  exit /b 1
)
if not defined RUN_API if not defined RUN_ANGULAR if not defined RUN_REACT (
  set "RUN_API=1"
  set "RUN_ANGULAR=1"
  set "RUN_REACT=1"
)
if not exist "%PID_DIR%" mkdir "%PID_DIR%"

if /i "%ACTION%"=="stop" (
  call :stop_selected
  exit /b 0
)

rem ---------------------------------------------------------------- checks
call :check_prerequisites || exit /b 1
call :check_ports || exit /b 1
if defined RUN_API (call :prepare_api || exit /b 1)
if defined RUN_ANGULAR (call :install_if_needed "%ANGULAR_DIR%" Angular || exit /b 1)
if defined RUN_REACT (call :install_if_needed "%REACT_DIR%" React || exit /b 1)

rem ---------------------------------------------------------------- start
if defined RUN_API (
  set "LAUNCH_CMD=!API_CMD!"
  call :launch api API "%API_DIR%" || exit /b 1
)
if defined RUN_ANGULAR (
  set "LAUNCH_CMD=npm start"
  call :launch angular Angular "%ANGULAR_DIR%" || exit /b 1
)
if defined RUN_REACT (
  rem BROWSER=none: do not let create-react-app open a browser tab.
  set "BROWSER=none"
  set "LAUNCH_CMD=npm start"
  call :launch react React "%REACT_DIR%" || exit /b 1
)

rem ---------------------------------------------------------------- wait
set "FAILED="
if defined RUN_API call :wait_ready api API "http://localhost:8080/api/actuator/health" || set "FAILED=1"
if defined RUN_ANGULAR call :wait_ready angular Angular "http://localhost:4200/" || set "FAILED=1"
if defined RUN_REACT call :wait_ready react React "http://localhost:3000/" || set "FAILED=1"

echo.
if defined FAILED (
  echo [run] Some servers did not start. Check their windows for errors.
) else (
  echo [run] All servers are ready:
  if defined RUN_API echo [run]   API          http://localhost:8080/api/books
  if defined RUN_ANGULAR echo [run]   Angular app  http://localhost:4200
  if defined RUN_REACT echo [run]   React app    http://localhost:3000
)

if /i "%ACTION%"=="check" (
  echo [run] check: stopping the servers.
  call :stop_selected
) else (
  echo [run] Each server runs in its own window. Stop them all with: run-windows.bat stop
)
rem Exit at top level: "cmd /c" loses an exit code set by "if ... exit /b" inside a block.
if defined FAILED exit /b 1
exit /b 0


rem ============================================================================
rem  Subroutines
rem ============================================================================

:usage
echo Usage: run-windows.bat [start^|check^|stop] [api] [angular] [react]
echo.
echo   start   (default) start the servers, each in its own console window
echo   check   start the servers, wait until they all answer, then stop them
echo   stop    stop the servers started by this script
echo.
echo   Without service names, all three are used.
echo   Example (API already running in your IDE): run-windows.bat start angular react
echo.
echo   API      http://localhost:8080/api  (Maven if on PATH, else the built jar)
echo   Angular  http://localhost:4200
echo   React    http://localhost:3000
exit /b 0


:check_prerequisites
where curl >nul 2>nul || (echo [run] ERROR: curl.exe not found. It ships with Windows 10 1803+ and Windows 11. & exit /b 1)
where powershell >nul 2>nul || (echo [run] ERROR: Windows PowerShell not found. & exit /b 1)
if defined RUN_ANGULAR (call :check_node || exit /b 1) else if defined RUN_REACT (call :check_node || exit /b 1)
if defined RUN_API (call :check_java || exit /b 1)
exit /b 0


:check_node
where npm >nul 2>nul || (echo [run] ERROR: npm not found. Install Node.js 20 or 22 LTS from https://nodejs.org & exit /b 1)
set "NODE_MAJOR=0"
for /f "tokens=1 delims=v." %%v in ('node -v') do set "NODE_MAJOR=%%v"
if !NODE_MAJOR! LSS 18 (echo [run] ERROR: Node.js 18.19 or newer is required, found major version !NODE_MAJOR!. & exit /b 1)
exit /b 0


:check_java
set "JAVA=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA=%JAVA_HOME%\bin\java.exe"
"%JAVA%" -version >nul 2>nul || (echo [run] ERROR: Java not found. Install JDK 21 and set JAVA_HOME or add it to PATH. & exit /b 1)
"%JAVA%" -version 2>"%PID_DIR%\java-version.txt"
set "JAVA_VERSION="
for /f "usebackq tokens=3" %%v in ("%PID_DIR%\java-version.txt") do if not defined JAVA_VERSION set "JAVA_VERSION=%%~v"
set "JAVA_MAJOR=0"
for /f "delims=." %%m in ("!JAVA_VERSION!") do set "JAVA_MAJOR=%%m"
if !JAVA_MAJOR! LSS 21 (echo [run] ERROR: JDK 21 is required, found Java !JAVA_VERSION!. & exit /b 1)
exit /b 0


:check_ports
set "BUSY="
if defined RUN_API call :port_busy 8080 API
if defined RUN_ANGULAR call :port_busy 4200 Angular
if defined RUN_REACT call :port_busy 3000 React
if defined BUSY (
  echo [run] Stop whatever uses it first: run-windows.bat stop, or the server running in your IDE.
  exit /b 1
)
exit /b 0


:port_busy
netstat -ano | findstr /r /c:":%~1 .*LISTENING" >nul && (
  echo [run] ERROR: port %~1 ^(%~2^) is already in use.
  set "BUSY=1"
)
exit /b 0


:prepare_api
where mvn >nul 2>nul || goto :prepare_api_jar
rem Maven compiles the sources: Lombok 1.18.38 (managed by Spring Boot 3.5.5) needs JDK 21.
if not "!JAVA_MAJOR!"=="21" (
  echo [run] ERROR: building the API with Maven needs JDK 21, but Java !JAVA_VERSION! was found.
  echo [run]        Lombok does not run on newer JDKs. Point JAVA_HOME to a JDK 21, for example:
  echo [run]        set "JAVA_HOME=C:\Program Files\Java\jdk-21"
  exit /b 1
)
set "API_CMD=mvn spring-boot:run"
echo [run] API: Maven found, running from sources ^(mvn spring-boot:run^).
exit /b 0

:prepare_api_jar
if not exist "%API_JAR%" (
  echo [run] ERROR: Maven is not on the PATH and the API jar does not exist:
  echo [run]        %API_JAR%
  echo [run]        Install Maven 3.9+ or build the jar in your IDE ^(Maven "package" goal^).
  exit /b 1
)
powershell -NoProfile -Command "$jar = (Get-Item -LiteralPath $env:API_JAR).LastWriteTime; $newer = Get-ChildItem -LiteralPath (Join-Path $env:API_DIR 'src\main') -Recurse -File | Where-Object { $_.LastWriteTime -gt $jar } | Select-Object -First 1; if ($newer) { exit 1 } else { exit 0 }"
if errorlevel 1 (
  echo [run] WARNING: the API jar is older than the API sources, so recent changes ^(e.g. CORS^) may be missing.
  echo [run]          Rebuild it with "mvn package" in adventure-book-api, or the Maven "package" goal in your IDE.
)
set API_CMD="!JAVA!" -jar "%API_JAR%"
echo [run] API: Maven not found, running the built jar with java -jar.
exit /b 0


:install_if_needed
if exist "%~1\node_modules\" exit /b 0
echo [run] Installing %~2 dependencies ^(npm ci^)...
pushd "%~1"
call npm ci
set "NPM_RC=!errorlevel!"
popd
if not "!NPM_RC!"=="0" (echo [run] ERROR: npm ci failed for %~2. & exit /b 1)
exit /b 0


:launch
rem %1 = id (pid file name), %2 = label, %3 = working directory, LAUNCH_CMD = command.
rem Start-Process returns the PID of the new cmd.exe, kept for "run-windows.bat stop".
set "LAUNCH_ARGS=/k title Adventure Book - %~2 & !LAUNCH_CMD!"
set "LAUNCH_DIR=%~3"
set "LAUNCH_PID_FILE=%PID_DIR%\%~1.pid"
powershell -NoProfile -Command "$p = Start-Process -FilePath $env:ComSpec -ArgumentList $env:LAUNCH_ARGS -WorkingDirectory $env:LAUNCH_DIR -PassThru; Set-Content -LiteralPath $env:LAUNCH_PID_FILE -Value $p.Id"
if errorlevel 1 (echo [run] ERROR: could not start %~2. & exit /b 1)
echo [run] Started %~2 in a new window: !LAUNCH_CMD!
exit /b 0


:wait_ready
rem %1 = id (pid file name), %2 = label, %3 = URL that answers once the server is up.
echo [run] Waiting for %~2 at %~3 ...
set /a "WAITED=0"
set "WAIT_PID="
set /p WAIT_PID=<"%PID_DIR%\%~1.pid"
:wait_ready_loop
curl -sf -o nul --max-time 3 "%~3" >nul 2>nul && (echo [run] %~2 is ready. & exit /b 0)
if !WAITED! GEQ %WAIT_SECONDS% (
  echo [run] ERROR: %~2 did not answer within %WAIT_SECONDS% seconds. Check its window for errors.
  exit /b 1
)
rem The window stays open after a crash (cmd /k): fail fast once it runs nothing anymore.
if !WAITED! GTR 0 (
  powershell -NoProfile -Command "if (Get-CimInstance Win32_Process -Filter ('ParentProcessId=' + $env:WAIT_PID) | Where-Object { $_.Name -ne 'conhost.exe' }) { exit 0 } else { exit 1 }"
  if errorlevel 1 (
    echo [run] ERROR: %~2 stopped before it was ready. Check its window for the error.
    exit /b 1
  )
)
set /a "REMAINDER=WAITED %% 30"
if !WAITED! GTR 0 if !REMAINDER! EQU 0 echo [run] Still waiting for %~2 ^(!WAITED!s^)...
ping -n 3 127.0.0.1 >nul
set /a "WAITED+=2"
goto :wait_ready_loop


:stop_selected
if defined RUN_REACT call :stop_one react React
if defined RUN_ANGULAR call :stop_one angular Angular
if defined RUN_API call :stop_one api API
exit /b 0


:stop_one
rem Kills the cmd.exe window started by :launch and everything it runs (java, node).
set "PID_FILE=%PID_DIR%\%~1.pid"
if not exist "%PID_FILE%" (echo [run] %~2: not started by this script. & exit /b 0)
set "PID="
set /p PID=<"%PID_FILE%"
del "%PID_FILE%" >nul 2>nul
tasklist /FI "PID eq !PID!" /FI "IMAGENAME eq cmd.exe" /NH 2>nul | findstr /i "cmd.exe" >nul
if errorlevel 1 (echo [run] %~2: already stopped. & exit /b 0)
taskkill /PID !PID! /T /F >nul 2>nul
echo [run] Stopped %~2.
exit /b 0
