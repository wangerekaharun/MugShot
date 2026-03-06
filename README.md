# MugShot -- Checkpoint 1

> Camera preview and capture working!

## What's Built
- CameraX Preview with Compose ViewFinder (`CameraXViewfinder`)
- ImageCapture use case bound to front camera
- Manual capture button (saves JPEG to cache)
- Static white oval guide overlay
- Accompanist camera permission flow

## What's Next: Add Face Detection (Step 2)
In this step you'll:
1. Implement `FaceAnalyzer.kt` with MLKit face detection
2. Implement `CoordinateTransformer.kt` for mapping image coords to screen coords
3. Add `FaceOverlay.kt` to draw bounding boxes
4. Add `RejectionMessageDisplay.kt` for playful rejection messages
5. Update `GuideOverlay.kt` to change color based on quality state
6. Bind `ImageAnalysis` as a third use case in `CameraScreen.kt`

## Key Files to Open
| File | Status |
|------|--------|
| `CameraScreen.kt` | Working -- camera preview + capture |
| `FaceAnalyzer.kt` | Stub -- implement MLKit here |
| `CoordinateTransformer.kt` | Stub -- implement transforms here |
| `FaceOverlay.kt` | Stub -- implement bounding box here |
| `RejectionMessageDisplay.kt` | Stub -- implement messages here |

## How to Verify
1. Run the app on your physical device
2. Camera preview should show your face
3. Tap the capture button -- image saves (check logcat)
4. White oval guide is visible on screen
