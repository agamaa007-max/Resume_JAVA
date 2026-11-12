/**
 * BlindHire - Candidate Details & Authorized Identity Reveal Controller
 */

let candidateId = null;
let jobId = null;

document.addEventListener("DOMContentLoaded", async () => {
    if (!api.requireAuth()) return;

    const urlParams = new URLSearchParams(window.location.search);
    candidateId = urlParams.get("id");
    jobId = urlParams.get("jobId") || 1;

    if (!candidateId) {
        showToast("No candidate ID specified.", "error");
        setTimeout(() => window.location.href = "/candidates.html", 1000);
        return;
    }

    await loadCandidateData();
    setupRevealModal();
});

async function loadCandidateData() {
    try {
        // 1. Fetch Candidate Profile (Anonymous)
        const candRes = await api.get(`/candidates/${candidateId}`);
        const candidate = candRes.data;

        // 2. Fetch Screening Evaluation
        let screening = null;
        try {
            const screenRes = await api.get(`/screening/candidate/${candidateId}?jobId=${jobId}`);
            screening = screenRes.data;
        } catch (ignored) {}

        renderCandidateView(candidate, screening);
    } catch (err) {
        showToast("Error loading candidate profile: " + err.message, "error");
    }
}

function renderCandidateView(candidate, screening) {
    document.getElementById("candidate-code-header").textContent = candidate.candidateCode;
    document.getElementById("candidate-status-badge").textContent = candidate.status;

    // Score & Breakdown
    const finalScore = screening ? screening.finalScore : 0;
    const scoreNumEl = document.getElementById("score-number-display");
    if (scoreNumEl) scoreNumEl.textContent = `${finalScore}%`;

    if (screening) {
        document.getElementById("score-skills-val").textContent = `${screening.skillScore}%`;
        document.getElementById("score-exp-val").textContent = `${screening.experienceScore}%`;
        document.getElementById("score-edu-val").textContent = `${screening.educationScore}%`;
        document.getElementById("score-proj-val").textContent = `${screening.projectScore}%`;

        // Matched & Missing Skills
        const matchedContainer = document.getElementById("matched-skills-container");
        if (matchedContainer) {
            matchedContainer.innerHTML = (screening.matchedSkills && screening.matchedSkills.length > 0)
                ? screening.matchedSkills.map(s => `<span class="skill-chip matched">✓ ${escapeHtml(s)}</span>`).join('')
                : '<span style="color:var(--text-muted); font-size:0.85rem;">No direct required skills matched</span>';
        }

        const missingContainer = document.getElementById("missing-skills-container");
        if (missingContainer) {
            missingContainer.innerHTML = (screening.missingSkills && screening.missingSkills.length > 0)
                ? screening.missingSkills.map(s => `<span class="skill-chip missing">✕ ${escapeHtml(s)}</span>`).join('')
                : '<span style="color:var(--success); font-size:0.85rem; font-weight:600;">All required skills satisfied!</span>';
        }

        // Explanation text
        const expBox = document.getElementById("score-explanation-content");
        if (expBox) expBox.textContent = screening.scoreExplanation || "Screening score calculated via multi-factor rule algorithm.";
    }

    // Candidate Extracted Skills
    const allSkillsContainer = document.getElementById("candidate-all-skills-container");
    if (allSkillsContainer) {
        allSkillsContainer.innerHTML = (candidate.skills && candidate.skills.length > 0)
            ? candidate.skills.map(s => `<span class="skill-chip">${escapeHtml(s.skillName)}</span>`).join('')
            : '<span style="color:var(--text-muted);">No skills extracted.</span>';
    }

    // Candidate Education
    const eduContainer = document.getElementById("education-history-container");
    if (eduContainer) {
        if (!candidate.education || candidate.education.length === 0) {
            eduContainer.innerHTML = `<p style="color:var(--text-muted);">No education records found.</p>`;
        } else {
            eduContainer.innerHTML = candidate.education.map(e => `
                <div style="margin-bottom: 14px; padding-bottom: 12px; border-bottom: 1px solid var(--border-color);">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <strong style="color:var(--navy-dark); font-size:0.95rem;">${escapeHtml(e.degree || 'Degree')} in ${escapeHtml(e.field || 'General')}</strong>
                        <span class="badge badge-gray">${e.graduationYear || 'Year -'}</span>
                    </div>
                    <div style="font-size:0.85rem; color:var(--text-secondary); margin-top:2px;">
                        ${escapeHtml(e.institution || 'Recognized Institute')} ${e.percentage > 0 ? '• Score: ' + e.percentage + '%' : ''}
                    </div>
                </div>
            `).join('');
        }
    }

    // Candidate Experience
    const expContainer = document.getElementById("experience-history-container");
    if (expContainer) {
        if (!candidate.experience || candidate.experience.length === 0) {
            expContainer.innerHTML = `<p style="color:var(--text-muted);">No prior experience records (Fresher profile).</p>`;
        } else {
            expContainer.innerHTML = candidate.experience.map(exp => `
                <div style="margin-bottom: 16px; padding-bottom: 14px; border-bottom: 1px solid var(--border-color);">
                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <strong style="color:var(--navy-dark); font-size:0.95rem;">${escapeHtml(exp.jobTitle || 'Role')}</strong>
                        <span class="badge badge-primary">${exp.yearsOfExperience} yrs</span>
                    </div>
                    <div style="font-size:0.825rem; font-weight:600; color:var(--text-secondary); margin-top:2px;">
                        ${escapeHtml(exp.company || 'Enterprise')}
                    </div>
                    <p style="font-size:0.85rem; color:var(--text-secondary); margin-top:6px;">
                        ${escapeHtml(exp.description || '')}
                    </p>
                </div>
            `).join('');
        }
    }

    // Candidate Projects
    const projContainer = document.getElementById("projects-history-container");
    if (projContainer) {
        if (!candidate.projects || candidate.projects.length === 0) {
            projContainer.innerHTML = `<p style="color:var(--text-muted);">No projects listed.</p>`;
        } else {
            projContainer.innerHTML = candidate.projects.map(p => `
                <div style="margin-bottom: 14px; padding-bottom: 12px; border-bottom: 1px solid var(--border-color);">
                    <strong style="color:var(--navy-dark); font-size:0.95rem;">${escapeHtml(p.projectName || 'Project')}</strong>
                    <p style="font-size:0.85rem; color:var(--text-secondary); margin: 4px 0;">
                        ${escapeHtml(p.description || '')}
                    </p>
                    ${p.technologies ? `<div style="font-size:0.775rem; color:var(--primary); font-weight:600;">Technologies: ${escapeHtml(p.technologies)}</div>` : ''}
                </div>
            `).join('');
        }
    }
}

