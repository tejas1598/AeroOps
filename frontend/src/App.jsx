import { useState } from 'react';
import { Activity, Eye, EyeOff, Plane, ShieldCheck } from 'lucide-react';
import { login } from './api.js';
import FlightsPage from './FlightsPage.jsx';

export default function App() {
  const [token, setToken] = useState(() => sessionStorage.getItem('aeroops-token'));
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  async function handleLogin(event) {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      const nextToken = await login(username.trim(), password);
      sessionStorage.setItem('aeroops-token', nextToken);
      setToken(nextToken);
      setPassword('');
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  function handleLogout() {
    sessionStorage.removeItem('aeroops-token');
    setToken(null);
  }

  if (token) return <FlightsPage token={token} onLogout={handleLogout} />;

  return (
    <main className="login-layout">
      <section className="login-story">
        <div className="brand brand-light">
          <span className="brand-mark"><Plane size={21} strokeWidth={2.4} /></span>
          <span>AeroOps</span>
        </div>
        <div className="story-copy">
          <p className="eyebrow">AIRLINE OPERATIONS PLATFORM</p>
          <h1>Keep every aircraft<br />ready for its next flight.</h1>
          <p className="story-subtitle">A clear view of schedules, aircraft readiness, and the small decisions that keep a fleet moving.</p>
        </div>
        <div className="story-foot"><span className="live-dot" /> Operations workspace <span className="story-foot-divider">·</span> Local environment</div>
        <div className="story-orbit orbit-one" />
        <div className="story-orbit orbit-two" />
        <div className="story-grid" />
      </section>

      <section className="login-panel">
        <div className="login-card">
          <div className="login-icon"><Activity size={20} /></div>
          <p className="eyebrow">WELCOME BACK</p>
          <h2>Sign in to AeroOps</h2>
          <p className="login-caption">Use your operations account to open the control desk.</p>
          <form onSubmit={handleLogin} className="login-form">
            <label htmlFor="username">Username</label>
            <input id="username" autoComplete="username" value={username} onChange={(event) => setUsername(event.target.value)} placeholder="Enter your username" required />
            <label htmlFor="password">Password</label>
            <div className="password-wrap">
              <input id="password" type={showPassword ? 'text' : 'password'} autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Enter your password" required />
              <button className="password-toggle" type="button" onClick={() => setShowPassword((value) => !value)} aria-label={showPassword ? 'Hide password' : 'Show password'}>
                {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
              </button>
            </div>
            {error && <div className="form-error" role="alert">{error}</div>}
            <button className="button button-primary login-submit" type="submit" disabled={busy}>
              {busy ? 'Signing in…' : 'Sign in'} <span aria-hidden="true">→</span>
            </button>
          </form>
          <div className="secure-note"><ShieldCheck size={15} /> Protected with AeroOps token authentication</div>
        </div>
        <div className="login-footer">AeroOps <span>·</span> Fleet operations, made clear</div>
      </section>
    </main>
  );
}
