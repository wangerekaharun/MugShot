# MugShot Workshop - Step-by-Step Reference Guide

> **"Building Intelligent Camera Experiences on Android: CameraX meets MLKit & MediaPipe"**
>
> Duration: ~2 hours | 3 Checkpoints + 1 Bonus

---

## Table of Contents

1. [Workshop Overview](#1-workshop-overview)
2. [Project Structure Walkthrough](#2-project-structure-walkthrough)
3. [Pre-Built Classes (Already in main)](#3-pre-built-classes-already-in-main)
4. [Checkpoint 1: Camera Preview & Capture](#4-checkpoint-1-camera-preview--capture)
5. [Checkpoint 2: Face Detection & Overlays](#5-checkpoint-2-face-detection--overlays)
6. [Checkpoint 3: Auto-Capture & Confirmation](#6-checkpoint-3-auto-capture--confirmation)
7. [Bonus: MediaPipe Face Mesh](#7-bonus-mediapipe-face-mesh)
8. [Testing Checklist](#8-testing-checklist)

---

## 1. Workshop Overview

### What We're Building

MugShot - a selfie camera app that **refuses to take a bad photo**. It uses on-device ML to analyze face quality in real-time and only captures when conditions are optimal.

### Architecture at a Glance

A camera preview is a continuous stream of **frames** - individual images captured by the sensor, typically at **30 fps** (30 frames per second). Each frame is a snapshot of what the camera sees at that instant. CameraX delivers these frames to us as `ImageProxy` objects through the `ImageAnalysis` use case. We don't need to analyze every single frame - MLKit inference takes longer than 33ms (1/30s), and processing every frame would waste battery. So we skip frames and only analyze every 3rd one (~10 fps), which is more than enough for smooth real-time face tracking.

```
Camera Sensor (30 fps)
  |
  v
CameraX ImageAnalysis (every 3rd frame ~10 fps)
  |
  v
MLKit Face Detection --> FaceQuality (angles, smile, size, position)
  |
  v
Quality Analysis --> State Machine (NoFace/Poor/Adjusting/Stabilizing/Captured)
  |
  v
Auto-Capture + Rejection Messages + Overlays
```

### Starting Point

**Start on the `main` branch.** This is your starter scaffold with all stubs and pre-built model classes.

```bash
git checkout main
```

### Build Flow

Follow this progression - each step builds on the previous:

```
main (start here)
  |-- Build camera preview --> checkpoint-1
        |-- Add face detection & overlays --> checkpoint-2
              |-- Add auto-capture & confirmation --> checkpoint-3
                    |-- (Demo only) MediaPipe mesh --> bonus-mediapipe
```

### Branch Structure

| Branch | What's Built |
|--------|-------------|
| `main` | Starter scaffold with stubs (START HERE) |
| `checkpoint-1` | CameraX preview + manual capture |
| `checkpoint-2` | MLKit face detection + bounding box overlay + rejection messages |
| `checkpoint-3` | Auto-capture state machine + confirmation screen |
| `bonus-mediapipe` | MediaPipe 478-point face mesh overlay |

### If You Fall Behind

```bash
git stash
git checkout checkpoint-1   # or checkpoint-2, checkpoint-3
```

---

## 2. Project Structure Walkthrough

Open the project and walk through the package structure:

```
app/src/main/java/com/wangerekaharun/mugshot/
├── MainActivity.kt                    # Entry point, sets up edge-to-edge + theme
├── navigation/
│   └── MugShotNavigation.kt          # NavHost with camera & confirmation routes
├── model/
│   ├── FaceQuality.kt                # Data class with quality thresholds
│   ├── CaptureState.kt               # Sealed class: NoFace/Poor/Adjusting/Stabilizing/Captured
│   ├── QualityGrade.kt               # Enum: A+/B/C grades with colors
│   └── RejectionMessage.kt           # Witty message pools by rejection category
├── analysis/
│   ├── FaceAnalyzer.kt               # [STUB] MLKit face detection analyzer
│   └── CoordinateTransformer.kt      # [STUB] Image-to-Compose coordinate mapping
├── capture/
│   └── AutoCaptureStateMachine.kt    # [STUB] State machine for auto-capture
├── ui/
│   ├── theme/
│   │   ├── Color.kt                  # MugShotRed, MugShotYellow, MugShotGreen, etc.
│   │   ├── Theme.kt                  # Dark theme with edge-to-edge
│   │   └── Type.kt                   # Typography
│   ├── camera/
│   │   └── CameraScreen.kt           # [STUB] Main camera screen
│   ├── confirmation/
│   │   └── CaptureConfirmationScreen.kt  # [STUB] Post-capture review screen
│   └── components/
│       ├── CaptureButton.kt          # [PRE-BUILT] Animated capture button
│       ├── QualityBadge.kt           # [PRE-BUILT] Grade display badge
│       ├── FaceOverlay.kt            # [STUB] Bounding box overlay
│       ├── GuideOverlay.kt           # [STUB] Oval face guide
│       └── RejectionMessageDisplay.kt # [STUB] Animated rejection messages
```

**Key point:** Files marked `[STUB]` compile but do nothing. Files marked `[PRE-BUILT]` are fully implemented. Model classes are all complete.

---

## 3. Pre-Built Classes (Already in main)

Walk attendees through these classes so they understand the data model before writing any camera code.

### 3.1 FaceQuality.kt

The core data class that represents everything we know about a detected face.

**Key fields:**
- `boundingBox: RectF` - face position in image coordinates
- `eulerAngleX/Y/Z` - head rotation (pitch/yaw/tilt)
- `smilingProbability` - 0.0 to 1.0
- `leftEyeOpenProbability`, `rightEyeOpenProbability`
- `faceSizeRatio` - face area / frame area
- `centerOffsetX`, `centerOffsetY` - how far from center (-1.0 to 1.0)

**Computed properties (quality checks):**
- `isYawGood` - head not turned more than 15 degrees
- `isPitchGood` - head not tilted up/down more than 15 degrees
- `isTiltGood` - head not tilted sideways more than 15 degrees
- `isSmiling` - smile probability > 0.5
- `isFaceSizeGood` - face fills 3-30% of frame
- `isCentered` - face within 30% of center
- `isAllGood` - ALL checks pass (the auto-capture trigger)
- `passingSignalCount` - how many of 6 signals pass (used for state transitions)

**Thresholds (companion object):**
```
MAX_YAW = 15f, MAX_PITCH = 15f, MAX_TILT = 15f
MIN_SMILE_PROBABILITY = 0.5f
MIN_FACE_SIZE = 0.03f, MAX_FACE_SIZE = 0.30f
MAX_CENTER_OFFSET = 0.3f
```

### 3.2 CaptureState.kt

Sealed class representing the 5-state auto-capture flow:

```
NoFace -> Poor -> Adjusting -> Stabilizing -> Captured
```

| State | Meaning | Visual |
|-------|---------|--------|
| `NoFace` | No face detected | White guide oval |
| `Poor(quality)` | Face found, < 3 signals pass | Red overlay |
| `Adjusting(quality)` | 3+ signals pass, not all | Yellow overlay |
| `Stabilizing(quality, startTime, frames)` | All good, counting down | Green overlay + ring |
| `Captured(quality)` | Done! | Flash effect |

### 3.3 QualityGrade.kt

Simple enum for the confirmation screen:

| Grade | Label | Condition |
|-------|-------|-----------|
| `EXCELLENT` | "A+" | passingSignalCount >= 6 |
| `GOOD` | "B" | passingSignalCount >= 4 |
| `FAIR` | "C" | everything else |

Each grade has a color (green/yellow/red).

### 3.4 RejectionMessage.kt

Categorized pools of witty messages shown when quality checks fail:

**Categories (in priority order):**
1. `NO_FACE` - "Where did you go?", "Hello? Anyone there?"
2. `NOT_CENTERED` - "Center stage, please!", "A little to the left..."
3. `FACE_TOO_FAR` - "Come closer!", "I can barely see you!"
4. `FACE_TOO_CLOSE` - "Personal space!", "Back it up!"
5. `HEAD_TURNED` - "Eyes on me!", "Look this way!"
6. `HEAD_TILTED` - "Gravity works - use it!", "Level up - literally!"
7. `HEAD_PITCHED` - "Chin up! (or down)", "Look straight ahead!"
8. `NOT_SMILING` - "This is a mugshot, not a mug frown!", "Say cheese!"
9. `ALL_GOOD` - "Perfect! Hold it...", "Looking great! Don't move..."

`getActiveMessage(quality)` returns the highest-priority rejection for the current face quality.

### 3.5 CaptureButton.kt (Pre-built)

Animated circular button with press-scale effect. Already fully implemented - just use it in CameraScreen.

### 3.6 QualityBadge.kt (Pre-built)

Rounded badge that shows the quality grade letter with appropriate color. Used on the confirmation screen.

---

## 4. Checkpoint 1: Camera Preview & Capture

> **Goal:** Get a live camera preview on screen and capture a photo when the button is tapped.
>
> **Branch:** `checkpoint-1`
> **Time:** ~25 minutes

### What You're Building

- Camera permission request flow using Accompanist
- CameraX `Preview` and `ImageCapture` use cases
- `CameraXViewfinder` composable for rendering the preview
- Capture button that saves a JPEG to cache

### Find the TODO

Open `CameraScreen.kt` and find the TODO in `CameraContent()`:

```kotlin
// File: ui/camera/CameraScreen.kt (line 67)

@Composable
private fun CameraContent(
    onImageCaptured: (Uri, FaceQuality) -> Unit,
) {
    // TODO: Workshop Step 1 -- Set up CameraX ViewFinder here
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Ready to build MugShot!",
            ...
        )
    }
}
```

You'll replace this entire placeholder `Box` with the camera preview, use cases, and capture button.

**Note:** Permission handling (`CameraScreen` and `PermissionRequest`) is already implemented - you don't need to touch those.

### Step 1: Set Up Camera Use Cases

Replace the TODO placeholder in `CameraContent` with the following. Start by creating the camera use cases:

A **`SurfaceRequest`** is CameraX's way of asking for a drawing surface to render frames onto. When the camera is ready to start streaming, it emits a `SurfaceRequest` through `Preview.setSurfaceProvider`. We store this in Compose state and pass it to `CameraXViewfinder`, which creates the actual `Surface` and hands it back to CameraX. This is the bridge between CameraX's camera pipeline and Compose's UI - CameraX says "I need somewhere to draw", and the composable responds with a surface it controls.

```kotlin
val context = LocalContext.current
val lifecycleOwner = LocalLifecycleOwner.current
val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }

val imageCapture = remember {
    ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()
}

val preview = remember {
    Preview.Builder().build().apply {
        setSurfaceProvider { request ->
            surfaceRequest = request
        }
    }
}
```

### Step 2: Bind Camera to Lifecycle

Bind the use cases to the activity lifecycle so the camera starts/stops automatically:

```kotlin
LaunchedEffect(Unit) {
    val cameraProvider = ProcessCameraProvider.getInstance(context).get()
    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        cameraSelector,
        preview,
        imageCapture,
    )
}

DisposableEffect(Unit) {
    onDispose { cameraExecutor.shutdown() }
}
```

### Step 3: Build the UI Layer

Layer the camera preview and capture button:

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    // Camera preview
    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = Modifier.fillMaxSize(),
        )
    }

    // Capture button at bottom center
    CaptureButton(
        onClick = { /* Step 4 */ },
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 48.dp),
    )
}
```

### Step 4: Implement Photo Capture

Create a `takePicture` helper function (can be private at file level):

```kotlin
private fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService,
    onSaved: (Uri) -> Unit,
) {
    val outputFile = File(context.cacheDir, "mugshot_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val uri = Uri.fromFile(outputFile)
                // IMPORTANT: Dispatch to main thread for navigation
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onSaved(uri)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        }
    )
}
```

> **IMPORTANT:** The `onImageSaved` callback runs on the camera executor thread, but Compose navigation requires the main thread. Always dispatch via `Handler(Looper.getMainLooper()).post {}`.

Wire the button's onClick:

```kotlin
CaptureButton(
    onClick = {
        takePicture(context, imageCapture, cameraExecutor) { uri ->
            onImageCaptured(uri)
        }
    },
    ...
)
```

### Checkpoint 1 Verification

Run the app on a physical device:
- [ ] Camera permission dialog appears
- [ ] Live preview renders (front camera)
- [ ] Capture button is visible and responds to press
- [ ] Tapping capture saves a photo (check logcat)
- [ ] No crashes on rotation or backgrounding

---

## 5. Checkpoint 2: Face Detection & Overlays

> **Goal:** Detect faces in real-time using MLKit, draw bounding boxes, and show rejection messages.
>
> **Branch:** `checkpoint-2`
> **Time:** ~30 minutes

### What You're Building

- `FaceAnalyzer` - processes camera frames with MLKit
- `CoordinateTransformer` - maps image coords to screen coords
- `FaceOverlay` - draws colored bounding box
- `GuideOverlay` - state-colored oval guide
- `RejectionMessageDisplay` - debounced witty messages
- Updated `CameraScreen` with `ImageAnalysis` as third use case

### Find the TODOs

This checkpoint has 5 stub files to fill in. Here's where to find each TODO:

| File | TODO Location | What It Says |
|------|--------------|--------------|
| `analysis/FaceAnalyzer.kt` | Class doc (line 7) + `analyze()` (line 20) | `TODO: Workshop Step 2 -- Implement MLKit face detection` / `TODO: Implement face detection here` |
| `analysis/CoordinateTransformer.kt` | Class doc (line 8) + `transformRect()` (line 25) + `transformPoint()` (line 30) | `TODO: Workshop Step 2 -- Implement coordinate transformation` / `TODO: Implement coordinate transformation` / `TODO: Implement point transformation` |
| `ui/components/FaceOverlay.kt` | Class doc (line 9) + body (line 17) | `TODO: Workshop Step 2 -- Draw face bounding box overlay` / `TODO: Implement face bounding box overlay` |
| `ui/components/GuideOverlay.kt` | Class doc (line 16) | `TODO: Workshop Step 2 -- Make the color change based on capture state` (currently draws static white oval) |
| `ui/components/RejectionMessageDisplay.kt` | Class doc (line 8) + body (line 15) | `TODO: Workshop Step 2 -- Display rejection messages` / `TODO: Implement rejection message display` |

### Step 1: Implement FaceAnalyzer.kt

Open `analysis/FaceAnalyzer.kt`. The stub currently just closes the imageProxy without doing anything:

```kotlin
// Current stub:
override fun analyze(imageProxy: ImageProxy) {
    // TODO: Implement face detection here
    imageProxy.close() // Always close the imageProxy!
}
```

Replace this with MLKit face detection. This is the bridge between CameraX and MLKit.

**a) Create the MLKit detector:**

```kotlin
private val detector = FaceDetection.getClient(
    FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setMinFaceSize(0.25f)
        .build()
)
```

> **Talk point:** "FAST mode trades a bit of accuracy for speed - critical for real-time. We enable ALL landmarks and classifications because we need euler angles and smile probability."

**b) Add frame skipping (production pattern):**

```kotlin
private var frameSkipCounter = 0
private val analyzeEveryNFrames = 3
```

> **Talk point:** "We only analyze every 3rd frame. You don't need to process every frame - it wastes battery and ML inference is slower than the frame rate."

**c) Implement the `analyze()` method:**

```kotlin
@OptIn(ExperimentalGetImage::class)
override fun analyze(imageProxy: ImageProxy) {
    frameSkipCounter++
    if (frameSkipCounter % analyzeEveryNFrames != 0) {
        imageProxy.close()
        return
    }

    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val inputImage = InputImage.fromMediaImage(
        mediaImage,
        imageProxy.imageInfo.rotationDegrees
    )

    // Account for rotation when computing frame dimensions
    val rotation = imageProxy.imageInfo.rotationDegrees
    val frameWidth: Int
    val frameHeight: Int
    if (rotation == 90 || rotation == 270) {
        frameWidth = imageProxy.height
        frameHeight = imageProxy.width
    } else {
        frameWidth = imageProxy.width
        frameHeight = imageProxy.height
    }

    detector.process(inputImage)
        .addOnSuccessListener { faces ->
            if (faces.isEmpty()) {
                onFaceDetected(null, frameWidth, frameHeight)
            } else {
                val face = faces.maxByOrNull {
                    it.boundingBox.width() * it.boundingBox.height()
                } ?: return@addOnSuccessListener
                val quality = mapFaceToQuality(face, frameWidth, frameHeight)
                onFaceDetected(quality, frameWidth, frameHeight)
            }
        }
        .addOnFailureListener { onFaceDetected(null, frameWidth, frameHeight) }
        .addOnCompleteListener { imageProxy.close() }
}
```

> **CRITICAL:** Always close `imageProxy` - in `addOnCompleteListener`, not before. If you close it before MLKit finishes, you get a crash. And you **must** close it every time - CameraX uses a backpressure strategy with a limited buffer. If you don't call `imageProxy.close()`, CameraX thinks you're still processing that frame and won't deliver new ones. Your camera feed will freeze because no new frames flow through the analysis pipeline. One leaked proxy = dead analyzer.

**d) Map MLKit Face to FaceQuality:**

```kotlin
private fun mapFaceToQuality(face: Face, frameWidth: Int, frameHeight: Int): FaceQuality {
    val box = face.boundingBox
    val faceArea = box.width().toFloat() * box.height().toFloat()
    val frameArea = frameWidth.toFloat() * frameHeight.toFloat()

    val faceCenterX = box.centerX().toFloat()
    val faceCenterY = box.centerY().toFloat()
    val frameCenterX = frameWidth / 2f
    val frameCenterY = frameHeight / 2f

    return FaceQuality(
        boundingBox = RectF(box.left.toFloat(), box.top.toFloat(),
                           box.right.toFloat(), box.bottom.toFloat()),
        eulerAngleX = face.headEulerAngleX,
        eulerAngleY = face.headEulerAngleY,
        eulerAngleZ = face.headEulerAngleZ,
        smilingProbability = face.smilingProbability ?: 0f,
        leftEyeOpenProbability = face.leftEyeOpenProbability ?: 0f,
        rightEyeOpenProbability = face.rightEyeOpenProbability ?: 0f,
        detectionConfidence = face.trackingId?.toFloat() ?: 1f,
        faceSizeRatio = faceArea / frameArea,
        centerOffsetX = (faceCenterX - frameCenterX) / frameCenterX,
        centerOffsetY = (faceCenterY - frameCenterY) / frameCenterY,
    )
}
```

### Step 2: Implement CoordinateTransformer.kt

Open `analysis/CoordinateTransformer.kt`. The stub currently returns `Rect.Zero` and `Offset.Zero` for everything:

```kotlin
// Current stub:
fun transformRect(imageRect: RectF): Rect {
    // TODO: Implement coordinate transformation
    return Rect.Zero
}

fun transformPoint(x: Float, y: Float): Offset {
    // TODO: Implement point transformation
    return Offset.Zero
}
```

Replace these with actual coordinate mapping from image space to Compose UI space.

```kotlin
class CoordinateTransformer(
    private val imageWidth: Int,
    private val imageHeight: Int,
    private val viewWidth: Float,
    private val viewHeight: Float,
    private val isFrontCamera: Boolean = true,
) {
    private val scaleX: Float = viewWidth / imageWidth.toFloat()
    private val scaleY: Float = viewHeight / imageHeight.toFloat()

    fun transformRect(imageRect: RectF): Rect {
        val left: Float
        val right: Float

        if (isFrontCamera) {
            // Mirror horizontally for front camera
            left = viewWidth - (imageRect.right * scaleX)
            right = viewWidth - (imageRect.left * scaleX)
        } else {
            left = imageRect.left * scaleX
            right = imageRect.right * scaleX
        }

        val top = imageRect.top * scaleY
        val bottom = imageRect.bottom * scaleY

        return Rect(left = left, top = top, right = right, bottom = bottom)
    }

    fun transformPoint(x: Float, y: Float): Offset {
        val transformedX = if (isFrontCamera) {
            viewWidth - (x * scaleX)
        } else {
            x * scaleX
        }
        return Offset(transformedX, y * scaleY)
    }
}
```

> **Talk point:** "Why do we need this transformation? MLKit detects faces in the **camera sensor's coordinate space** - typically 640x480 or 1280x720 pixels, rotated 90 degrees on most phones. But our Compose UI renders in **screen coordinates** - say 1080x2400 density-independent pixels. These are completely different scales and orientations. Without this transformer, the bounding box would be tiny, offset, and in the wrong position. On top of that, the front camera image is **mirrored** - what's on your left appears on the right in the raw image. We flip the X axis so the overlay matches what you actually see in the preview. The coordinate transformation is the hardest part. The front camera mirroring is where bugs hide. In production, you'd write unit tests for this."

### Step 3: Implement FaceOverlay.kt

Open `ui/components/FaceOverlay.kt`. The stub is an empty composable:

```kotlin
// Current stub:
@Composable
fun FaceOverlay(
    boundingBox: Rect?,
    captureState: CaptureState,
    modifier: Modifier = Modifier,
) {
    // TODO: Implement face bounding box overlay
}
```

Replace with a Canvas that draws a color-coded rounded rectangle around the detected face:

```kotlin
@Composable
fun FaceOverlay(
    boundingBox: Rect?,
    captureState: CaptureState,
    modifier: Modifier = Modifier,
) {
    val boxColor = when (captureState) {
        is CaptureState.NoFace -> Color.Transparent
        is CaptureState.Poor -> MugShotRed
        is CaptureState.Adjusting -> MugShotYellow
        is CaptureState.Stabilizing -> MugShotGreen
        is CaptureState.Captured -> MugShotGreen
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        boundingBox?.let { box ->
            drawRoundRect(
                color = boxColor,
                topLeft = Offset(box.left, box.top),
                size = Size(box.width, box.height),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 8f),
            )
        }
    }
}
```

### Step 4: Implement GuideOverlay.kt

Open `ui/components/GuideOverlay.kt`. The stub already draws a static white oval:

```kotlin
// Current stub - draws oval but always white:
// TODO: Workshop Step 2 -- Make the color change based on capture state
drawOval(
    color = Color.White.copy(alpha = 0.3f),  // Always white - needs to be dynamic
    ...
)
```

Update the color to change based on capture state:

```kotlin
@Composable
fun GuideOverlay(
    captureState: CaptureState,
    modifier: Modifier = Modifier,
) {
    val guideColor = when (captureState) {
        is CaptureState.NoFace -> Color.White.copy(alpha = 0.3f)
        is CaptureState.Poor -> MugShotRed.copy(alpha = 0.6f)
        is CaptureState.Adjusting -> MugShotYellow.copy(alpha = 0.6f)
        is CaptureState.Stabilizing -> MugShotGreen.copy(alpha = 0.8f)
        is CaptureState.Captured -> MugShotGreen
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val ovalWidth = size.width * 0.6f
        val ovalHeight = size.height * 0.45f
        val centerX = size.width / 2
        val centerY = size.height * 0.4f

        drawOval(
            color = guideColor,
            topLeft = Offset(centerX - ovalWidth / 2, centerY - ovalHeight / 2),
            size = Size(ovalWidth, ovalHeight),
            style = Stroke(width = 8f),
        )
    }
}
```

### Step 5: Implement RejectionMessageDisplay.kt

Open `ui/components/RejectionMessageDisplay.kt`. The stub is an empty composable:

```kotlin
// Current stub:
@Composable
fun RejectionMessageDisplay(
    quality: FaceQuality?,
    modifier: Modifier = Modifier,
) {
    // TODO: Implement rejection message display
}
```

Replace with debounced animated text that shows rejection messages:

```kotlin
@Composable
fun RejectionMessageDisplay(
    quality: FaceQuality?,
    modifier: Modifier = Modifier,
) {
    var displayedMessage by remember { mutableStateOf("") }
    var lastUpdateTime by remember { mutableLongStateOf(0L) }
    val debounceInterval = 1500L

    LaunchedEffect(quality) {
        val now = System.currentTimeMillis()
        if (now - lastUpdateTime > debounceInterval) {
            val (_, message) = RejectionMessage.getActiveMessage(quality)
            displayedMessage = message
            lastUpdateTime = now
        }
    }

    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = displayedMessage,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "rejection_message",
        ) { message ->
            Text(
                text = message,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
    }
}
```

> **Talk point:** "We debounce at 1.5 seconds so messages don't flash rapidly. Without this, the text would change every frame and be unreadable."

### Step 6: Update CameraScreen.kt

Add ImageAnalysis as a third use case and wire up all the overlays.

**a) Add state variables:**

```kotlin
var faceQuality by remember { mutableStateOf<FaceQuality?>(null) }
var imageWidth by remember { mutableIntStateOf(0) }
var imageHeight by remember { mutableIntStateOf(0) }
var viewSize by remember { mutableStateOf(Size.Zero) }
```

**b) Create the ImageAnalysis use case:**

```kotlin
val imageAnalysis = remember {
    ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.BACKPRESSURE_STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .apply {
            setAnalyzer(cameraExecutor, FaceAnalyzer { quality, imgW, imgH ->
                faceQuality = quality
                imageWidth = imgW
                imageHeight = imgH
            })
        }
}
```

**c) Bind it to the camera (add to `bindToLifecycle`):**

```kotlin
cameraProvider.bindToLifecycle(
    lifecycleOwner,
    cameraSelector,
    preview,
    imageCapture,
    imageAnalysis,  // NEW
)
```

**d) Get view dimensions with `onGloballyPositioned`:**

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned { coordinates ->
            viewSize = Size(
                coordinates.size.width.toFloat(),
                coordinates.size.height.toFloat()
            )
        }
) { ... }
```

**e) Compute bounding box and add overlays:**

