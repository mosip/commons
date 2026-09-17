@echo off
setlocal EnableExtensions EnableDelayedExpansion
REM Standalone Windows cmd runner for kernel-idgenerator-service (no .ps1).
REM Linux / macOS / Git Bash: use run-local.sh
REM
REM   run-local.bat init | start | smoke | stop | test | all

set "MODULE_DIR=%~dp0"
if "%MODULE_DIR:~-1%"=="\" set "MODULE_DIR=%MODULE_DIR:~0,-1%"
for %%I in ("%MODULE_DIR%\..") do set "KERNEL_DIR=%%~fI"

set "LOCAL_DIR=%MODULE_DIR%\.local"
set "LOG_DIR=%LOCAL_DIR%\logs"
set "PID_DIR=%LOCAL_DIR%\pids"
set "PID_FILE=%PID_DIR%\idgenerator.pid"
set "LOG_FILE=%LOG_DIR%\idgenerator.log"
set "ERR_FILE=%LOG_DIR%\idgenerator.err.log"
set "MODULE=kernel-idgenerator-service"
set "CONTEXT=/v1/idgenerator"
set "RID_CONTEXT=/v1/ridgenerator"

if not defined IDGENERATOR_PORT set "IDGENERATOR_PORT=8080"

set "CMD=%~1"
if "%CMD%"=="" goto :usage
if /I "%CMD%"=="-h" goto :usage
if /I "%CMD%"=="--help" goto :usage
if /I "%CMD%"=="help" goto :usage
if /I "%CMD%"=="init" goto :init
if /I "%CMD%"=="test" goto :test
if /I "%CMD%"=="start" goto :start
if /I "%CMD%"=="stop" goto :stop
if /I "%CMD%"=="smoke" goto :smoke
if /I "%CMD%"=="all" goto :all

echo error: unknown command '%CMD%'
goto :usage

:usage
echo Local kernel-idgenerator-service ^(local profile, :8080 /v1/idgenerator + /v1/ridgenerator^)
echo.
echo   run-local.bat init     package this module
echo   run-local.bat start    run and wait until Vert.x HTTP is ready
echo   run-local.bat smoke    GET /v1/idgenerator/actuator/health
echo   run-local.bat stop
echo   run-local.bat test      Maven tests
echo   run-local.bat all      init + test + start + smoke
echo.
echo Port: %IDGENERATOR_PORT% ^(override with set IDGENERATOR_PORT=...^)
echo Config: src\main\resources\application-local.properties
echo DB: in-memory H2 ^(no Postgres required^)
exit /b 1

:port_in_use
netstat -ano | findstr /R /C:":%IDGENERATOR_PORT% .*LISTENING" >nul 2>&1
exit /b %ERRORLEVEL%

:pid_on_port
set "PORT_PID="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%IDGENERATOR_PORT% .*LISTENING"') do set "PORT_PID=%%P"
exit /b 0

:ensure_dirs
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%PID_DIR%" mkdir "%PID_DIR%"
exit /b 0

:check_prereqs
where java >nul 2>&1
if errorlevel 1 (
  echo error: java is required on PATH
  exit /b 1
)
where mvn >nul 2>&1
if errorlevel 1 (
  echo error: mvn is required on PATH
  exit /b 1
)
exit /b 0

:find_jar
set "BOOT_JAR="
for %%F in ("%MODULE_DIR%\target\%MODULE%-*.jar") do (
  set "CAND=%%~nxF"
  echo !CAND! | findstr /I /C:".original" /C:"sources" /C:"javadoc" >nul
  if errorlevel 1 (
    set "BOOT_JAR=%%~fF"
    goto :find_jar_done
  )
)
:find_jar_done
if not defined BOOT_JAR (
  echo error: no boot jar in target. Run: %~nx0 init
  exit /b 1
)
exit /b 0

:is_running
if not exist "%PID_FILE%" exit /b 1
set /p CHECK_PID=<"%PID_FILE%"
if not defined CHECK_PID exit /b 1
tasklist /FI "PID eq !CHECK_PID!" 2>nul | findstr /I "!CHECK_PID!" >nul
if errorlevel 1 exit /b 1
exit /b 0

:init
call :check_prereqs
if errorlevel 1 exit /b 1
REM Windows locks the boot jar while the process is running; clean fails otherwise
call :stop
ping -n 3 127.0.0.1 >nul
echo ==^> packaging %MODULE% ^(skip tests^)
pushd "%KERNEL_DIR%"
call mvn -pl %MODULE% -am clean package -DskipTests "-Dmaven.test.skip=true" "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
set "RC=%ERRORLEVEL%"
popd
if not "%RC%"=="0" exit /b %RC%
echo init complete
exit /b 0

:test
call :check_prereqs
if errorlevel 1 exit /b 1
echo ==^> maven tests
pushd "%KERNEL_DIR%"
call mvn -pl %MODULE% -am test "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
set "RC=%ERRORLEVEL%"
popd
exit /b %RC%

