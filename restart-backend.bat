@echo off
echo Restarting Inventory System Backend...
echo.

cd inventory-system

echo Stopping any existing Spring Boot processes...
taskkill /f /im java.exe 2>nul

echo.
echo Starting Spring Boot application...
echo This will clean up any corrupted data and create fresh sample data.
echo.

call mvnw.cmd spring-boot:run

pause