```kotlin
// Compute transformed bounding box
val transformedBox = remember(faceQuality, imageWidth, imageHeight, viewSize) {
    if (faceQuality != null && imageWidth > 0 && viewSize.width > 0) {
        val transformer = CoordinateTransformer(
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            viewWidth = viewSize.width,
            viewHeight = viewSize.height,
        )
        transformer.transformRect(faceQuality!!.boundingBox)
    } else null
}

// Determine capture state (simple version for checkpoint-2)
val captureState = when {
    faceQuality == null -> CaptureState.NoFace
    faceQuality!!.passingSignalCount >= 5 -> CaptureState.Adjusting(faceQuality!!)
    faceQuality!!.passingSignalCount >= 3 -> CaptureState.Adjusting(faceQuality!!)
    else -> CaptureState.Poor(faceQuality!!)
}

// Layer overlays on top of camera preview
Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { ... }) {
    surfaceRequest?.let { request ->
        CameraXViewfinder(surfaceRequest = request, modifier = Modifier.fillMaxSize())
    }

    // Overlays
    GuideOverlay(captureState = captureState)
    FaceOverlay(boundingBox = transformedBox, captureState = captureState)

    // Rejection message at top
    RejectionMessageDisplay(
        quality = faceQuality,
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp),
    )

    // Capture button still at bottom
    CaptureButton(
        onClick = { takePicture(...) { uri -> onImageCaptured(uri) } },
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
    )
}
```

