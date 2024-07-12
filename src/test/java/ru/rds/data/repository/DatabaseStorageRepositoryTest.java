package ru.rds.data.repository;

import com.zaxxer.hikari.HikariDataSource;
import java.util.*;
import org.junit.jupiter.api.*;
import ru.rds.data.database.*;
import ru.rds.data.repository.tasks.TaskEntity;
import ru.rds.data.repository.tasks.TasksDatabaseStorageRepository;
import ru.rds.data.storage.ElementsSelectionCondition;
import ru.rds.data.storage.SelectionConditionExpression;
import ru.rds.data.storage.SelectionType;

@DisplayName("Тестирование Репозитория <DatabaseStorageRepository>")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseStorageRepositoryTest {

	private static TasksDatabaseStorageRepository databaseStorageRepository;

	@BeforeAll
	static void init() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName("org.h2.Driver");
		dataSource.setJdbcUrl("jdbc:h2:mem:test;DATABASE_TO_UPPER=false;INIT=RUNSCRIPT FROM 'classpath:h2init.sql'");
		dataSource.setUsername("sa");
		dataSource.setPassword("sa");

		DatabaseQueryBuilder databaseQueryBuilder = new H2DatabaseQueryBuilder();

		DatabaseStorage databaseStorage = new DatabaseStorage(dataSource, databaseQueryBuilder);

		ValueConvertersProvider valueConvertersProvider = new ValueConvertersProvider();

		databaseStorageRepository = new TasksDatabaseStorageRepository(databaseStorage, valueConvertersProvider);
	}

	@Test
	@DisplayName("Вставка сущности")
	@Order(1)
	void insert() {
		UUID id = UUID.fromString("6ebc167b-7d3c-4661-a7f9-a014378b6559");

		TaskEntity taskEntity = new TaskEntity();
		taskEntity.setId(id);
		taskEntity.setName("Task 1");
		taskEntity.setVersion(1);

		databaseStorageRepository.insert(taskEntity);

		Optional<TaskEntity> optional = databaseStorageRepository.findById(id);
		Assertions.assertNotNull(optional.orElseGet(() -> null));
		Assertions.assertEquals(id, optional.get().getId());
	}

	@Test
	@DisplayName("Изменение сущности")
	@Order(2)
	void save() {
		UUID id = UUID.fromString("6ebc167b-7d3c-4661-a7f9-a014378b6559");

		TaskEntity taskEntity = databaseStorageRepository.findById(id).orElseGet(() -> null);
		Assertions.assertNotNull(taskEntity);
		Assertions.assertEquals(1, taskEntity.getVersion());

		taskEntity.setVersion(2);
		databaseStorageRepository.save(taskEntity);

		taskEntity = databaseStorageRepository.findById(id).orElseGet(() -> null);
		Assertions.assertNotNull(taskEntity);
		Assertions.assertEquals(2, taskEntity.getVersion());
	}

	@Test
	@DisplayName("Подсчет количества всех сущностей")
	@Order(3)
	void countAll() {
		long count = databaseStorageRepository.count();
		Assertions.assertEquals(1, count);
	}

	@Test
	@DisplayName("Получение всех сущностей")
	@Order(4)
	void findAll() {
		List<TaskEntity> tasks = databaseStorageRepository.findAll();
		Assertions.assertNotNull(tasks);
		Assertions.assertEquals(1, tasks.size());
	}

	@Test
	@DisplayName("Подсчет количества сущностей (в соответствии с условиями)")
	@Order(5)
	void count() {
		ElementsSelectionCondition condition = new ElementsSelectionCondition();
		condition.setSpacePropertyName("VERSION");
		condition.setSpacePropertyValue(0);
		condition.setSelectionConditionExpression(SelectionConditionExpression.GREATER_THAN_OR_EQUAL);

		long count = databaseStorageRepository.count(Collections.singletonList(condition), SelectionType.AND);
		Assertions.assertEquals(1, count);
	}

	@Test
	@DisplayName("Получение сущности по ID")
	@Order(6)
	void findById() {
		UUID id = UUID.fromString("6ebc167b-7d3c-4661-a7f9-a014378b6559");

		TaskEntity taskEntity = databaseStorageRepository.findById(id).orElseGet(() -> null);
		Assertions.assertNotNull(taskEntity);
	}

	@Test
	@DisplayName("Удаление сущности")
	@Order(7)
	void delete() {
		UUID id = UUID.fromString("6ebc167b-7d3c-4661-a7f9-a014378b6559");

		databaseStorageRepository.deleteById(id);

		TaskEntity taskEntity = databaseStorageRepository.findById(id).orElseGet(() -> null);
		Assertions.assertNull(taskEntity);
	}

}
