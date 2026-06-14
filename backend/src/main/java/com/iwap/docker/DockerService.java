package com.iwap.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.*;
import com.iwap.exception.SessionCreationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

@Service
@Slf4j
public class DockerService {

    @Autowired private DockerClient dockerClient;

    @Value("${docker.browser-image}") private String browserImage;
    @Value("${docker.browser-network}") private String browserNetwork;

    public String startBrowserContainer(String containerName, int vncPort) {
        try {
            HostConfig hostConfig = HostConfig.newHostConfig()
                .withNetworkMode(browserNetwork)
                .withPortBindings(PortBinding.parse(vncPort + ":5900"))
                .withMemory(512 * 1024 * 1024L)
                .withNanoCPUs(1_000_000_000L)
                .withSecurityOpts(List.of("no-new-privileges:true"))
                .withCapDrop(Capability.ALL)
                .withReadonlyRootfs(false);

            var container = dockerClient.createContainerCmd(browserImage)
                .withName(containerName)
                .withHostConfig(hostConfig)
                .withEnv(
                    "VNC_PW=iwap_vnc_secret",
                    "VNC_RESOLUTION=1280x720",
                    "VNC_COL_DEPTH=24"
                )
                .withExposedPorts(ExposedPort.tcp(5900))
                .exec();

            dockerClient.startContainerCmd(container.getId()).exec();
            log.info("Started browser container: {} ({})", containerName, container.getId());
            return container.getId();
        } catch (Exception e) {
            log.error("Failed to start browser container: {}", containerName, e);
            throw new SessionCreationException("Failed to start browser container", e);
        }
    }

    public void stopAndRemoveContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).withTimeout(10).exec();
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            log.info("Removed container: {}", containerId);
        } catch (Exception e) {
            log.warn("Failed to cleanly remove container {}: {}", containerId, e.getMessage());
        }
    }

    public int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Cannot find free port", e);
        }
    }
}
