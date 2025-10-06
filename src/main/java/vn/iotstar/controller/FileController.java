package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.service.impl.FileStorageService;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/download/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName, 
            HttpServletRequest request) {
        
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            contentType = "application/octet-stream";
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}