@echo off
setlocal
set "H2_JAR=%USERPROFILE%\.m2\repository\com\h2database\h2\2.3.232\h2-2.3.232.jar"
set "DB_URL=jdbc:h2:file:D:/Camunda-Workspace/models/ASSIGNMENT(END OF WEEK6)/603_Development_Presentation(end of week6)/04_External_Workers/data/hospital-applications"

if not exist "%H2_JAR%" (
  echo H2 driver was not found at:
  echo %H2_JAR%
  pause
  exit /b 1
)

echo Reading the H2 persistent database. This command is read-only.
java -cp "%H2_JAR%" org.h2.tools.Shell -url "%DB_URL%" -user sa -password "" -sql "SELECT patient_id, data_key, data_value, source, updated_at FROM application_data ORDER BY patient_id, data_key"
echo.
pause
