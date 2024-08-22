package com.example.ITTools.infrastructure.controllers.role;

import com.example.ITTools.application.usecases.roles.DeleteRolesUseCase;
import com.example.ITTools.application.usecases.roles.GetRolesUseCase;
import com.example.ITTools.application.usecases.roles.RegisterRolesUseCase;
import com.example.ITTools.application.usecases.roles.UpdateRolesUseCase;
import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final GetRolesUseCase getRolesUseCase;
    private final RegisterRolesUseCase registerRolesUseCase;
    private final DeleteRolesUseCase deleteRolesUseCase;
    private final UpdateRolesUseCase updateRolesUseCase;


    public RoleController(GetRolesUseCase getRolesUseCase,
                          RegisterRolesUseCase registerRolesUseCase,
                          DeleteRolesUseCase deleteRolesUseCase,
                          UpdateRolesUseCase updateRolesUseCase) {
        this.getRolesUseCase = getRolesUseCase;
        this.registerRolesUseCase = registerRolesUseCase;
        this.deleteRolesUseCase = deleteRolesUseCase;
        this.updateRolesUseCase = updateRolesUseCase;
    }

    @GetMapping
    public ResponseEntity<List<RolesDTO>> getRoles() {
        List<RolesDTO> roles = getRolesUseCase.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PostMapping
    public ResponseEntity<RolesDTO> registerRole(@RequestBody RolesDTO roleDTO) {
        // Ensure that the status is always true when registering a new role
        roleDTO.setStatus(true);
        RolesDTO savedRole = registerRolesUseCase.register(roleDTO);
        return new ResponseEntity<>(savedRole, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolesDTO> updateRole(@PathVariable UUID id, @RequestBody RolesDTO roleDTO) {
        RolesDTO updatedRole = updateRolesUseCase.updateRole(id, roleDTO);
        return new ResponseEntity<>(updatedRole, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        deleteRolesUseCase.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
