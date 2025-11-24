"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const ws_1 = require("ws");
const app = (0, express_1.default)();
// Bind to all interfaces so Android/browser can reach it
const server = app.listen(8080, "0.0.0.0", () => {
    console.log("WebSocket server running on ws://0.0.0.0:8080");
});
const wss = new ws_1.WebSocketServer({ server });
wss.on("connection", (socket) => {
    console.log("Client connected");
    socket.on("message", (data) => __awaiter(void 0, void 0, void 0, function* () {
        let text;
        // Convert received data to string ALWAYS
        if (typeof data === "string") {
            text = data;
        }
        else if (data instanceof Buffer) {
            text = data.toString("utf-8");
        }
        else {
            console.log("Unknown type:", typeof data);
            return;
        }
        // Broadcast only the string
        wss.clients.forEach((client) => {
            if (client.readyState === 1) {
                client.send(text); // send BASE64 string
            }
        });
    }));
});
