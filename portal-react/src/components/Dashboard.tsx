import React from 'react';
import { useAnalytics, ProductStat } from '../hooks/useAnalytics';

// ── Stat card ─────────────────────────────────────────────────────────────────

interface StatCardProps {
  label: string;
  value: number | null;
  accent?: 'positive' | 'danger' | 'highlight';
}

function StatCard({ label, value, accent }: StatCardProps) {
  const colorMap = { positive: '#2A6146', danger: '#B83C2E', highlight: '#D4920C' };
  const color = accent ? colorMap[accent] : '#1C1C1A';

  return (
    <div style={styles.statCard}>
      <p style={styles.statLabel}>{label}</p>
      <p style={{ ...styles.statValue, color }}>
        {value !== null ? value.toLocaleString() : '—'}
      </p>
    </div>
  );
}

// ── Leaderboard row ───────────────────────────────────────────────────────────

function LeaderboardRow({ stat, rank, max }: { stat: ProductStat; rank: number; max: number }) {
  const pct = Math.round((stat.count / max) * 100);
  const isTop = rank <= 2;

  return (
    <div style={styles.lbRow}>
      <span style={{ ...styles.lbRank, color: isTop ? '#D4920C' : '#7A7A74' }}>
        0{rank}
      </span>
      <div style={styles.lbInfo}>
        <p style={styles.lbName}>{stat.product_name}</p>
        <p style={styles.lbId}>{stat.product_id}</p>
      </div>
      <div style={styles.lbBarWrap}>
        <div style={styles.lbTrack}>
          <div style={{
            ...styles.lbFill,
            width: `${pct}%`,
            background: isTop ? '#D4920C' : '#2A6146',
          }} />
        </div>
        <p style={styles.lbCount}>{stat.count.toLocaleString()}</p>
      </div>
    </div>
  );
}

// ── Dashboard ─────────────────────────────────────────────────────────────────

