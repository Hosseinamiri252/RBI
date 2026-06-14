package com.iwap.recording;

import com.iwap.exception.ResourceNotFoundException;
import com.iwap.session.BrowserSession;
import com.iwap.user.User;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class RecordingService {

    @Autowired private RecordingRepository recordingRepository;
    @Autowired private MinioClient minioClient;

    @Value("${minio.bucket}") private String bucket;
    @Value("${recording.path}") private String recordingBasePath;

    @Transactional
    public Recording finalizeRecording(BrowserSession session) {
        String recordingFilePath = session.getRecordingPath() + "/" + session.getId().toString();
        File recordingFile = new File(recordingFilePath);

        if (!recordingFile.exists()) {
            log.warn("Recording file not found: {}", recordingFilePath);
            return null;
        }

        String minioKey = "recordings/" + session.getId() + ".guac";
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(minioKey)
                    .stream(new FileInputStream(recordingFile), recordingFile.length(), -1)
                    .contentType("application/octet-stream")
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to upload recording to MinIO", e);
        }

        Recording recording = new Recording();
        recording.setSession(session);
        recording.setUser(session.getUser());
        recording.setFilePath(recordingFilePath);
        recording.setFileSizeBytes(recordingFile.length());
        recording.setMinioObjectKey(minioKey);
        return recordingRepository.save(recording);
    }

    public InputStream streamRecording(UUID recordingId, User requestingUser) throws Exception {
        Recording recording = recordingRepository.findById(recordingId)
            .orElseThrow(() -> new ResourceNotFoundException("Recording not found"));

        if (!recording.getUser().getId().equals(requestingUser.getId()) &&
            requestingUser.getRole() != com.iwap.user.UserRole.ADMIN) {
            throw new com.iwap.exception.PolicyViolationException("Access denied");
        }

        if (recording.getMinioObjectKey() != null) {
            return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(recording.getMinioObjectKey())
                .build());
        }

        return Files.newInputStream(Path.of(recording.getFilePath()));
    }

    public List<Recording> getUserRecordings(UUID userId) {
        return recordingRepository.findByUserId(userId);
    }

    public Page<Recording> findAll(Pageable pageable) {
        return recordingRepository.findAll(pageable);
    }

    @Transactional
    public void delete(UUID id) {
        recordingRepository.deleteById(id);
    }
}
