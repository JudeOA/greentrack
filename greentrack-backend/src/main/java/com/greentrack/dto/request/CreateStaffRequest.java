package com.greentrack.dto.request;
import com.greentrack.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateStaffRequest {
    @NotBlank(message = "Name is required") @Size(min = 2, max = 100) private String name;
    @NotBlank(message = "Email is required") @Email private String email;
    @NotBlank(message = "Password is required") @Size(min = 8) private String password;
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number") private String phone;
    @NotNull(message = "Role is required") private User.Role role;
    private String badgeId;
}