### Checkpoint 2 Verification

Run the app:
- [ ] Face bounding box appears when face is visible
- [ ] Box tracks face movement smoothly
- [ ] Box color changes: red (poor) / yellow (adjusting) / green (good)
- [ ] Rejection messages appear with debouncing
- [ ] Guide oval changes color with state
- [ ] "Where did you go?" appears when no face
- [ ] Manual capture button still works
- [ ] Moving off-center shows centering messages

---

## 6. Checkpoint 3: Auto-Capture & Confirmation

> **Goal:** Build the state machine that auto-captures when quality is stable, add confirmation screen.
>
> **Branch:** `checkpoint-3`
> **Time:** ~30 minutes

### What You're Building

- `AutoCaptureStateMachine` - drives state transitions
- Countdown ring animation during Stabilizing
- Shutter flash effect on Captured
- Haptic feedback
- `CaptureConfirmationScreen` with quality badge and retake
- Navigation from camera to confirmation

### Find the TODOs

This checkpoint has 2 stub files to fill in:

| File | TODO Location | What It Says |
|------|--------------|--------------|
| `capture/AutoCaptureStateMachine.kt` | Class doc (line 7) + `onNewFrame()` (line 26) + `reset()` (line 31) | `TODO: Workshop Step 3 -- Implement the auto-capture state machine` / `TODO: Implement state transitions` / `TODO: Reset state` |
| `ui/confirmation/CaptureConfirmationScreen.kt` | Class doc (line 13) + body (line 27) | `TODO: Workshop Step 3 -- Build the capture confirmation screen` / `TODO: Implement confirmation screen` |

