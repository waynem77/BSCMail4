package io.github.waynem77.bscmail4.controller;

import io.github.waynem77.bscmail4.model.entity.Person;
import io.github.waynem77.bscmail4.model.request.CreateOrUpdatePersonRequest;
import io.github.waynem77.bscmail4.model.response.PersonResponse;
import io.github.waynem77.bscmail4.model.specification.PersonSpecification;
import io.github.waynem77.bscmail4.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

/**
 * Controller for managing Person entities.
 * Provides web interface for CRUD operations on Person objects.
 */
@Controller
@RequestMapping("/persons")
@RequiredArgsConstructor
public class PersonController
{

    private final PersonService personService;

    /**
     * Displays a paginated list of persons with optional sorting and filtering.
     *
     * @param page       The page number (0-based, defaults to 0)
     * @param size       The page size (defaults to 25)
     * @param sortBy     The field to sort by (name, emailAddress, phone)
     * @param direction  The sort direction (asc, desc)
     * @param active     The active status filter (all, true, false)
     * @param name       The name filter (partial match, case-insensitive)
     * @param email      The email filter (partial match, case-insensitive)
     * @param phone      The phone filter (partial match)
     * @param textSearch The text search filter (searches across name, email, and phone)
     * @param model      Model to add attributes for the view
     * @return Thymeleaf template name
     */
    @GetMapping
    public String listPersons(@RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "25") int size,
                              @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
                              @RequestParam(value = "direction", defaultValue = "asc") String direction,
                              @RequestParam(value = "active", defaultValue = "all") String active,
                              @RequestParam(value = "name", required = false) String name,
                              @RequestParam(value = "email", required = false) String email,
                              @RequestParam(value = "phone", required = false) String phone,
                              @RequestParam(value = "textSearch", required = false) String textSearch,
                              Model model)
    {
        // Validate and constrain parameters
        page = Math.max(0, page); // Ensure page is not negative
        size = Math.max(1, Math.min(100, size)); // Constrain size between 1 and 100

        // Validate sort field
        String validSortBy = validateSortField(sortBy);

        // Validate and parse active filter
        Boolean activeFilter = parseActiveFilter(active);

        // Build specification for filtering
        Specification<Person> specification = buildSpecification(activeFilter, name, email, phone, textSearch);

        // Create sort object
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, validSortBy);

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get paginated results with specification-based filtering
        Page<PersonResponse> personPage = personService.findBySpecification(specification, pageable);

        // Add attributes to model
        model.addAttribute("persons", personPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        model.addAttribute("currentSortBy", validSortBy);
        model.addAttribute("currentDirection", direction);
        model.addAttribute("currentActive", active);
        model.addAttribute("currentName", name);
        model.addAttribute("currentEmail", email);
        model.addAttribute("currentPhone", phone);
        model.addAttribute("currentTextSearch", textSearch);
        model.addAttribute("totalPages", personPage.getTotalPages());
        model.addAttribute("totalElements", personPage.getTotalElements());
        model.addAttribute("isFirst", personPage.isFirst());
        model.addAttribute("isLast", personPage.isLast());
        model.addAttribute("hasNext", personPage.hasNext());
        model.addAttribute("hasPrevious", personPage.hasPrevious());

        return "persons/list";
    }

    /**
     * Validates the sort field parameter to prevent SQL injection and ensure valid fields.
     *
     * @param sortBy The sort field to validate
     * @return A valid sort field name
     */
    private String validateSortField(String sortBy)
    {
        if (sortBy == null || sortBy.trim().isEmpty())
        {
            return "name";
        }

        // Only allow specific fields for sorting
        return switch (sortBy.toLowerCase())
        {
            case "emailaddress", "email_address" -> "emailAddress";
            case "phone" -> "phone";
            case "active" -> "active";
            default -> "name";
        };
    }

    /**
     * Parses the active filter parameter.
     *
     * @param active The active filter string (all, true, false)
     * @return Boolean filter value (null for all, true for active, false for inactive)
     */
    private Boolean parseActiveFilter(String active)
    {
        if (active == null || active.trim().isEmpty())
        {
            return null;
        }

        return switch (active.toLowerCase())
        {
            case "true" -> true;
            case "false" -> false;
            default -> null;
        };
    }

    /**
     * Builds a specification for filtering persons based on the provided criteria.
     *
     * @param active     The active status filter
     * @param name       The name filter
     * @param email      The email filter
     * @param phone      The phone filter
     * @param textSearch The text search filter
     * @return Combined specification for all filters
     */
    private Specification<Person> buildSpecification(Boolean active, String name, String email, String phone, String textSearch)
    {
        return PersonSpecification.and(
                PersonSpecification.hasActiveStatus(active),
                PersonSpecification.hasNameLike(name),
                PersonSpecification.hasEmailLike(email),
                PersonSpecification.hasPhoneLike(phone),
                PersonSpecification.hasTextSearch(textSearch)
        );
    }

    /**
     * Displays the form for creating a new person.
     *
     * @param model Model to add attributes for the view
     * @return Thymeleaf template name
     */
    @GetMapping("/new")
    public String showCreateForm(Model model)
    {
        model.addAttribute("person", new CreateOrUpdatePersonRequest());
        model.addAttribute("isEdit", false);
        return "persons/form";
    }

    /**
     * Handles the creation of a new person.
     *
     * @param request            The person request data from the form
     * @param redirectAttributes Attributes for redirect
     * @return Redirect to persons list
     */
    @PostMapping
    public String createPerson(@ModelAttribute CreateOrUpdatePersonRequest request, RedirectAttributes redirectAttributes)
    {
        try
        {
            personService.createPerson(request);
            redirectAttributes.addFlashAttribute("successMessage", "Person created successfully!");
        }
        catch (Exception e)
        {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating person: " + e.getMessage());
        }
        return "redirect:/persons";
    }

    /**
     * Displays the form for editing an existing person.
     *
     * @param id                 The ID of the person to edit
     * @param model              Model to add attributes for the view
     * @param redirectAttributes Attributes for redirect
     * @return Thymeleaf template name or redirect
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes)
    {
        Optional<PersonResponse> person = personService.findById(id);
        if (person.isPresent())
        {
            CreateOrUpdatePersonRequest request = CreateOrUpdatePersonRequest.fromPersonResponse(person.get());
            model.addAttribute("person", request);
            model.addAttribute("personId", id);
            model.addAttribute("isEdit", true);
            return "persons/form";
        }
        else
        {
            redirectAttributes.addFlashAttribute("errorMessage", "Person not found!");
            return "redirect:/persons";
        }
    }

    /**
     * Handles the update of an existing person.
     *
     * @param id                 The ID of the person to update
     * @param request            The updated person data from the form
     * @param redirectAttributes Attributes for redirect
     * @return Redirect to persons list
     */
    @PostMapping("/{id}")
    public String updatePerson(@PathVariable Long id, @ModelAttribute CreateOrUpdatePersonRequest request, RedirectAttributes redirectAttributes)
    {
        try
        {
            personService.updatePerson(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Person updated successfully!");
        }
        catch (Exception e)
        {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating person: " + e.getMessage());
        }
        return "redirect:/persons";
    }

    /**
     * Handles the deletion of a person.
     *
     * @param id                 The ID of the person to delete
     * @param redirectAttributes Attributes for redirect
     * @return Redirect to persons list
     */
    @PostMapping("/{id}/delete")
    public String deletePerson(@PathVariable Long id, RedirectAttributes redirectAttributes)
    {
        try
        {
            if (personService.existsById(id))
            {
                personService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Person deleted successfully!");
            }
            else
            {
                redirectAttributes.addFlashAttribute("errorMessage", "Person not found!");
            }
        }
        catch (Exception e)
        {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting person: " + e.getMessage());
        }
        return "redirect:/persons";
    }

    /**
     * Toggles the active status of a person.
     *
     * @param id The ID of the person to toggle
     * @return ResponseEntity with success/error message
     */
    @PostMapping("/{id}/toggle-active")
    @ResponseBody
    public ResponseEntity<String> toggleActiveStatus(@PathVariable Long id)
    {
        try
        {
            PersonResponse updatedPerson = personService.toggleActiveStatus(id);
            String message = updatedPerson.getActive() ? "Person activated successfully!" : "Person deactivated successfully!";
            return ResponseEntity.ok(message);
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.notFound().build();
        }
        catch (Exception e)
        {
            return ResponseEntity.internalServerError().body("Error updating person status: " + e.getMessage());
        }
    }
}