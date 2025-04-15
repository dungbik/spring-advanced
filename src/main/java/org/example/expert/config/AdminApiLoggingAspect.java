package org.example.expert.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class AdminApiLoggingAspect {

	private final HttpServletRequest request;
	private final ObjectMapper objectMapper;

	@Around("execution(* org.example.expert..controller..*(..))")
	public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {
		String requestURI = request.getRequestURI();

		if (!requestURI.startsWith("/admin")) {
			return joinPoint.proceed();
		}

		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		String method = request.getMethod();
		Long userId = (Long) request.getAttribute("userId");

		String requestBody = extractRequestBody(joinPoint);
		Object result = joinPoint.proceed();
		String responseBody;
		try {
			responseBody = objectMapper.writeValueAsString(result);
		} catch (Exception e) {
			responseBody = "추출 실패: " + e.getMessage();
		}

		log.info("""
				[관리자 API 요청]
				API 요청 시각: {}
				요청한 사용자의 ID: {}
				HTTP 메서드: {}
				API 요청 URL: {}
				요청 본문: {}
				응답 본문: {}
				""",
			timestamp, userId, method, requestURI, requestBody, responseBody);

		return result;
	}

	private String extractRequestBody(ProceedingJoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		Object[] args = joinPoint.getArgs();
		Annotation[][] paramAnnotations = method.getParameterAnnotations();

		try {
			for (int i = 0; i < paramAnnotations.length; i++) {
				for (Annotation annotation : paramAnnotations[i]) {
					if (annotation.annotationType().equals(RequestBody.class)) {
						return objectMapper.writeValueAsString(args[i]);
					}
				}
			}
		} catch (Exception e) {
			return "추출 실패: " + e.getMessage();
		}
		return "없음";
	}
}
