/**
 * BlindHire - Audit Logs Controller
 */

document.addEventListener("DOMContentLoaded", async () => {
    if (!api.requireAuth()) return;
    await loadAuditLogs();
});

async function loadAuditLogs() {
    const tableBody = document.getElementById("audit-logs-tbody");
    if (!tableBody) return;

    try {
        const res = await api.get("/audit-logs");
        const logs = res.data || [];

        if (logs.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 32px;">No audit events recorded yet.</td></tr>`;
            return;
        }

        tableBody.innerHTML = logs.map(log => {
            const actionBadgeClass = getActionBadgeClass(log.action);
            return `
                <tr>
                    <td>
                        <span class="badge ${actionBadgeClass}">${escapeHtml(log.action)}</span>
                    </td>
                    <td>
                        <strong>${escapeHtml(log.userName)}</strong>
                        <div style="font-size: 0.775rem; color: var(--text-muted);">${escapeHtml(log.userEmail)}</div>
                    </td>
                    <td>
                        <span style="font-family: monospace; font-weight: 600;">${escapeHtml(log.candidateCode)}</span>
                    </td>
                    <td style="max-width: 400px; font-size: 0.875rem;">
                        ${escapeHtml(log.description)}
                    </td>
                    <td style="font-size: 0.8rem; color: var(--text-secondary); white-space: nowrap;">
                        ${formatTimestamp(log.timestamp)}
                    </td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--danger); padding: 24px;">Failed to load audit logs: ${err.message}</td></tr>`;
    }
}

function getActionBadgeClass(action) {
    if (!action) return "badge-gray";
    switch (action.toUpperCase()) {
        case "IDENTITY_REVEAL": return "badge-warning";
        case "CREATE_JOB": return "badge-primary";
        case "RESUME_SCREENED": return "badge-success";
        case "DELETE_JOB": return "badge-danger";
        default: return "badge-info";
    }
}

function formatTimestamp(ts) {
    if (!ts) return "-";
    const d = new Date(ts);
    return isNaN(d.getTime()) ? ts : d.toLocaleString();
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