### Step 1: Implement AutoCaptureStateMachine.kt

Open `capture/AutoCaptureStateMachine.kt`. The stub has the right signature but returns minimal states:

```kotlin
// Current stub:
class AutoCaptureStateMachine(
    private val stabilizationDurationMs: Long = 1500L,
    private val requiredConsecutiveFrames: Int = 3,
    private val enableSmileGate: Boolean = true,
) {
    fun getCurrentState(): CaptureState = CaptureState.NoFace  // Always NoFace!

    fun onNewFrame(quality: FaceQuality?): CaptureState {
        // TODO: Implement state transitions
        return if (quality == null) CaptureState.NoFace else CaptureState.Poor(quality)  // Only Poor!
    }

    fun reset() {
        // TODO: Reset state
    }
}
```

The class doc already describes the transition rules you need to implement:
- `NoFace -> Poor`: face detected
- `Poor -> Adjusting`: >= 3 of 5 quality signals pass
- `Adjusting -> Stabilizing`: all signals pass for N consecutive frames
- `Stabilizing -> Captured`: all signals hold for stabilizationDurationMs
- `Any -> NoFace`: face lost

Replace with the full state machine that tracks consecutive good frames and a stabilization timer:

```kotlin
class AutoCaptureStateMachine(
    private val stabilizationDurationMs: Long = 1500L,
    private val requiredConsecutiveFrames: Int = 3,
    private val enableSmileGate: Boolean = true,
) {
    private var currentState: CaptureState = CaptureState.NoFace
    private var consecutiveGoodFrames: Int = 0
    private var stabilizationStartTime: Long = 0L

    fun getCurrentState(): CaptureState = currentState

    fun onNewFrame(quality: FaceQuality?): CaptureState {
        if (currentState is CaptureState.Captured) return currentState

        if (quality == null) {
            consecutiveGoodFrames = 0
            stabilizationStartTime = 0L
            currentState = CaptureState.NoFace
            return currentState
        }

        val allGood = if (enableSmileGate) quality.isAllGood else quality.isAllGoodExceptSmile

        currentState = when {
            allGood -> handleAllGood(quality)
            quality.passingSignalCount >= 3 -> {
                consecutiveGoodFrames = 0
                stabilizationStartTime = 0L
                CaptureState.Adjusting(quality)
            }
            else -> {
                consecutiveGoodFrames = 0
                stabilizationStartTime = 0L
                CaptureState.Poor(quality)
            }
        }

        return currentState
    }

    private fun handleAllGood(quality: FaceQuality): CaptureState {
        consecutiveGoodFrames++

        if (consecutiveGoodFrames >= requiredConsecutiveFrames) {
            if (stabilizationStartTime == 0L) {
                stabilizationStartTime = System.currentTimeMillis()
            }

            val elapsed = System.currentTimeMillis() - stabilizationStartTime
            return if (elapsed >= stabilizationDurationMs) {
                CaptureState.Captured(quality)
            } else {
                CaptureState.Stabilizing(
                    quality = quality,
                    startTimeMillis = stabilizationStartTime,
                    consecutiveGoodFrames = consecutiveGoodFrames,
                )
            }
        }

        return CaptureState.Adjusting(quality)
    }

    fun reset() {
        currentState = CaptureState.NoFace
        consecutiveGoodFrames = 0
        stabilizationStartTime = 0L
    }
}
```

