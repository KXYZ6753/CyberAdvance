package org.cyberA;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class Main {
    static class Bridge extends WebSocketClient {

        private final BlockingQueue<String> inbox = new ArrayBlockingQueue<>(16);

        Bridge(URI url){
            super(url);
        }

        @Override
        public void onOpen(ServerHandshake h) {
            System.out.println("[CyberA Panel] Connected to the VM, Status:" + h.getHttpStatus());
        }

        @Override
        public void onMessage(String message) {
            inbox.offer(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("[CyberA Panel] Connection closed: " + reason);
        }

        @Override
        public void onError(Exception ex) {
            System.out.println("[CyberA Panel] An error occurred: " + ex);
        }

        void drain() {
            inbox.clear();
        }

        String request(JSONObject payload) throws InterruptedException {
            drain();
            send(payload.toString());
            String response = inbox.poll(30, TimeUnit.SECONDS);
            return response == null ? "{\"ERR\":\"timeout waiting for VM\"}" : response;
        }
    }
}
