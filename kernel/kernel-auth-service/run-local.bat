@echo off
setlocal EnableExtensions EnableDelayedExpansion
REM Standalone Windows cmd runner for kernel-auth-service (no .ps1).
REM Linux / macOS / Git Bash: use run-local.sh
REM
REM   run-local.bat init | start | smoke | stop | test | all | docker
REM Optional: SPRING_CLOUD_CONFIG_URI SPRING_CLOUD_CONFIG_LABEL PORT JDK_JAVA_OPTIONS

set "MODULE_DIR=%~dp0"
if "%MODULE_DIR:~-1%"=="\" set "MODULE_DIR=%MODULE_DIR:~0,-1%"
for %%I in ("%MODULE_DIR%\..") do set "KERNEL_DIR=%%~fI"

set "LOCAL_DIR=%MODULE_DIR%\.local"
set "LOG_DIR=%LOCAL_DIR%\logs"
set "PID_DIR=%LOCAL_DIR%\pids"
set "PID_FILE=%PID_DIR%\auth-service.pid"
set "LOG_FILE=%LOG_DIR%\auth-service.log"
set "ERR_FILE=%LOG_DIR%\auth-service.err.log"
set "MODULE=kernel-auth-service"

if not defined PORT set "PORT=8091"
if not defined SPRING_PROFILES_ACTIVE set "SPRING_PROFILES_ACTIVE=local"
if not defined IMAGE set "IMAGE=kernel-auth-service"
set "CONTEXT=/v1/authmanager"
set "BASE=http://127.0.0.1:%PORT%%CONTEXT%"

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
if /I "%CMD%"=="docker" goto :docker

echo error: unknown command '%CMD%'
goto :usage

:usage
echo Local kernel-auth-service ^(local profile, :8091 /v1/authmanager^)
echo.
echo   run-local.bat init     package this module
echo   run-local.bat start    run and wait until Spring Boot is ready
echo   run-local.bat smoke    GET /v1/authmanager/actuator/health
echo   run-local.bat stop
echo   run-local.bat test     Maven tests
echo   run-local.bat all      init + test + start + smoke
echo   run-local.bat docker   package, docker build, run
echo.
echo Port: %PORT% ^(override with set PORT=...^)
echo Config: src\main\resources\application-local.properties
echo Optional: SPRING_CLOUD_CONFIG_URI SPRING_CLOUD_CONFIG_LABEL JDK_JAVA_OPTIONS IMAGE
exit /b 1

:port_in_use
netstat -ano | findstr /R /C:":%PORT% .*LISTENING" >nul 2>&1
exit /b %ERRORLEVEL%

:pid_on_port
set "PORT_PID="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%PORT% .*LISTENING"') do set "PORT_PID=%%P"
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

:print_endpoints
echo.
echo authmanager  profile=%SPRING_PROFILES_ACTIVE%  port=%PORT%  context=%CONTEXT%
echo   health     %BASE%/actuator/health
echo   swagger    %BASE%/swagger-ui/index.html
echo   openapi    %BASE%/v3/api-docs
echo   info       %BASE%/actuator/info
echo   mappings   %BASE%/actuator/mappings
echo   prometheus %BASE%/actuator/prometheus
echo   token      POST %BASE%/authenticate/clientidsecretkey
echo   validate   GET  %BASE%/authorize/admin/validateToken
echo   refresh    POST %BASE%/authorize/refreshToken/{appid}
echo   invalidate POST %BASE%/authorize/invalidateToken
echo.
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
for %%S in ("!BOOT_JAR!") do if %%~zS LSS 1048576 (
  echo error: !BOOT_JAR! is not the Boot ZIP ^(no Main-Class^). Run: %~nx0 init
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
echo ==^> packaging %MODULE% ^(skip tests^)
REM Surefire inherits the env. local profile would take the deprecated offline path in adapter tests.
set "SAVED_PROFILE=%SPRING_PROFILES_ACTIVE%"
set "SPRING_PROFILES_ACTIVE="
pushd "%KERNEL_DIR%"
call mvn -pl %MODULE% -am clean package -DskipTests "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
set "RC=%ERRORLEVEL%"
popd
set "SPRING_PROFILES_ACTIVE=%SAVED_PROFILE%"
if not "%RC%"=="0" exit /b %RC%
echo init complete
exit /b 0

:test
call :check_prereqs
if errorlevel 1 exit /b 1
echo ==^> maven tests
set "SAVED_PROFILE=%SPRING_PROFILES_ACTIVE%"
set "SPRING_PROFILES_ACTIVE="
pushd "%KERNEL_DIR%"
call mvn -pl %MODULE% -am test "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
set "RC=%ERRORLEVEL%"
popd
set "SPRING_PROFILES_ACTIVE=%SAVED_PROFILE%"
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
  echo auth-service already running ^(pid !OLD_PID!^) on port %PORT%
  call :print_endpoints
  exit /b 0
)
call :port_in_use
if not errorlevel 1 (
  call :pid_on_port
  echo auth-service already listening on port %PORT% ^(pid !PORT_PID!^)
  if defined PORT_PID > "%PID_FILE%" echo !PORT_PID!
  call :print_endpoints
  exit /b 0
)
if exist "%PID_FILE%" del /q "%PID_FILE%" >nul 2>&1
if exist "%LOG_FILE%" del /q "%LOG_FILE%" >nul 2>&1
if exist "%ERR_FILE%" del /q "%ERR_FILE%" >nul 2>&1

call :find_jar
if errorlevel 1 exit /b 1

echo ==^> starting auth-service from %BOOT_JAR%
echo     profile=%SPRING_PROFILES_ACTIVE%
echo     port=%PORT%
echo     context=%CONTEXT%
echo     properties=src\main\resources\application-local.properties

