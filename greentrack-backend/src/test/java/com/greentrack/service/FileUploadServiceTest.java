package com.greentrack.service;

import com.greentrack.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileUploadServiceTest {

    private FileUploadService fileUploadService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        fileUploadService = new FileUploadService();
        ReflectionTestUtils.setField(fileUploadService, "uploadPath", tempDir.toString());
    }

    // The stored extension must come from the validated content type, never the
    // client-supplied filename — otherwise a PNG-typed upload named "x.html" gets
    // stored and served back as text/html, enabling stored XSS on the app's origin.
    @Test
    void uploadFile_derivesExtensionFromContentType_ignoringMaliciousFilename() {
        MockMultipartFile file = new MockMultipartFile("file", "evil.html", "image/png", "fake-png-bytes".getBytes());

        String url = fileUploadService.uploadFile(file, "reports/1");

        assertThat(url).endsWith(".png");
        assertThat(url).doesNotContain(".html");
    }

    @Test
    void uploadFile_rejectsDisallowedContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "x.svg", "image/svg+xml", "<svg></svg>".getBytes());

        assertThatThrownBy(() -> fileUploadService.uploadFile(file, "reports/1"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void uploadFile_rejectsOversizedFile() {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.isEmpty()).thenReturn(false);
        Mockito.when(file.getSize()).thenReturn(11L * 1024 * 1024);

        assertThatThrownBy(() -> fileUploadService.uploadFile(file, "reports/1"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void uploadFile_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "x.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> fileUploadService.uploadFile(file, "reports/1"))
                .isInstanceOf(BusinessException.class);
    }
}
