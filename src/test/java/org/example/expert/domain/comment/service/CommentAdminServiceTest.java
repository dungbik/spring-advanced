package org.example.expert.domain.comment.service;

import static org.mockito.Mockito.*;

import org.example.expert.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {

	@Mock
	CommentRepository commentRepository;

	@InjectMocks
	CommentAdminService commentAdminService;

	@Test
	void 댓글_삭제_성공() {
		// when
		commentAdminService.deleteComment(1L);

		// then
		verify(commentRepository, times(1)).deleteById(1L);
	}
}