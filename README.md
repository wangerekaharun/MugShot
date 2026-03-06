# MugShot -- Checkpoint 2

> Face detection and quality feedback working!

## What's Built
- Everything from Checkpoint 1, plus:
- MLKit face detection via `FaceAnalyzer` (processes every 3rd frame)
- `CoordinateTransformer` mapping image coords to Compose coords (with front camera mirroring)
- Face bounding box overlay (color-coded: red/yellow/green)
- Guide oval that changes color based on face quality state
- Playful rejection messages with debounced display
- Debug quality readout (euler angles, smile probability)
- Manual capture still works

## What's Next: Build Auto-Capture (Step 3)
In this step you'll:
1. Implement `AutoCaptureStateMachine.kt` with state transitions (NoFace -> Poor -> Adjusting -> Stabilizing -> Captured)
2. Add countdown ring animation during Stabilizing state
3. Add shutter flash effect on capture
4. Add haptic feedback on capture
5. Build `CaptureConfirmationScreen.kt` with quality grade badge
6. Wire up navigation between camera and confirmation screens

## Key Files to Open
| File | Status |
|------|--------|
| `AutoCaptureStateMachine.kt` | Stub -- implement state machine here |
| `CaptureConfirmationScreen.kt` | Stub -- implement confirmation UI here |
| `CameraScreen.kt` | Working -- add auto-capture triggering |
| `GuideOverlay.kt` | Working -- add countdown ring |

## How to Verify
1. Run the app on your physical device
2. Face bounding box should appear and track your face
3. Bounding box color changes based on quality (red -> yellow -> green)
4. Rejection messages appear when conditions aren't met
5. Manual capture button still works
