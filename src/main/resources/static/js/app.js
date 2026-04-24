/**
 * PlasticAudit — app.js
 * Client-side enhancements:
 *   1. Live reduction rate preview on the waste entry form
 *   2. Auto-dismiss flash alerts
 *   3. Confirm-on-delete forms
 *   4. Loading spinners on submit buttons
 */

document.addEventListener('DOMContentLoaded', function () {

    /* ── 1. Live Reduction Rate Preview ─────────────────────── */
    const genInput = document.getElementById('plasticGeneratedKg');
    const recInput = document.getElementById('plasticRecycledKg');
    const elimInput = document.getElementById('plasticEliminatedKg');
    const rateDisplay = document.getElementById('reductionValue');

    function updateReductionPreview() {
        const gen  = parseFloat(genInput?.value)  || 0;
        const rec  = parseFloat(recInput?.value)  || 0;
        const elim = parseFloat(elimInput?.value) || 0;

        if (gen > 0) {
            const rate = ((rec + elim) / gen) * 100;
            if (rateDisplay) {
                rateDisplay.textContent = rate.toFixed(1) + '%';
                rateDisplay.style.color = rate >= 50 ? '#16a34a' : rate >= 25 ? '#f59e0b' : '#dc2626';
            }
        } else {
            if (rateDisplay) rateDisplay.textContent = '–';
        }
    }

    [genInput, recInput, elimInput].forEach(el => {
        if (el) el.addEventListener('input', updateReductionPreview);
    });


    /* ── 2. Auto-dismiss flash alerts after 5 seconds ─────── */
    document.querySelectorAll('.alert').forEach(function (alert) {
        setTimeout(function () {
            alert.style.transition = 'opacity .6s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 700);
        }, 5000);
    });


    /* ── 3. Loading spinner on primary submit buttons ─────── */
    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            // Don't spin on delete-confirmation dialogs (they may be cancelled)
            const btn = form.querySelector('button[type="submit"]');
            if (btn && !form.dataset.noSpin) {
                btn.disabled = true;
                const original = btn.innerHTML;
                btn.innerHTML = '⏳ Processing…';
                // Re-enable after 8 seconds as safety fallback
                setTimeout(() => {
                    btn.disabled = false;
                    btn.innerHTML = original;
                }, 8000);
            }
        });
    });


    /* ── 4. Animate progress bars on load ───────────────────── */
    document.querySelectorAll('.progress-bar-fill').forEach(bar => {
        const target = bar.style.width || '0%';
        bar.style.width = '0%';
        requestAnimationFrame(() => {
            setTimeout(() => { bar.style.width = target; }, 100);
        });
    });


    /* ── 5. Table row row-hover highlight (accessibility) ───── */
    document.querySelectorAll('.data-table tbody tr').forEach(row => {
        row.setAttribute('tabindex', '0');
    });


    /* ── 6. Socket panel "Connect & Send" button feedback ───── */
    const testClientBtn = document.getElementById('testClientBtn');
    if (testClientBtn) {
        const clientForm = document.getElementById('clientTestForm');
        if (clientForm) {
            clientForm.addEventListener('submit', function () {
                testClientBtn.textContent = '🔌 Connecting…';
            });
        }
    }

    const generateBtn = document.getElementById('generateBtn');
    if (generateBtn) {
        const generateForm = document.getElementById('generateForm');
        if (generateForm) {
            generateForm.addEventListener('submit', function () {
                generateBtn.innerHTML = '⚡ Spawning threads…';
                generateBtn.disabled = true;
            });
        }
    }

});