> **Talk point:** "This state machine pattern is exactly how identity verification SDKs like SmileID work. The concept generalizes to any 'smart capture' scenario."

**Transition rules summary:**
- `NoFace -> Poor`: face detected but quality low
- `Poor -> Adjusting`: 3+ of 6 quality signals pass
- `Adjusting -> Stabilizing`: ALL signals pass for 3 consecutive frames
- `Stabilizing -> Captured`: all signals hold for 1.5 seconds
- `Stabilizing -> Poor/Adjusting`: quality drops (moving during countdown resets it)
- `Any -> NoFace`: face lost

### Step 2: Implement CaptureConfirmationScreen.kt

Open `ui/confirmation/CaptureConfirmationScreen.kt`. The stub shows placeholder text:

```kotlin
// Current stub:
/**
 * TODO: Workshop Step 3 -- Build the capture confirmation screen
 *
 * This screen should:
 * 1. Display the captured image using Coil
 * 2. Show a QualityBadge with the grade
 * 3. Offer Retake and Accept buttons
 */
@Composable
fun CaptureConfirmationScreen(
    imageUri: Uri,
    quality: FaceQuality,
    onAccept: () -> Unit,
    onRetake: () -> Unit,
) {
    // TODO: Implement confirmation screen
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Capture confirmation coming in Step 3!")
    }
}
```

