# Library App - Generate Keystore and Build Signed AAB
# PowerShell Script

Write-Host "==============================================" -ForegroundColor Cyan
Write-Host "Library App - Generate Keystore and Build AAB" -ForegroundColor Cyan
Write-Host "==============================================" -ForegroundColor Cyan
Write-Host

# Check if Java/keytool is available
try {
    keytool -help | Out-Null
} catch {
    Write-Host "Error: keytool not found. Please ensure Java JDK is installed and in PATH." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Step 1: Generating Keystore..." -ForegroundColor Yellow
Write-Host

$keyAlias = Read-Host "Enter key alias (e.g., libraryapp)"
$keystorePassword = Read-Host "Enter keystore password" -AsSecureString
$keyPassword = Read-Host "Enter key password" -AsSecureString
$validity = Read-Host "Enter validity in years (e.g., 25)"

# Convert secure strings to plain text for keytool
$keystorePasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($keystorePassword))
$keyPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($keyPassword))

Write-Host
Write-Host "Generating keystore..." -ForegroundColor Green

$validityDays = [int]$validity * 365

try {
    & keytool -genkey -v `
        -keystore "keystore\libraryapp-keystore.jks" `
        -alias $keyAlias `
        -keyalg RSA `
        -keysize 2048 `
        -validity $validityDays `
        -storepass $keystorePasswordPlain `
        -keypass $keyPasswordPlain

    Write-Host
    Write-Host "✓ Keystore generated successfully!" -ForegroundColor Green
} catch {
    Write-Host "Error: Failed to generate keystore" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host
Write-Host "Step 2: Updating gradle.properties..." -ForegroundColor Yellow

# Create temp file with signing properties
$tempPropsContent = @"
# Signing Configuration
KEYSTORE_PASSWORD=$keystorePasswordPlain
KEY_ALIAS=$keyAlias
KEY_PASSWORD=$keyPasswordPlain
"@

$tempPropsContent | Out-File -FilePath "keystore\temp_gradle.properties" -Encoding UTF8

Write-Host
Write-Host "Please add the following lines to your gradle.properties file:" -ForegroundColor Cyan
Write-Host
Write-Host "KEYSTORE_PASSWORD=$keystorePasswordPlain" -ForegroundColor White
Write-Host "KEY_ALIAS=$keyAlias" -ForegroundColor White
Write-Host "KEY_PASSWORD=$keyPasswordPlain" -ForegroundColor White
Write-Host
Write-Host "(Or copy from keystore\temp_gradle.properties)" -ForegroundColor Gray
Write-Host

Read-Host "Press Enter when you've updated gradle.properties"

Write-Host
Write-Host "Step 3: Building signed AAB..." -ForegroundColor Yellow
Write-Host

try {
    if (Test-Path "gradlew.bat") {
        & .\gradlew.bat bundleRelease
    } else {
        & gradle bundleRelease
    }
    
    Write-Host
    Write-Host "✓ Signed AAB generated successfully!" -ForegroundColor Green
    Write-Host
    Write-Host "Location: app\build\outputs\bundle\release\app-release.aab" -ForegroundColor Cyan
    Write-Host
    Write-Host "Next steps:" -ForegroundColor Yellow
    Write-Host "1. Test the AAB using bundletool" -ForegroundColor White
    Write-Host "2. Upload to Google Play Console" -ForegroundColor White
    Write-Host "3. Keep your keystore file safe and secure!" -ForegroundColor Red
    Write-Host
} catch {
    Write-Host "Error: Failed to build AAB" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

Read-Host "Press Enter to exit"
