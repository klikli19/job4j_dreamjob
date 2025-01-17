import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.repository.Sql2oUserRepository;

import java.util.Properties;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Sql2oUserRepositoryTest {

    private static Sql2oUserRepository sql2oUserRepository;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oUserRepositoryTest.class.getClassLoader().getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");
        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        var sql2o = configuration.databaseClient(datasource);
        sql2oUserRepository = new Sql2oUserRepository(sql2o);
    }

    @AfterEach
    public void clearUsers() {
        User user = new User();
        sql2oUserRepository.deleteById(user.getId());
    }

    @Test
    public void whenSaveThenSaved() {
        User user = new User(0, "Ivan", "email@mail.ru", "1234");
        sql2oUserRepository.save(user);
        assertThat(user).usingRecursiveComparison().isEqualTo(sql2oUserRepository.findByEmailAndPassword(user.getEmail(),
                user.getPassword()).get());
    }

    @Test
    public void whenSaveWithSameEmail() {
        var user1 = sql2oUserRepository.save(new User(1, "name", "en@ya.ru",  "password"));
        assertThat(user1).isNotEmpty();
        var user2 = sql2oUserRepository.save(new User(1, "name", "en@ya.ru", "password"));
        assertThat(user2).isEmpty();
    }

    @Test
    public void whenDontSaveThenNothingFound() {
        assertThat(sql2oUserRepository.findByEmailAndPassword("email@com", "1233")).isEmpty();
    }
}