The class doc tells you exactly what to build. Replace with the full confirmation screen:

```kotlin
@Composable
fun CaptureConfirmationScreen(
    imageUri: Uri,
    quality: FaceQuality,
    onAccept: () -> Unit,
    onRetake: () -> Unit,
) {
    val grade = QualityGrade.fromQuality(quality)

    Box(modifier = Modifier.fillMaxSize().background(MugShotDark)) {
        // Captured image
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = "Captured selfie",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        )

        // Quality badge (top-right corner)
        QualityBadge(
            grade = grade,
            modifier = Modifier.align(Alignment.TopEnd).padding(24.dp).statusBarsPadding(),
        )

        // Action buttons (bottom)
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            OutlinedButton(onClick = onRetake, modifier = Modifier.weight(1f).height(56.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Retake")
            }
            Spacer(Modifier.width(16.dp))
            Button(onClick = onAccept, modifier = Modifier.weight(1f).height(56.dp)) {
                Icon(Icons.Default.ThumbUp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Looks Good!")
            }
        }
    }
}
```

### Step 3: Update CameraScreen.kt with Auto-Capture

Replace the simple state derivation from checkpoint-2 with the state machine:

**a) Create the state machine:**

```kotlin
val autoCapture = remember { AutoCaptureStateMachine() }
```

