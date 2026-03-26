@echo off
echo Restarting Inventory System Frontend...
echo.

cd inventory-frontend

echo Installing dependencies...
call npm install

echo.
echo Starting development server...
call npm run dev

pause