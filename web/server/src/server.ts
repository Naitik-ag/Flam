import express from "express";
import { WebSocketServer } from "ws";

const app = express();

// Bind to all interfaces so Android/browser can reach it
const server = app.listen(8080, "0.0.0.0", () => {
  console.log("WebSocket server running on ws://0.0.0.0:8080");
});

const wss = new WebSocketServer({ server });

wss.on("connection", (socket) => {
    console.log("Client connected");

    socket.on("message", async (data) => {

        let text: string;

        // Convert received data to string ALWAYS
        if (typeof data === "string") {
            text = data;
        } else if (data instanceof Buffer) {
            text = data.toString("utf-8");
        } else {
            console.log("Unknown type:", typeof data);
            return;
        }

        // Broadcast only the string
        wss.clients.forEach((client) => {
            if (client.readyState === 1) {
                client.send(text); // send BASE64 string
            }
        });
    });
});

