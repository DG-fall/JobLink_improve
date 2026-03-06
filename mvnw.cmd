@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------
@if "%DEBUG%"=="" @echo off
@REM set title of command window
title %0
@REM enable echoing by setting MAVEN_BATCH_ECHO to 'on'
@if "%MAVEN_BATCH_ECHO%"=="on"  echo %MAVEN_BATCH_ECHO%

@REM set %HOME% to equivalent of $HOME
if "%HOME%"=="" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

@REM Execute Java with Maven Wrapper
set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR%"=="" set MAVEN_PROJECTBASEDIR=.
set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@REM Locate Java
if not "%JAVA_HOME%"=="" goto findJavaFromJavaHome
where java >NUL 2>&1
if %ERRORLEVEL%==0 goto init
echo.
echo Error: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto end

:findJavaFromJavaHome
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if exist "%JAVA_EXE%" goto init
echo.
echo Error: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto end

:init
@REM if the maven-wrapper.jar does not exist, download it using PowerShell
if exist "%WRAPPER_JAR%" goto execute

set WRAPPER_URL=
for /F "usebackq tokens=1,2 delims==" %%A in ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") do (
  if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
)
if "%WRAPPER_URL%"=="" set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

echo Downloading Maven Wrapper JAR from: %WRAPPER_URL%

powershell -Command ^
  "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; ^
   $wc = New-Object System.Net.WebClient; ^
   $url = '%WRAPPER_URL%'; ^
   $jar = '%WRAPPER_JAR%'; ^
   If (!(Test-Path -Path (Split-Path $jar))) { New-Item -ItemType Directory -Path (Split-Path $jar) | Out-Null }; ^
   $wc.DownloadFile($url, $jar)"

if exist "%WRAPPER_JAR%" goto execute
echo Failed to download %WRAPPER_JAR%
goto end

:execute
set MAVEN_CMD_LINE_ARGS=%*
"%JAVA_EXE%" %JVM_CONFIG_MAVEN_PROPS% ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %MAVEN_CMD_LINE_ARGS%
goto end

:end
@endlocal
