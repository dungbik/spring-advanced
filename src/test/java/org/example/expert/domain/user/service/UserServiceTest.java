package org.example.expert.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
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
class UserServiceTest {

	@InjectMocks
	UserService userService;

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	User user;

	@BeforeEach
	void setUp() {
		user = new User("test@test.com", "test1234", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", 1L);
	}

	@Test
	void 사용자_조회_성공() {
		// give
		given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

		// when
		UserResponse res = userService.getUser(user.getId());

		// then
		assertEquals(user.getId(), res.getId());
		assertEquals(user.getEmail(), res.getEmail());
	}

	@Test
	void 존재하지_않는_사용자_조회시_예외_발생() {
		// given
		given(userRepository.findById(2L)).willReturn(Optional.empty());

		// when & then
		InvalidRequestException exception = assertThrows(InvalidRequestException.class,
			() -> userService.getUser(2L));
		assertEquals("User not found", exception.getMessage());
	}

	@Test
	void 비밀번호_변경_성공() {
		// given
		UserChangePasswordRequest req = new UserChangePasswordRequest("test1234", "new12345");

		given(passwordEncoder.matches(req.getNewPassword(), user.getPassword())).willReturn(false);
		given(passwordEncoder.matches(req.getOldPassword(), user.getPassword())).willReturn(true);
		given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

		// when
		userService.changePassword(user.getId(), req);

		// then
		verify(passwordEncoder, times(1)).matches(req.getNewPassword(), "test1234");
		verify(passwordEncoder, times(1)).matches(req.getOldPassword(), "test1234");
		verify(passwordEncoder, times(1)).encode(req.getNewPassword());
	}

	@Test
	void 존재하지_않는_사용자_비밀번호_변경시_예외_발생() {
		// given
		UserChangePasswordRequest req = new UserChangePasswordRequest("test1234", "new12345");

		given(userRepository.findById(2L)).willReturn(Optional.empty());

		// when & then
		InvalidRequestException exception = assertThrows(InvalidRequestException.class,
			() -> userService.changePassword(2L, req));
		assertEquals("User not found", exception.getMessage());
	}

	@Test
	void 새_비밀번호가_기존_비밀번호와_일치할_때_예외_발생() {
		// given
		UserChangePasswordRequest req = new UserChangePasswordRequest("test1234", "test1234");

		given(passwordEncoder.matches(req.getNewPassword(), user.getPassword())).willReturn(true);
		given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

		// when & then
		InvalidRequestException exception = assertThrows(InvalidRequestException.class,
			() -> userService.changePassword(user.getId(), req));
		assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
	}

	@Test
	void 비밀번호가_틀렸을_때_예외_발생() {
		// given
		UserChangePasswordRequest req = new UserChangePasswordRequest("test12345", "new12345");

		given(passwordEncoder.matches(req.getNewPassword(), user.getPassword())).willReturn(false);
		given(passwordEncoder.matches(req.getOldPassword(), user.getPassword())).willReturn(false);
		given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

		// when & then
		InvalidRequestException exception = assertThrows(InvalidRequestException.class,
			() -> userService.changePassword(user.getId(), req));
		assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
	}
}
