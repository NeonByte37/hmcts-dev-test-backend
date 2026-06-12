package uk.gov.hmcts.reform.dev.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import uk.gov.hmcts.reform.dev.dto.TaskDTO;
import uk.gov.hmcts.reform.dev.exception.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.repository.TaskRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    private static final UUID TASK_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final LocalDateTime DUE_DATE = LocalDateTime.of(2026, 6, 10, 17, 0);

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task sampleTask() {
        return new Task(TASK_ID, "My task", "Description", "OPEN", DUE_DATE);
    }

    private TaskDTO sampleDto() {
        return new TaskDTO(TASK_ID, "My task", "Description", "OPEN", DUE_DATE);
    }

    @Test
    @DisplayName("createTask saves entity built from DTO and returns mapped DTO")
    void createTask_shouldSaveAndReturnDto() {
        TaskDTO input = new TaskDTO(null, "New task", "Desc", "OPEN", DUE_DATE);
        Task saved = sampleTask();
        TaskDTO output = sampleDto();
        when(taskRepository.save(any(Task.class))).thenReturn(saved);
        when(modelMapper.map(saved, TaskDTO.class)).thenReturn(output);
        TaskDTO result = taskService.createTask(input);
        verify(taskRepository).save(argThat(task ->
                                                task.getTitle().equals("New task")
                                                    && task.getDescription().equals("Desc")
                                                    && task.getStatus().equals("OPEN")
                                                    && task.getDueDate().equals(DUE_DATE)
        ));
        assertThat(result).isEqualTo(output);
    }

    @Test
    @DisplayName("findTaskById returns mapped DTO when task exists")
    void findTaskById_shouldReturnDtoWhenFound() {
        Task task = sampleTask();
        TaskDTO dto = sampleDto();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(modelMapper.map(task, TaskDTO.class)).thenReturn(dto);
        TaskDTO result = taskService.findTaskById(TASK_ID);
        assertThat(result).isEqualTo(dto);
        verify(taskRepository).findById(TASK_ID);
    }

    @Test
    @DisplayName("findTaskById throws when task does not exist")
    void findTaskById_shouldThrowWhenNotFound() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.findTaskById(TASK_ID))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found");
        verify(modelMapper, never()).map(any(Task.class), eq(TaskDTO.class));
    }

    @Test
    @DisplayName("findAllTasks maps every entity to a DTO")
    void findAllTasks_shouldReturnMappedList() {
        UUID otherId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        Task task1 = sampleTask();
        Task task2 = new Task(otherId, "Other", "D", "DONE", DUE_DATE);
        TaskDTO dto1 = sampleDto();
        TaskDTO dto2 = new TaskDTO(otherId, "Other", "D", "DONE", DUE_DATE);
        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));
        when(modelMapper.map(task1, TaskDTO.class)).thenReturn(dto1);
        when(modelMapper.map(task2, TaskDTO.class)).thenReturn(dto2);
        List<TaskDTO> result = taskService.findAllTasks();
        assertThat(result).containsExactly(dto1, dto2);
        verify(taskRepository).findAll();
    }

    @Test
    @DisplayName("updateTask updates fields on existing entity and returns mapped DTO")
    void updateTask_shouldUpdateAndReturnDto() {
        Task existing = sampleTask();
        TaskDTO input = new TaskDTO(null, "Updated", "New desc", "IN_PROGRESS", DUE_DATE.plusDays(1));
        TaskDTO output = new TaskDTO(TASK_ID, "Updated", "New desc", "IN_PROGRESS", DUE_DATE.plusDays(1));
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);
        when(modelMapper.map(existing, TaskDTO.class)).thenReturn(output);
        TaskDTO result = taskService.updateTask(TASK_ID, input);
        verify(taskRepository).save(argThat(task ->
                                                task.getTitle().equals("Updated")
                                                    && task.getDescription().equals("New desc")
                                                    && task.getStatus().equals("IN_PROGRESS")
        ));
        assertThat(result).isEqualTo(output);
    }

    @Test
    @DisplayName("updateTask throws when task does not exist")
    void updateTask_shouldThrowWhenNotFound() {
        TaskDTO input = sampleDto();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.updateTask(TASK_ID, input))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found");
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTaskStatus updates only status and returns mapped DTO")
    void updateTaskStatus_shouldUpdateStatusOnly() {
        Task existing = sampleTask();
        TaskDTO output = new TaskDTO(TASK_ID, "My task", "Description", "DONE", DUE_DATE);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existing));
        when(taskRepository.save(existing)).thenReturn(existing);
        when(modelMapper.map(existing, TaskDTO.class)).thenReturn(output);
        TaskDTO result = taskService.updateTaskStatus("DONE", TASK_ID);
        verify(taskRepository).save(argThat(task -> task.getStatus().equals("DONE")));
        assertThat(result.getStatus()).isEqualTo("DONE");
    }

    @Test
    @DisplayName("updateTaskStatus throws when task does not exist")
    void updateTaskStatus_shouldThrowWhenNotFound() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.updateTaskStatus("DONE", TASK_ID))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found");
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("deleteTaskById deletes entity when it exists")
    void deleteTaskById_shouldDeleteWhenFound() {
        Task task = sampleTask();
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        taskService.deleteTaskById(TASK_ID);
        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("deleteTaskById throws when task does not exist")
    void deleteTaskById_shouldThrowWhenNotFound() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.deleteTaskById(TASK_ID))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found");
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("convertToTaskDTO delegates to ModelMapper")
    void convertToTaskDTO_shouldMapEntity() {
        Task task = sampleTask();
        TaskDTO dto = sampleDto();
        when(modelMapper.map(task, TaskDTO.class)).thenReturn(dto);
        TaskDTO result = taskService.convertToTaskDTO(task);
        assertThat(result).isEqualTo(dto);
    }
}