function setupRevealModal() {
    const revealBtn = document.getElementById("btn-open-reveal-modal");
    const modal = document.getElementById("reveal-modal");
    const closeBtn = document.getElementById("btn-close-reveal-modal");
    const cancelBtn = document.getElementById("btn-cancel-reveal");
    const confirmBtn = document.getElementById("btn-confirm-reveal");
    const reasonInput = document.getElementById("reveal-reason-input");

    if (!revealBtn || !modal) return;

    revealBtn.addEventListener("click", () => {
        modal.classList.add("active");
        if (reasonInput) reasonInput.value = "";
    });

    const closeModal = () => modal.classList.remove("active");
    if (closeBtn) closeBtn.addEventListener("click", closeModal);
    if (cancelBtn) cancelBtn.addEventListener("click", closeModal);

    if (confirmBtn) {
        confirmBtn.addEventListener("click", async () => {
            const reason = reasonInput.value.trim();
            if (!reason || reason.length < 5) {
                showToast("Please provide a legitimate justification reason (min 5 chars).", "error");
                return;
            }

            try {
                confirmBtn.disabled = true;
                confirmBtn.textContent = "Authorizing...";

                const res = await api.post(`/candidates/${candidateId}/reveal`, { reason });
                showToast("Identity revealed and logged to audit trail.", "success");
                closeModal();
                revealBtn.style.display = "none"; // Hide button once revealed

                renderRevealedIdentity(res.data);
            } catch (err) {
                showToast(err.message, "error");
            } finally {
                confirmBtn.disabled = false;
                confirmBtn.textContent = "Confirm & Reveal";
            }
        });
    }
}

function renderRevealedIdentity(identity) {
    const box = document.getElementById("revealed-identity-container");
    if (!box || !identity) return;

    box.innerHTML = `
        <div class="revealed-identity-box">
            <h3>
                <span>🔓 Candidate Identity Revealed (Audit Logged)</span>
            </h3>
            <div class="identity-grid">
                <div class="identity-item">
                    <label>Full Name</label>
                    <value>${escapeHtml(identity.name || 'Not provided')}</value>
                </div>
                <div class="identity-item">
                    <label>Email Address</label>
                    <value><a href="mailto:${escapeHtml(identity.email)}" style="color:inherit;">${escapeHtml(identity.email || 'Not provided')}</a></value>
                </div>
                <div class="identity-item">
                    <label>Phone Number</label>
                    <value>${escapeHtml(identity.phone || 'Not provided')}</value>
                </div>
                <div class="identity-item">
                    <label>Address / Location</label>
                    <value>${escapeHtml(identity.address || 'Not provided')}</value>
                </div>
            </div>
        </div>
    `;
    box.style.display = "block";
    box.scrollIntoView({ behavior: "smooth" });
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
