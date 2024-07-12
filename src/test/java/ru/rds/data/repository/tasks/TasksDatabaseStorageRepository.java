package ru.rds.data.repository.tasks;

import java.util.UUID;
import javax.sql.DataSource;
import ru.rds.data.database.DatabaseQueryBuilder;
import ru.rds.data.database.DatabaseStorage;
import ru.rds.data.database.Table;
import ru.rds.data.repository.DatabaseStorageRepository;
import ru.rds.data.repository.RowConverter;
import ru.rds.data.repository.ValueConvertersProvider;

public class TasksDatabaseStorageRepository extends DatabaseStorageRepository<TaskEntity, UUID> {

	private TaskRowConverter rowConverter;

	public TasksDatabaseStorageRepository(DataSource dataSource, DatabaseQueryBuilder databaseQueryBuilder, ValueConvertersProvider provider) {
		super(dataSource, databaseQueryBuilder);
		init(provider);
	}

	public TasksDatabaseStorageRepository(DatabaseStorage databaseStorage, ValueConvertersProvider provider) {
		super(databaseStorage);
		init(provider);
	}

	public TasksDatabaseStorageRepository(Table table, ValueConvertersProvider provider) {
		super(table);
		init(provider);
	}

	private void init(ValueConvertersProvider provider) {
		this.rowConverter = new TaskRowConverter(provider);
	}

	@Override
	protected String getRepositoryName() {
		return "TASKS";
	}

	@Override
	protected RowConverter<TaskEntity> getRowConverter() {
		return this.rowConverter;
	}

	@Override
	protected String getIdValue(UUID uuid, String idColumnName) {
		if (uuid != null) {
			return uuid.toString();
		}
		return null;
	}

}
