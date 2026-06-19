import { useState, useEffect, useCallback, useRef } from 'react';

export interface ProductStat {
  product_id: string;
  product_name: string;
  count: number;
}

export interface AnalyticsData {
  cart_adds: number;
  cart_clears: number;
  cart_shares: number;
  wishlist_adds: number;
  active_carts: number;
  top_wishlisted: ProductStat[];
  top_carted: ProductStat[];
}

interface UseAnalyticsResult {
  data: AnalyticsData | null;
  loading: boolean;
  error: string | null;
  refresh: () => void;
  lastUpdated: Date | null;
}

const API_BASE = process.env.REACT_APP_API_BASE ?? 'http://localhost:8000';
const POLL_MS  = 30_000;

export function useAnalytics(): UseAnalyticsResult {
  const [data, setData]               = useState<AnalyticsData | null>(null);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const abortRef                      = useRef<AbortController | null>(null);

  const fetchData = useCallback(async (showLoading = false) => {
    // Cancel any in-flight request before starting a new one
    abortRef.current?.abort();
    abortRef.current = new AbortController();

    if (showLoading) setLoading(true);

    try {
      const res = await fetch(`${API_BASE}/api/analytics`, {
        signal: abortRef.current.signal,
        headers: { Accept: 'application/json' },
      });

      if (!res.ok) throw new Error(`Server returned ${res.status}`);

      const json: AnalyticsData = await res.json();
      setData(json);
      setError(null);
      setLastUpdated(new Date());
    } catch (err) {
      if ((err as Error).name === 'AbortError') return; // intentional cancel
      setError((err as Error).message ?? 'Unknown error');
    } finally {
      setLoading(false);
    }
  }, []);

  // Initial fetch + polling
  useEffect(() => {
    fetchData(true);
    const timer = setInterval(() => fetchData(false), POLL_MS);

    return () => {
      clearInterval(timer);
      abortRef.current?.abort();
    };
  }, [fetchData]);

  return { data, loading, error, refresh: () => fetchData(true), lastUpdated };
}
