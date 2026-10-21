@echo off
setlocal
set "H2_JAR=%USERPROFILE%\.m2\repository\com\h2database\h2\2.3.232\h2-2.3.232.jar"
set "DB_URL=jdbc:h2:file:D:/Camunda-Workspace/models/ASSIGNMENT(END OF WEEK6)/603_Development_Presentation(end of week6)/04_External_Workers/data/hospital-applications"

if not exist "%H2_JAR%" (
  echo H2 driver was not found at:
  echo %H2_JAR%
  echo Please run the worker project once with Maven, then try again.
  pause
  exit /b 1
)

echo Starting the H2 web console...
echo.
echo In the browser login page use:
echo Driver Class: org.h2.Driver
echo JDBC URL:     %DB_URL%
echo User Name:    sa
echo Password:     leave blank
echo.
start "Hospital H2 Console" /min java -cp "%H2_JAR%" org.h2.tools.Server -web -webPort 8082
timeout /t 2 /nobreak >nul
start "" "http://127.0.0.1:8082"
echo The browser has been opened. Do NOT use SQLiteStudio for this .mv.db file.
pause
