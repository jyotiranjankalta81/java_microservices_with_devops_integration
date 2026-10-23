import { NavLink } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function Layout({ children }: { children: React.ReactNode }) {
  const { logout } = useAuth();
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1>JobIntel</h1>
        <nav>
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/evidencing">📸 Evidencing</NavLink>
          <NavLink to="/jobs">Jobs</NavLink>
          <NavLink to="/exports">Exports</NavLink>
          <NavLink to="/observability">Observability</NavLink>
          <NavLink to="/profile">Profile</NavLink>
        </nav>
        <button className="btn-secondary" onClick={logout}>Logout</button>
      </aside>
      <main className="content">{children}</main>
    </div>
  );
}
