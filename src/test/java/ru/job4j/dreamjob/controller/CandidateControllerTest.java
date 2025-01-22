package ru.job4j.dreamjob.controller;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CandidateControllerTest {

    private CandidateService candidateService;

    private CityService cityService;

    private CandidateController candidateController;

    private MultipartFile testFile;

    private HttpSession session;

    @BeforeEach
    public void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        session = mock(HttpSession.class);
        candidateController = new CandidateController(candidateService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[]{1, 2, 3});
    }

    @Test
    public void whenRequestCandidateListPageThenGetPageWithCandidates() {

        Candidate candidate1 = new Candidate(1, "jjj", "kkk", LocalDateTime.now(), 1, 1);
        Candidate candidate2 = new Candidate(2, "jjj1", "kkk1", LocalDateTime.now(), 2, 2);
        List<Candidate> expectedCandidates = List.of(candidate1, candidate2);

        when(candidateService.findAll()).thenReturn(expectedCandidates);

        Model model = new ConcurrentModel();
        String view = candidateController.getAll(model, session);
        Object actualCandidates = model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(actualCandidates).isEqualTo(expectedCandidates);
    }

    @Test
    public void whenRequestCandidateCreationPageThenGetPageWithCities() {

        City city1 = new City(1, "Москва");
        City city2 = new City(2, "Санкт-Петербург");
        List<City> expectedCities = List.of(city1, city2);

        when(cityService.findAll()).thenReturn(expectedCities);

        Model model = new ConcurrentModel();
        String view = candidateController.getCreationPage(model, session);
        Object actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    public void whenPostCandidateWithFileThenSameDataAndRedirectToCandidatesPage() throws Exception {

        Candidate candidate = new Candidate(1, "kkk", "kkkk", LocalDateTime.now(), 1, 1);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());

        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        ArgumentCaptor<FileDto> fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);

        when(candidateService.save(candidateArgumentCaptor.capture(), fileDtoArgumentCaptor.capture()))
                .thenReturn(candidate);

        Model model = new ConcurrentModel();
        String view = candidateController.create(candidate, testFile, model);
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        FileDto actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    public void whenSomeExceptionThrownThenGetErrorPageWithMessage() {

        RuntimeException expectedException = new RuntimeException("Failed to write file");

        when(candidateService.save(any(), any())).thenThrow(expectedException);

        Model model = new ConcurrentModel();
        String view = candidateController.create(new Candidate(), testFile, model);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenRequestSingleCandidateByIdThenGetPageWithCandidate() {

        Candidate candidate = new Candidate(1, "kkk", "kkkk", LocalDateTime.now(), 1, 1);

        when(candidateService.findById(candidate.getId())).thenReturn(Optional.of(candidate));

        Model model = new ConcurrentModel();
        String view = candidateController.getById(model, candidate.getId(), session);
        Object actualVacancy = model.getAttribute("candidate");

        assertThat(view).isEqualTo("candidates/one");
        assertThat(actualVacancy).isEqualTo(candidate);
    }

    @Test
    public void whenNotFoundSingleCandidateByIdThenGetErrorPageWithMessage() {

        String expectedMessage
                = "Резюме с указанным идентификатором не найдено";

        when(candidateService.findById(anyInt())).thenReturn(Optional.empty());

        Model model = new ConcurrentModel();
        String view = candidateController.getById(model, -1, session);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void whenUpdateCandidateSuccessThenRedirectToCandidatesPage() throws Exception {

        Candidate candidate = new Candidate(1, "kkk", "kkkk", LocalDateTime.now(), 1, 1);
        FileDto fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());

        ArgumentCaptor<Candidate> candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        ArgumentCaptor<FileDto> fileDTOArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);

        when(candidateService.update(candidateArgumentCaptor.capture(), fileDTOArgumentCaptor.capture()))
                .thenReturn(true);

        Model model = new ConcurrentModel();
        String view = candidateController.update(candidate, testFile, model);
        Candidate actualCandidate = candidateArgumentCaptor.getValue();
        FileDto actualFileDto = fileDTOArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        AssertionsForClassTypes.assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    public void whenUpdateCandidateWithFileUnsuccessThrownThenGetErrorPageWithMessage() {

        RuntimeException expectedException = new RuntimeException("Failed to write file");

        when(candidateService.update(any(Candidate.class), any(FileDto.class)))
                .thenThrow(expectedException);

        Model model = new ConcurrentModel();
        String view = candidateController.update(new Candidate(), testFile, model);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenDeleteCandidateByIdThenGetPageWithCandidates() {

        when(candidateService.deleteById(anyInt())).thenReturn(true);

        Model model = new ConcurrentModel();
        String view = candidateController.delete(model, -1);

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    public void whenDeleteByTdThenGetErrorPageWithMessage() {

        String expectedMessage
                = "Резюме с указанным идентификатором не найдено";

        when(candidateService.deleteById(anyInt())).thenReturn(false);

        Model model = new ConcurrentModel();
        String view = candidateController.delete(model, -1);
        Object actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedMessage);
    }
}