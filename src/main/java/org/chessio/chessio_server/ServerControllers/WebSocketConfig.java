// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class configures the server websockets

package org.chessio.chessio_server.ServerControllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChessWebSocketHandler chessWebSocketHandler;

    // handles games under /game
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chessWebSocketHandler, "/game").setAllowedOrigins("*");
    }
}

