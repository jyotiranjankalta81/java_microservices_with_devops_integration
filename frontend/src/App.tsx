import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import JobsPage from './pages/JobsPage';
import JobFormPage from './pages/JobFormPage';
import ExportsPage from './pages/ExportsPage';
import ProfilePage from './pages/ProfilePage';
import ObservabilityPage from './pages/ObservabilityPage';
import EvidencingFeedPage from './pages/evidencing/EvidencingFeedPage';
import PostCreatePage from './pages/evidencing/PostCreatePage';
import PostDetailPage from './pages/evidencing/PostDetailPage';
import { useAuth } from './hooks/useAuth';

export default function App() {
  const { token } = useAuth();

  if (!token) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/jobs" element={<JobsPage />} />
        <Route path="/jobs/new" element={<JobFormPage />} />
        <Route path="/jobs/:id" element={<JobFormPage />} />
        <Route path="/exports" element={<ExportsPage />} />
        <Route path="/evidencing" element={<EvidencingFeedPage />} />
        <Route path="/evidencing/new" element={<PostCreatePage />} />
        <Route path="/evidencing/:id" element={<PostDetailPage />} />
        <Route path="/observability" element={<ObservabilityPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </Layout>
  );
}
