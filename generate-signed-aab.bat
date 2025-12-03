@echo off
echo ==============================================
echo Library App - Generate Keystore and Build AAB
echo ==============================================
echo.

echo Step 1: Generating Keystore...
echo.
set /p KEY_ALIAS="Enter key alias (e.g., libraryapp): "
set /p KEYSTORE_PASSWORD="Enter keystore password: "
set /p KEY_PASSWORD="Enter key password: "
set /p VALIDITY="Enter validity in years (e.g., 25): "

echo.
echo Generating keystore...
keytool -genkey -v ^
    -keystore keystore\libraryapp-keystore.jks ^
    -alias %KEY_ALIAS% ^
    -keyalg RSA ^
    -keysize 2048 ^
    -validity %VALIDITY%000 ^
    -storepass %KEYSTORE_PASSWORD% ^
    -keypass %KEY_PASSWORD%

if errorlevel 1 (
    echo Error: Failed to generate keystore
    pause
    exit /b 1
)

echo.
echo ✓ Keystore generated successfully!
echo.

echo Step 2: Updating gradle.properties...
echo.
echo # Signing Configuration > keystore\temp_gradle.properties
echo KEYSTORE_PASSWORD=%KEYSTORE_PASSWORD% >> keystore\temp_gradle.properties
echo KEY_ALIAS=%KEY_ALIAS% >> keystore\temp_gradle.properties
echo KEY_PASSWORD=%KEY_PASSWORD% >> keystore\temp_gradle.properties

echo.
echo Please add the following lines to your gradle.properties file:
echo.
echo KEYSTORE_PASSWORD=%KEYSTORE_PASSWORD%
echo KEY_ALIAS=%KEY_ALIAS%
echo KEY_PASSWORD=%KEY_PASSWORD%
echo.
echo (Or copy from keystore\temp_gradle.properties)
echo.

pause

echo.
echo Step 3: Building signed AAB...
echo.
gradlew.bat bundleRelease

if errorlevel 1 (
    echo Error: Failed to build AAB
    pause
    exit /b 1
)

echo.
echo ✓ Signed AAB generated successfully!
echo.
echo Location: app\build\outputs\bundle\release\app-release.aab
echo.
echo Next steps:
echo 1. Test the AAB using bundletool
echo 2. Upload to Google Play Console
echo.
pause
