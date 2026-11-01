package com.campus.config;

import com.campus.security.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器：从握手请求的 token 中解析用户身份，
 * 将 userId 存入 session attributes，供 convertAndSendToUser 使用
 */
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public WebSocketHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 从 token 参数或 Authorization 头解析用户身份
        String token = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            token = servletRequest.getServletRequest().getParameter("token");
            if (token == null || token.isEmpty()) {
                String header = servletRequest.getServletRequest().getHeader("Authorization");
                if (header != null && header.startsWith("Bearer ")) {
                    token = header.substring(7);
                }
            }
        }

        if (token != null && !token.isEmpty()) {
            try {
                Long userId = jwtUtil.getUserId(token);
                // 存入 session attributes，key 为 "userId"
                attributes.put("userId", userId);
            } catch (Exception e) {
                // token 无效，忽略，不阻断握手
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
