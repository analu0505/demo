package com.proyecto.demo.service;

import com.proyecto.demo.model.User;
import com.proyecto.demo.model.VaultItem;
import com.proyecto.demo.repository.UserRepository;
import com.proyecto.demo.repository.VaultItemRepository;
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

    public List<VaultItem> listarPorUsuario(Long userId) {
        return vaultRepo.findAllByOwnerId(userId);
    }

    public VaultItem obtener(Long id) {
        return vaultRepo.findById(id).orElse(null);
    }

    public VaultItem crear(Long userId, VaultItem item) {
        User owner = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        EncryptionService.EncryptionResult enc =
                encryptionService.encrypt(item.getContenidoPlano());

        item.setOwner(owner);
        item.setContentEnc(enc.getContentEnc());
        item.setNonce(enc.getNonce());
        item.setKdfSalt(enc.getKdfSalt());
        item.setContenidoPlano(null); // no guardar texto plano

        VaultItem saved = vaultRepo.save(item);

        // Auditoría: creación
        auditLogService.log(
                userId,
                "VAULT_CREATE",
                "Creado item id=" + saved.getId() + " por userId=" + userId
        );

        return saved;
    }

    public VaultItem editar(Long userId, Long itemId, VaultItem item) {
        VaultItem db = vaultRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));

        if (!db.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("No tiene permiso para editar este item");
        }

        db.setTitulo(item.getTitulo());

        if (item.getContenidoPlano() != null && !item.getContenidoPlano().isBlank()) {
            EncryptionService.EncryptionResult enc =
                    encryptionService.encrypt(item.getContenidoPlano());
            db.setContentEnc(enc.getContentEnc());
            db.setNonce(enc.getNonce());
            db.setKdfSalt(enc.getKdfSalt());
            db.setContenidoPlano(null);
        }

        VaultItem saved = vaultRepo.save(db);

        // Auditoría: edición
        auditLogService.log(
                userId,
                "VAULT_UPDATE",
                "Editado item id=" + saved.getId() + " por userId=" + userId
        );

        return saved;
    }

    public void eliminar(Long userId, Long itemId) {
        VaultItem db = vaultRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));

        if (!db.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("No tiene permiso para eliminar este item");
        }

        vaultRepo.delete(db);

        // Auditoría: eliminación
        auditLogService.log(
                userId,
                "VAULT_DELETE",
                "Eliminado item id=" + itemId + " por userId=" + userId
        );
    }

    public String descifrarContenido(Long userId, Long itemId) {
        VaultItem db = vaultRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado"));

        if (!db.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("No tiene permiso para ver este contenido");
        }

        String contenido = encryptionService.decrypt(
                db.getContentEnc(),
                db.getNonce(),
                db.getKdfSalt()
        );

        // Auditoría: descifrado
        auditLogService.log(
                userId,
                "VAULT_DECRYPT",
                "Descifrado item id=" + itemId + " por userId=" + userId
        );

        return contenido;
    }
}

