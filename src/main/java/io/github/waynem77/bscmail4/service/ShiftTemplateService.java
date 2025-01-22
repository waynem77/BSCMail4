package io.github.waynem77.bscmail4.service;

import io.github.waynem77.bscmail4.exception.BadRequestException;
import io.github.waynem77.bscmail4.exception.NotFoundException;
import io.github.waynem77.bscmail4.model.entity.Role;
import io.github.waynem77.bscmail4.model.entity.ShiftTemplate;
import io.github.waynem77.bscmail4.model.repository.RoleRepository;
import io.github.waynem77.bscmail4.model.repository.ShiftTemplateRepository;
import io.github.waynem77.bscmail4.model.request.CreateOrUpdateShiftTemplateRequest;
import io.github.waynem77.bscmail4.model.response.ShiftTemplateResponse;
import io.github.waynem77.bscmail4.model.response.ShiftTemplatesResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Provides database operations on {@link ShiftTemplate} objects.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftTemplateService
{
    @Autowired
    private final ShiftTemplateRepository shiftTemplateRepository;
    @Autowired
    private final RoleRepository roleRepository;

    /**
     * Creates a new ShiftTemplate object from the given request and stores it in the database
     *
     * @param request the creation request
     * @return a ShiftTemplateResponse representing the Person
     * @throws BadRequestException if the request is invalid
     */
    public ShiftTemplateResponse createShiftTemplate(@NonNull CreateOrUpdateShiftTemplateRequest request)
    {
        log.info("Creating a new shift template. request={}", request);
        validateRequestForCreate(request);

        ShiftTemplate shiftTemplate = new ShiftTemplate();
        shiftTemplate.setName(request.getName());
        shiftTemplate.setRequiredRole(getRoleOrThrow(request.getRequiredRoleId()));
        shiftTemplate = shiftTemplateRepository.save(shiftTemplate);

        return createResponseFromShiftTemplate(shiftTemplate);
    }

    /**
     * Updates an existing ShiftTemplate object according to the given request. If the ShiftTemplate does not already exist in the database, it is created.
     *
     * @param request the update request
     * @param id      the id of the ShiftTemplate object to update
     * @return a ShiftTemplateResponse representing the ShiftTemplate
     * @throws BadRequestException if the request is invalid
     */
    public ShiftTemplateResponse updateShiftTemplate(@NonNull CreateOrUpdateShiftTemplateRequest request, Long id)
    {
        log.info("Updating shift template. request={}, id={}", request, id);

        Optional<ShiftTemplate> possibleExistingShiftTemplate = shiftTemplateRepository.findById(id);

        if (possibleExistingShiftTemplate.isEmpty()) {
            log.info("ShiftTemplate with id={} does not exist.", id);
            return createShiftTemplate(request);
        }

        validateRequestForCreate(request);

        ShiftTemplate shiftTemplate = possibleExistingShiftTemplate.get();
        shiftTemplate.setName(request.getName());
        shiftTemplate.setRequiredRole(getRoleOrThrow(request.getRequiredRoleId()));
        shiftTemplate = shiftTemplateRepository.save(shiftTemplate);

        return createResponseFromShiftTemplate(shiftTemplate);
    }

    /**
     * Finds the ShiftTemplate with the given id in the database and returns a ShiftTemplateResponse representing it.
     *
     * @param id the ShiftTemplate id
     * @return a ShiftTemplateResponse representing the ShiftTemplate
     * @throws NotFoundException if the ShiftTemplate does not exist
     */
    public ShiftTemplateResponse getShiftTemplateById(Long id)
    {
        log.info("Getting shift template with id={}", id);

        return ShiftTemplateResponse.fromShiftTemplate(
                shiftTemplateRepository
                        .findById(id)
                        .orElseThrow(() ->
                        {
                            log.error("Unable to find shift template. id={}", id);
                            return new NotFoundException("Unable to find shiftTemplate.");
                        }));
    }

    /**
     * Deletes the ShiftTemplate with the given id in the database.
     * <p>
     * This method is idempotent. If no shift template with the given id exists in the database, this method does nothing.
     *
     * @param id the ShiftTemplate id
     */
    public void deleteShiftTemplateById(Long id)
    {
        log.info("Deleting shift template. id={}", id);
        shiftTemplateRepository.deleteById(id);
    }

    /**
     * Returns all the saved ShiftTemplates, in ascending order by name, by page.
     *
     * @param page the page number to return; this parameter is 0-indexed
     * @param size the page size
     * @return the requested page
     */
    public ShiftTemplatesResponse getShiftTemplates(int page, int size)
    {
        log.info("Getting all shift templates. page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "name");

        return new ShiftTemplatesResponse(
                shiftTemplateRepository.findAll(pageable).map(ShiftTemplateResponse::fromShiftTemplate));
    }

    /**
     * Validates the given request for creation. The request may not be null, and the name also may not be null.
     *
     * @param request the creation request
     * @throws BadRequestException if the request is invalid
     */
    private void validateRequestForCreate(CreateOrUpdateShiftTemplateRequest request)
    {
        log.info("Validating request for create. request={}", request);
        if (request.getName() == null) {
            log.error("Invalid request. Request name must be non-null. request={}", request);
            throw new BadRequestException("Invalid request");
        }
    }

    /**
     * Transforms a ShiftTemplate object into an equivalent ShiftTemplateResponse object.
     *
     * @param shiftTemplate the ShiftTemplate object
     * @return the ShiftTemplateResponse object
     */
    private ShiftTemplateResponse createResponseFromShiftTemplate(@NonNull ShiftTemplate shiftTemplate)
    {
        ShiftTemplateResponse response = new ShiftTemplateResponse();
        response.setId(shiftTemplate.getId());
        response.setName(shiftTemplate.getName());
        response.setRequiredRoleId(shiftTemplate.getRequiredRoleId());

        return response;
    }

    /**
     * Returns the Role with the given id. If the Role does not exist in the database, this method throws a BadRequestException.
     *
     * @param roleId the requested Role id
     * @return the Role with the given id
     * @throws BadRequestException if the Role does not exist
     */
    private Role getRoleOrThrow(Long roleId)
    {
        if (roleId == null) {
            return null;
        }
        return roleRepository.findById(roleId).orElseThrow(() -> {
            log.error("Role does not exist. id={}", roleId);
            return new BadRequestException("Role does not exist.");
        });
    }
}
