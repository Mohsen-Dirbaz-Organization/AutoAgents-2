import './ConservationRenormalizationPanel.css';

/**
 * The Conservation-Renormalization Layer (CRL), live on the harness. Loudness
 * is moved across the sensor multiplet so the weighted log-gain budget stays
 * pinned at Q = 0; the conserved fusion coordinate (read from shape only) does
 * not move.
 *
 * Standing discipline (EPU Companion): clauses (i)/(iii) are CONSTRUCTED —
 * true by definition/algebra, so their per-tick values are diagnostics, not
 * verification. The falsifiable live check is the adversarial MASKING PROBE
 * (clause (ii)): a compensating gain must still be detected by the shape band.
 */
function ConservationRenormalizationPanel({ crl }) {
  if (!crl) return null;
  const { channels, Qbefore, Qafter, residual, conservedDrift, verification } = crl;

  const maxAbs = Math.max(0.4, ...channels.map(c => Math.abs(c.ellStar)));
  const v = verification || {};
  const probe = crl.probe || {};
  const diag = crl.diagnostics || {};

  return (
    <div className="crl-panel">
      <div className="crl-verify">
        <div className="crl-verify-title">
          §3.4 gauge-covariance — clause standing (canonical multiplet)
        </div>
        <div className="crl-verify-rows">
          <Clause ok={v.clause_i} label="c(Rx) = c(x) · constructed" sub="true by definition — design semantics only" />
          <Clause ok={v.clause_ii} label="masking blocked · falsifiable" sub="adversarial: CAN fail (raw band does)" />
          <Clause ok={v.clause_iii} label="Q = 0 · constructed" sub="algebraic identity of the projection" />
        </div>
        <div className={`crl-live ${probe.pass ? 'ok' : 'warn'}`}>
          {probe.pass ? '✓' : '⚠'} <strong>live masking probe</strong> — adversarial gain applied; genuine
          defect {probe.detected ? 'DETECTED' : 'MASKED (violation!)'} in the shape band
          {' '}(measured {typeof probe.measured === 'number' ? probe.measured.toFixed(3) : '…'} &gt; ε {probe.epsilon})
        </div>
        <div className="crl-live diag">
          diagnostics (constructed, cannot fail): drift {diag.driftWithinTol ? '≈0' : '⚠ fault'},
          residual {diag.residualWithinTol ? '≈0' : '⚠ fault'} — rounding-fault detectors, not evidence
        </div>
      </div>

      <div className="crl-budget-label">Zero-sum gain budget — redistribute loudness, never create</div>
      <div className="crl-budget">
        <div className="crl-axis" />
        {channels.map(ch => {
          const h = (Math.abs(ch.ellStar) / maxAbs) * 46;
          return (
            <div key={ch.name} className="crl-col" title={`raw gain ${ch.gainRaw.toFixed(2)} → renormalized ${ch.gainStar.toFixed(2)}`}>
              <div className="crl-bar-area">
                {ch.amplified ? (
                  <div className="crl-bar amp" style={{ height: `${h}px`, marginTop: `${46 - h}px` }} />
                ) : (
                  <div className="crl-bar att" style={{ height: `${h}px` }} />
                )}
              </div>
              <div className="crl-col-name">{ch.name}</div>
              <div className={`crl-col-ell ${ch.amplified ? 'amp' : 'att'}`}>
                {ch.ellStar >= 0 ? '+' : ''}{ch.ellStar.toFixed(2)}
              </div>
            </div>
          );
        })}
      </div>
      <div className="crl-legend">
        <span><i className="crl-key amp" /> amplified (ℓ*&gt;0)</span>
        <span><i className="crl-key att" /> attenuated (ℓ*&lt;0)</span>
        <span>Σ wₖ ℓₖ = 0</span>
      </div>

      <div className="crl-residuals">
        <div className="crl-res">
          <span className="crl-res-k">Q before</span>
          <span className="crl-res-v">{Qbefore.toFixed(3)}</span>
        </div>
        <div className="crl-res-arrow">→ R →</div>
        <div className="crl-res">
          <span className="crl-res-k">Q after</span>
          <span className="crl-res-v zero">{fmtZero(Qafter)}</span>
        </div>
        <div className="crl-res">
          <span className="crl-res-k">|Q| residual</span>
          <span className="crl-res-v zero">{residual.toExponential(1)}</span>
        </div>
        <div className="crl-res">
          <span className="crl-res-k">conserved drift</span>
          <span className="crl-res-v zero">{conservedDrift.toExponential(1)}</span>
        </div>
      </div>

      <p className="crl-note">
        The conserved coordinate moved by {conservedDrift.toExponential(1)} (machine zero) while the
        gains were redistributed — loudness is a gauge degree of freedom, and the band is read in a
        sector the gain cannot reach.
      </p>
    </div>
  );
}

function Clause({ ok, label, sub }) {
  return (
    <div className={`crl-clause ${ok ? 'pass' : 'fail'}`}>
      <span className="crl-clause-mark">{ok ? '✓' : '✕'}</span>
      <span className="crl-clause-text">
        <strong>{label}</strong>
        <span className="crl-clause-sub">{sub}</span>
      </span>
    </div>
  );
}

function fmtZero(x) {
  return Math.abs(x) < 1e-6 ? '0' : x.toFixed(3);
}

export default ConservationRenormalizationPanel;
