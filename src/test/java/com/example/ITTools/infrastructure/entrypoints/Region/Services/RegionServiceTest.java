package com.example.ITTools.infrastructure.entrypoints.Region.Services;

import com.example.ITTools.infrastructure.entrypoints.Region.Models.RegionModel;
import com.example.ITTools.infrastructure.entrypoints.Region.Repositories.RegionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class RegionServiceTest {

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private RegionService regionService;
    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createRegion() {
        RegionModel region = new RegionModel();
        region.setNameRegion(null);  // Nombre nulo
        region.setDescription(null);   // Descripción nula

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            regionService.createRegion(region, request);
        });

        // Validar que el mensaje de error sea el esperado
        assertEquals("The region name and description cannot be null.", thrown.getMessage());

    }
}