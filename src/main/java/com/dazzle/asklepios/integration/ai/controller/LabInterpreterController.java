package com.dazzle.asklepios.integration.ai.controller;

import com.dazzle.asklepios.integration.ai.client.dto.lab.LabInterpretationRequestDTO;
import com.dazzle.asklepios.integration.ai.client.dto.lab.LabInterpretationResponseDTO;
import com.dazzle.asklepios.integration.ai.controller.vm.LabInterpretationRequestVM;
import com.dazzle.asklepios.integration.ai.service.LabInterpreterAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class LabInterpreterController {

    private final LabInterpreterAiService service;

    @PostMapping("/lab-interpretation")
    public ResponseEntity<LabInterpretationResponseDTO> interpret(
            @RequestBody LabInterpretationRequestVM request
    ) {
        return ResponseEntity.ok(service.interpret(request));
    }
    @PostMapping("/lab-interpretation/debug")
    public ResponseEntity<LabInterpretationRequestDTO> debugInterpret(
            @RequestBody LabInterpretationRequestVM request
    ) {
        return ResponseEntity.ok(
                service.buildDebugRequest(request)
        );
    }
}