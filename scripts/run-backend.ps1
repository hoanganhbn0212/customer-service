$backend = Join-Path $PSScriptRoot "..\backend"
Set-Location $backend

$mvnTools = Join-Path $PSScriptRoot "..\.tools\apache-maven-3.9.6\bin\mvn.cmd"

if (Get-Command mvn -ErrorAction SilentlyContinue) {
  mvn clean spring-boot:run
} elseif (Test-Path $mvnTools) {
  & $mvnTools clean spring-boot:run
} elseif (Test-Path ".\mvnw.cmd") {
  .\mvnw.cmd clean spring-boot:run
} else {
  Write-Error "Maven chua cai. Cai JDK 17+ va Maven, hoac dat maven vao .tools."
  exit 1
}
