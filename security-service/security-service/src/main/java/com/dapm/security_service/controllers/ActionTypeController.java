package com.dapm.security_service.controllers;

import com.dapm.security_service.models.ActionType;
import com.dapm.security_service.repositories.interfaces.IActionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/actions")
public class ActionTypeController {

    @Autowired
    private IActionTypeRepository actionTypeRepository;

    @GetMapping
    public List<ActionType> getAllActions() {
        return actionTypeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ActionType getActionTypeById(@PathVariable UUID id) {
        return actionTypeRepository.findById(id).orElse(null);
    }

    @GetMapping("/name/{name}")
    public ActionType getActionTypeByName(@PathVariable String name) {
        return actionTypeRepository.findByName(name);
    }

    @PostMapping
    public ActionType createActionType(@RequestBody ActionType action) {
        if (action.getId() == null) {
            action.setId(UUID.randomUUID());
        }
        return actionTypeRepository.save(action);
    }

    @PutMapping("/{id}")
    public ActionType updateActionType(@PathVariable UUID id, @RequestBody ActionType actionType) {
        actionType.setId(id);
        return actionTypeRepository.save(actionType);
    }

    @DeleteMapping("/{id}")
    public void deleteActionType(@PathVariable UUID id) {
        actionTypeRepository.deleteById(id);
    }
}
