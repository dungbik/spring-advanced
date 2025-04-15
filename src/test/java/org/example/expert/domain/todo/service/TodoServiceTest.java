package org.example.expert.domain.todo.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

	@Mock
	private TodoRepository todoRepository;

	@Mock
	private WeatherClient weatherClient;

	@InjectMocks
	private TodoService todoService;

	@Test
	void 일정_저장_성공() {
		// given
		AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
		TodoSaveRequest req = new TodoSaveRequest("title", "contents");
		User user = User.fromAuthUser(authUser);
		Todo todo = new Todo("title1", "contents1", "weather1", user);
		String weather = "sunny";

		given(weatherClient.getTodayWeather()).willReturn(weather);
		given(todoRepository.save(any(Todo.class))).willReturn(todo);

		// when
		TodoSaveResponse res = todoService.saveTodo(authUser, req);

		// then
		assertThat(res.getTitle()).isEqualTo("title1");
		assertThat(res.getContents()).isEqualTo("contents1");
		assertThat(res.getWeather()).isEqualTo("sunny");
		assertThat(res.getUser().getId()).isEqualTo(1L);
		assertThat(res.getUser().getEmail()).isEqualTo("test@test.com");
	}

	@Test
	void 일정_목록_조회_성공() {
		// given
		PageRequest pageable = PageRequest.of(0, 10);
		User user = new User("test@test.com", "test1234", UserRole.USER);
		List<Todo> todos = List.of(
			new Todo("title1", "contents1", "weather1", user),
			new Todo("title2", "contents2", "weather2", user)
		);
		Page<Todo> page = new PageImpl<>(todos, pageable, todos.size());

		given(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(page);

		// when
		Page<TodoResponse> res = todoService.getTodos(1, 10);

		// then
		assertThat(res.getContent()).hasSize(2);
		assertThat(res.getContent().get(0).getTitle()).isEqualTo("title1");
		assertThat(res.getContent().get(1).getTitle()).isEqualTo("title2");
	}

	@Test
	void 일정_조회_성공() {
		User user = new User("test@test.com", "test1234", UserRole.USER);
		Todo todo = new Todo("title1", "contents1", "weather1", user);

		when(todoRepository.findByIdWithUser(1L)).thenReturn(Optional.of(todo));

		TodoResponse res = todoService.getTodo(1L);

		assertThat(res.getTitle()).isEqualTo("title1");
		assertThat(res.getUser().getEmail()).isEqualTo("test@test.com");
	}
}
