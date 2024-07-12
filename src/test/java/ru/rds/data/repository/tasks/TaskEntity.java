package ru.rds.data.repository.tasks;

import java.util.UUID;
import ru.rds.data.repository.annotations.RepositoryData;
import ru.rds.data.repository.annotations.RepositoryDataProperty;

@RepositoryData(repositoryName = "TASKS")
/*@RepositoryDataConverter(TaskRowConverter.class)*/
public class TaskEntity {

	@RepositoryDataProperty(columnName = "ID")
	private UUID id;

	@RepositoryDataProperty(columnName = "NAME")
	private String name;

	@RepositoryDataProperty(columnName = "VERSION")
	private Integer version;

	public TaskEntity() {
	}

	public TaskEntity(UUID id, String name, Integer version) {
		this.id = id;
		this.name = name;
		this.version = version;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "TaskEntity{" +
		       "id=" + id +
		       ", name='" + name + '\'' +
		       ", version=" + version +
		       '}';
	}

}
