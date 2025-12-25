package io.github.waynem77.bscmail4.client.controller;

import io.github.waynem77.bscmail4.client.dto.CreatePersonRequest;
import io.github.waynem77.bscmail4.client.dto.PersonContainer;
import io.github.waynem77.bscmail4.client.dto.PersonResponse;
import io.github.waynem77.bscmail4.client.dto.UpdatePersonRequest;
import io.github.waynem77.bscmail4.client.service.PersonClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web controller for managing Person entities via Thymeleaf views.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class PersonClientController
{
    private final PersonClientService personClientService;

    /**
     * Displays a paginated list of persons with optional filtering and sorting.
     *
     * @param page      the page number (0-based, default 0)
     * @param size      the page size (default 20)
     * @param sortBy    the field to sort by (name, emailAddress, or createdAt, default name)
     * @param direction the sort direction (asc or desc, default asc)
     * @param isActive  filter by active status (optional)
     * @param search    filter by search string matching name or emailAddress (optional)
     * @param model     the model to populate
     * @return the view name for the person list
     */
    @GetMapping("/person")
    public String listPersons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            Model model)
    {
        log.info("Displaying person list. page={}, size={}, sortBy={}, direction={}, isActive={}, search={}",
                page, size, sortBy, direction, isActive, search);

        try
        {
            PersonContainer container = personClientService.getPersons(page, size, sortBy, direction, isActive, search);
            model.addAttribute("container", container);
            model.addAttribute("currentPage", page);
            model.addAttribute("currentSize", size);
            model.addAttribute("currentSortBy", sortBy);
            model.addAttribute("currentDirection", direction);
            model.addAttribute("currentIsActive", isActive);
            model.addAttribute("currentSearch", search);
            return "person/list";
        }
        catch (RuntimeException e)
        {
            log.error("Error retrieving persons", e);
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            // Return empty container on error
            PersonContainer emptyContainer = new PersonContainer();
            model.addAttribute("container", emptyContainer);
            model.addAttribute("currentPage", page);
            model.addAttribute("currentSize", size);
            model.addAttribute("currentSortBy", sortBy);
            model.addAttribute("currentDirection", direction);
            model.addAttribute("currentIsActive", isActive);
            model.addAttribute("currentSearch", search);
            return "person/list";
        }
    }

    /**
     * Displays the form for creating a new person.
     *
     * @param model the model to populate
     * @return the view name for the create form
     */
    @GetMapping("/person/new")
    public String showCreateForm(Model model)
    {
        log.info("Displaying create person form");
        model.addAttribute("person", CreatePersonRequest.builder().build());
        model.addAttribute("isEdit", false);
        return "person/form";
    }

    /**
     * Handles the submission of the create person form.
     *
     * @param request            the person data from the form
     * @param redirectAttributes attributes for redirect
     * @return redirect to the person list
     */
    @PostMapping("/person")
    public String createPerson(
            @ModelAttribute CreatePersonRequest request,
            RedirectAttributes redirectAttributes)
    {
        log.info("Creating person. request={}", request);
        PersonResponse created = personClientService.createPerson(request);
        redirectAttributes.addFlashAttribute("successMessage", "Person created successfully: " + created.getName());
        return "redirect:/person";
    }

    /**
     * Displays the details of a person.
     *
     * @param personId the ID of the person to display
     * @param model    the model to populate
     * @return the view name for the person details
     */
    @GetMapping("/person/{personId}")
    public String viewPerson(@PathVariable Long personId, Model model)
    {
        log.info("Displaying person details. personId={}", personId);
        PersonResponse person = personClientService.getPersonById(personId);
        model.addAttribute("person", person);
        return "person/view";
    }

    /**
     * Displays the form for editing an existing person.
     *
     * @param personId the ID of the person to edit
     * @param model    the model to populate
     * @return the view name for the edit form
     */
    @GetMapping("/person/{personId}/edit")
    public String showEditForm(@PathVariable Long personId, Model model)
    {
        log.info("Displaying edit person form. personId={}", personId);
        PersonResponse person = personClientService.getPersonById(personId);
        UpdatePersonRequest request = UpdatePersonRequest.builder()
                .name(person.getName())
                .emailAddress(person.getEmailAddress())
                .phone(person.getPhone())
                .isActive(person.getIsActive())
                .build();
        model.addAttribute("person", request);
        model.addAttribute("personId", personId);
        model.addAttribute("isEdit", true);
        return "person/form";
    }

    /**
     * Handles the submission of the update person form.
     *
     * @param personId           the ID of the person to update
     * @param request            the updated person data from the form
     * @param redirectAttributes attributes for redirect
     * @return redirect to the person list
     */
    @PostMapping("/person/{personId}")
    public String updatePerson(
            @PathVariable Long personId,
            @ModelAttribute UpdatePersonRequest request,
            RedirectAttributes redirectAttributes)
    {
        log.info("Updating person. personId={}, request={}", personId, request);
        PersonResponse updated = personClientService.updatePerson(personId, request);
        redirectAttributes.addFlashAttribute("successMessage", "Person updated successfully: " + updated.getName());
        return "redirect:/person";
    }

    /**
     * Toggles the active status of a person.
     *
     * @param personId           the ID of the person to toggle
     * @param redirectAttributes attributes for redirect
     * @return redirect to the person list
     */
    @PostMapping("/person/{personId}/toggle-status")
    public String toggleActiveStatus(
            @PathVariable Long personId,
            RedirectAttributes redirectAttributes)
    {
        log.info("Toggling active status. personId={}", personId);
        PersonResponse updated = personClientService.toggleActiveStatus(personId);
        String status = updated.getIsActive() ? "activated" : "deactivated";
        redirectAttributes.addFlashAttribute("successMessage",
                "Person " + status + " successfully: " + updated.getName());
        return "redirect:/person";
    }
}

