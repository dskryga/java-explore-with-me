package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;
    private Boolean pinned = false;
    @NotNull
    private Set<Long> events = Set.of();
}
