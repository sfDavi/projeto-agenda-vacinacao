@echo off
REM ==================================================================
REM  Compila o projeto e publica vacinacao.war no Tomcat 10.1
REM
REM  Requisitos:
REM    - Maven no PATH (ou variavel MVN apontando para mvn.cmd)
REM    - CATALINA_HOME apontando para a pasta do Tomcat
REM
REM  Uso: deploy.bat            (build + copia o .war)
REM       deploy.bat start      (build + copia + inicia o Tomcat)
REM ==================================================================
setlocal
cd /d "%~dp0"

if "%MVN%"=="" set "MVN=mvn"

if "%CATALINA_HOME%"=="" (
    echo [ERRO] Defina CATALINA_HOME com a pasta do Tomcat 10.1. Ex.:
    echo        set CATALINA_HOME=C:\apache-tomcat-10.1.61
    exit /b 1
)

echo === Build (mvn clean package) ===
call "%MVN%" -q clean package
if errorlevel 1 (
    echo [ERRO] Falha no build.
    exit /b 1
)

echo === Publicando em %CATALINA_HOME%\webapps ===
if exist "%CATALINA_HOME%\webapps\vacinacao" rmdir /s /q "%CATALINA_HOME%\webapps\vacinacao"
copy /y "target\vacinacao.war" "%CATALINA_HOME%\webapps\vacinacao.war" >nul
if errorlevel 1 (
    echo [ERRO] Nao foi possivel copiar o .war.
    exit /b 1
)

if /i "%~1"=="start" (
    echo === Iniciando o Tomcat ===
    call "%CATALINA_HOME%\bin\startup.bat"
)

echo Pronto. Acesse http://localhost:8080/vacinacao/
endlocal
