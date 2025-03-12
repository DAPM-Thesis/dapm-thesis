package com.dapm.security_service.controllers.ClientApi;

import com.dapm.security_service.models.Node;
import com.dapm.security_service.models.dtos.NodeDto;
import com.dapm.security_service.repositories.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pipeline-nodes")
public class PipelineNodeController {

    @Autowired
    private NodeRepository nodeRepository;

    @GetMapping
    public List<NodeDto> getAllNodes() {
        return nodeRepository.findAll().stream().map(NodeDto::new).toList();
    }

    @GetMapping("/{id}")
    public Node getNodeById(@PathVariable UUID id) {
        return nodeRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Node createNode(@RequestBody Node node) {
        if (node.getId() == null) {
            node.setId(UUID.randomUUID());
        }
        return nodeRepository.save(node);
    }

    @PutMapping("/{id}")
    public Node updateNode(@PathVariable UUID id, @RequestBody Node node) {
        node.setId(id);
        return nodeRepository.save(node);
    }

    @DeleteMapping("/{id}")
    public void deleteNode(@PathVariable UUID id) {
        nodeRepository.deleteById(id);
    }
}
