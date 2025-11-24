# **FLAM – Real-Time Android + OpenCV (C++) + OpenGL ES + Web Viewer**

A complete real-time image-processing pipeline built using Android (Kotlin), NDK/C++, OpenCV, OpenGL ES 2.0, and a TypeScript Web Viewer.

This project implements every requirement from the Android + OpenCV-C++ + OpenGL + Web R&D Internship Assessment.

---

# **Features Implemented**

## **Android (Kotlin + Native)**

- Real-time camera feed using **Camera2 (NV21 image stream)**
- JNI bridge for sending **NV21 → C++** and receiving **RGBA buffer**
- Full OpenCV C++ processing:
  - RAW (RGBA)
  - Grayscale  
  - Canny Edge Detection (live sliders T1/T2)  
  - Threshold mode  
  - Motion Detection  
    - absdiff → threshold → contours  
    - red bounding boxes drawn directly on RGBA in native code
- **OpenGL ES 2.0 renderer**
  - GLSurfaceView + custom GLES2 renderer
  - Texture upload using ByteBuffer
  - Triangle-strip quad rendering
  - RENDERMODE_WHEN_DIRTY for low-latency
- **Stats panel (on device)**
  - FPS
  - Frame processing time (ms)
  - Resolution
- **Snapshot capture**
  - Rotate → JPEG → Base64
  - Auto-send to WebSocket server

---

## **Native (C++ / OpenCV / JNI)**

- NV21 → BGR → Gray → RGBA conversions
- Canny (thresholds from UI)
- Threshold binary
- Motion detection with bounding rectangles
- Light morphology (noise reduction)
- All processing done in native C++ exactly as the assignment requires
- Efficient ByteBuffer return → Kotlin → OpenGL renderer

---

## **OpenGL ES Renderer**

- RGBA texture creation
- Dynamic reallocation on resolution change
- Texture upload via glTexSubImage2D
- Full-screen quad rendering
- Clean separation inside `/app/gl/`

---

## **Web Viewer (TypeScript)**

- Clean modular `/web/viewer` folder (TS + HTML)
- Live WebSocket frame display (**Base64 JPEG**)
- Fixed preview box (400×400, object-fit contain)
- Shows:
  - Resolution
  - Timestamp
- Build system using TypeScript (`npm run serv`)
- Viewer dev/build using:
  - `npm run serv` (viewer)
  - `npm run dev` (server)

Assignment requires a TS-based web page that displays processed frames, which is implemented fully.

---

# **Architecture (Short & Clear)**


```txt
Camera (NV21)
        ↓
Kotlin (FrameExtractor)
        ↓
JNI (NativeBridge)
        ↓
C++ OpenCV Processing
      • Grayscale
      • Canny
      • Threshold
      • Motion Detection + Rectangles
        ↓
RGBA ByteArray
        ↓
OpenGL ES 2.0 Renderer (GLSurfaceView)
        ↓
Device Screen

Snapshot
        ↓
Rotate + JPEG + Base64
        ↓
WebSocket Client
        ↓
TypeScript Web Server
        ↓
Web Viewer (HTML + TS)
```

# **Project Structure**

```txt
FLAM/
│
├── app/
│   ├── src/main/java/com/example/flam/
│   │      ├── data/
|              |_camera/        # CameraController, FrameExtractor
|              |_ nat/          # JNI bridge + NativeRepository
│   │          └── web/         # WebSocket client
│   │      ├── gl/              # GLTextureRenderer (OpenGL ES)
│   │      ├── ui/              # Jetpack Compose UI
│   │      └── web/             # WebSocket client
|
|
│   └── src/main/cpp/           # Native OpenCV C++ (NDK stable)
│          ├── native_bridge.cpp
│          ├── dummy.cpp
│          ├── includes/
│          └── CMakeLists.txt
│
└── web/
    ├── server/                 # TypeScript WebSocket server
    │     ├── src/server.ts
    │     └── package.json
          └── tsconfig.json
    │
    └── web-viewer/             # TypeScript web viewer
          ├── src/viewer.ts
          ├── dist/
             ├── index.html
             ├── viewer.js
          └── tsconfig.json
          └── package.json

```

# **⚙️ Setup Instructions**

## **Android Setup**

```txt
1. Install NDK + CMake from Android Studio → SDK Tools
2. Open the project and let Gradle sync completely
3. Connect a physical Android device (Camera2 + OpenGL requires real hardware)
4. Run the app 
   → Camera2 starts
   → NV21 frames extracted
   → JNI loads native library
   → OpenCV processing runs (gray/canny/threshold/motion)
   → OpenGL ES 2.0 renders RGBA output on screen
No additional configuration required.
CMake + Gradle automatically compile C++ and link OpenCV during build.
JNI → C++ → OpenCV pipeline is pre-wired through NativeBridge.
```
## **WebSocket Server Setup (TypeScript)**

```txt
cd web/server
npm install
npm run dev

Starts WebSocket server on:
    ws://<your-ip>:8080
    Change Ip addressin code also , as phone and pc needs to be on same local network for websocket.

cd web/viewer
npm install
npm run serv
```

# 📷 Screenshots(All are motion detecting)

## Raw Mode
![Raw Mode](assets/raw.png)

## Gray Mode
![Gray Mode](assets/gray.png)

## Canny Mode
![Canny Mode](assets/canny.png)

## Threshold Mode
![Threshold Mode](assets/thresh.png)

## WebView Mode
![WebView Mode](assets/web.png)
