package com.example.ITTools.infrastructure.entrypoints.Region.Controllers;

import com.example.ITTools.infrastructure.entrypoints.Region.DTO.RegionDTO;
import com.example.ITTools.infrastructure.entrypoints.Region.Models.RegionModel;
import com.example.ITTools.infrastructure.entrypoints.Region.Services.RegionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import java.util.List;

@RestController
@RequestMapping("/api/regions")
public class RegionController {

    @Autowired
    private RegionService regionService;

    @GetMapping
    public List<RegionDTO> getAllRegions() {
        return regionService.getAllRegions().stream()
                .map(RegionModel::toDTO)
                .toList();
    }

    @PostMapping("/register")
    public ResponseEntity<Object> createRegion(@RequestBody RegionDTO regionDTO, HttpServletRequest request) {
        RegionModel regionModel = new RegionModel();
        regionModel.setNameRegion(regionDTO.getNameRegion());
        regionModel.setDescription(regionDTO.getDescription());
        regionModel.setStatus(regionDTO.getStatus());

        try {
            RegionModel createdRegion = regionService.createRegion(regionModel, request);
            return ResponseEntity.ok(createdRegion.toDTO());
        } catch (RuntimeException e) {
            // Manejar la RuntimeException lanzada por el servicio
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // Devolver 409 Conflict con el mensaje
        } catch (Exception e) {
            // Manejar cualquier otra excepción
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<RegionDTO> getRegionById(@PathVariable Long id) {
        return regionService.getRegionById(id)
                .map(RegionModel::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegionDTO> updateRegion(@PathVariable Long id, @RequestBody RegionDTO regionDTO, HttpServletRequest request) {
        RegionModel regionModel = new RegionModel();
        regionModel.setNameRegion(regionDTO.getNameRegion());
        regionModel.setDescription(regionDTO.getDescription());
        regionModel.setStatus(regionDTO.getStatus());

        RegionModel updatedRegion = regionService.updateRegion(id, regionModel, request);
        return ResponseEntity.ok(updatedRegion.toDTO());
    }

    @DeleteMapping("/delete/{idRegion}")
    public ResponseEntity<?> deleteRegion(@PathVariable Long idRegion, HttpServletRequest request) {
        try {
            RegionDTO deletedRegion = regionService.deleteRegion(idRegion, request);
            return new ResponseEntity<>(deletedRegion, HttpStatus.OK);
        } catch (IllegalStateException e) {
            // Envía el mensaje de error en el cuerpo de la respuesta
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }



    @PatchMapping("/status/{id}")
    public ResponseEntity<Void> updateRegionStatus(@PathVariable Long id, @RequestParam int status, HttpServletRequest request) {
        regionService.updateRegionStatus(id, status, request);
        return ResponseEntity.noContent().build();
    }
}