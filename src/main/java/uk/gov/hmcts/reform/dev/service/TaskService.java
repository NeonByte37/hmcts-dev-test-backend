package uk.gov.hmcts.reform.dev.service;

import uk.gov.hmcts.reform.dev.dto.TaskDTO;


import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskDTO createTask(TaskDTO taskDTO);

    TaskDTO findTaskById(UUID id);

    List<TaskDTO> findAllTasks();

    TaskDTO updateTaskStatus(String status, UUID id);

    TaskDTO updateTask(UUID id, TaskDTO taskDTO);

    void deleteTaskById(UUID id);

}
