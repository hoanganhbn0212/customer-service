# Build iPhone app and create a download link

This app uses Vue + Capacitor. Android can be built on Windows, but iPhone distribution requires Apple signing. A signed iOS app must be produced on macOS with Xcode or on a macOS CI service.

## Recommended route: TestFlight link

Use this when you want a link that iPhone users can open and install safely.

Requirements:

- Apple Developer Program account.
- macOS machine with Xcode, or a CI runner using macOS.
- A public HTTPS backend URL for `VITE_API_BASE_URL`.

Steps on this Windows machine:

```powershell
cd C:\Users\ADMIN\AnhDH\customer-service
.\scripts\build-ios.ps1
```

Commit and move the repo to a Mac if needed.

Steps on macOS:

```bash
cd frontend
npm install
npm run build:cap
npx cap sync ios
npx cap open ios
```

In Xcode:

1. Select the `App` target.
2. Set `Signing & Capabilities` to your Apple Developer team.
3. Check the bundle id, currently `com.customerservice.layla`.
4. Choose `Any iOS Device`.
5. Use `Product > Archive`.
6. Upload the archive to App Store Connect.
7. In App Store Connect, enable TestFlight and invite testers.

Result: Apple provides a TestFlight invite link that users can open on iPhone.

## Private HTTPS install link: Ad Hoc IPA

Use this only for a controlled set of devices.

Requirements:

- Apple Developer Program account.
- Every iPhone UDID must be registered in Apple Developer.
- Xcode export method: `Ad Hoc`.
- An HTTPS server that hosts the `.ipa` and an installation manifest `.plist`.

The install link format is:

```text
itms-services://?action=download-manifest&url=https://your-domain.example/layla-care/manifest.plist
```

This route is more fragile than TestFlight because every device must be registered before export.

## Important backend note

For Android local testing, `VITE_API_BASE_URL=http://<PC-IP>:8080` can work on the same Wi-Fi. For iPhone distribution, use a real HTTPS API endpoint, for example:

```env
VITE_API_BASE_URL=https://api.your-domain.example
```

If the app is built with a LAN IP, it will only work while the phone can reach that exact PC on the same network.

## What cannot be done from Windows only

Windows can prepare the Capacitor web bundle and iOS project files. Windows cannot create a valid signed `.ipa` by itself because Apple requires Xcode signing tooling on macOS.
