package uk.gov.hmcts.reform.dev.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.dev.dto.TaskDTO;
import uk.gov.hmcts.reform.dev.exception.ApiExceptionHandler;
import uk.gov.hmcts.reform.dev.service.TaskService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(ApiExceptionHandler.class)
class TaskControllerTest {

    private static final UUID TASK_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private TaskDTO sampleDto() {
        return new TaskDTO(
            TASK_ID,
            "My task",
            "Description",
            "OPEN",
            LocalDateTime.of(2026, 6, 10, 17, 0)
        );
    }

    @Test
    @DisplayName("GET /api/tasks/list returns 200 and task list")
    void listTasks_returns200() throws Exception {
        when(taskService.findAllTasks()).thenReturn(List.of(sampleDto()));
        mockMvc.perform(get("/api/tasks/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("My task"))
                .andExpect(jsonPath("$[0].status").value("OPEN"));
        verify(taskService).findAllTasks();
    }

    @Test
    @DisplayName("GET /api/tasks/{id} returns 200 when task exists")
    void getTaskById_returns200() throws Exception {
        when(taskService.findTaskById(TASK_ID)).thenReturn(sampleDto());
        mockMvc.perform(get("/api/tasks/{id}", TASK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TASK_ID.toString()))
                .andExpect(jsonPath("$.title").value("My task"));
        verify(taskService).findTaskById(TASK_ID);
    }

    @Test
    @DisplayName("POST /api/tasks returns 201 when payload is valid")
    void createTask_returns201() throws Exception {
        TaskDTO created = new TaskDTO(
            TASK_ID, "New", "D", "OPEN", LocalDateTime.of(2026, 6, 10, 17, 0)
        );
        when(taskService.createTask(any(TaskDTO.class))).thenReturn(created);
        mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "title": "New",
                              "description": "D",
                              "status": "OPEN",
                              "dueDate": "2026-06-10T17:00:00"
                            }
                            """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("New"));
        verify(taskService).createTask(any(TaskDTO.class));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} returns 200 when update succeeds")
    void updateTask_returns200() throws Exception {
        TaskDTO updated = new TaskDTO(
            TASK_ID, "Updated", "New desc", "IN_PROGRESS", LocalDateTime.of(2026, 6, 15, 10, 30)
        );
        when(taskService.updateTask(eq(TASK_ID), any(TaskDTO.class))).thenReturn(updated);
        mockMvc.perform(put("/api/tasks/{id}", TASK_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "title": "Updated",
                              "description": "New desc",
                              "status": "IN_PROGRESS",
                              "dueDate": "2026-06-15T10:30:00"
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        verify(taskService).updateTask(eq(TASK_ID), any(TaskDTO.class));
    }

    @Test
    @DisplayName("POST /api/tasks/{id}/status-update returns 200")
    void updateTaskStatus_returns200() throws Exception {
        when(taskService.updateTaskStatus("DONE", TASK_ID)).thenReturn(sampleDto());
        mockMvc.perform(post("/api/tasks/{id}/status-update", TASK_ID)
                            .param("status", "DONE"))
                            .andExpect(status().isOk());
        verify(taskService).updateTaskStatus("DONE", TASK_ID);
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id}/delete returns 200")
    void deleteTask_returns200() throws Exception {
        doNothing().when(taskService).deleteTaskById(TASK_ID);
        mockMvc.perform(delete("/api/tasks/{id}/delete", TASK_ID))
                .andExpect(status().isOk());
        verify(taskService).deleteTaskById(TASK_ID);
    }

    @Test
    @DisplayName("GET /api/tasks/{id} returns 400 when id is not a valid UUID")
    void getTaskById_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks/not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/tasks returns 400 when title is blank")
    void createTask_blankTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "title": "",
                              "description": "D",
                              "status": "OPEN"
                            }
                            """))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.message").value("Validation failed"))
                            .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"));
    }

    @Test
    @DisplayName("POST /api/tasks returns 400 when status is invalid")
    void createTask_invalidStatus_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "title": "New",
                              "description": "D",
                              "status": "BANANA"
                            }
                            """))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.fieldErrors.status").exists());
    }

    @Test
    @DisplayName("POST status-update returns 400 for invalid status")
    void updateTaskStatus_invalidStatus_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks/{id}/status-update", TASK_ID)
                            .param("status", "INVALID"))
                            .andExpect(status().isBadRequest());
    }
}
