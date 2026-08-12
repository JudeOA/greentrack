package com.greentrack.dto.request;
import com.greentrack.entity.Assignment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskStatusUpdateRequest {
    @NotNull private Assignment.Status status;
}