:start
echo.
echo ==^> run-local start
call :ensure_dirs
call :check_prereqs
if errorlevel 1 exit /b 1
call :is_running
if not errorlevel 1 (
  set /p OLD_PID=<"%PID_FILE%"
  echo idgenerator already running ^(pid !OLD_PID!^) on port %IDGENERATOR_PORT%
  echo   health   http://127.0.0.1:!IDGENERATOR_PORT!!CONTEXT!/actuator/health
  echo   uin      GET http://127.0.0.1:!IDGENERATOR_PORT!!CONTEXT!/uin
  echo   vid      GET http://127.0.0.1:!IDGENERATOR_PORT!!CONTEXT!/vid
  echo   rid      GET http://127.0.0.1:!IDGENERATOR_PORT!!RID_CONTEXT!/generate/rid/{centerid}/{machineid}
  exit /b 0
)
call :port_in_use
if not errorlevel 1 (
  call :pid_on_port
  echo idgenerator already listening on port %IDGENERATOR_PORT% ^(pid !PORT_PID!^)
  if defined PORT_PID > "%PID_FILE%" echo !PORT_PID!
  echo   health   http://127.0.0.1:!IDGENERATOR_PORT!!CONTEXT!/actuator/health
  echo   uin      GET http://127.0.0.1:!IDGENERATOR_PORT!!CONTEXT!/uin
  echo   vid      GET http://127.0.0.1:!IDGENERATOR_PORT!!CONTEXT!/vid
  echo   rid      GET http://127.0.0.1:!IDGENERATOR_PORT!!RID_CONTEXT!/generate/rid/{centerid}/{machineid}
  exit /b 0
)
if exist "%PID_FILE%" del /q "%PID_FILE%" >nul 2>&1
if exist "%LOG_FILE%" del /q "%LOG_FILE%" >nul 2>&1
if exist "%ERR_FILE%" del /q "%ERR_FILE%" >nul 2>&1

call :find_jar
if errorlevel 1 exit /b 1

echo ==^> starting idgenerator from %BOOT_JAR%
echo     profile=local
echo     port=%IDGENERATOR_PORT%
echo     context=%CONTEXT%  rid=%RID_CONTEXT%
echo     properties=src\main\resources\application-local.properties
echo     db=H2 in-memory

REM Detached process (no console). Avoid parenthesized echo blocks — unescaped
REM "(" in @(...) breaks cmd parsing and corrupts later labels like :smoke.
set "LAUNCH_PS1=%LOCAL_DIR%\launch-idgenerator.ps1"
> "%LAUNCH_PS1%" echo $ErrorActionPreference = 'Stop'
>> "%LAUNCH_PS1%" echo $argList = @^('-Dspring.profiles.active=local','-Dserver.port=%IDGENERATOR_PORT%','-Dspring.cloud.config.enabled=false','-Dspring.sleuth.enabled=false','-jar','%BOOT_JAR%'^)
>> "%LAUNCH_PS1%" echo $p = Start-Process -FilePath 'java' -ArgumentList $argList -WorkingDirectory '%MODULE_DIR%' -RedirectStandardOutput '%LOG_FILE%' -RedirectStandardError '%ERR_FILE%' -WindowStyle Hidden -PassThru
>> "%LAUNCH_PS1%" echo Set-Content -LiteralPath '%PID_FILE%' -Value $p.Id -Encoding ascii
powershell -NoProfile -ExecutionPolicy Bypass -File "%LAUNCH_PS1%"
if errorlevel 1 (
  echo error: failed to start java process
  exit /b 1
)

echo ==^> waiting for Vert.x HTTP on port %IDGENERATOR_PORT% ^(up to 180s; UIN/VID pool first^) ...
call :wait_ready 180
if errorlevel 1 exit /b 1

if exist "%PID_FILE%" (
  set /p STARTED_PID=<"%PID_FILE%"
  echo pid !STARTED_PID!  log %LOG_FILE%
) else (
  call :pid_on_port
  if defined PORT_PID (
    > "%PID_FILE%" echo !PORT_PID!
    echo pid !PORT_PID!  log %LOG_FILE%
  ) else (
    echo warn: ready but PID for port %IDGENERATOR_PORT% not resolved
  )
)
echo.
echo idgenerator ready
echo   port     %IDGENERATOR_PORT%
echo   health   http://127.0.0.1:!IDGENERATOR_PORT!!CONTEXT!/actuator/health
echo   uin      GET http://127.0.0.1:!IDGENERATOR_PORT!!CONTEXT!/uin
echo   vid      GET http://127.0.0.1:!IDGENERATOR_PORT!!CONTEXT!/vid
echo   rid      GET http://127.0.0.1:!IDGENERATOR_PORT!!RID_CONTEXT!/generate/rid/{centerid}/{machineid}
exit /b 0

:wait_ready
set /a WAIT_MAX=%~1
if "!WAIT_MAX!"=="" set /a WAIT_MAX=180
set /a WAIT_ELAPSED=0
:wait_ready_loop
if !WAIT_ELAPSED! GEQ !WAIT_MAX! goto :wait_ready_fail

