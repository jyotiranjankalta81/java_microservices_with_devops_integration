import { useState } from 'react';
import api from '../api/client';
import { useAuth } from '../hooks/useAuth';

type AuthMode = 'login' | 'register';

export default function LoginPage() {
  const { login } = useAuth();
  const [mode, setMode] = useState<AuthMode>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const resetMessages = () => {
    setError('');
    setSuccess('');
  };

  const openRegister = () => {
    resetMessages();
    setMode('register');
  };

  const openLogin = () => {
    resetMessages();
    setMode('login');
    setConfirmPassword('');
  };

  const validateRegister = (): string | null => {
    if (!fullName.trim()) return 'Full name is required.';
    if (!email.trim()) return 'Email is required.';
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) return 'Enter a valid email address.';
    if (!password) return 'Password is required.';
    if (password.length < 8) return 'Password must be at least 8 characters.';
    if (password.length > 100) return 'Password must be at most 100 characters.';
    if (password !== confirmPassword) return 'Passwords do not match.';
    return null;
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    resetMessages();
    setSubmitting(true);
    try {
      const { data } = await api.post('/auth/login', { email: email.trim(), password });
      login(data.accessToken, data);
    } catch (err: unknown) {
      const message =
        err && typeof err === 'object' && 'response' in err
          ? (err as { response?: { data?: { message?: string } } }).response?.data?.message
          : undefined;
      setError(message ?? 'Invalid email or password.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    resetMessages();

    const validationError = validateRegister();
    if (validationError) {
      setError(validationError);
      return;
    }

    setSubmitting(true);
    try {
      const { data } = await api.post('/auth/register', {
        email: email.trim(),
        password,
        fullName: fullName.trim(),
      });
      setSuccess('Account created successfully. Signing you in...');
      login(data.accessToken, data);
    } catch (err: unknown) {
      const message =
        err && typeof err === 'object' && 'response' in err
          ? (err as { response?: { data?: { message?: string } } }).response?.data?.message
          : undefined;
      setError(message ?? 'Registration failed. Email may already be in use.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="login-page">
      <div className="card login-card">
        <h2>Job Intelligence Platform</h2>
        <p>Track opportunities. Analyze pipeline. Export to Excel.</p>

        {error && <div className="error">{error}</div>}
        {success && <div className="success">{success}</div>}

        {mode === 'login' ? (
          <form className="login-form" onSubmit={handleLogin}>
            <label>
              Email
              <input
                type="email"
                required
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
              />
            </label>
            <label>
              Password
              <input
                type="password"
                required
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Your password"
              />
            </label>
            <div className="login-actions">
              <button className="btn-primary" type="submit" disabled={submitting}>
                {submitting ? 'Signing in...' : 'Sign In'}
              </button>
              <button
                className="btn-secondary"
                type="button"
                onClick={openRegister}
                disabled={submitting}
              >
                Create User
              </button>
            </div>
          </form>
        ) : (
          <form className="login-form" onSubmit={handleRegister}>
            <label>
              Full Name <span className="required">*</span>
              <input
                type="text"
                required
                autoComplete="name"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="John Doe"
                maxLength={255}
              />
            </label>
            <label>
              Email <span className="required">*</span>
              <input
                type="email"
                required
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
              />
            </label>
            <label>
              Password <span className="required">*</span>
              <input
                type="password"
                required
                autoComplete="new-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Minimum 8 characters"
                minLength={8}
                maxLength={100}
              />
              <span className="field-hint">Must be 8–100 characters</span>
            </label>
            <label>
              Confirm Password <span className="required">*</span>
              <input
                type="password"
                required
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Re-enter password"
                minLength={8}
                maxLength={100}
              />
            </label>
            <div className="login-actions">
              <button className="btn-primary" type="submit" disabled={submitting}>
                {submitting ? 'Creating account...' : 'Register'}
              </button>
              <button
                className="btn-secondary"
                type="button"
                onClick={openLogin}
                disabled={submitting}
              >
                Back to Sign In
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
