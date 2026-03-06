# MugShot

> The camera that refuses to take a bad photo.

Workshop project for **"Building Intelligent Camera Experiences on Android: CameraX meets MLKit & MediaPipe"** at Build with AI -- GDG Kisumu Dala.

## Setup Instructions

### Prerequisites
- Android Studio (latest stable, Ladybug or newer)
- Physical Android device with USB debugging enabled
- USB cable

### Steps
1. Clone this repo:
   ```bash
   git clone https://github.com/<your-username>/MugShot.git
   cd MugShot
   ```
2. Open in Android Studio
3. Wait for Gradle sync to complete
4. Connect your physical device
5. Run the app -- you should see "Ready to build MugShot!"

### If You Fall Behind
Each workshop checkpoint has a corresponding branch. If you get stuck:
```bash
git stash
git checkout checkpoint-1   # or checkpoint-2, checkpoint-3
```

### Branches
| Branch | What's Built |
|--------|-------------|
| `main` | Starter scaffold (you are here) |
| `checkpoint-1` | CameraX preview + capture |
| `checkpoint-2` | MLKit face detection + overlay |
| `checkpoint-3` | Auto-capture state machine |
| `bonus-mediapipe` | MediaPipe face mesh demo |

## Workshop Outline
1. **Camera Preview (25 min):** Set up CameraX ViewFinder with Compose
2. **Face Detection (30 min):** Add MLKit + bounding box overlay
3. **Auto-Capture (30 min):** Build the smart capture state machine
4. **MediaPipe Demo (15 min):** See 478-point face mesh in action

## What You'll Build
Open `CameraScreen.kt` -- that's where we start!

## Key Constraints
- Physical Android devices required (no emulator camera)
- No network calls (MLKit runs on-device)
- Single Activity with Compose Navigation
- Minimum SDK 24

## Tech Stack
- **CameraX** with Compose ViewFinder (`camera-compose`)
- **MLKit** Face Detection (on-device)
- **MediaPipe** Face Landmarker (bonus)
- **Jetpack Compose** with Material 3
- **Accompanist** for runtime permissions
