set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.

java %FLIT_JAVA_OPTS% -jar %DIRNAME%\plugin-*-all.jar %*