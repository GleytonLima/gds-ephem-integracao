@echo off
setlocal enabledelayedexpansion

REM Define o caminho do arquivo .env
set ENV_FILE=.env

REM Lê o arquivo .env linha por linha
for /f "usebackq delims=" %%A in ("%ENV_FILE%") do (
  REM Remove os espaços em branco do início e do fim da linha
  set "LINE=%%A"
  for /f "tokens=*" %%B in ("!LINE!") do set "LINE=%%B"

  REM Ignora linhas vazias ou comentadas (iniciadas com #)
  if defined LINE (
    setlocal enabledelayedexpansion
    echo !LINE!| findstr /r "^#" >nul
    if errorlevel 1 (
      REM Separa a linha em nome e valor da variável
      for /f "tokens=1* delims==" %%C in ("!LINE!") do (
        set "VAR_NAME=%%C"
        set "VAR_VALUE=%%D"

        REM Remove possíveis aspas no valor da variável
        setlocal enabledelayedexpansion
        if defined VAR_VALUE set "VAR_VALUE=!VAR_VALUE:"=!& set "VAR_VALUE=!VAR_VALUE:'=!"
        endlocal & set "VAR_VALUE=!VAR_VALUE!"

        REM Define a variável de ambiente
        if defined VAR_VALUE (
          echo Definindo variável de ambiente: !VAR_NAME!=!VAR_VALUE!
          setx !VAR_NAME! "!VAR_VALUE!" >nul
        )
      )
    )
    endlocal
  )
)

echo Variáveis de ambiente definidas com sucesso.

endlocal