**b) Update face quality callback to use state machine:**

```kotlin
FaceAnalyzer { quality, imgW, imgH ->
    faceQuality = quality
    imageWidth = imgW
    imageHeight = imgH
    captureState = autoCapture.onNewFrame(quality)
}
```

**c) React to Captured state:**

```kotlin
LaunchedEffect(captureState) {
    if (captureState is CaptureState.Captured) {
        // Haptic feedback
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))

        // Trigger capture
        takePicture(context, imageCapture, cameraExecutor) { uri ->
            onImageCaptured(uri)
        }
    }
}
```

**d) Add shutter flash effect:**

```kotlin
var showFlash by remember { mutableStateOf(false) }

LaunchedEffect(captureState) {
    if (captureState is CaptureState.Captured) {
        showFlash = true
        delay(150)
        showFlash = false
    }
}

// In the UI
if (showFlash) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White))
}
```

### Step 4: Update Navigation

Update `MugShotNavigation.kt` to handle the confirmation route. Use remembered state to pass URI and quality between screens:

```kotlin
var capturedUri by remember { mutableStateOf<Uri?>(null) }
var capturedQuality by remember { mutableStateOf<FaceQuality?>(null) }

NavHost(navController = navController, startDestination = "camera") {
    composable("camera") {
        CameraScreen(
            onImageCaptured = { uri, quality ->
                capturedUri = uri
                capturedQuality = quality
                navController.navigate("confirmation")
            }
        )
    }
    composable("confirmation") {
        if (capturedUri != null && capturedQuality != null) {
            CaptureConfirmationScreen(
                imageUri = capturedUri!!,
                quality = capturedQuality!!,
                onRetake = { navController.popBackStack() },
                onAccept = { /* Done! */ },
            )
        }
    }
}
```

