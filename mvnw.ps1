#!/usr/bin/env pwsh

$ErrorActionPreference = "Stop"

function Die($message) {
    Write-Error $message
    exit 1
}

# Resolve project base directory
$PROJECT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $PROJECT_DIR) { $PROJECT_DIR = "." }
Set-Location $PROJECT_DIR

# Locate Java
if ($env:JAVA_HOME) {
    $JAVA_EXE = Join-Path $env:JAVA_HOME "bin\java.exe"
    if (-not (Test-Path $JAVA_EXE)) {
        Die "JAVA_HOME is set to an invalid directory: $env:JAVA_HOME"
    }
} else {
    $JAVA_EXE = (Get-Command java -ErrorAction SilentlyContinue).Path
    if (-not $JAVA_EXE) {
        Die "JAVA_HOME is not set and no 'java' command could be found in your PATH."
    }
}

$WRAPPER_JAR = Join-Path $PROJECT_DIR ".mvn\wrapper\maven-wrapper.jar"
$WRAPPER_MAIN = "org.apache.maven.wrapper.MavenWrapperMain"
$PROPS_FILE = Join-Path $PROJECT_DIR ".mvn\wrapper\maven-wrapper.properties"

# Download wrapper jar if missing
if (-not (Test-Path $WRAPPER_JAR)) {
    $wrapperUrl = $null
    if (Test-Path $PROPS_FILE) {
        Get-Content $PROPS_FILE | ForEach-Object {
            if ($_ -match "^\s*wrapperUrl\s*=\s*(.+)$") {
                $wrapperUrl = $Matches[1].Trim()
            }
        }
    }
    if (-not $wrapperUrl) {
        $wrapperUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
    }
    New-Item -ItemType Directory -Force -Path (Split-Path $WRAPPER_JAR) | Out-Null
    Write-Host "Downloading Maven Wrapper JAR from: $wrapperUrl"
    try {
        $ProgressPreference = "SilentlyContinue"
        Invoke-WebRequest -UseBasicParsing -Uri $wrapperUrl -OutFile $WRAPPER_JAR
    } catch {
        Die "Failed to download $WRAPPER_JAR from $wrapperUrl. $_"
    }
}

# Compose Java args
$argsLine = $args -join " "
& $JAVA_EXE -classpath $WRAPPER_JAR "-Dmaven.multiModuleProjectDirectory=$PROJECT_DIR" $WRAPPER_MAIN $args
exit $LASTEXITCODE
