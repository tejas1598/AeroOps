import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Activity, ArrowDownUp, CalendarDays, Check, ChevronDown, CircleHelp,
  Clock3, LogOut, MapPin, Plane, Plus, RefreshCw, Search, Settings2,
  SlidersHorizontal, Sparkles, X,
} from 'lucide-react';
import {
  assignAircraft, changeFlightStatus, getAircraft, getAirports, getFlights, scheduleFlight,
} from './api.js';
import { AircraftPage, AirportsPage, MaintenancePage } from './ResourcePages.jsx';

const EMPTY_DATA = { flights: [], aircraft: [], airports: [] };

function collection(page) {
  return Array.isArray(page) ? page : (page?.content || []);
}

function formatTime(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat(undefined, { hour: '2-digit', minute: '2-digit', timeZoneName: 'short' }).format(new Date(value));
}

function formatDate(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat(undefined, { weekday: 'short', month: 'short', day: 'numeric' }).format(new Date(value));
}

function duration(departure, arrival) {
  const minutes = Math.max(0, Math.round((new Date(arrival) - new Date(departure)) / 60000));
  return `${Math.floor(minutes / 60)}h ${String(minutes % 60).padStart(2, '0')}m`;
}

function localDateTimeValue(date) {
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 16);
}

function nextStatus(status) {
  return {
    SCHEDULED: ['BOARDING', 'CANCELLED'],
    BOARDING: ['IN_AIR', 'CANCELLED'],
    IN_AIR: ['LANDED'],
  }[status] || [];
}

function prettyStatus(status) {
  return (status || 'UNKNOWN').replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase());
}