export default function Dashboard() {
  const { data, loading, error, refresh, lastUpdated } = useAnalytics();

  const abandonment =
    data && data.cart_adds > 0
      ? Math.round((data.cart_clears / data.cart_adds) * 100)
      : 0;

  return (
    <div style={styles.page}>
      {/* Header */}
      <div style={styles.header}>
        <div>
          <h1 style={styles.heading}>Overview</h1>
          <p style={styles.subheading}>Real-time activity from your CartWishlist SDK.</p>
        </div>
        <div style={styles.headerRight}>
          {lastUpdated && (
            <span style={styles.updatedAt}>
              Updated {lastUpdated.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </span>
          )}
          <button style={styles.refreshBtn} onClick={refresh} disabled={loading}>
            {loading ? 'Loading…' : '↻ Refresh'}
          </button>
        </div>
      </div>

      {/* Error banner */}
      {error && (
        <div style={styles.errorBanner}>
          ⚠ Could not reach analytics API: {error}
        </div>
      )}

      {/* Stats row */}
      <div style={styles.statsRow}>
        <StatCard label="Cart Adds"      value={data?.cart_adds      ?? null} accent="positive"  />
        <StatCard label="Active Carts"   value={data?.active_carts   ?? null}                     />
        <StatCard label="Abandoned"      value={data?.cart_clears    ?? null} accent="danger"    />
        <StatCard label="Wishlist Saves" value={data?.wishlist_adds  ?? null} accent="highlight" />
      </div>

      {/* Two-column */}
      <div style={styles.grid2}>
        {/* Cart activity */}
        <div style={styles.card}>
          <div style={styles.cardHeader}>
            <span style={styles.cardTitle}>Cart Activity</span>
            <span style={styles.cardMeta}>{data?.cart_shares ?? 0} shares</span>
          </div>
          <div style={styles.cardBody}>
            <p style={styles.abanLabel}>Abandonment rate</p>
            <p style={{ ...styles.abanPct, color: '#B83C2E' }}>{loading ? '—' : `${abandonment}%`}</p>
            <div style={styles.abanTrack}>
              <div style={{ ...styles.abanFill, width: `${100 - abandonment}%`, background: '#2A6146' }} />
              <div style={{ ...styles.abanFill, width: `${abandonment}%`, background: '#B83C2E' }} />
            </div>
            <div style={styles.legendRow}>
              <span><span style={{ ...styles.dot, background: '#2A6146' }} />Active {data?.active_carts ?? '—'}</span>
              <span><span style={{ ...styles.dot, background: '#B83C2E' }} />Abandoned {data?.cart_clears ?? '—'}</span>
            </div>
          </div>
        </div>

        {/* Wishlist leaderboard */}
        <div style={styles.card}>
          <div style={styles.cardHeader}>
            <span style={styles.cardTitle}>Popular in Wishlist</span>
            <span style={styles.cardMeta}>{data?.wishlist_adds?.toLocaleString() ?? '—'} total saves</span>
          </div>
          {loading ? (
            <p style={{ padding: '24px', color: '#7A7A74' }}>Loading…</p>
          ) : (data?.top_wishlisted?.length ?? 0) === 0 ? (
            <p style={{ padding: '24px', color: '#7A7A74' }}>No wishlist data yet.</p>
          ) : (
            data!.top_wishlisted.map((stat, i) => (
              <LeaderboardRow
                key={stat.product_id}
                stat={stat}
                rank={i + 1}
                max={data!.top_wishlisted[0].count}
              />
            ))
          )}
        </div>
      </div>
    </div>
  );
}

// ── Styles ────────────────────────────────────────────────────────────────────

const styles: Record<string, React.CSSProperties> = {
  page:        { padding: '36px', fontFamily: 'system-ui, sans-serif', color: '#1C1C1A', background: '#F5F3EE', minHeight: '100vh' },
  header:      { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '28px' },
  heading:     { fontSize: '20px', fontWeight: 700, margin: 0 },
  subheading:  { fontSize: '13px', color: '#7A7A74', marginTop: '4px' },
  headerRight: { display: 'flex', alignItems: 'center', gap: '12px' },
  updatedAt:   { fontSize: '11px', color: '#7A7A74' },
  refreshBtn:  { padding: '8px 14px', borderRadius: '6px', border: '1px solid #E0DDD5', background: '#fff', cursor: 'pointer', fontSize: '12px' },
  errorBanner: { background: 'rgba(184,60,46,.08)', border: '1px solid rgba(184,60,46,.3)', borderRadius: '8px', padding: '10px 14px', fontSize: '12.5px', color: '#B83C2E', marginBottom: '20px' },
  statsRow:    { display: 'grid', gridTemplateColumns: 'repeat(4,1fr)', gap: '16px', marginBottom: '24px' },
  statCard:    { background: '#fff', border: '1px solid #E0DDD5', borderRadius: '8px', padding: '20px 22px' },
  statLabel:   { fontSize: '10.5px', letterSpacing: '.09em', textTransform: 'uppercase', color: '#7A7A74', fontWeight: 600, marginBottom: '6px' },
  statValue:   { fontFamily: 'Courier New, monospace', fontSize: '32px', fontWeight: 700, lineHeight: 1 },
  grid2:       { display: 'grid', gridTemplateColumns: '1fr 1.35fr', gap: '20px' },
  card:        { background: '#fff', border: '1px solid #E0DDD5', borderRadius: '8px', overflow: 'hidden' },
  cardHeader:  { display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', padding: '20px 24px 14px', borderBottom: '1px solid #E0DDD5' },
  cardTitle:   { fontSize: '12px', fontWeight: 700, letterSpacing: '.09em', textTransform: 'uppercase', color: '#7A7A74' },
  cardMeta:    { fontSize: '11px', color: '#7A7A74', fontFamily: 'Courier New, monospace' },
  cardBody:    { padding: '24px' },
  abanLabel:   { fontSize: '11px', letterSpacing: '.08em', textTransform: 'uppercase', color: '#7A7A74', fontWeight: 600, marginBottom: '6px' },
  abanPct:     { fontFamily: 'Courier New, monospace', fontSize: '36px', fontWeight: 700, lineHeight: 1, marginBottom: '14px' },
  abanTrack:   { display: 'flex', height: '6px', borderRadius: '99px', overflow: 'hidden', background: '#E0DDD5', marginBottom: '12px' },
  abanFill:    { height: '100%', transition: 'width .6s ease' },
  legendRow:   { display: 'flex', gap: '16px', fontSize: '13px', color: '#1C1C1A' },
  dot:         { display: 'inline-block', width: '10px', height: '10px', borderRadius: '50%', marginRight: '6px' },
  lbRow:       { display: 'flex', alignItems: 'center', padding: '14px 24px', borderBottom: '1px solid #E0DDD5' },
  lbRank:      { fontFamily: 'Courier New, monospace', fontSize: '11px', fontWeight: 700, width: '28px', flexShrink: 0 },
  lbInfo:      { flex: 1, padding: '0 12px' },
  lbName:      { fontSize: '14.5px', fontWeight: 600, margin: 0 },
  lbId:        { fontFamily: 'Courier New, monospace', fontSize: '10.5px', color: '#7A7A74', margin: '2px 0 0' },
  lbBarWrap:   { width: '80px', flexShrink: 0 },
  lbTrack:     { height: '4px', background: '#E0DDD5', borderRadius: '99px', overflow: 'hidden', marginBottom: '4px' },
  lbFill:      { height: '100%', borderRadius: '99px', transition: 'width .5s ease' },
  lbCount:     { fontFamily: 'Courier New, monospace', fontSize: '13px', fontWeight: 700, textAlign: 'right', margin: 0 },
};
