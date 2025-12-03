# 📦 Signed AAB Generation Guide

This guide will help you create a signed Android App Bundle (AAB) for the Library App.

## 🔐 Step 1: Generate Keystore

First, you need to create a keystore file to sign your app:

### Option A: Using Command Line
```bash
keytool -genkey -v \
    -keystore keystore/libraryapp-keystore.jks \
    -alias libraryapp \
    -keyalg RSA \
    -keysize 2048 \
    -validity 9125 \
    -storepass YOUR_KEYSTORE_PASSWORD \
    -keypass YOUR_KEY_PASSWORD
```

### Option B: Using Android Studio
1. Go to **Build → Generate Signed Bundle/APK**
2. Select **Android App Bundle**
3. Click **Create new...**
4. Fill in the keystore information:
   - Key store path: `keystore/libraryapp-keystore.jks`
   - Password: Choose a strong password
   - Key alias: `libraryapp`
   - Key password: Choose a strong password
   - Validity: 25 years
   - Certificate info: Fill as required

## 🔧 Step 2: Configure Signing

Update your `gradle.properties` file with the signing information:

```properties
# Signing Configuration
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=libraryapp
KEY_PASSWORD=your_key_password
```

**⚠️ Security Note:** Never commit these credentials to version control!

## 🚀 Step 3: Build Signed AAB

### Option A: Using Gradle Command
```bash
# Windows
.\gradlew bundleRelease

# macOS/Linux
./gradlew bundleRelease
```

### Option B: Using Android Studio
1. Go to **Build → Generate Signed Bundle/APK**
2. Select **Android App Bundle**
3. Choose your keystore file
4. Enter your passwords
5. Select **release** build variant
6. Click **Finish**

## 📍 Output Location

Your signed AAB will be generated at:
```
app/build/outputs/bundle/release/app-release.aab
```

## 🧪 Step 4: Test Your AAB

### Install bundletool
```bash
# Download from GitHub releases
# https://github.com/google/bundletool/releases
```

### Generate APKs from AAB
```bash
java -jar bundletool.jar build-apks \
    --bundle=app/build/outputs/bundle/release/app-release.aab \
    --output=app-release.apks \
    --ks=keystore/libraryapp-keystore.jks \
    --ks-pass=pass:YOUR_KEYSTORE_PASSWORD \
    --ks-key-alias=libraryapp \
    --key-pass=pass:YOUR_KEY_PASSWORD
```

### Install on connected device
```bash
java -jar bundletool.jar install-apks --apks=app-release.apks
```

## 📱 Step 5: Upload to Google Play

1. Go to [Google Play Console](https://play.google.com/console)
2. Create a new app or select existing
3. Go to **Release → Production**
4. Upload your AAB file
5. Complete the store listing
6. Submit for review

## 🔒 Security Best Practices

1. **Keep your keystore safe**: Store it in a secure location with backups
2. **Use strong passwords**: At least 12 characters with mixed case, numbers, and symbols
3. **Never share credentials**: Keep keystore passwords private
4. **Version control**: Add `keystore/` to `.gitignore`
5. **Environment variables**: Use environment variables for CI/CD

## 📊 AAB Benefits

- **Smaller downloads**: Dynamic delivery reduces app size
- **Feature modules**: Split features into separate modules
- **Multiple APKs**: Google Play generates optimized APKs for different devices
- **Asset packs**: Deliver large assets on-demand

## 🐛 Troubleshooting

### Common Issues:

1. **Keystore not found**: Ensure the path is correct
2. **Password incorrect**: Double-check your credentials
3. **Build fails**: Check ProGuard rules and dependencies
4. **Signing fails**: Verify keystore permissions

### Debug Commands:
```bash
# Check keystore info
keytool -list -v -keystore keystore/libraryapp-keystore.jks

# Verify AAB
java -jar bundletool.jar validate --bundle=app-release.aab

# Check AAB contents
java -jar bundletool.jar dump manifest --bundle=app-release.aab
```

## 📋 Checklist

- [ ] Keystore generated and secured
- [ ] Signing configuration added to gradle.properties
- [ ] ProGuard rules updated
- [ ] Release build successful
- [ ] AAB tested with bundletool
- [ ] App tested on different devices
- [ ] Store listing prepared
- [ ] Ready for Google Play upload

---

**Note**: Keep your keystore file and passwords secure. If you lose them, you won't be able to update your app on Google Play!