export default function FlightsPage({ token, onLogout }) {
  const [activePage, setActivePage] = useState('flights');
  const [currentTime, setCurrentTime] = useState(() => new Date());
  const [data, setData] = useState(EMPTY_DATA);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState('');
  const [query, setQuery] = useState('');
  const [dayFilter, setDayFilter] = useState('all');
  const [dialog, setDialog] = useState(null);
  const [toast, setToast] = useState('');
  const [workingId, setWorkingId] = useState(null);

  const loadData = useCallback(async (quiet = false) => {
    if (quiet) setRefreshing(true);
    else setLoading(true);
    setError('');
    try {
      const [flightPage, aircraftPage, airportPage] = await Promise.all([
        getFlights(token), getAircraft(token), getAirports(token),
      ]);
      setData({ flights: collection(flightPage), aircraft: collection(aircraftPage), airports: collection(airportPage) });
    } catch (requestError) {
      setError(requestError.message);
      if (requestError.message.includes('session expired')) onLogout();
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [token, onLogout]);

  useEffect(() => { loadData(); }, [loadData]);
  useEffect(() => {
    const timer = window.setInterval(() => setCurrentTime(new Date()), 1000);
    return () => window.clearInterval(timer);
  }, []);
  useEffect(() => {
    if (!toast) return undefined;
    const timer = window.setTimeout(() => setToast(''), 3600);
    return () => window.clearTimeout(timer);
  }, [toast]);

  function openFlightSchedule() {
    setActivePage('flights');
    loadData(true);
  }

  const aircraftById = useMemo(() => new Map(data.aircraft.map((aircraft) => [aircraft.aircraftId, aircraft])), [data.aircraft]);
  const visibleFlights = useMemo(() => {
    const search = query.trim().toLowerCase();
    return data.flights.filter((flight) => {
      const aircraft = aircraftById.get(flight.aircraftId);
      const matchesSearch = !search || [flight.flightNumber, flight.originAirportCode, flight.destinationAirportCode, aircraft?.registrationCode]
        .some((value) => value?.toLowerCase().includes(search));
      const flightDate = new Date(flight.scheduledDeparture);
      const today = new Date();
      const matchesDay = dayFilter === 'all'
        || (dayFilter === 'today' && flightDate.toDateString() === today.toDateString())
        || (dayFilter === 'upcoming' && flightDate > today && flight.status !== 'CANCELLED');
      return matchesSearch && matchesDay;
    });
  }, [data.flights, aircraftById, query, dayFilter]);

  const activeCount = data.flights.filter((flight) => ['BOARDING', 'IN_AIR'].includes(flight.status)).length;
  const unassignedCount = data.flights.filter((flight) => !flight.aircraftId && flight.status === 'SCHEDULED').length;
  const readyCount = data.aircraft.filter((aircraft) => aircraft.status === 'READY').length;
  const pageTitles = { flights: 'Flight schedule', aircraft: 'Aircraft', airports: 'Airports', maintenance: 'Maintenance' };

  async function runAction(id, action, successMessage) {
    setWorkingId(id);
    setError('');
    try {
      await action();
      setToast(successMessage);
      await loadData(true);
    } catch (requestError) {
      setError(requestError.message);
      if (requestError.message.includes('session expired')) onLogout();
    } finally {
      setWorkingId(null);
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a className="brand" href="#flights" aria-label="AeroOps home">
          <span className="brand-mark"><Plane size={20} strokeWidth={2.4} /></span><span>AeroOps</span>
        </a>
        <div className="workspace-label">LOCAL TIME</div>
        <div className="clock-card" aria-label={`Local time ${new Intl.DateTimeFormat(undefined, { timeStyle: 'medium' }).format(currentTime)}`}>
          <time className="clock-time" dateTime={currentTime.toISOString()}>{new Intl.DateTimeFormat(undefined, { hour: '2-digit', minute: '2-digit', hour12: false }).format(currentTime)}</time>
          <span className="clock-seconds">:{String(currentTime.getSeconds()).padStart(2, '0')}</span>
          <span className="clock-date">{new Intl.DateTimeFormat(undefined, { weekday: 'long', month: 'short', day: 'numeric' }).format(currentTime)}</span>
        </div>
        <div className="sidebar-section-label">OPERATIONS</div>
        <nav className="side-nav" aria-label="Operations navigation">
          <button className={`nav-link ${activePage === 'flights' ? 'active' : ''}`} type="button" aria-pressed={activePage === 'flights'} onClick={openFlightSchedule}><Plane size={17} /> Flight schedule <span className="nav-count">{data.flights.length}</span></button>
          <button className={`nav-link ${activePage === 'aircraft' ? 'active' : ''}`} type="button" aria-pressed={activePage === 'aircraft'} onClick={() => setActivePage('aircraft')}><Settings2 size={17} /> Aircraft</button>
          <button className={`nav-link ${activePage === 'airports' ? 'active' : ''}`} type="button" aria-pressed={activePage === 'airports'} onClick={() => setActivePage('airports')}><MapPin size={17} /> Airports</button>
          <button className={`nav-link ${activePage === 'maintenance' ? 'active' : ''}`} type="button" aria-pressed={activePage === 'maintenance'} onClick={() => setActivePage('maintenance')}><SlidersHorizontal size={17} /> Maintenance</button>
        </nav>
        <div className="sidebar-bottom">
          <div className="help-card"><span className="help-icon"><CircleHelp size={16} /></span><strong>Need a hand?</strong><p>Flight actions follow the rules enforced by the AeroOps API.</p></div>
          <button className="profile-row" type="button" onClick={onLogout}><span className="profile-avatar">OP</span><span className="profile-info"><strong>Operations user</strong><small>Signed in</small></span><LogOut size={16} /></button>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div className="breadcrumbs"><span>Operations</span><span className="crumb-slash">/</span><strong>{pageTitles[activePage]}</strong></div>
          <div className="topbar-actions"><span className="system-status"><i /> Local API</span><span className="top-avatar" aria-label="Operations user">OP</span></div>
        </header>

        <div className="page-content">
          {activePage === 'flights' && <>
          <section className="page-heading">
            <div><div className="eyebrow heading-eyebrow"><span className="heading-star"><Sparkles size={13} /></span> DAILY OPERATIONS</div><h1>Flight schedule</h1><p className="page-subtitle">Plan the route, assign the aircraft, keep the operation moving.</p></div>
            <div className="heading-actions"><button className="button button-secondary" type="button" onClick={() => loadData(true)} disabled={refreshing}><RefreshCw size={15} className={refreshing ? 'spin' : ''} /> Refresh</button><button className="button button-primary" type="button" onClick={() => setDialog({ type: 'schedule' })}><Plus size={17} /> Schedule flight</button></div>
          </section>

          {error && <div className="alert-error" role="alert"><span>{error}</span><button type="button" onClick={() => setError('')} aria-label="Dismiss error"><X size={16} /></button></div>}

          <section className="metric-grid" aria-label="Flight schedule overview">
            <Metric icon={<Plane size={17} />} label="Flights on schedule" value={loading ? '—' : String(data.flights.length).padStart(2, '0')} note="Across all routes" tone="mint" />
            <Metric icon={<Activity size={17} />} label="In operation" value={loading ? '—' : String(activeCount).padStart(2, '0')} note="Boarding or in air" tone="blue" />
            <Metric icon={<Clock3 size={17} />} label="Awaiting aircraft" value={loading ? '—' : String(unassignedCount).padStart(2, '0')} note="Scheduled, unassigned" tone="amber" />
            <Metric icon={<Settings2 size={17} />} label="Aircraft ready" value={loading ? '—' : String(readyCount).padStart(2, '0')} note={`of ${data.aircraft.length} in fleet`} tone="violet" />
          </section>

          <section className="schedule-card">
            <div className="schedule-toolbar">
              <div><h2>Scheduled flights</h2><p>Review timings, assignments, and live status.</p></div>
              <div className="toolbar-controls"><label className="search-box"><Search size={16} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search flights" aria-label="Search flights" /></label><button className="filter-button" type="button" onClick={() => setDayFilter((current) => current === 'all' ? 'upcoming' : current === 'upcoming' ? 'today' : 'all')}><CalendarDays size={16} /><span>{dayFilter === 'all' ? 'All dates' : dayFilter === 'today' ? 'Today' : 'Upcoming'}</span><ChevronDown size={14} /></button></div>
            </div>

            {loading ? <div className="loading-state"><span className="loader" /> Loading flight schedule…</div> : visibleFlights.length === 0 ? (
              <div className="empty-state"><span className="empty-icon"><Plane size={22} /></span><h3>{query || dayFilter !== 'all' ? 'No matching flights' : 'Your schedule is clear'}</h3><p>{query || dayFilter !== 'all' ? 'Try a different search or date filter.' : 'Create the first flight to start your operations flow.'}</p>{!query && dayFilter === 'all' && <button type="button" className="button button-primary" onClick={() => setDialog({ type: 'schedule' })}><Plus size={16} /> Schedule first flight</button>}</div>
            ) : (
              <div className="table-wrap"><table className="flight-table"><thead><tr><th>FLIGHT</th><th>ROUTE</th><th>DEPARTURE</th><th>ARRIVAL</th><th>AIRCRAFT</th><th>STATUS</th><th><span className="sr-only">Actions</span></th></tr></thead><tbody>
                {visibleFlights.map((flight) => {
                  const aircraft = aircraftById.get(flight.aircraftId);
                  const next = nextStatus(flight.status);
                  return <tr key={flight.flightId}>
                    <td><div className="flight-number"><span className="flight-mini-icon"><Plane size={14} /></span><strong>{flight.flightNumber}</strong><small>#{String(flight.flightId).padStart(4, '0')}</small></div></td>
                    <td><div className="route-cell"><strong>{flight.originAirportCode || '—'} <span>→</span> {flight.destinationAirportCode || '—'}</strong><small>{duration(flight.scheduledDeparture, flight.scheduledArrival)}</small></div></td>
                    <td><div className="time-cell"><strong>{formatTime(flight.scheduledDeparture)}</strong><small>{formatDate(flight.scheduledDeparture)}</small></div></td>
                    <td><div className="time-cell"><strong>{formatTime(flight.scheduledArrival)}</strong><small>{formatDate(flight.scheduledArrival)}</small></div></td>
                    <td>{aircraft ? <div className="aircraft-cell"><span className="aircraft-symbol"><Plane size={14} /></span><span><strong>{aircraft.registrationCode}</strong><small>{aircraft.model}</small></span></div> : <button className="assign-link" type="button" disabled={flight.status !== 'SCHEDULED' || refreshing} onClick={() => setDialog({ type: 'assign', flight })}><Plus size={14} /> {refreshing ? 'Refreshing…' : 'Assign aircraft'}</button>}</td>
                    <td><span className={`status-pill status-${(flight.status || '').toLowerCase()}`}><i />{prettyStatus(flight.status)}</span></td>
                    <td><div className="row-actions">{next.length > 0 && <select aria-label={`Update ${flight.flightNumber} status`} value="" disabled={workingId === flight.flightId} onChange={(event) => { const status = event.target.value; if (status) runAction(flight.flightId, () => changeFlightStatus(token, flight.flightId, status), `${flight.flightNumber} moved to ${prettyStatus(status)}.`); }}><option value="">Update</option>{next.map((status) => <option value={status} key={status}>{prettyStatus(status)}</option>)}</select>}{workingId === flight.flightId && <span className="tiny-loader" />}</div></td>
                  </tr>;
                })}
              </tbody></table></div>
            )}

            <div className="table-footer"><span>Showing <strong>{visibleFlights.length}</strong> of <strong>{data.flights.length}</strong> flights</span><span className="footer-sync"><i /> Synced with operations data</span></div>
          </section>

          <footer className="page-footer"><span>Flight planning</span><span>Readiness and schedule conflicts are enforced by the AeroOps API.</span></footer>
          </>}
          {activePage === 'aircraft' && <AircraftPage token={token} />}
          {activePage === 'airports' && <AirportsPage token={token} />}
          {activePage === 'maintenance' && <MaintenancePage token={token} />}
        </div>
      </main>

      {dialog?.type === 'schedule' && <ScheduleDialog airports={data.airports} token={token} onClose={() => setDialog(null)} onDone={(message) => { setDialog(null); setToast(message); loadData(true); }} />}
      {dialog?.type === 'assign' && <AssignDialog flight={dialog.flight} aircraft={data.aircraft} token={token} onClose={() => setDialog(null)} onDone={(message) => { setDialog(null); setToast(message); loadData(true); }} />}
      {toast && <div className="toast"><span><Check size={15} /></span>{toast}</div>}
    </div>
  );
}

function Metric({ icon, label, value, note, tone }) {
  return <article className="metric-card"><span className={`metric-icon metric-${tone}`}>{icon}</span><span className="metric-label">{label}</span><strong className="metric-value">{value}</strong><span className="metric-note">{note}</span><ArrowDownUp className="metric-decoration" size={18} /></article>;
}

function ScheduleDialog({ airports, token, onClose, onDone }) {
  const [form, setForm] = useState({ flightNumber: '', originAirportId: '', destinationAirportId: '', scheduledDeparture: '', scheduledArrival: '' });
  const [earliestDeparture] = useState(() => localDateTimeValue(new Date(Date.now() + 5 * 60 * 1000)));
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  function update(event) { setForm((current) => ({ ...current, [event.target.name]: event.target.value })); }
  async function submit(event) {
    event.preventDefault();
    setBusy(true); setError('');
    try {
      await scheduleFlight(token, {
        flightNumber: form.flightNumber.trim().toUpperCase(),
        originAirportId: Number(form.originAirportId),
        destinationAirportId: Number(form.destinationAirportId),
        scheduledDeparture: new Date(form.scheduledDeparture).toISOString(),
        scheduledArrival: new Date(form.scheduledArrival).toISOString(),
      });
      onDone(`${form.flightNumber.toUpperCase()} added to the schedule.`);
    } catch (requestError) { setError(requestError.message); }
    finally { setBusy(false); }
  }
  return <DialogFrame title="Schedule a flight" subtitle="Add a route and departure window to the operations schedule." onClose={onClose}>
    <form className="dialog-form" onSubmit={submit}>
      <div className="field"><label htmlFor="flight-number">Flight number</label><input id="flight-number" name="flightNumber" value={form.flightNumber} onChange={update} placeholder="e.g. AO204" maxLength="8" pattern="[A-Za-z0-9]{2,8}" required /></div>
      <div className="field-grid"><div className="field"><label htmlFor="origin">Origin airport</label><select id="origin" name="originAirportId" value={form.originAirportId} onChange={update} required><option value="">Select origin</option>{airports.map((airport) => <option key={airport.airportId} value={airport.airportId}>{airport.iataCode} · {airport.city}</option>)}</select></div><div className="field"><label htmlFor="destination">Destination</label><select id="destination" name="destinationAirportId" value={form.destinationAirportId} onChange={update} required><option value="">Select destination</option>{airports.filter((airport) => String(airport.airportId) !== form.originAirportId).map((airport) => <option key={airport.airportId} value={airport.airportId}>{airport.iataCode} · {airport.city}</option>)}</select></div></div>
      <div className="field-grid"><div className="field"><label htmlFor="departure">Departure</label><input id="departure" type="datetime-local" name="scheduledDeparture" min={earliestDeparture} value={form.scheduledDeparture} onChange={update} required /></div><div className="field"><label htmlFor="arrival">Arrival</label><input id="arrival" type="datetime-local" name="scheduledArrival" min={form.scheduledDeparture || earliestDeparture} value={form.scheduledArrival} onChange={update} required /></div></div>
      <p className="inline-note">Departure must be in the future, and arrival must be later than departure. Times are sent to the API with a UTC offset.</p>
      {airports.length < 2 && <p className="inline-note">Add at least two airports before scheduling a flight.</p>}
      {error && <div className="form-error" role="alert">{error}</div>}
      <div className="dialog-actions"><button type="button" className="button button-secondary" onClick={onClose}>Cancel</button><button type="submit" className="button button-primary" disabled={busy || airports.length < 2}>{busy ? 'Saving…' : 'Add to schedule'}</button></div>
    </form>
  </DialogFrame>;
}

function AssignDialog({ flight, aircraft, token, onClose, onDone }) {
  const available = aircraft.filter((item) => item.status === 'READY');
  const [aircraftId, setAircraftId] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  async function submit(event) {
    event.preventDefault(); setBusy(true); setError('');
    try {
      await assignAircraft(token, flight.flightId, aircraftId);
      const assigned = available.find((item) => String(item.aircraftId) === aircraftId);
      onDone(`${assigned?.registrationCode || 'Aircraft'} assigned to ${flight.flightNumber}.`);
    } catch (requestError) { setError(requestError.message); }
    finally { setBusy(false); }
  }
  return <DialogFrame title="Assign aircraft" subtitle={`Choose a ready aircraft for ${flight.flightNumber}.`} onClose={onClose}>
    <form className="dialog-form" onSubmit={submit}>
      <div className="assignment-route"><span>{flight.originAirportCode}</span><span className="route-dash" /><span>{flight.destinationAirportCode}</span><small>{formatDate(flight.scheduledDeparture)} · {formatTime(flight.scheduledDeparture)}</small></div>
      <div className="field"><label htmlFor="aircraft-select">Available aircraft</label><select id="aircraft-select" value={aircraftId} onChange={(event) => setAircraftId(event.target.value)} required><option value="">Choose an aircraft</option>{available.map((item) => <option key={item.aircraftId} value={item.aircraftId}>{item.registrationCode} · {item.model} · {item.seatingCapacity} seats</option>)}</select></div>
      {available.length === 0 && <p className="inline-note">No aircraft are currently marked ready.</p>}
      <p className="inline-note">AeroOps checks readiness, overlapping flights, and maintenance before saving.</p>
      {error && <div className="form-error" role="alert">{error}</div>}
      <div className="dialog-actions"><button type="button" className="button button-secondary" onClick={onClose}>Cancel</button><button type="submit" className="button button-primary" disabled={busy || available.length === 0}>{busy ? 'Assigning…' : 'Confirm assignment'}</button></div>
    </form>
  </DialogFrame>;
}

function DialogFrame({ title, subtitle, onClose, children }) {
  useEffect(() => {
    function onKeyDown(event) { if (event.key === 'Escape') onClose(); }
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onClose]);
  return <div className="dialog-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}><section className="dialog" role="dialog" aria-modal="true" aria-labelledby="dialog-title"><div className="dialog-heading"><div><p className="eyebrow">FLIGHT OPERATIONS</p><h2 id="dialog-title">{title}</h2><p>{subtitle}</p></div><button className="icon-button" type="button" onClick={onClose} aria-label="Close dialog"><X size={18} /></button></div>{children}</section></div>;
}
