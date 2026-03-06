# MugShot -- Checkpoint 3

> Auto-capture state machine working!

## What's Built
- Everything from Checkpoints 1 and 2, plus:
- `AutoCaptureStateMachine` with full state transitions (NoFace -> Poor -> Adjusting -> Stabilizing -> Captured)
- Countdown ring animation during Stabilizing state (1.5s hold)
- Shutter flash effect on auto-capture
- Haptic feedback on capture
- `CaptureConfirmationScreen` with captured image display, quality grade badge, retake/accept buttons
- Navigation between camera and confirmation screens
- Manual capture button still works as fallback

## What to Explore Next
- Check out the `bonus-mediapipe` branch to see 478-point face mesh overlay
- Try tuning quality thresholds in `FaceQuality.kt`
- Experiment with `stabilizationDurationMs` in `AutoCaptureStateMachine`
- Try disabling the smile gate: `AutoCaptureStateMachine(enableSmileGate = false)`

## Key Files
| File | What It Does |
|------|-------------|
| `AutoCaptureStateMachine.kt` | State machine driving auto-capture |
| `CaptureConfirmationScreen.kt` | Shows captured image with quality grade |
| `CameraScreen.kt` | Full camera UI with all overlays and auto-capture |
| `FaceQuality.kt` | Quality thresholds -- tune these! |

## How to Verify
1. Run the app on your physical device
2. Face the camera, center your face, and smile
3. After ~1.5s of holding steady, auto-capture triggers
4. Confirmation screen shows your photo with a grade (A+/B/C)
5. Moving during countdown resets it
6. Manual capture button still works
