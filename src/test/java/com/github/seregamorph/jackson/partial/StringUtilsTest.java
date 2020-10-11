package com.github.seregamorph.jackson.partial;

import static com.github.seregamorph.jackson.partial.StringUtils.capitalize;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void shouldCapitalizeFieldName() {
        assertEquals("Name", capitalize("name"));
    }
}
