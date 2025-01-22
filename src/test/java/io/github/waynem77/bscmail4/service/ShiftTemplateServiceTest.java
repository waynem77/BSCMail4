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
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static io.github.waynem77.bscmail4.TestUtils.randomLong;
import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ShiftTemplateService}.
 */
class ShiftTemplateServiceTest
{
    private ShiftTemplateRepository shiftTemplateRepository;
    private RoleRepository roleRepository;

    private ShiftTemplate shiftTemplate;
    private CreateOrUpdateShiftTemplateRequest request;


    @BeforeEach
    public void setup()
    {
        shiftTemplateRepository = mock(ShiftTemplateRepository.class);
        roleRepository = mock(RoleRepository.class);

        resetShiftTemplateAndRequest();
    }

    @Test
    public void createShiftTemplateThrowsWhenRequestIsNull()
    {
        ShiftTemplateService shiftTemplateService = createTestable();

        request = null;
        assertThrows(NullPointerException.class, () -> shiftTemplateService.createShiftTemplate(request));

        verify(shiftTemplateRepository, never()).save(any());
    }

    @Test
    public void createShiftTemplateThrowsWhenRequestIsInvalid()
    {
        given(request.getName()).willReturn(null);

        ShiftTemplateService shiftTemplateService = createTestable();

        assertThrows(BadRequestException.class, () -> shiftTemplateService.createShiftTemplate(request));

        verify(shiftTemplateRepository, never()).save(any());
    }

    @Test
    public void createShiftTemplateCreatesShiftTemplate()
    {
        ShiftTemplateService shiftTemplateService = createTestable();

        ShiftTemplateResponse response = shiftTemplateService.createShiftTemplate(request);
        validateResponseFromRequest(response, request);

        ArgumentCaptor<ShiftTemplate> shiftTemplateCaptor = ArgumentCaptor.forClass(ShiftTemplate.class);
        verify(shiftTemplateRepository, times(1)).save(shiftTemplateCaptor.capture());
        validateShiftTemplateFromRequest(shiftTemplateCaptor.getValue(), request);
    }

    @Test
    public void updateShiftTemplateUpdatesShiftTemplate()
    {
        ShiftTemplate shiftTemplate = randomShiftTemplate();
        given(shiftTemplateRepository.findById(shiftTemplate.getId())).willReturn(Optional.of(shiftTemplate));

        ShiftTemplateService shiftTemplateService = createTestable();

        ShiftTemplateResponse response = shiftTemplateService.updateShiftTemplate(request, shiftTemplate.getId());
        validateResponseFromRequest(response, request);

        verify(shiftTemplate).setName(request.getName());

        verify(shiftTemplateRepository).save(shiftTemplate);
    }

    @Test
    public void updateShiftTemplateCreatesShiftTemplateWhenOriginalDoesNotExist()
    {
        Long shiftTemplateId = randomLong();
        given(shiftTemplateRepository.findById(shiftTemplateId)).willReturn(Optional.empty());

        ShiftTemplateService shiftTemplateService = createTestable();

        ShiftTemplateResponse response = shiftTemplateService.updateShiftTemplate(request, shiftTemplateId);
        validateResponseFromRequest(response, request);

        ArgumentCaptor<ShiftTemplate> shiftTemplateCaptor = ArgumentCaptor.forClass(ShiftTemplate.class);
        verify(shiftTemplateRepository, times(1)).save(shiftTemplateCaptor.capture());
        validateShiftTemplateFromRequest(shiftTemplateCaptor.getValue(), request);
    }

