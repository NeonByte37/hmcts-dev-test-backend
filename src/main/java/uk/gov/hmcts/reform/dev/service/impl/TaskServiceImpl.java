package uk.gov.hmcts.reform.dev.service.impl;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.dev.dto.TaskDTO;
import uk.gov.hmcts.reform.dev.exception.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;
import uk.gov.hmcts.reform.dev.service.TaskService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final ModelMapper moduleMapper;
    private final TaskRepository taskRepository;

    public TaskDTO createTask(TaskDTO taskDTO) {
        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus());
        task.setDueDate(taskDTO.getDueDate());
        return moduleMapper.map(taskRepository.save(task), TaskDTO.class);
    }

    public TaskDTO findTaskById(UUID id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        return moduleMapper.map(task, TaskDTO.class);
    }

    public List<TaskDTO> findAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream().map(task -> moduleMapper.map(task, TaskDTO.class))
            .collect(Collectors.toList());
    }

    public TaskDTO updateTask(UUID id, TaskDTO taskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus());
        task.setDueDate(taskDTO.getDueDate());
        return moduleMapper.map(taskRepository.save(task), TaskDTO.class);
    }

    public TaskDTO updateTaskStatus(String status, UUID id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        task.setStatus(status);
        return moduleMapper.map(taskRepository.save(task), TaskDTO.class);
    }

    public void deleteTaskById(UUID id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.delete(task);
    }

    public TaskDTO convertToTaskDTO(Task task) {
        return moduleMapper.map(task, TaskDTO.class);
    }

}
