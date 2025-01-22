package ru.job4j.dreamjob.controller;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.service.VacancyService;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class VacancyControllerTest {

    private VacancyService vacancyService;

    private CityService cityService;

    private UserController userController;

    private VacancyController vacancyController;

    private MultipartFile testFile;

    private HttpSession session;

    @BeforeEach
    public void initServices() {
        vacancyService = mock(VacancyService.class);
        cityService = mock(CityService.class);
        session = mock(HttpSession.class);
        vacancyController = new VacancyController(vacancyService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[]{1, 2, 3});
    }

    @Test
    public void whenRequestVacancyListPageThenGetPageWithVacancies() {

        Vacancy vacancy1 = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        Vacancy vacancy2 = new Vacancy(2, "test2", "desc2", now(), false, 3, 4);
        List<Vacancy> expectedVacancies = List.of(vacancy1, vacancy2);

        when(vacancyService.findAll()).thenReturn(expectedVacancies);

        Model model = new ConcurrentModel();
        String view = vacancyController.getAll(model, session);
        Object actualVacancies = model.getAttribute("vacancies");

        assertThat(view).isEqualTo("vacancies/list");
        assertThat(actualVacancies).isEqualTo(expectedVacancies);
    }

    @Test
    public void whenRequestVacancyCreationPageThenGetPageWithCities() {

        City city1 = new City(1, "Москва");
        City city2 = new City(2, "Санкт-Петербург");
        List<City> expectedCities = List.of(city1, city2);

        when(cityService.findAll()).thenReturn(expectedCities);

        Model model = new ConcurrentModel();
        String view = vacancyController.getCreationPage(model, session);
        Object actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("vacancies/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    public void whenPostVacancyWithFileThenSameDataAndRedirectToVacanciesPage() throws Exception {

        Vacancy vacancy = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());

        ArgumentCaptor<Vacancy> vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);

        when(vacancyService.save(vacancyArgumentCaptor.capture(), fileDtoArgumentCaptor.capture()))
                .thenReturn(vacancy);

        Model model = new ConcurrentModel();
        String view = vacancyController.create(vacancy, testFile, model);
        Vacancy actualVacancy = vacancyArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    public void whenSomeExceptionThrownThenGetErrorPageWithMessage() {

        RuntimeException expectedException = new RuntimeException("Failed to write file");

        when(vacancyService.save(any(), any())).thenThrow(expectedException);

        Model model = new ConcurrentModel();
        String view = vacancyController.create(new Vacancy(), testFile, model);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenRequestSingleVacancyByIdThenGetPageWithVacancy() {

        Vacancy vacancy = new Vacancy(7, "title7", "desc7",
                now(), true, 1, 2);

        when(vacancyService.findById(vacancy.getId())).thenReturn(Optional.of(vacancy));

        Model model = new ConcurrentModel();
        String view = vacancyController.getById(model, vacancy.getId(), session);
        Object actualVacancy = model.getAttribute("vacancy");

        assertThat(view).isEqualTo("vacancies/one");
        assertThat(actualVacancy).isEqualTo(vacancy);
    }

    @Test
    public void whenNotFoundSingleVacancyByIdThenGetErrorPageWithMessage() {

        String expectedMessage
                = "Вакансия с указанным идентификатором не найдена";

        when(vacancyService.findById(anyInt())).thenReturn(Optional.empty());

        Model model = new ConcurrentModel();
        String view = vacancyController.getById(model, -1, session);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void whenUpdateVacancySuccessThenRedirectToVacanciesPage() throws Exception {

        Vacancy vacancy = new Vacancy(7, "title7", "desc7",
                now(), true, 1, 2);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());

        ArgumentCaptor<Vacancy> vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        ArgumentCaptor<FileDto> fileDTOArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);

        when(vacancyService.update(vacancyArgumentCaptor.capture(), fileDTOArgumentCaptor.capture()))
                .thenReturn(true);

        Model model = new ConcurrentModel();
        String view = vacancyController.update(vacancy, testFile, model);
        Vacancy actualVacancy = vacancyArgumentCaptor.getValue();
        FileDto actualFileDto = fileDTOArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        AssertionsForClassTypes.assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    public void whenUpdateVacancyWithFileUnsuccessThrownThenGetErrorPageWithMessage() {

        RuntimeException expectedException = new RuntimeException("Failed to write file");

        when(vacancyService.update(any(Vacancy.class), any(FileDto.class)))
                .thenThrow(expectedException);

        Model model = new ConcurrentModel();
        String view = vacancyController.update(new Vacancy(), testFile, model);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenDeleteVacancyByIdThenGetPageWithVacancies() {

        when(vacancyService.deleteById(anyInt())).thenReturn(true);

        Model model = new ConcurrentModel();
        String view = vacancyController.delete(model, -1);

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @Test
    public void whenDeleteVacancyByIdThenGetErrorPageWithMessage() {

        String expectedMessage
                = "Вакансия с указанным идентификатором не найдена";

        when(vacancyService.deleteById(anyInt())).thenReturn(false);

        Model model = new ConcurrentModel();
        String view = vacancyController.delete(model, -1);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedMessage);
    }
}