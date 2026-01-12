package com.example.planning_service.controller;

import com.example.planning_service.entity.CarrierComplianceEntity;
import com.example.planning_service.entity.FleetVehicleEntity;
import com.example.planning_service.entity.VehicleProfileEntity;
import com.example.planning_service.repository.CarrierComplianceRepository;
import com.example.planning_service.repository.FleetVehicleRepository;
import com.example.planning_service.repository.VehicleProfileRepository;
import com.example.planning_service.optimization.VrpOptimizerService;
import com.example.planning_service.client.OrderServiceClient;
import com.example.planning_service.service.OptimizationProfileService;
import com.example.planning_service.dto.OptimizationRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OptimizationControllerComplianceTest {

    @Mock
    private VrpOptimizerService vrpOptimizerService;
    @Mock
    private OrderServiceClient orderServiceClient;
    @Mock
    private FleetVehicleRepository vehicleRepository;
    @Mock
    private CarrierComplianceRepository carrierComplianceRepository;
    @Mock
    private VehicleProfileRepository vehicleProfileRepository;

    // Other dependencies required by Controller constructor
    @Mock
    private com.example.planning_service.repository.WebhookConfigRepository webhookConfigRepository;
    @Mock
    private com.example.planning_service.repository.DriverTaskRepository driverTaskRepository;
    @Mock
    private com.example.planning_service.repository.OptimizationSolutionRepository solutionRepository;
    @Mock
    private com.example.planning_service.repository.CommunicationLogRepository communicationLogRepository;
    @Mock
    private OptimizationProfileService optimizationProfileService;
    @Mock
    private com.example.planning_service.service.ManifestService manifestService;

    @InjectMocks
    private OptimizationController optimizationController;

    @Test
    public void testOptimizeRoutes_filtersNonCompliantCarriers() {
        // Setup Vehicles
        FleetVehicleEntity v1 = new FleetVehicleEntity();
        v1.setId(UUID.randomUUID());
        v1.setCarrierId("GOOD_CARRIER");
        v1.setProfileId("PROFILE_VAN");
        v1.setAvailable(true);

        FleetVehicleEntity v2 = new FleetVehicleEntity();
        v2.setId(UUID.randomUUID());
        v2.setCarrierId("BAD_CARRIER");
        v2.setProfileId("PROFILE_VAN");
        v2.setAvailable(true);

        when(vehicleRepository.findAllById(any())).thenReturn(Arrays.asList(v1, v2));

        // Setup Compliance
        CarrierComplianceEntity c1 = new CarrierComplianceEntity();
        c1.setComplianceStatus("COMPLIANT");
        c1.setIsInsured(true);
        when(carrierComplianceRepository.findById("GOOD_CARRIER")).thenReturn(Optional.of(c1));

        CarrierComplianceEntity c2 = new CarrierComplianceEntity();
        c2.setComplianceStatus("SUSPENDED");
        c2.setIsInsured(false);
        when(carrierComplianceRepository.findById("BAD_CARRIER")).thenReturn(Optional.of(c2));

        // Setup Profile
        VehicleProfileEntity p1 = new VehicleProfileEntity();
        p1.setMaxCapacityWeight(1000.0);
        when(vehicleProfileRepository.findById("PROFILE_VAN")).thenReturn(Optional.of(p1));

        // Setup Optimizer Mock Return to avoid NPE in controller
        when(vrpOptimizerService.calculateRoutes(any(), any(), any()))
                .thenReturn(new VrpOptimizerService.VehicleRoutingSolution(List.of()));

        // Execute
        OptimizationRequestDto request = new OptimizationRequestDto();
        request.setVehicleIds(List.of(v1.getId(), v2.getId()));
        request.setOrderIds(List.of(UUID.randomUUID())); // Dummy order

        optimizationController.optimizeRoutes(request);

        // Verify
        ArgumentCaptor<List<FleetVehicleEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(vrpOptimizerService).calculateRoutes(any(), captor.capture(), any());

        List<FleetVehicleEntity> filtered = captor.getValue();
        assertEquals(1, filtered.size(), "Should have filtered out the non-compliant carrier vehicle");
        assertEquals("GOOD_CARRIER", filtered.get(0).getCarrierId());
        assertEquals(1000.0, filtered.get(0).getCapacityWeight(), "Should have enriched capacity from profile");
    }
}
