package org.example.expert.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks
	AuthService authService;

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@Mock
	JwtUtil jwtUtil;

	User user;

	@BeforeEach
	void setUp() {
		user = new User("test@test.com", "encoded_test1234", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", 1L);
	}

	@Test
	void 회원가입_성공() {
		// given
		SignupRequest req = new SignupRequest("test@test.com", "test1234", UserRole.USER.name());

		given(userRepository.save(any())).willReturn(user);
		given(userRepository.existsByEmail(req.getEmail())).willReturn(false);
		given(jwtUtil.createToken(user.getId(), "test@test.com", UserRole.USER)).willReturn("token");

		// when
		SignupResponse res = authService.signup(req);

		// then
		assertEquals("token", res.getBearerToken());
		verify(passwordEncoder).encode(req.getPassword());
	}

	@Test
	void 존재하는_이메일로_회원가입시_예외_발생() {
		// given
		SignupRequest req = new SignupRequest("test@test.com", "test1234", UserRole.USER.name());

		given(userRepository.existsByEmail(req.getEmail())).willReturn(true);

		// when & then
		InvalidRequestException exception = assertThrows(InvalidRequestException.class,
			() -> authService.signup(req));
		assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
	}

	@Test
	void 로그인_성공() {
		// given
		SigninRequest req = new SigninRequest("test@test.com", "test1234");

		given(userRepository.findByEmail(req.getEmail())).willReturn(Optional.of(user));
		given(passwordEncoder.matches(req.getPassword(), user.getPassword())).willReturn(true);
		given(jwtUtil.createToken(user.getId(), "test@test.com", UserRole.USER)).willReturn("token");

		// when
		SigninResponse res = authService.signin(req);

		// then
		assertEquals("token", res.getBearerToken());
	}

	@Test
	void 존재하지_않는_사용자로_로그인시_예외_발생() {
		// given
		SigninRequest req = new SigninRequest("test@test.com", "test1234");

		given(userRepository.findByEmail(req.getEmail())).willReturn(Optional.empty());

		// when & then
		InvalidRequestException exception = assertThrows(InvalidRequestException.class,
			() -> authService.signin(req));
		assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
	}

	@Test
	void 비밀번호가_틀린_로그인시_예외_발생() {
		SigninRequest req = new SigninRequest("test@test.com", "test12345");

		given(userRepository.findByEmail(req.getEmail())).willReturn(Optional.of(user));
		given(passwordEncoder.matches(req.getPassword(), user.getPassword())).willReturn(false);

		// when & then
		AuthException exception = assertThrows(AuthException.class, () -> authService.signin(req));
		assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
	}
}
