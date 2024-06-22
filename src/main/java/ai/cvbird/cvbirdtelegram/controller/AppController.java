package ai.cvbird.cvbirdtelegram.controller;

import ai.cvbird.cvbirdtelegram.dto.EmployerResponse;
import ai.cvbird.cvbirdtelegram.service.EmployerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET,RequestMethod.POST, RequestMethod.DELETE})
public class AppController {

    @Autowired
    EmployerService employerService;

    @PostMapping(value = "/employer_response")
    public ResponseEntity<?> getCVByTelegramId(@RequestBody @Valid EmployerResponse employerResponse){
        employerService.sendEmployerResponse(employerResponse);
        return ResponseEntity.ok("Sent");
    }
}