if defined SPRING_CLOUD_CONFIG_URI echo     config.uri=%SPRING_CLOUD_CONFIG_URI%
if defined SPRING_CLOUD_CONFIG_LABEL echo     config.label=%SPRING_CLOUD_CONFIG_LABEL%

REM Detached process (no console). Avoid parenthesized echo blocks — unescaped
REM "(" in @(...) breaks cmd parsing and corrupts later labels like :smoke.
set "LAUNCH_PS1=%LOCAL_DIR%\launch-auth-service.ps1"
> "%LAUNCH_PS1%" echo $ErrorActionPreference = 'Stop'
>> "%LAUNCH_PS1%" echo $argList = New-Object System.Collections.Generic.List[string]
>> "%LAUNCH_PS1%" echo $argList.Add^('-Dspring.profiles.active=%SPRING_PROFILES_ACTIVE%'^)
>> "%LAUNCH_PS1%" echo $argList.Add^('-Dserver.port=%PORT%'^)
if defined SPRING_CLOUD_CONFIG_URI >> "%LAUNCH_PS1%" echo $argList.Add^('-Dspring.cloud.config.uri=%SPRING_CLOUD_CONFIG_URI%'^)
if defined SPRING_CLOUD_CONFIG_LABEL >> "%LAUNCH_PS1%" echo $argList.Add^('-Dspring.cloud.config.label=%SPRING_CLOUD_CONFIG_LABEL%'^)
>> "%LAUNCH_PS1%" echo $argList.Add^('-jar'^)
>> "%LAUNCH_PS1%" echo $argList.Add^('%BOOT_JAR%'^)
>> "%LAUNCH_PS1%" echo $p = Start-Process -FilePath 'java' -ArgumentList $argList.ToArray^(^) -WorkingDirectory '%MODULE_DIR%' -RedirectStandardOutput '%LOG_FILE%' -RedirectStandardError '%ERR_FILE%' -WindowStyle Hidden -PassThru
>> "%LAUNCH_PS1%" echo Set-Content -LiteralPath '%PID_FILE%' -Value $p.Id -Encoding ascii
powershell -NoProfile -ExecutionPolicy Bypass -File "%LAUNCH_PS1%"
if errorlevel 1 (
  echo error: failed to start java process
  exit /b 1
)

echo ==^> waiting for Spring Boot startup on port %PORT% ^(up to 90s^) ...
call :wait_ready 90
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
    echo warn: ready but PID for port %PORT% not resolved
  )
)
echo.
echo auth-service ready
echo   port    %PORT%
call :print_endpoints
exit /b 0

:wait_ready
set /a WAIT_MAX=%~1
if "!WAIT_MAX!"=="" set /a WAIT_MAX=90
set /a WAIT_ELAPSED=0
:wait_ready_loop
if !WAIT_ELAPSED! GEQ !WAIT_MAX! goto :wait_ready_fail

call :port_in_use
if not errorlevel 1 (
  if exist "%LOG_FILE%" (
    findstr /C:"Started AuthBootApplication" "%LOG_FILE%" >nul 2>&1
    if not errorlevel 1 exit /b 0
  )
)

if exist "%LOG_FILE%" (
  findstr /C:"APPLICATION FAILED TO START" /C:"Port %PORT% was already in use" "%LOG_FILE%" >nul 2>&1
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
echo error: Spring Boot did not become ready within !WAIT_MAX!s on port %PORT%
if exist "%LOG_FILE%" type "%LOG_FILE%"
if exist "%ERR_FILE%" type "%ERR_FILE%"
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
  echo auth-service is not running
  exit /b 0
)
echo ==^> stopping auth-service ^(%STOP_PID%^) on port %PORT%
taskkill /PID %STOP_PID% /T /F >nul 2>&1
del /q "%PID_FILE%" >nul 2>&1
echo stopped.
exit /b 0

:http_ok
set "HTTP_CODE=000"
set "HTTP_URL=%~1"
REM Prefer curl; write status to a temp file to avoid FOR /F parenthesis parsing bugs.
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
echo ==^> waiting for auth-service ^(up to 60s^)
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
call :http_ok "%BASE%/actuator/health"
if "%HTTP_CODE%"=="200" goto :smoke_ok
ping -n 3 127.0.0.1 >nul
set /a ELAPSED+=2
goto :smoke_loop

:smoke_ok
echo healthy  %BASE%/actuator/health
call :http_ok "%BASE%/swagger-ui/index.html"
echo swagger  %BASE%/swagger-ui/index.html  HTTP %HTTP_CODE%
if not "%HTTP_CODE%"=="200" (
  echo error: swagger not healthy
  exit /b 1
)
echo smoke ok
call :print_endpoints
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

:docker
call :init
if errorlevel 1 exit /b 1
where docker >nul 2>&1
if errorlevel 1 (
  echo error: docker is required on PATH
  exit /b 1
)
docker rm -f kernel-auth-service >nul 2>&1
echo ==^> docker build %IMAGE%
docker build -t %IMAGE% "%MODULE_DIR%"
if errorlevel 1 exit /b 1
call :print_endpoints
docker run --rm -p %PORT%:8091 --name kernel-auth-service -e active_profile_env=%SPRING_PROFILES_ACTIVE% -e spring_config_url_env=%SPRING_CLOUD_CONFIG_URI% -e spring_config_label_env=%SPRING_CLOUD_CONFIG_LABEL% -e JDK_JAVA_OPTIONS=%JDK_JAVA_OPTIONS% %IMAGE%
exit /b %ERRORLEVEL%
