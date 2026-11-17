import { useCallback, useEffect, useMemo, useState } from 'react';
import { Check, Clock3, MapPin, Plane, Plus, RefreshCw, Search, Settings2, Wrench, X } from 'lucide-react';
import {
  cancelMaintenanceWindow, createAirport, createAircraft, createMaintenanceWindow,
  getAircraft, getAirports, getMaintenanceWindows, updateAirport, updateAircraft,
  updateAircraftStatus,
} from './api.js';

function items(page) { return Array.isArray(page) ? page : (page?.content || []); }
function localDateTimeValue(date) { return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16); }
function formatDateTime(value) {
  return value ? new Intl.DateTimeFormat(undefined, { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value)) : '—';
}
function titleCase(value) { return (value || 'UNKNOWN').replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase()); }

function ResourceHeading({ eyebrow, title, subtitle, onRefresh, refreshing, actionLabel, onAction }) {
  return <section className="page-heading"><div><p className="eyebrow heading-eyebrow">{eyebrow}</p><h1>{title}</h1><p className="page-subtitle">{subtitle}</p></div><div className="heading-actions"><button className="button button-secondary" type="button" onClick={onRefresh} disabled={refreshing}><RefreshCw size={15} className={refreshing ? 'spin' : ''} /> Refresh</button><button className="button button-primary" type="button" onClick={onAction}><Plus size={17} /> {actionLabel}</button></div></section>;
}

function ResourceError({ error, clear }) {
  return error ? <div className="alert-error" role="alert"><span>{error}</span><button type="button" onClick={clear} aria-label="Dismiss error"><X size={16} /></button></div> : null;
}

function ResourceSearch({ value, onChange, placeholder = 'Search records' }) {
  return <label className="search-box"><Search size={16} /><input value={value} onChange={(event) => onChange(event.target.value)} placeholder={placeholder} aria-label={placeholder} /></label>;
}

function SimpleModal({ title, subtitle, onClose, children }) {
  useEffect(() => {
    function handleKey(event) { if (event.key === 'Escape') onClose(); }
    window.addEventListener('keydown', handleKey);
    return () => window.removeEventListener('keydown', handleKey);
  }, [onClose]);
  return <div className="dialog-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose(); }}><section className="dialog" role="dialog" aria-modal="true" aria-labelledby="resource-dialog-title"><div className="dialog-heading"><div><p className="eyebrow">AEROOPS RECORD</p><h2 id="resource-dialog-title">{title}</h2><p>{subtitle}</p></div><button className="icon-button" type="button" onClick={onClose} aria-label="Close dialog"><X size={18} /></button></div>{children}</section></div>;
}

function MetricCard({ icon, label, value, note, tone }) {
  return <article className="metric-card"><span className={`metric-icon metric-${tone}`}>{icon}</span><span className="metric-label">{label}</span><strong className="metric-value">{String(value).padStart(2, '0')}</strong><span className="metric-note">{note}</span></article>;
}

function EmptyResource({ icon, title, note, action, onAction }) {
  return <div className="empty-state"><span className="empty-icon">{icon}</span><h3>{title}</h3><p>{note}</p>{action && <button className="button button-primary" type="button" onClick={onAction}><Plus size={16} /> {action}</button>}</div>;
}

