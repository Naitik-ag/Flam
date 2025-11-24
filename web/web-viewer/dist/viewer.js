"use strict";
const img = document.getElementById("preview");
const ws = new WebSocket("ws://192.168.1.34:8080");
ws.onopen = () => {
    console.log("Connected to WebSocket server.");
};
ws.onmessage = (event) => {
    const base64 = event.data;
    img.src = `data:image/jpeg;base64,${base64}`;
};
ws.onclose = () => {
    console.log("Disconnected from WebSocket.");
};
