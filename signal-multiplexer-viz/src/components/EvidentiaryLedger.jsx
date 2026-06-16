import './EvidentiaryLedger.css';

// Status coding mirrors the EVD assessment §10.1.2 palette.
const STANDING_META = {
  measured:    { label: 'Measured',    color: '#228B22', icon: '✓' },
  established: { label: 'Established',  color: '#2f855a', icon: '◆' },
  grounded:    { label: 'Grounded',    color: '#2b6cb0', icon: '◐' },
  ratified:    { label: 'Ratified',    color: '#6b46c1', icon: '§' },
  projected:   { label: 'Projected',   color: '#DAA520', icon: '◌' }
};

function EvidentiaryLedger({ ledger, veto, metrics }) {
  if (!ledger) return null;
  return (
    <div className="evidentiary-ledger">
      <p className="el-preamble">
        The projected / measured seam is <strong>load-bearing</strong>: every figure is
        <em> projected</em> unless explicitly <em>measured</em>. Ratifying a decision or
        publishing a plan upgrades nothing to measured.
      </p>

      <div className="el-callout">
        <div className="el-callout-num">~{veto?.lastVetoLatencyNs ?? 32} ns</div>
        <div className="el-callout-text">
          <strong>The only measured latency in the corpus.</strong> The FPGA analog-veto
          witness reaches the actuator well inside the ~{veto?.digitalWindowNs ?? 1000} ns
          digital decision window. Analog vetoes fired this run: <strong>{veto?.analogVetoCount ?? 0}</strong>.
        </div>
      </div>

      <table className="el-table">
        <thead>
          <tr><th>Element</th><th>Standing</th><th>Note</th></tr>
        </thead>
        <tbody>
          {ledger.map((row, i) => {
            const m = STANDING_META[row.standing] || STANDING_META.projected;
            return (
              <tr key={i}>
                <td className="el-el">{row.element}</td>
                <td>
                  <span className="el-badge" style={{ background: m.color }}>
                    {m.icon} {m.label}
                  </span>
                </td>
                <td className="el-note">{row.detail}</td>
              </tr>
            );
          })}
        </tbody>
      </table>

      {metrics && (
        <div className="el-metrics">
          <div className="el-metric">
            <span className="el-metric-val">{metrics.enforcedCausalityEvents}</span>
            <span className="el-metric-label">enforced-causality ingests</span>
          </div>
          <div className="el-metric">
            <span className="el-metric-val">{metrics.retrievals}</span>
            <span className="el-metric-label">shadow-price retrievals</span>
          </div>
          <div className="el-metric">
            <span className="el-metric-val">{metrics.retrievalsDenied}</span>
            <span className="el-metric-label">denied (below λ)</span>
          </div>
        </div>
      )}
    </div>
  );
}

export default EvidentiaryLedger;
