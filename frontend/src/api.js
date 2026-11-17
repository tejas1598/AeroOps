async function request(path, token, options = {}) {
  const headers = { Accept: 'application/json', ...options.headers };
  if (token) headers.Authorization = `Bearer ${token}`;
  if (options.body) headers['Content-Type'] = 'application/json';

  let response;
  try {
    response = await fetch(path, { ...options, headers });
  } catch {
    throw new Error('Could not reach AeroOps. Check that the backend is running.');
  }

  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!response.ok) {
    if (response.status === 401 && !path.startsWith('/auth/')) {
      throw new Error('Your session expired. Please sign in again.');
    }
    if (response.status === 403) {
      throw new Error('Your account does not have permission for this action.');
    }
    const message = data?.message || (path.startsWith('/auth/') && response.status === 401
      ? 'Username or password is incorrect.'
      : `Request failed (${response.status}).`);
    const fieldErrors = data?.errors && typeof data.errors === 'object'
      ? Object.entries(data.errors).map(([field, detail]) => `• ${field}: ${detail}`)
      : [];
    throw new Error([message, ...fieldErrors].join('\n'));
  }

  return data;
}

export async function login(username, password) {
  const result = await request('/auth/login', null, {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  });
  return typeof result === 'string' ? result.replace(/^"|"$/g, '') : result;
}

export function getFlights(token) {
  return request('/api/v1/flights?page=0&size=100&sort=scheduledDeparture,asc', token);
}

export function getAircraft(token) {
  return request('/api/v1/aircraft?page=0&size=100&sort=registrationCode,asc', token);
}

export function getAirports(token) {
  return request('/api/v1/airports?page=0&size=100&sort=iataCode,asc', token);
}

export function getMaintenanceWindows(token) {
  return request('/api/v1/maintenance-windows?page=0&size=100&sort=startsAt,asc', token);
}

export function createAircraft(token, aircraft) {
  return request('/api/v1/aircraft', token, { method: 'POST', body: JSON.stringify(aircraft) });
}

export function updateAircraft(token, aircraftId, aircraft) {
  return request(`/api/v1/aircraft/${aircraftId}`, token, { method: 'PUT', body: JSON.stringify(aircraft) });
}

export function updateAircraftStatus(token, aircraftId, status) {
  return request(`/api/v1/aircraft/${aircraftId}/status`, token, { method: 'PATCH', body: JSON.stringify({ status }) });
}

export function createAirport(token, airport) {
  return request('/api/v1/airports', token, { method: 'POST', body: JSON.stringify(airport) });
}

export function updateAirport(token, airportId, airport) {
  return request(`/api/v1/airports/${airportId}`, token, { method: 'PUT', body: JSON.stringify(airport) });
}

export function createMaintenanceWindow(token, window) {
  return request('/api/v1/maintenance-windows', token, { method: 'POST', body: JSON.stringify(window) });
}

export function cancelMaintenanceWindow(token, windowId) {
  return request(`/api/v1/maintenance-windows/${windowId}/cancel`, token, { method: 'PATCH' });
}

export function scheduleFlight(token, flight) {
  return request('/api/v1/flights', token, {
    method: 'POST',
    body: JSON.stringify(flight),
  });
}

export function assignAircraft(token, flightId, aircraftId) {
  return request(`/api/v1/flights/${flightId}/aircraft`, token, {
    method: 'PUT',
    body: JSON.stringify({ aircraftId: Number(aircraftId) }),
  });
}

export function changeFlightStatus(token, flightId, status) {
  return request(`/api/v1/flights/${flightId}/status`, token, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  });
}
