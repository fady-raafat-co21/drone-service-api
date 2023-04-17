package com.example.droneservice.exception;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ExceptionResponse{
    private Instant instant;
    private int status;
    private List<String> errors;
    private String type;
    private String path;
    private String message;
}
