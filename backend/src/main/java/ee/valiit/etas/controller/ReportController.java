package ee.valiit.etas.controller;

import ee.valiit.etas.service.ReportControllerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {
    private final ReportControllerService reportControllerService;

    @PostMapping("/import/user/{userId}")
    public void addReport(
            @PathVariable Integer userId,
            @RequestParam("file") MultipartFile file) {
        reportControllerService.addReport(userId, file);
    }

}
