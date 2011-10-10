@echo off
setLocal EnableDelayedExpansion

if "%CLOJURESCRIPT_HOME%" == "" goto ERROR_HOME

set CLASSPATH=.;src\;%CLOJURESCRIPT_HOME%\src\clj;%CLOJURESCRIPT_HOME%\src\cljs
for /R "%CLOJURESCRIPT_HOME%\lib" %%a in (*.jar) do (
  set CLASSPATH=!CLASSPATH!;%%a
)
set CLASSPATH=!CLASSPATH!
for /R "lib" %%a in (*.jar) do (
  set CLASSPATH=!CLASSPATH!;%%a
)
set CLASSPATH=!CLASSPATH!
java -server -Xmx1G -Xms1G -Xmn256m -cp "%CLASSPATH%" clojure.main %~dp0\watcher.clj %*
goto EOF
:ERROR_HOME
echo CLOJURESCRIPT_HOME is not set
:EOF