call :port_in_use
if not errorlevel 1 (
  if exist "%LOG_FILE%" (
    findstr /C:"Deployed verticle HttpServerVerticle" /C:"Starting vidgenerator service" "%LOG_FILE%" >nul 2>&1
    if not errorlevel 1 exit /b 0
  )
  REM Port bound is enough even if the log line used a different logger
  exit /b 0
)

if exist "%LOG_FILE%" (
  findstr /C:"APPLICATION FAILED TO START" /C:"Port %IDGENERATOR_PORT% was already in use" /C:"Failed to deploy verticle HttpServerVerticle" /C:"vid fetcher verticle deployment failed" "%LOG_FILE%" >nul 2>&1
  if not errorlevel 1 (
    echo error: startup failed. See %LOG_FILE%
    type "%LOG_FILE%"
    exit /b 1
  )
)

echo     ... still starting ^(!WAIT_ELAPSED!s / !WAIT_MAX!s^)
REM ping sleep works when stdin is redirected (timeout does not)
ping -n 3 127.0.0.1 >nul
set /a WAIT_ELAPSED+=2
goto :wait_ready_loop

:wait_ready_fail
echo error: Vert.x HTTP did not become ready within !WAIT_MAX!s on port %IDGENERATOR_PORT%
if exist "%LOG_FILE%" type "%LOG_FILE%"
exit /b 1

:stop
call :ensure_dirs
set "STOP_PID="
if exist "%PID_FILE%" set /p STOP_PID=<"%PID_FILE%"
if not defined STOP_PID (
  call :pid_on_port
  set "STOP_PID=!PORT_PID!"
)
if not defined STOP_PID (
  echo idgenerator is not running
  exit /b 0
)
echo ==^> stopping idgenerator ^(%STOP_PID%^) on port %IDGENERATOR_PORT%
taskkill /PID %STOP_PID% /T /F >nul 2>&1
del /q "%PID_FILE%" >nul 2>&1
echo stopped.
exit /b 0

:http_ok
set "HTTP_CODE=000"
set "HTTP_URL=%~1"
set "HTTP_CODE_FILE=%LOCAL_DIR%\http-code.txt"
curl.exe -sS -o NUL -w "%%{http_code}" --connect-timeout 2 --max-time 3 "%HTTP_URL%" > "%HTTP_CODE_FILE%" 2>nul
if exist "%HTTP_CODE_FILE%" (
  for /f "usebackq delims=" %%C in ("%HTTP_CODE_FILE%") do set "HTTP_CODE=%%C"
  del /q "%HTTP_CODE_FILE%" >nul 2>&1
)
if not defined HTTP_CODE set "HTTP_CODE=000"
if "!HTTP_CODE!"=="" set "HTTP_CODE=000"
exit /b 0

:smoke
call :ensure_dirs
echo ==^> waiting for idgenerator ^(up to 60s^)
set /a ELAPSED=0
:smoke_loop
if %ELAPSED% GEQ 60 goto :smoke_fail
if exist "%PID_FILE%" (
  call :is_running
  if errorlevel 1 (
    echo error: process exited. See %LOG_FILE%
    if exist "%LOG_FILE%" type "%LOG_FILE%"
    if exist "%ERR_FILE%" type "%ERR_FILE%"
    exit /b 1
  )
)
call :http_ok "http://127.0.0.1:%IDGENERATOR_PORT%%CONTEXT%/actuator/health"
if "%HTTP_CODE%"=="200" (
  set "HEALTH_URL=http://127.0.0.1:%IDGENERATOR_PORT%%CONTEXT%/actuator/health"
  goto :smoke_ok
)
call :http_ok "http://127.0.0.1:%IDGENERATOR_PORT%/actuator/health"
if "%HTTP_CODE%"=="200" (
  set "HEALTH_URL=http://127.0.0.1:%IDGENERATOR_PORT%/actuator/health"
  goto :smoke_ok
)
ping -n 3 127.0.0.1 >nul
set /a ELAPSED+=2
goto :smoke_loop

:smoke_ok
echo healthy  %HEALTH_URL%
call :http_ok "http://127.0.0.1:%IDGENERATOR_PORT%%CONTEXT%/metrics"
echo metrics  http://127.0.0.1:%IDGENERATOR_PORT%%CONTEXT%/metrics  HTTP %HTTP_CODE%
echo smoke ok
exit /b 0

:smoke_fail
echo error: not healthy. See %LOG_FILE%
if exist "%LOG_FILE%" type "%LOG_FILE%"
if exist "%ERR_FILE%" type "%ERR_FILE%"
exit /b 1

:all
echo ==^> all: init + test + start + smoke
call :init
if errorlevel 1 exit /b 1
call :test
if errorlevel 1 exit /b 1
call :start
if errorlevel 1 exit /b 1
call :smoke
exit /b %ERRORLEVEL%