### Checkpoint 3 Verification

Run the app:
- [ ] Auto-capture triggers after face is stable + smiling for ~1.5s
- [ ] Moving during countdown resets it
- [ ] Flash effect on capture
- [ ] Haptic feedback on capture
- [ ] Confirmation screen shows the captured image
- [ ] Quality badge displays correct grade (A+/B/C)
- [ ] "Retake" returns to camera
- [ ] "Looks Good!" accepts
- [ ] Manual capture button still works as fallback

---

## 7. Bonus: MediaPipe Face Mesh

> **Goal:** Add a 478-point face mesh wireframe overlay using MediaPipe. This is a demo - attendees watch, not build.
>
> **Branch:** `bonus-mediapipe`
> **Time:** ~15 minutes (demo only)

### What's Added

- `MediaPipeFaceMeshAnalyzer` - runs Face Landmarker in LIVE_STREAM mode
- `FaceMeshOverlay` - renders 478 landmark dots on the face
- `face_landmarker.task` model file in `assets/`

### MediaPipeFaceMeshAnalyzer.kt

```kotlin
class MediaPipeFaceMeshAnalyzer(context: Context) {
    private val faceLandmarker: FaceLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("face_landmarker.task")
            .build()

        val options = FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setNumFaces(1)
            .setMinFaceDetectionConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, _ -> latestResult = result }
            .build()

        faceLandmarker = FaceLandmarker.createFromOptions(context, options)
    }

    var latestResult: FaceLandmarkerResult? = null
        private set

    fun detectAsync(bitmap: Bitmap, timestampMs: Long) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        faceLandmarker.detectAsync(mpImage, timestampMs)
    }

    fun close() { faceLandmarker.close() }
}
```

### FaceMeshOverlay.kt

```kotlin
@Composable
fun FaceMeshOverlay(
    result: FaceLandmarkerResult?,
    viewWidth: Float,
    viewHeight: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        result?.faceLandmarks()?.firstOrNull()?.let { landmarks ->
            landmarks.forEach { landmark ->
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.9f),
                    radius = 4f,
                    center = Offset(landmark.x() * viewWidth, landmark.y() * viewHeight),
                )
            }
        }
    }
}
```

> **Talk point:** "478 landmarks vs MLKit's ~7. But MLKit is way simpler to integrate. Choose based on what you need. For identity verification, MLKit is usually enough. For AR effects or face filters, you need MediaPipe."

---

## 8. Testing Checklist

### Quick Smoke Test (Each Checkpoint)

| Test | CP1 | CP2 | CP3 | Bonus |
|------|-----|-----|-----|-------|
| App launches without crash | x | x | x | x |
| Camera permission flow works | x | x | x | x |
| Live preview renders | x | x | x | x |
| Manual capture works | x | x | x | x |
| Face bounding box appears | - | x | x | x |
| Box color changes with quality | - | x | x | x |
| Rejection messages show | - | x | x | x |
| Auto-capture triggers | - | - | x | x |
| Countdown resets on movement | - | - | x | x |
| Confirmation screen appears | - | - | x | x |
| Retake works | - | - | x | x |
| Face mesh dots render | - | - | - | x |

### Cross-Branch Check

- [ ] Can switch between any two branches and build successfully
- [ ] Each branch's README accurately describes what's built
- [ ] No leftover files or merge artifacts between branches

---

## Presenter Notes

Key things to emphasize at each checkpoint:

1. **Checkpoint 1:** "Notice we didn't touch Camera2 at all. CameraX handles all the complexity. The `CameraXViewfinder` composable is the Compose-native way to show a camera preview."

2. **Checkpoint 2:** "The coordinate transformation is the hardest part. In production, you'd write tests for this. The front camera mirroring is where bugs hide." Also: "We're only analyzing every 3rd frame. This is a production pattern."

3. **Checkpoint 3:** "This state machine pattern is exactly how identity verification SDKs work. The concept generalizes to any 'smart capture' scenario."

4. **Bonus:** "478 landmarks vs MLKit's ~7. Choose based on what you need."
