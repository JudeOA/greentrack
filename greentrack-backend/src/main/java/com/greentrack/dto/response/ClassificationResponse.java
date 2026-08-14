package com.greentrack.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResponse {
    /** The suggested category name (exactly matches one of the app's categories), or null if none. */
    private String categoryName;
    /** Model confidence 0-100 (0 when no suggestion). */
    private Integer confidence;
}
