export function MetricCard({ label, value, accent = 'gold' }) {
  return (
    <div className={`metric-card metric-card--${accent}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
