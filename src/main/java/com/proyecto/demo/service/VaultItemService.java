package com.proyecto.demo.service;

import com.proyecto.demo.model.Role;
import com.proyecto.demo.model.User;
import com.proyecto.demo.model.VaultItem;
import com.proyecto.demo.repository.UserRepository;
import com.proyecto.demo.repository.VaultItemRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VaultItemService {

    private final VaultItemRepository vaultRepo;
    private final UserRepository userRepo;
    private final EncryptionService encryptionService;
    private final AuditLogService auditLogService;

    public VaultItemService(VaultItemRepository vaultRepo,
                            UserRepository userRepo,
                            EncryptionService encryptionService,
                            AuditLogService auditLogService) {
        this.vaultRepo = vaultRepo;
        this.userRepo = userRepo;
        this.encryptionService = encryptionService;
        this.auditLogService = auditLogService;
    }

    // ✅ Obtener el usuario autenticado por email
    public User getUserFromAuth(Authentication auth) {
        String email = auth.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
    }

    // ✅ ADMIN puede listar bóveda de cualquiera (controller decide)
    public List<VaultItem> listarPorOwner(Long ownerId) {
        return vaultRepo.findAllByOwnerId(ownerId);
    }

    // ✅ Obtener item por id
    public VaultItem obtener(Long id) {
        return vaultRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("VaultItem no encontrado: " + id));
    }

    /**
     * ✅ Crear VaultItem:
     * - USER solo puede crear para sí mismo
     * - ADMIN puede crear para sí o para otro (según ownerId recibido)
     * - cifra contenidoPlano -> contentEnc, nonce, kdfSalt
     */
    public VaultItem crear(Long ownerId, VaultItem item, Authentication auth) {

        User requester = getUserFromAuth(auth);
        boolean isAdmin = requester.getRole() == Role.ADMIN;

        // USER solo en su propia bóveda
        if (!isAdmin && !requester.getId().equals(ownerId)) {
            throw new IllegalArgumentException("No tienes permiso para crear items para otro usuario.");
        }

        User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado: " + ownerId));

        String plain = item.getContenidoPlano();
        if (plain == null || plain.isBlank()) {
            throw new IllegalArgumentException("El contenido no puede estar vacío.");
        }

        // Cifrar
        EncryptionService.EncryptionResult res = encryptionService.encrypt(plain);

        item.setOwner(owner);
        item.setContentEnc(res.getContentEnc());
        item.setNonce(res.getNonce());
        item.setKdfSalt(res.getKdfSalt());

        // limpiar transient
        item.setContenidoPlano(null);

        VaultItem saved = vaultRepo.save(item);

        auditLogService.registrar(requester.getId(), "VAULT_CREATE",
                "Creó VaultItem id=" + saved.getId() + " para ownerId=" + ownerId);

        return saved;
    }

    /**
     * ✅ Descifrar SOLO si:
     * - el que pide es dueño
     * - y NO es ADMIN (según tu requisito: admin NO ve descifrado)
     */
    public String descifrarContenido(VaultItem item, Long requesterUserId) {
        User requester = userRepo.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        boolean isAdmin = requester.getRole() == Role.ADMIN;
        boolean isOwner = item.getOwner().getId().equals(requesterUserId);

        if (isAdmin || !isOwner) {
            return null; // no permitido
        }

        auditLogService.registrar(requesterUserId, "VAULT_DECRYPT",
                "Descifró VaultItem id=" + item.getId());

        return encryptionService.decrypt(item.getContentEnc(), item.getNonce(), item.getKdfSalt());
    }

    /**
     * ✅ Eliminar:
     * - USER solo elimina lo suyo
     * - ADMIN puede eliminar cualquiera (si lo ocupás)
     */
    public void eliminar(Long id, Authentication auth) {
        User requester = getUserFromAuth(auth);
        VaultItem item = obtener(id);

        boolean isAdmin = requester.getRole() == Role.ADMIN;
        boolean isOwner = item.getOwner().getId().equals(requester.getId());

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este VaultItem.");
        }

        vaultRepo.deleteById(id);

        auditLogService.registrar(requester.getId(), "VAULT_DELETE",
                "Eliminó VaultItem id=" + id + " (ownerId=" + item.getOwner().getId() + ")");
    }
}

