package com.example.ITTools.infrastructure.controllers.role;

import com.example.ITTools.application.usecases.roles.DeleteRolesUseCase;
import com.example.ITTools.application.usecases.roles.GetRolesUseCase;
import com.example.ITTools.application.usecases.roles.RegisterRolesUseCase;
import com.example.ITTools.application.usecases.roles.UpdateRolesUseCase;
import com.example.ITTools.domain.ports.in.auth.dtos.RolesDTO;
import com.example.ITTools.infrastructure.adapters.jpa.role.repositories.RoleRepositoryAdapter;
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
    public ResponseEntity<Object> updateRole(@PathVariable UUID id, @RequestBody RolesDTO roleDTO) {
        try {
            RolesDTO updatedRole = updateRolesUseCase.updateRole(id, roleDTO);
            return new ResponseEntity<>(updatedRole, HttpStatus.OK);
        } catch (RoleRepositoryAdapter.RoleAlreadyExistsException e) {
            // Retornar 409 Conflict con mensaje de error
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            // Retornar 404 Not Found u otro estado según la excepción
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable UUID id) {
        try {
            deleteRolesUseCase.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (RoleRepositoryAdapter.RoleInUseException e) {
            // Retornar 409 Conflict con un mensaje explicativo en el cuerpo
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


}