export function AircraftPage({ token }) {
  const [aircraft, setAircraft] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [query, setQuery] = useState('');
  const [error, setError] = useState('');
  const [toast, setToast] = useState('');
  const [dialog, setDialog] = useState(null);
  const [workingId, setWorkingId] = useState(null);

  const load = useCallback(async (quiet = false) => {
    quiet ? setRefreshing(true) : setLoading(true);
    setError('');
    try { setAircraft(items(await getAircraft(token))); }
    catch (requestError) { setError(requestError.message); }
    finally { setLoading(false); setRefreshing(false); }
  }, [token]);
  useEffect(() => { load(); }, [load]);
  useEffect(() => { if (!toast) return undefined; const id = window.setTimeout(() => setToast(''), 3200); return () => window.clearTimeout(id); }, [toast]);

  const visible = useMemo(() => {
    const term = query.toLowerCase().trim();
    return aircraft.filter((item) => !term || [item.registrationCode, item.manufacturer, item.model, item.status].some((value) => value?.toLowerCase().includes(term)));
  }, [aircraft, query]);
  const ready = aircraft.filter((item) => item.status === 'READY').length;
  const inMaintenance = aircraft.filter((item) => item.status === 'IN_MAINTENANCE').length;
  const retired = aircraft.filter((item) => item.status === 'RETIRED').length;

  async function changeStatus(item, status) {
    setWorkingId(item.aircraftId); setError('');
    try { await updateAircraftStatus(token, item.aircraftId, status); setToast(`${item.registrationCode} status updated.`); await load(true); }
    catch (requestError) { setError(requestError.message); }
    finally { setWorkingId(null); }
  }
  function finish(message) { setDialog(null); setToast(message); load(true); }

  return <>
    <ResourceHeading eyebrow="FLEET READINESS" title="Aircraft" subtitle="Manage registrations and keep the fleet status current." onRefresh={() => load(true)} refreshing={refreshing} actionLabel="Register aircraft" onAction={() => setDialog({ type: 'create' })} />
    <ResourceError error={error} clear={() => setError('')} />
    <section className="metric-grid resource-metrics"><MetricCard icon={<Plane size={17} />} label="Total aircraft" value={aircraft.length} note="Registered fleet" tone="mint" /><MetricCard icon={<Check size={17} />} label="Ready to fly" value={ready} note="Available for assignment" tone="blue" /><MetricCard icon={<Wrench size={17} />} label="In maintenance" value={inMaintenance} note="Temporarily unavailable" tone="amber" /><MetricCard icon={<Settings2 size={17} />} label="Retired" value={retired} note="Out of active service" tone="violet" /></section>
    <section className="schedule-card"><div className="schedule-toolbar"><div><h2>Fleet registry</h2><p>Aircraft details and operational readiness.</p></div><div className="toolbar-controls"><ResourceSearch value={query} onChange={setQuery} placeholder="Search fleet" /></div></div>
      {loading ? <div className="loading-state"><span className="loader" /> Loading fleet…</div> : visible.length === 0 ? <EmptyResource icon={<Plane size={22} />} title={query ? 'No matching aircraft' : 'No aircraft registered'} note={query ? 'Try a different registration or model.' : 'Register an aircraft before assigning it to a flight.'} action={!query ? 'Register aircraft' : null} onAction={() => setDialog({ type: 'create' })} /> :
        <div className="table-wrap"><table className="flight-table resource-table"><thead><tr><th>AIRCRAFT</th><th>MANUFACTURER / MODEL</th><th>CAPACITY</th><th>STATUS</th><th>CHANGE STATUS</th><th></th></tr></thead><tbody>{visible.map((item) => <tr key={item.aircraftId}><td><div className="aircraft-cell"><span className="aircraft-symbol"><Plane size={14} /></span><span><strong>{item.registrationCode}</strong><small>Aircraft #{item.aircraftId}</small></span></div></td><td><div className="route-cell"><strong>{item.manufacturer}</strong><small>{item.model}</small></div></td><td>{item.seatingCapacity} seats</td><td><span className={`status-pill status-${(item.status || '').toLowerCase()}`}><i />{titleCase(item.status)}</span></td><td><select className="resource-select" aria-label={`Change status for ${item.registrationCode}`} value={item.status} disabled={workingId === item.aircraftId} onChange={(event) => changeStatus(item, event.target.value)}>{['READY', 'IN_MAINTENANCE', 'RETIRED'].map((status) => <option key={status} value={status}>{titleCase(status)}</option>)}</select></td><td><button className="text-action" type="button" onClick={() => setDialog({ type: 'edit', aircraft: item })}>Edit</button></td></tr>)}</tbody></table></div>}
      <div className="table-footer"><span>Showing <strong>{visible.length}</strong> of <strong>{aircraft.length}</strong> aircraft</span><span className="footer-sync"><i /> Synced with fleet data</span></div>
    </section>
    {dialog && <AircraftDialog key={`${dialog.type}-${dialog.aircraft?.aircraftId || 'new'}`} aircraft={dialog.aircraft} token={token} onClose={() => setDialog(null)} onDone={finish} />}
    {toast && <div className="toast"><span><Check size={15} /></span>{toast}</div>}
  </>;
}

function AircraftDialog({ aircraft, token, onClose, onDone }) {
  const [form, setForm] = useState({ registrationCode: aircraft?.registrationCode || '', manufacturer: aircraft?.manufacturer || '', model: aircraft?.model || '', seatingCapacity: aircraft?.seatingCapacity || '' });
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  function update(event) { setForm((current) => ({ ...current, [event.target.name]: event.target.value })); }
  async function submit(event) {
    event.preventDefault(); setBusy(true); setError('');
    const payload = { registrationCode: form.registrationCode.trim().toUpperCase(), manufacturer: form.manufacturer.trim(), model: form.model.trim(), seatingCapacity: Number(form.seatingCapacity) };
    try { if (aircraft) await updateAircraft(token, aircraft.aircraftId, payload); else await createAircraft(token, payload); onDone(aircraft ? 'Aircraft details updated.' : `${payload.registrationCode} registered.`); }
    catch (requestError) { setError(requestError.message); }
    finally { setBusy(false); }
  }
  return <SimpleModal title={aircraft ? 'Edit aircraft' : 'Register aircraft'} subtitle="Enter the fleet details used for flight assignment." onClose={onClose}><form className="dialog-form" onSubmit={submit}><div className="field"><label htmlFor="aircraft-registration">Registration code</label><input id="aircraft-registration" name="registrationCode" value={form.registrationCode} onChange={update} maxLength="16" placeholder="e.g. VT-ABC" required /></div><div className="field-grid"><div className="field"><label htmlFor="aircraft-maker">Manufacturer</label><input id="aircraft-maker" name="manufacturer" value={form.manufacturer} onChange={update} maxLength="80" placeholder="e.g. Airbus" required /></div><div className="field"><label htmlFor="aircraft-model">Model</label><input id="aircraft-model" name="model" value={form.model} onChange={update} maxLength="80" placeholder="e.g. A320" required /></div></div><div className="field"><label htmlFor="aircraft-capacity">Seating capacity</label><input id="aircraft-capacity" name="seatingCapacity" type="number" min="1" value={form.seatingCapacity} onChange={update} placeholder="180" required /></div>{error && <div className="form-error" role="alert">{error}</div>}<div className="dialog-actions"><button type="button" className="button button-secondary" onClick={onClose}>Cancel</button><button type="submit" className="button button-primary" disabled={busy}>{busy ? 'Saving…' : aircraft ? 'Save changes' : 'Register aircraft'}</button></div></form></SimpleModal>;
}

export function AirportsPage({ token }) {
  const [airports, setAirports] = useState([]); const [loading, setLoading] = useState(true); const [refreshing, setRefreshing] = useState(false); const [query, setQuery] = useState(''); const [error, setError] = useState(''); const [toast, setToast] = useState(''); const [editing, setEditing] = useState(null);
  const load = useCallback(async (quiet = false) => { quiet ? setRefreshing(true) : setLoading(true); setError(''); try { setAirports(items(await getAirports(token))); } catch (requestError) { setError(requestError.message); } finally { setLoading(false); setRefreshing(false); } }, [token]);
  useEffect(() => { load(); }, [load]);
  useEffect(() => { if (!toast) return undefined; const id = window.setTimeout(() => setToast(''), 3200); return () => window.clearTimeout(id); }, [toast]);
  const visible = useMemo(() => { const term = query.toLowerCase().trim(); return airports.filter((item) => !term || [item.iataCode, item.name, item.city, item.countryCode].some((value) => value?.toLowerCase().includes(term))); }, [airports, query]);
  function finish(message) { setEditing(null); setToast(message); load(true); }
  return <>
    <ResourceHeading eyebrow="ROUTE NETWORK" title="Airports" subtitle="Maintain the airport directory used when planning routes." onRefresh={() => load(true)} refreshing={refreshing} actionLabel="Add airport" onAction={() => setEditing({})} />
    <ResourceError error={error} clear={() => setError('')} />
    <section className="metric-grid resource-metrics"><MetricCard icon={<MapPin size={17} />} label="Airports" value={airports.length} note="Available in the directory" tone="mint" /><MetricCard icon={<Plane size={17} />} label="Route planning" value="On" note="Used by flight schedules" tone="blue" /><MetricCard icon={<Check size={17} />} label="Unique codes" value="IATA" note="Three-letter airport codes" tone="violet" /><MetricCard icon={<Clock3 size={17} />} label="Ready to use" value={airports.length} note="Selectable for new flights" tone="amber" /></section>
    <section className="schedule-card"><div className="schedule-toolbar"><div><h2>Airport directory</h2><p>Airport codes, names, and locations.</p></div><div className="toolbar-controls"><ResourceSearch value={query} onChange={setQuery} placeholder="Search airports" /></div></div>
      {loading ? <div className="loading-state"><span className="loader" /> Loading airports…</div> : visible.length === 0 ? <EmptyResource icon={<MapPin size={22} />} title={query ? 'No matching airports' : 'No airports added'} note={query ? 'Try another code, city, or name.' : 'Add at least two airports to schedule a route.'} action={!query ? 'Add airport' : null} onAction={() => setEditing({})} /> :
        <div className="table-wrap"><table className="flight-table resource-table"><thead><tr><th>IATA CODE</th><th>AIRPORT</th><th>CITY</th><th>COUNTRY</th><th></th></tr></thead><tbody>{visible.map((item) => <tr key={item.airportId}><td><span className="iata-code">{item.iataCode}</span></td><td><strong>{item.name}</strong></td><td>{item.city}</td><td><span className="country-code">{item.countryCode}</span></td><td><button className="text-action" type="button" onClick={() => setEditing(item)}>Edit</button></td></tr>)}</tbody></table></div>}
      <div className="table-footer"><span>Showing <strong>{visible.length}</strong> of <strong>{airports.length}</strong> airports</span><span className="footer-sync"><i /> Route directory</span></div>
    </section>
    {editing && <AirportDialog key={editing.airportId || 'new'} airport={editing.airportId ? editing : null} token={token} onClose={() => setEditing(null)} onDone={finish} />}
    {toast && <div className="toast"><span><Check size={15} /></span>{toast}</div>}
  </>;
}

function AirportDialog({ airport, token, onClose, onDone }) {
  const [form, setForm] = useState({ iataCode: airport?.iataCode || '', name: airport?.name || '', city: airport?.city || '', countryCode: airport?.countryCode || '' }); const [error, setError] = useState(''); const [busy, setBusy] = useState(false);
  function update(event) { setForm((current) => ({ ...current, [event.target.name]: event.target.value })); }
  async function submit(event) { event.preventDefault(); setBusy(true); setError(''); const payload = { iataCode: form.iataCode.trim().toUpperCase(), name: form.name.trim(), city: form.city.trim(), countryCode: form.countryCode.trim().toUpperCase() }; try { if (airport) await updateAirport(token, airport.airportId, payload); else await createAirport(token, payload); onDone(airport ? `${payload.iataCode} airport updated.` : `${payload.iataCode} airport added.`); } catch (requestError) { setError(requestError.message); } finally { setBusy(false); } }
  return <SimpleModal title={airport ? 'Edit airport' : 'Add airport'} subtitle="Use a unique three-letter IATA code." onClose={onClose}><form className="dialog-form" onSubmit={submit}><div className="field-grid"><div className="field"><label htmlFor="iata-code">IATA code</label><input id="iata-code" name="iataCode" value={form.iataCode} onChange={update} pattern="[A-Za-z]{3}" maxLength="3" placeholder="BOM" required /></div><div className="field"><label htmlFor="country-code">Country code</label><input id="country-code" name="countryCode" value={form.countryCode} onChange={update} pattern="[A-Za-z]{2}" maxLength="2" placeholder="IN" required /></div></div><div className="field"><label htmlFor="airport-name">Airport name</label><input id="airport-name" name="name" value={form.name} onChange={update} maxLength="120" placeholder="Chhatrapati Shivaji Maharaj International" required /></div><div className="field"><label htmlFor="airport-city">City</label><input id="airport-city" name="city" value={form.city} onChange={update} maxLength="80" placeholder="Mumbai" required /></div>{error && <div className="form-error" role="alert">{error}</div>}<div className="dialog-actions"><button type="button" className="button button-secondary" onClick={onClose}>Cancel</button><button type="submit" className="button button-primary" disabled={busy}>{busy ? 'Saving…' : airport ? 'Save changes' : 'Add airport'}</button></div></form></SimpleModal>;
}

export function MaintenancePage({ token }) {
  const [windows, setWindows] = useState([]); const [aircraft, setAircraft] = useState([]); const [loading, setLoading] = useState(true); const [refreshing, setRefreshing] = useState(false); const [query, setQuery] = useState(''); const [filter, setFilter] = useState('ALL'); const [error, setError] = useState(''); const [toast, setToast] = useState(''); const [dialogOpen, setDialogOpen] = useState(false); const [workingId, setWorkingId] = useState(null);
  const load = useCallback(async (quiet = false) => { quiet ? setRefreshing(true) : setLoading(true); setError(''); try { const [windowPage, aircraftPage] = await Promise.all([getMaintenanceWindows(token), getAircraft(token)]); setWindows(items(windowPage)); setAircraft(items(aircraftPage)); } catch (requestError) { setError(requestError.message); } finally { setLoading(false); setRefreshing(false); } }, [token]);
  useEffect(() => { load(); }, [load]);
  useEffect(() => { if (!toast) return undefined; const id = window.setTimeout(() => setToast(''), 3200); return () => window.clearTimeout(id); }, [toast]);
  const aircraftById = useMemo(() => new Map(aircraft.map((item) => [item.aircraftId, item])), [aircraft]);
  const visible = useMemo(() => { const term = query.toLowerCase().trim(); return windows.filter((item) => (filter === 'ALL' || item.status === filter) && (!term || [item.aircraftRegistrationCode, item.reason, item.status].some((value) => value?.toLowerCase().includes(term)))); }, [windows, query, filter]);
  const scheduled = windows.filter((item) => item.status === 'SCHEDULED').length; const cancelled = windows.filter((item) => item.status === 'CANCELLED').length;
  async function cancel(item) { setWorkingId(item.maintenanceWindowId); setError(''); try { await cancelMaintenanceWindow(token, item.maintenanceWindowId); setToast('Maintenance window cancelled.'); await load(true); } catch (requestError) { setError(requestError.message); } finally { setWorkingId(null); } }
  function finish(message) { setDialogOpen(false); setToast(message); load(true); }
  return <>
    <ResourceHeading eyebrow="FLEET CARE" title="Maintenance" subtitle="Schedule service windows and avoid conflicts with assigned flights." onRefresh={() => load(true)} refreshing={refreshing} actionLabel="Schedule maintenance" onAction={() => setDialogOpen(true)} />
    <ResourceError error={error} clear={() => setError('')} />
    <section className="metric-grid resource-metrics"><MetricCard icon={<Wrench size={17} />} label="Total windows" value={windows.length} note="Maintenance records" tone="mint" /><MetricCard icon={<Clock3 size={17} />} label="Scheduled" value={scheduled} note="Upcoming service" tone="amber" /><MetricCard icon={<Check size={17} />} label="Cancelled" value={cancelled} note="Cancelled before start" tone="violet" /><MetricCard icon={<Plane size={17} />} label="Fleet size" value={aircraft.length} note="Aircraft in registry" tone="blue" /></section>
    <section className="schedule-card"><div className="schedule-toolbar"><div><h2>Maintenance windows</h2><p>Planned service and aircraft availability.</p></div><div className="toolbar-controls"><ResourceSearch value={query} onChange={setQuery} placeholder="Search maintenance" /><button className="filter-button" type="button" onClick={() => setFilter((current) => current === 'ALL' ? 'SCHEDULED' : current === 'SCHEDULED' ? 'CANCELLED' : 'ALL')}>{filter === 'ALL' ? 'All windows' : titleCase(filter)} <span aria-hidden="true">⌄</span></button></div></div>
      {loading ? <div className="loading-state"><span className="loader" /> Loading maintenance…</div> : visible.length === 0 ? <EmptyResource icon={<Wrench size={22} />} title={query ? 'No matching windows' : 'No maintenance planned'} note={query ? 'Try another aircraft registration or reason.' : 'Add a maintenance window when an aircraft needs service.'} action={!query ? 'Schedule maintenance' : null} onAction={() => setDialogOpen(true)} /> :
        <div className="table-wrap"><table className="flight-table resource-table"><thead><tr><th>AIRCRAFT</th><th>SERVICE WINDOW</th><th>REASON</th><th>STATUS</th><th></th></tr></thead><tbody>{visible.map((item) => { const plane = aircraftById.get(item.aircraftId); return <tr key={item.maintenanceWindowId}><td><div className="aircraft-cell"><span className="aircraft-symbol"><Plane size={14} /></span><span><strong>{item.aircraftRegistrationCode || plane?.registrationCode || `Aircraft #${item.aircraftId}`}</strong><small>Maintenance #{item.maintenanceWindowId}</small></span></div></td><td><div className="route-cell"><strong>{formatDateTime(item.startsAt)}</strong><small>to {formatDateTime(item.endsAt)}</small></div></td><td className="reason-cell">{item.reason}</td><td><span className={`status-pill ${item.status === 'SCHEDULED' ? 'status-boarding' : 'status-cancelled'}`}><i />{titleCase(item.status)}</span></td><td>{item.status === 'SCHEDULED' && <button className="text-action text-danger" type="button" disabled={workingId === item.maintenanceWindowId} onClick={() => cancel(item)}>{workingId === item.maintenanceWindowId ? 'Cancelling…' : 'Cancel window'}</button>}</td></tr>; })}</tbody></table></div>}
      <div className="table-footer"><span>Showing <strong>{visible.length}</strong> of <strong>{windows.length}</strong> windows</span><span className="footer-sync"><i /> Conflicts checked by AeroOps</span></div>
    </section>
    {dialogOpen && <MaintenanceDialog aircraft={aircraft.filter((item) => item.status !== 'RETIRED')} token={token} onClose={() => setDialogOpen(false)} onDone={finish} />}
    {toast && <div className="toast"><span><Check size={15} /></span>{toast}</div>}
  </>;
}

function MaintenanceDialog({ aircraft, token, onClose, onDone }) {
  const [form, setForm] = useState({ aircraftId: '', startsAt: '', endsAt: '', reason: '' }); const [error, setError] = useState(''); const [busy, setBusy] = useState(false); const [minimumStart] = useState(() => localDateTimeValue(new Date(Date.now() + 5 * 60 * 1000)));
  function update(event) { setForm((current) => ({ ...current, [event.target.name]: event.target.value })); }
  async function submit(event) { event.preventDefault(); setBusy(true); setError(''); try { await createMaintenanceWindow(token, { aircraftId: Number(form.aircraftId), startsAt: new Date(form.startsAt).toISOString(), endsAt: new Date(form.endsAt).toISOString(), reason: form.reason.trim() }); onDone('Maintenance window scheduled.'); } catch (requestError) { setError(requestError.message); } finally { setBusy(false); } }
  return <SimpleModal title="Schedule maintenance" subtitle="Choose an aircraft and a future service window." onClose={onClose}><form className="dialog-form" onSubmit={submit}><div className="field"><label htmlFor="maintenance-aircraft">Aircraft</label><select id="maintenance-aircraft" name="aircraftId" value={form.aircraftId} onChange={update} required><option value="">Select aircraft</option>{aircraft.map((item) => <option key={item.aircraftId} value={item.aircraftId}>{item.registrationCode} · {item.model} · {titleCase(item.status)}</option>)}</select></div><div className="field-grid"><div className="field"><label htmlFor="maintenance-start">Starts</label><input id="maintenance-start" type="datetime-local" name="startsAt" min={minimumStart} value={form.startsAt} onChange={update} required /></div><div className="field"><label htmlFor="maintenance-end">Ends</label><input id="maintenance-end" type="datetime-local" name="endsAt" min={form.startsAt || minimumStart} value={form.endsAt} onChange={update} required /></div></div><div className="field"><label htmlFor="maintenance-reason">Reason</label><textarea id="maintenance-reason" name="reason" value={form.reason} onChange={update} maxLength="300" rows="3" placeholder="Describe the planned inspection or repair" required /></div><p className="inline-note">AeroOps blocks windows that overlap assigned flights or other scheduled maintenance.</p>{error && <div className="form-error" role="alert">{error}</div>}<div className="dialog-actions"><button type="button" className="button button-secondary" onClick={onClose}>Cancel</button><button type="submit" className="button button-primary" disabled={busy || aircraft.length === 0}>{busy ? 'Scheduling…' : 'Schedule window'}</button></div></form></SimpleModal>;
}
