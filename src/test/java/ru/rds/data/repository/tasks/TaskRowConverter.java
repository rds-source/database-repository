package ru.rds.data.repository.tasks;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.rds.data.database.Column;
import ru.rds.data.database.Row;
import ru.rds.data.repository.AbstractRowConverter;
import ru.rds.data.repository.ValueConvertersProvider;

public class TaskRowConverter extends AbstractRowConverter<TaskEntity> {

	public TaskRowConverter(ValueConvertersProvider provider) {
		super(provider);
	}

	@Override
	public TaskEntity fromRow(Row row) {
		Optional<UUID> id = convertRowValue(row.getValue("ID"), UUID.class);
		Optional<String> name = convertRowValue(row.getValue("NAME"), String.class);
		Optional<Integer> version = convertRowValue(row.getValue("VERSION"), Integer.class);

		TaskEntity entity = new TaskEntity();
		entity.setId(id.orElseGet(() -> null));
		entity.setName(name.orElseGet(() -> null));
		entity.setVersion(version.orElseGet(() -> null));

		return entity;
	}

	@Override
	public Row toRow(List<Column> columns, TaskEntity entity) {
		Row row = new Row(columns);

		columns.forEach(column -> {
			row.setValue(column.getName(), mapEntityValue(entity, column.getName()));
		});

		return row;
	}

	private Object mapEntityValue(TaskEntity entity, String columnName) {
		switch (columnName) {
			case "ID":
				return entity.getId();
			case "NAME":
				return entity.getName();
			case "VERSION":
				return entity.getVersion();
			default:
				return null;
		}
	}

}
