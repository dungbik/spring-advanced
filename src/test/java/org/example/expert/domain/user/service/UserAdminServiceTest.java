package org.example.expert.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

	@Mock
	UserRepository userRepository;

	@InjectMocks
	UserAdminService userAdminService;

	@Mock
	User user;

	@Test
	void 사용자_역할_변경_성공() {
		// given
		UserRoleChangeRequest req = new UserRoleChangeRequest(UserRole.ADMIN.name());

		given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

		// when
		userAdminService.changeUserRole(user.getId(), req);

		// then
		verify(user, times(1)).updateRole(UserRole.ADMIN);
	}

	@Test
	void 존재하지_않는_사용자_역할_변경시_예외_발생() {
		// given
		UserRoleChangeRequest req = new UserRoleChangeRequest(UserRole.ADMIN.name());

		given(userRepository.findById(2L)).willReturn(Optional.empty());

		// when & then
		InvalidRequestException exception = assertThrows(InvalidRequestException.class,
			() -> userAdminService.changeUserRole(2L, req));
		assertEquals("User not found", exception.getMessage());
	}

}