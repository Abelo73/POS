package com.novapos.user.web;

import com.novapos.user.api.UserFacade;
import com.novapos.user.api.dto.AppUserDto;
import com.novapos.user.api.dto.RoleDto;
import com.novapos.user.web.dto.AssignRoleRequest;
import com.novapos.user.web.dto.CreateUserRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
class UserController {

    private final UserFacade userFacade;

    UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @PostMapping
    ResponseEntity<AppUserDto> create(@Valid @RequestBody CreateUserRequest request) {
        var user = userFacade.createUser(request.companyId(), request.email(), request.phone());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.id())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    @GetMapping("/{userId}")
    ResponseEntity<AppUserDto> getById(@PathVariable UUID userId) {
        return userFacade.getUser(userId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> UserException.notFound(userId));
    }

    @PostMapping("/{userId}/roles")
    ResponseEntity<Void> assignRole(@PathVariable UUID userId, @Valid @RequestBody AssignRoleRequest request) {
        userFacade.assignRole(userId, request.roleName(), request.branchId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/password")
    ResponseEntity<Void> setPassword(@PathVariable UUID userId, @Valid @RequestBody SetPasswordRequest request) {
        userFacade.setPassword(userId, request.password());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/pin")
    ResponseEntity<Void> setPin(@PathVariable UUID userId, @Valid @RequestBody SetPinRequest request) {
        userFacade.setPin(userId, request.pin());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(userFacade.getAllRoles());
    }

    record SetPasswordRequest(@NotBlank String password) {}
    record SetPinRequest(@NotBlank String pin) {}
}
