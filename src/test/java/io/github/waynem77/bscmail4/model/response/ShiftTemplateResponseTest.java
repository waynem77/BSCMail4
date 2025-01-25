package io.github.waynem77.bscmail4.model.response;

import io.github.waynem77.bscmail4.model.entity.ShiftTemplate;
import org.junit.jupiter.api.Test;

import static io.github.waynem77.bscmail4.TestUtils.randomLong;
import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class ShiftTemplateResponseTest
{
    @Test
    public void fromShiftTemplateThrowsWhenShiftTemplateIsNull()
    {
        ShiftTemplate shiftTemplate = null;

        assertThrows(NullPointerException.class, () -> ShiftTemplateResponse.fromShiftTemplate(shiftTemplate));
    }

    @Test
    public void fromShiftTemplateReturnsCorrectValue()
    {
        ShiftTemplate shiftTemplate = mock(ShiftTemplate.class);
        given(shiftTemplate.getId()).willReturn(randomLong());
        given(shiftTemplate.getName()).willReturn(randomString());
        given(shiftTemplate.getRequiredPermissionId()).willReturn(randomLong());

        ShiftTemplateResponse shiftTemplateResponse = ShiftTemplateResponse.fromShiftTemplate(shiftTemplate);

        assertThat(shiftTemplateResponse, notNullValue());
        assertThat(shiftTemplateResponse.getId(), equalTo(shiftTemplate.getId()));
        assertThat(shiftTemplateResponse.getName(), equalTo(shiftTemplate.getName()));
        assertThat(shiftTemplateResponse.getRequiredPermissionId(), equalTo(shiftTemplate.getRequiredPermissionId()));
    }

}