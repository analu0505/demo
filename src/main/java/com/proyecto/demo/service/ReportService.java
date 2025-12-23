package com.proyecto.demo.service;

import com.proyecto.demo.model.AuditLog;
import com.proyecto.demo.model.User;
import com.proyecto.demo.repository.AuditLogRepository;
import com.proyecto.demo.repository.UserRepository;
import com.proyecto.demo.repository.VaultItemRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private final AuditLogRepository auditRepo;
    private final VaultItemRepository vaultRepo;
    private final UserRepository userRepo;

    public ReportService(AuditLogRepository auditRepo,
                         VaultItemRepository vaultRepo,
                         UserRepository userRepo) {
        this.auditRepo = auditRepo;
        this.vaultRepo = vaultRepo;
        this.userRepo = userRepo;
    }

    public byte[] generarReporteCsv() {

        // Traer auditoría ordenada por fecha desc
        List<AuditLog> logs = auditRepo.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));

        long totalVaultItems = vaultRepo.count();

        long loginSuccess = logs.stream()
                .filter(l -> "LOGIN_SUCCESS".equalsIgnoreCase(l.getAction()))
                .count();

        long loginFailed = logs.stream()
                .filter(l -> "LOGIN_FAILED".equalsIgnoreCase(l.getAction()))
                .count();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StringBuilder sb = new StringBuilder();
        sb.append("SAFEBOX - REPORTE DEL SISTEMA\n");
        sb.append("TotalVaultItems,").append(totalVaultItems).append("\n");
        sb.append("TotalLoginSuccess,").append(loginSuccess).append("\n");
        sb.append("TotalLoginFailed,").append(loginFailed).append("\n");
        sb.append("\n");

        // Detalle de auditoría (mínimo: login exitoso y fallido)
        sb.append("AuditID,Fecha,UsuarioEmail,Accion,Detalle\n");

        for (AuditLog l : logs) {
            // Solo incluir eventos de login (para cumplir exacto el requerimiento)
            if (!"LOGIN_SUCCESS".equalsIgnoreCase(l.getAction())
                    && !"LOGIN_FAILED".equalsIgnoreCase(l.getAction())) {
                continue;
            }

            String email = "N/A";
            if (l.getUserId() != null) {
                User u = userRepo.findById(l.getUserId()).orElse(null);
                if (u != null && u.getEmail() != null) {
                    email = u.getEmail();
                }
            }

            String fecha = (l.getTimestamp() != null) ? l.getTimestamp().format(fmt) : "";

            sb.append(safe(l.getId())).append(",")
              .append(csv(email)).append(",")  // ojo: aquí vamos a usar email
              .append(csv(l.getAction())).append(",")
              .append(csv(l.getDetails())).append("\n");

            // 🔧 Corrige orden para que sea: AuditID,Fecha,UsuarioEmail,Accion,Detalle
            // como ya tenemos "fecha" lo insertamos bien:
            // (Para no enredarte, lo dejamos en el formato correcto abajo en una sola línea)
        }

        // ✅ Re-escribir el detalle bien (sin confusión):
        // (Esto evita que quede mal el orden)
        String header = "SAFEBOX - REPORTE DEL SISTEMA\n"
                + "TotalVaultItems," + totalVaultItems + "\n"
                + "TotalLoginSuccess," + loginSuccess + "\n"
                + "TotalLoginFailed," + loginFailed + "\n\n"
                + "AuditID,Fecha,UsuarioEmail,Accion,Detalle\n";

        StringBuilder detail = new StringBuilder();
        for (AuditLog l : logs) {
            if (!"LOGIN_SUCCESS".equalsIgnoreCase(l.getAction())
                    && !"LOGIN_FAILED".equalsIgnoreCase(l.getAction())) {
                continue;
            }

            String email = "N/A";
            if (l.getUserId() != null) {
                User u = userRepo.findById(l.getUserId()).orElse(null);
                if (u != null && u.getEmail() != null) email = u.getEmail();
            }

            String fecha = (l.getTimestamp() != null) ? l.getTimestamp().format(fmt) : "";

            detail.append(safe(l.getId())).append(",")
                  .append(csv(fecha)).append(",")
                  .append(csv(email)).append(",")
                  .append(csv(l.getAction())).append(",")
                  .append(csv(l.getDetails()))
                  .append("\n");
        }

        String finalCsv = header + detail;
        return finalCsv.getBytes(StandardCharsets.UTF_8);
    }

    private String safe(Object o) {
        return (o == null) ? "" : o.toString();
    }

    // Escapa comas y saltos para CSV básico
    private String csv(String s) {
        if (s == null) return "";
        String clean = s.replace("\n", " ").replace("\r", " ");
        if (clean.contains(",") || clean.contains("\"")) {
            clean = clean.replace("\"", "\"\"");
            return "\"" + clean + "\"";
        }
        return clean;
    }
}
