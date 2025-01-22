package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.model.request.CreateOrUpdateShiftTemplateRequest;
import io.github.waynem77.bscmail4.model.response.ShiftTemplateResponse;
import io.github.waynem77.bscmail4.model.response.ShiftTemplatesResponse;
import io.github.waynem77.bscmail4.service.ShiftTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * APIs regarding {@link io.github.waynem77.bscmail4.model.entity.ShiftTemplate} objects.
 */
@RestController
@RequiredArgsConstructor
public class ShiftTemplateController
{
    @Autowired
    private ShiftTemplateService shiftTemplateService;

    @PostMapping("/api/shift/template")
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftTemplateResponse createShiftTemplate(@RequestBody CreateOrUpdateShiftTemplateRequest request)
    {
        return shiftTemplateService.createShiftTemplate(request);
    }

    @PutMapping("/api/shift/template/{templateId}")
    public ShiftTemplateResponse updateShiftTemplate(
            @PathVariable(name = "templateId") Long templateId,
            @RequestBody CreateOrUpdateShiftTemplateRequest request)
    {
        return shiftTemplateService.updateShiftTemplate(request, templateId);
    }

    @GetMapping("/api/shift/template/{templateId}")
    public ShiftTemplateResponse getShiftTemplate(@PathVariable(name = "templateId") Long templateId)
    {
        return shiftTemplateService.getShiftTemplateById(templateId);
    }

    @DeleteMapping("/api/shift/template/{templateId}")
    public void deleteShiftTemplate(@PathVariable(name = "templateId") Long templateId)
    {
        shiftTemplateService.deleteShiftTemplateById(templateId);
    }

    @GetMapping("/api/shift/template")
    public ShiftTemplatesResponse getShiftTemplates(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "25") int size)
    {
        return shiftTemplateService.getShiftTemplates(page, size);
    }
}
