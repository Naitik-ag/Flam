const ws = new WebSocket("ws://192.168.1.34:8080");

const img = document.getElementById("preview") as HTMLImageElement;
const fpsLabel = document.getElementById("fps")!;
const resLabel = document.getElementById("resolution")!;
const timeLabel = document.getElementById("timestamp")!;

let lastTime = performance.now();
let frameCount = 0;

function updateFPS() {
    const now = performance.now();
    const delta = now - lastTime;

    if (delta >= 1000) {
        const fps = frameCount;
        fpsLabel.textContent = fps.toString();

        frameCount = 0;
        lastTime = now;
    }
}

ws.onmessage = async (event) => {
    let base64: string;

    if (typeof event.data === "string") {
        base64 = event.data;
    } else if (event.data instanceof Blob) {
        base64 = await event.data.text();
    } else {
        console.error("Unknown data:", event.data);
        return;
    }

    img.src = `data:image/jpeg;base64,${base64}`;

    frameCount++;
    updateFPS();

    // Update Stats
    timeLabel.textContent = new Date().toLocaleTimeString();

    // Try to read the resolution from the incoming JPEG
    const temp = new Image();
    temp.onload = () => {
        resLabel.textContent = `${temp.width}x${temp.height}`;
    };
    temp.src = img.src;
};
