package com.proyecto.demo.repository;

import com.proyecto.demo.model.VaultItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VaultItemRepository extends JpaRepository<VaultItem, Long> {

    List<VaultItem> findAllByOwnerId(Long ownerId);
}

