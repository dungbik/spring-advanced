package org.example.expert.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.example.expert.domain.user.enums.UserRole;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdminApiInterceptor implements HandlerInterceptor {
	@Override
	public boolean preHandle(
		HttpServletRequest request,
		HttpServletResponse response,
		Object handler
	) {
		UserRole userRole = UserRole.of((String) request.getAttribute("userRole"));
		if (!UserRole.ADMIN.equals(userRole)) {
			throw new AdminAccessDeniedException("관리자 권한이 없습니다");
		}

		String uri = request.getRequestURI();
		String method = request.getMethod();
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		log.info("[Admin API 요청] 시간: {}, 메서드: {}, URI: {}", timestamp, method, uri);

		return true;
	}
}
