package ru.rds.data.repository.tasks;

import java.util.UUID;
import ru.rds.data.database.DatabaseStorage;
import ru.rds.data.repository.AnnotationBasedDatabaseStorageRepository;
import ru.rds.data.repository.ValueConvertersProvider;

public class TasksRepository extends AnnotationBasedDatabaseStorageRepository<TaskEntity, UUID> {

	public TasksRepository(DatabaseStorage databaseStorage, ValueConvertersProvider valueConvertersProvider) {
		super(databaseStorage, valueConvertersProvider);
	}

	@Override
	protected String getIdValue(UUID uuid, String idColumnName) {
		if (uuid != null) {
			return uuid.toString();
		}
		return null;
	}

}