    @Test
    public void getShiftTemplateByIdThrowsIfShiftTemplateNotFound()
    {
        Long id = randomLong();

        given(shiftTemplateRepository.findById(id)).willReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> createTestable().getShiftTemplateById(id));
    }

    @Test
    public void getShiftTemplateByIdReturnsCorrectValue()
    {
        Long id = randomLong();

        ShiftTemplate shiftTemplate = mock(ShiftTemplate.class);
        given(shiftTemplate.getId()).willReturn(id);
        given(shiftTemplate.getName()).willReturn(randomString());

        given(shiftTemplateRepository.findById(id)).willReturn(Optional.of(shiftTemplate));

        ShiftTemplateService shiftTemplateService = createTestable();
        ShiftTemplateResponse response = shiftTemplateService.getShiftTemplateById(id);

        assertThat(response, notNullValue());
        assertThat(response.getId(), equalTo(id));
        assertThat(response.getName(), equalTo(shiftTemplate.getName()));
    }

    @Test
    public void deleteShiftTemplateByIdDeletesShiftTemplate()
    {
        Long id = randomLong();

        ShiftTemplateService shiftTemplateService = createTestable();
        shiftTemplateService.deleteShiftTemplateById(id);

        verify(shiftTemplateRepository, times(1)).deleteById(eq(id));
    }

    @Test
    public void getPeopleFilteredReturnsCorrectValue()
    {
        int page = 0;
        int size = 25;

        Page<ShiftTemplate> shiftTemplatePage = new PageImpl<>(List.of(mock(ShiftTemplate.class)));
        given(shiftTemplateRepository.findAll(any(Pageable.class))).willReturn(shiftTemplatePage);

        ShiftTemplateService shiftTemplateService = createTestable();

        ShiftTemplatesResponse response = shiftTemplateService.getShiftTemplates(page, size);
        assertThat(response, notNullValue());

        ArgumentCaptor<Pageable> pageableArgumentCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(shiftTemplateRepository).findAll(pageableArgumentCaptor.capture());
        Pageable pageable = pageableArgumentCaptor.getValue();
        assertThat(pageable.getPageNumber(), CoreMatchers.equalTo(page));
        assertThat(pageable.getPageSize(), CoreMatchers.equalTo(size));
        assertThat(pageable.getSort(), CoreMatchers.equalTo(Sort.by(Sort.Direction.ASC, "name")));
    }

    private void resetShiftTemplateAndRequest()
    {
        Long roleId = randomLong();
        String roleName = randomString();
        Role role = mock(Role.class);
        given(role.getId()).willReturn(roleId);
        given(role.getName()).willReturn(roleName);

        given(roleRepository.findById(roleId)).willReturn(Optional.of(role));

        Long shiftTemplateId = randomLong();
        String shiftTemplateName = randomString();
        shiftTemplate = mock(ShiftTemplate.class);
        given(shiftTemplate.getId()).willReturn(shiftTemplateId);
        given(shiftTemplate.getName()).willReturn(shiftTemplateName);
        given(shiftTemplate.getRequiredRole()).willReturn(role);
        given(shiftTemplate.getRequiredRoleId()).willReturn(roleId);

        given(shiftTemplateRepository.save(any())).willReturn(shiftTemplate);

        request = mock(CreateOrUpdateShiftTemplateRequest.class);
        given(request.getName()).willReturn(shiftTemplateName);
        given(request.getRequiredRoleId()).willReturn(roleId);
    }

    private void validateShiftTemplateFromRequest(ShiftTemplate shiftTemplate, CreateOrUpdateShiftTemplateRequest request)
    {
        if (request == null) {
            assertThat(shiftTemplate, nullValue());
            return;
        }

        assertThat(shiftTemplate, notNullValue());
        assertThat(shiftTemplate.getName(), equalTo(request.getName()));
        if (request.getRequiredRoleId() == null) {
            assertThat(shiftTemplate.getRequiredRole(), nullValue());
        } else {
            assertThat(shiftTemplate.getRequiredRole(), notNullValue());
            assertThat(shiftTemplate.getRequiredRole().getId(), equalTo(request.getRequiredRoleId()));
        }
    }

    private void validateResponseFromRequest(ShiftTemplateResponse response, CreateOrUpdateShiftTemplateRequest request)
    {
        if (request == null) {
            assertThat(response, nullValue());
            return;
        }

        assertThat(response, notNullValue());
        assertThat(response.getId(), notNullValue());
        assertThat(response.getName(), equalTo(request.getName()));
        assertThat(response.getRequiredRoleId(), equalTo(request.getRequiredRoleId()));
    }

    private ShiftTemplate randomShiftTemplate()
    {
        ShiftTemplate shiftTemplate = mock(ShiftTemplate.class);
        given(shiftTemplate.getId()).willReturn(randomLong());
        given(shiftTemplate.getName()).willReturn(randomString());

        return shiftTemplate;
    }

    private ShiftTemplateService createTestable()
    {
        return new ShiftTemplateService(
                shiftTemplateRepository,
                roleRepository);
    }
}