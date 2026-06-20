# Momentum Digital Wellness Runbook

## Purpose

This runbook tracks the Digital Wellness feature, starting with a simple "check today's app usage" flow for iPhone Screen Time data.

Keep this feature iOS-first and MVP-focused. Do not add blocking, limits, warnings, backend storage, HealthKit, or social features until the usage-report MVP is complete.

## Product Direction

Digital Wellness helps a Momentum user understand how much time they spent today in selected distracting apps or categories.

Target first flow:

1. User opens Momentum.
2. User sees top-level actions for `Run` and `Check My Digital Usage`.
3. User taps `Check My Digital Usage`.
4. On iPhone, Momentum requests Screen Time authorization.
5. Momentum opens Apple's app/category picker.
6. User selects apps/categories.
7. Momentum stores the selection locally on iOS.
8. Momentum shows today's usage report for the selected apps/categories.

Unsupported platforms:

- Web and Android must not crash.
- Show: `Screen Time usage is currently available only on iPhone.`

## Current Navigation Decision

Home should stay focused on primary modes:

- `Run`
- `Check My Digital Usage`

Run history belongs inside the Run flow, not as a separate top-level home action.

## Completed So Far

Phase 1A is complete in the Flutter app repository (`momentum-app`):

- Removed the top-level `History` button from the home screen.
- Added a top-level `Check My Digital Usage` button below `Run`.
- Moved `History` into the `Run` screen as an outlined action.
- Added `DigitalWellnessScreen`.
- Added `ScreenTimeService` with MethodChannel name `momentum/screen_time`.
- Added safe unsupported-platform handling for Web and non-iOS platforms.
- Added iOS MethodChannel handling in `ios/Runner/AppDelegate.swift`.
- Native iOS bridge now requests Family Controls authorization when available.
- Native iOS bridge opens `FamilyActivityPicker` on iOS 16+ when FamilyControls is available.
- Native iOS bridge stores `FamilyActivitySelection` in `UserDefaults` under `momentum.screenTime.selection`.
- Native iOS bridge returns a clear setup-required message after selection because the usage report extension is not wired yet.
- Added widget coverage for home navigation and unsupported-platform Digital Wellness behavior.

Phase 1B repo preparation and device-signing check are complete:

- Added `ios/Runner/Runner.entitlements` with `com.apple.developer.family-controls`.
- Added the entitlement file to the Xcode project navigator.
- Enabled `CODE_SIGN_ENTITLEMENTS = Runner/Runner.entitlements` for Runner Debug/Profile/Release after Xcode showed `Family Controls (Development)` under Signing & Capabilities.
- Confirmed `flutter devices` can see `Tharun Teja’s iPhone (wireless)` on iOS 26.5.
- Confirmed a real-device build starts and reaches Xcode signing.
- Earlier personal-team signing failed before Xcode showed the capability; retry the physical-device build now that Xcode lists `Family Controls (Development)`.

Verification completed:

- `flutter test test/widget_test.dart`
- `flutter analyze`
- `flutter build ios --simulator --debug`
- `plutil -lint ios/Runner/Runner.entitlements ios/Runner.xcodeproj/project.pbxproj`
- `flutter devices`
- `flutter run -d 00008130-000E08693609001C --debug --no-pub` reached device signing and exposed the Apple account capability blocker.
- After enabling the capability in Xcode, `flutter build ios --simulator --debug` succeeds with entitlements wired.

## Current Limitations

The feature does not yet display today's usage totals.

Reason: Apple usage reporting requires a Device Activity Report Extension target and Screen Time-related Xcode capabilities/entitlements. That target is not present yet. Creating it should be done in Xcode so the project file, build phases, extension bundle identifier, signing, and entitlements are correct.

The current native result after app/category selection is intentionally:

`Saved <n> Screen Time selections. Today's usage report needs the Device Activity Report Extension phase.`

## Phase 1B: iPhone Smoke Test

Goal: confirm the current native authorization and picker flow on a real iPhone.

Status: repo setup complete; waiting on a connected iPhone run to confirm the native picker flow.

Earlier blocker before Xcode showed `Family Controls (Development)`:

```text
Personal development teams, including "Tharun Teja Mogili", do not support the Family Controls (Development) capability.
Provisioning profile "iOS Team Provisioning Profile: com.mttauto.momentumApp" doesn't include the Family Controls (Development) capability.
Provisioning profile "iOS Team Provisioning Profile: com.mttauto.momentumApp" doesn't include the com.apple.developer.family-controls entitlement.
```

Important repo state:

- `ios/Runner/Runner.entitlements` exists and contains the Family Controls entitlement.
- `CODE_SIGN_ENTITLEMENTS = Runner/Runner.entitlements` is enabled for Runner Debug/Profile/Release.
- Xcode shows `Family Controls (Development)` under Runner > Signing & Capabilities.

Steps:

1. Open `momentum-app/ios/Runner.xcworkspace` in Xcode.
2. Confirm the Runner target has the Family Controls capability available for the signing team.
3. Run on a real iPhone with iOS 16 or newer.
4. Open Momentum.
5. Tap `Check My Digital Usage`.
6. Confirm Screen Time authorization appears when needed.
7. Confirm Apple's app/category picker opens.
8. Select one or more apps/categories.
9. Tap `Done`.
10. Confirm Momentum reports that selections were saved.

Expected result:

- No crash.
- Picker opens on iPhone.
- Selection count is shown after dismissing the picker.

## Phase 2: Usage Report Extension

Goal: show today's usage report for the selected apps/categories.

Native work:

- Add a new `Device Activity Report Extension` target in Xcode.
- Add or confirm required Screen Time capabilities.
- Add an App Group if the report extension needs shared access to the stored selection.
- Move selection storage from standard `UserDefaults` to App Group `UserDefaults` if required.
- Implement a `DeviceActivityReport` SwiftUI view filtered to today's date interval.
- Filter the report by the selected application, category, and web-domain tokens.
- Present the native report from the Flutter Digital Wellness flow after selection.

Flutter work:

- Update `ScreenTimeService` to distinguish:
  - authorization denied
  - no selection
  - selection saved
  - report available
  - report setup required
- Update `DigitalWellnessScreen` copy/states after the report is available.
- Add tests for no-selection and setup-required UI states.

Manual QA:

- Test real iPhone with no prior Screen Time permission.
- Test real iPhone after permission is denied.
- Test real iPhone after selecting apps.
- Test repeated open after selection is already saved.
- Test web still shows unsupported-platform message.

## Out Of Scope

Do not build yet:

- App blocking.
- App limits.
- Usage warnings.
- Backend storage.
- Cross-device sync.
- HealthKit integration.
- Authentication-gated wellness history.

## Implementation Notes

Flutter channel:

```dart
const MethodChannel('momentum/screen_time')
```

Current native method:

```text
checkTodayUsage
```

Current local iOS selection key:

```text
momentum.screenTime.selection
```

When the report extension is added, prefer App Group storage if the extension cannot read the selection from the app container.
