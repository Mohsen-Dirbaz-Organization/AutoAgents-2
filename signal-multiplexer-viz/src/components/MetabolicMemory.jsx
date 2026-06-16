import './MetabolicMemory.css';

/**
 * Metabolic Memory (Lane C / §3). Data is stratified by RESIDENCE TIME, not
 * type. Reflexive + tactical tiers are analog; the strategic tier is digital
 * Posit/quire. The τ = 5 s boundary is the analog/digital seam: nothing analog
 * crosses it without being re-quantized. Retrieval is a budgeted optimization
 * gated by each tier's shadow price λ_k (ΔJ/C ≥ λ_k).
 */
function MetabolicMemory({ memory }) {
  if (!memory) return null;
  const { tiers, boundaryTau, promotions, requantizations, evictions } = memory;

  return (
    <div className="metabolic-memory">
      <div className="mm-summary">
        <span><strong>{promotions}</strong> promotions</span>
        <span><strong>{requantizations}</strong> re-quantized at τ={boundaryTau}s</span>
        <span><strong>{evictions}</strong> aged out</span>
      </div>

      <div className="mm-tiers">
        {tiers.map((tier, idx) => (
          <div key={tier.id}>
            <div className={`mm-tier substrate-${tier.substrate}`}>
              <div className="mm-tier-head">
                <span className="mm-tier-name">{tier.label}</span>
                <span className={`mm-substrate ${tier.substrate}`}>
                  {tier.substrate === 'analog' ? '〰 analog' : '▦ digital Posit/quire'}
                </span>
              </div>
              <div className="mm-tier-meta">
                <span>τ ≈ {tier.tau < 1 ? `${(tier.tau * 1000).toFixed(0)} ms` : `${tier.tau}s`}</span>
                <span>{tier.store}</span>
                <span>λ = {tier.shadowPrice.toFixed(2)}</span>
              </div>
              <div className="mm-predicate">verify: {tier.predicate}</div>

              <div className="mm-slots">
                {Array.from({ length: tier.capacity }).map((_, i) => {
                  const e = tier.entries[i];
                  if (!e) return <div key={i} className="mm-slot empty" />;
                  const intensity = Math.max(0.1, Math.min(1, e.weight));
                  return (
                    <div
                      key={i}
                      className={`mm-slot filled ${e.lowTrajectory ? 'threat' : ''} ${e.digitized ? 'digitized' : ''}`}
                      style={{ opacity: 0.35 + 0.65 * intensity }}
                      title={`weight ${e.weight.toFixed(2)} · residence ${e.residence.toFixed(1)}s · ΔJ/C ${(e.deltaJ / e.cost).toFixed(2)}`}
                    >
                      {e.lowTrajectory ? '⚠' : ''}{e.digitized ? '▦' : ''}
                    </div>
                  );
                })}
              </div>
              <div className="mm-fill-label">
                {tier.entries.length}/{tier.capacity} held
              </div>
            </div>

            {idx < tiers.length - 1 && (
              <div className={`mm-boundary ${tiers[idx + 1].substrate === 'digital' ? 'tau-seam' : ''}`}>
                {tiers[idx + 1].substrate === 'digital'
                  ? `↓ τ = ${boundaryTau}s boundary — re-quantize to Posit/quire (safe-side round up), re-admit under cross-modality confirmation`
                  : '↓ promote (cheap)'}
              </div>
            )}
          </div>
        ))}
      </div>

      <div className="mm-legend">
        <span><i className="mm-key threat" />⚠ enforced-causality ingest (threat inflated)</span>
        <span><i className="mm-key digitized" />▦ re-quantized digital</span>
        <span>opacity ∝ importance weight (analog tiers decay)</span>
      </div>
    </div>
  );
}

export default MetabolicMemory;
