package com.iwap.docker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContainerInfo {
    private String containerId;
    private String containerName;
    private int vncPort;
}
