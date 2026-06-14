package com.iwap.recording;

import com.iwap.user.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class RecordingController {

    @Autowired private RecordingService recordingService;

    @GetMapping("/api/recordings")
    public List<Recording> list(@AuthenticationPrincipal User user) {
        return recordingService.getUserRecordings(user.getId());
    }

    @GetMapping("/api/recordings/{id}/stream")
    public void stream(@PathVariable UUID id, @AuthenticationPrincipal User user,
                       HttpServletResponse response) throws Exception {
        InputStream stream = recordingService.streamRecording(id, user);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"recording-" + id + ".guac\"");
        FileCopyUtils.copy(stream, response.getOutputStream());
    }

    @DeleteMapping("/api/recordings/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        recordingService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Recording deleted"));
    }

    @GetMapping("/api/admin/recordings")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Recording> adminList(Pageable pageable) {
        return recordingService.findAll(pageable);
    }
}
