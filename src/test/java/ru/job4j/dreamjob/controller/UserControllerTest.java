package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Optional;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    private UserService userService;

    private UserController userController;

    private HttpSession session;

    private HttpServletRequest request;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
        session = mock(HttpSession.class);
        request = mock(HttpServletRequest.class);
    }

    @Test
    public void whenRequestUserRegisterPageThenGetRegisterPage() {

        String view = userController.getRegistationPage();

        assertThat(view).isEqualTo("user/register");
    }

    @Test
    public void whenPostRegisterUserThenRedirectToVacanciesPage() {

        User user = new User(1, "ivan@email.ru", "name", "password");

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        when(userService.save(userArgumentCaptor.capture())).thenReturn(Optional.of(user));

        Model model = new ConcurrentModel();
        String view = userController.register(model, user);
        User actualUser = userArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/index");
        assertThat(actualUser).isEqualTo(user);
    }

    @Test
    public void whenPostRegisterUserUnsuccessThenGetErrorPageWithMessage() {

        String expectedMessage
                = "Пользователь с такой почтой уже существует";

        when(userService.save(any(User.class))).thenReturn(Optional.empty());

        Model model = new ConcurrentModel();
        String view = userController.register(model, new User());
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void whenRequestUserLoginPageThenGetUserLoginPage() {

        String view = userController.getLoginPage();

        assertThat(view).isEqualTo("user/login");
    }

    @Test
    public void whenPostLoginUserThenGetRedirectToVacanciesPage() {

        User user = new User(1, "ivan@email.ru", "name", "password");

        when(userService.findByEmailAndPassword(user.getEmail(), user.getPassword()))
                .thenReturn(Optional.of(user));
        when(request.getSession()).thenReturn(session);

        Model model = new ConcurrentModel();
        String view = userController.loginUser(user, model, request);

        assertThat(view).isEqualTo("redirect:/index");
    }

    @Test
    public void whenPostLoginUserUnsuccessThenGetLoginPageWithMessage() {

        String expectedMessage
                = "Почта или пароль введены неверно";

        when(userService.findByEmailAndPassword(any(String.class), any(String.class)))
                .thenReturn(Optional.empty());

        Model model = new ConcurrentModel();
        String view = userController.loginUser(new User(), model, request);
        Object actualExceptionMessage = model.getAttribute("error");

        assertThat(view).isEqualTo("user/login");
        assertThat(actualExceptionMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void whenRequestLogoutPageThenGetUserLoginPage() {

        String view = userController.logout(session);

        assertThat(view).isEqualTo("redirect:/user/login");
    }
}