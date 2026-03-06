# MugShot -- Bonus: MediaPipe Face Mesh

> Full app with 478-point face mesh overlay!

## What's Built
- Everything from Checkpoint 3 (complete app), plus:
- `MediaPipeFaceMeshAnalyzer` using MediaPipe Face Landmarker
- `FaceMeshOverlay` rendering 478 cyan landmark dots over the face in real-time
- `face_landmarker.task` model (~4MB) in assets
- MLKit still handles face detection for auto-capture; MediaPipe adds the visual mesh

## How It Works
- The `ImageAnalysis` pipeline converts each frame to a bitmap
- The bitmap is fed to both MLKit (for quality assessment) and MediaPipe (for mesh rendering)
- MediaPipe runs in `LIVE_STREAM` mode with async result delivery
- The mesh overlay renders as a constellation of 478 cyan dots on the face

## Key Files
| File | What It Does |
|------|-------------|
| `MediaPipeFaceMeshAnalyzer.kt` | Wraps MediaPipe Face Landmarker |
| `FaceMeshOverlay.kt` | Renders 478-point face mesh dots |
| `CameraScreen.kt` | Integrates both MLKit and MediaPipe pipelines |

## Comparison: MLKit vs MediaPipe
| Feature | MLKit | MediaPipe |
|---------|-------|-----------|
| Landmarks | ~7 key points | 478 mesh points |
| Setup complexity | Simple | Moderate (model file required) |
| Classification | Smile, eyes open | Not built-in |
| Best for | Quality assessment | AR effects, mesh visualization |

## How to Verify
1. Run the app on your physical device
2. You should see cyan dots forming a face mesh overlay
3. All auto-capture features from Checkpoint 3 still work
4. The mesh disappears when no face is detected
