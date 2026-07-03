package com.substring.foodies.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileData {
    private String fileName;
    private String filePath;
}
