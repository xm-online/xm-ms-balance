package com.icthh.xm.ms.balance.web.rest;

import com.icthh.xm.ms.balance.BalanceApp;
import com.icthh.xm.ms.balance.config.SecurityBeanOverrideConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.jhipster.config.JHipsterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the ProfileInfoResource REST controller.
 *
 * @see ProfileInfoResource
 **/
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SecurityBeanOverrideConfiguration.class, BalanceApp.class})
public class ProfileInfoResourceIntTest {

    @Mock
    private Environment environment;

    @Mock
    private JHipsterProperties jHipsterProperties;

    private MockMvc restProfileMockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        String mockProfile[] = {"test"};

        String activeProfiles[] = {"test"};
        when(environment.getDefaultProfiles()).thenReturn(activeProfiles);
        when(environment.getActiveProfiles()).thenReturn(activeProfiles);

        ProfileInfoResource profileInfoResource = new ProfileInfoResource(environment);
        this.restProfileMockMvc = MockMvcBuilders
            .standaloneSetup(profileInfoResource)
            .build();
    }

    @Test
    public void getProfileInfoWithRibbon() throws Exception {
        restProfileMockMvc.perform(get("/api/profile-info"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void getProfileInfoWithoutActiveProfiles() throws Exception {
        String emptyProfile[] = {};
        when(environment.getDefaultProfiles()).thenReturn(emptyProfile);
        when(environment.getActiveProfiles()).thenReturn(emptyProfile);

        restProfileMockMvc.perform(get("/api/profile-info"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
}
