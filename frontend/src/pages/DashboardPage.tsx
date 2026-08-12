import { useQuery } from '@tanstack/react-query';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import api from '../api/client';

export default function DashboardPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: async () => (await api.get('/dashboard')).data,
  });

  if (isLoading) return <p>Loading dashboard...</p>;

  const companyData = Object.entries(data?.byCompany ?? {}).map(([name, count]) => ({ name, count }));

  return (
    <div>
      <h2>Dashboard</h2>
      <div className="kpi-grid">
        <div className="kpi-card"><span>Total Jobs</span><strong>{data?.totalJobs ?? 0}</strong></div>
        <div className="kpi-card"><span>Today</span><strong>{data?.jobsToday ?? 0}</strong></div>
        <div className="kpi-card"><span>This Week</span><strong>{data?.jobsThisWeek ?? 0}</strong></div>
        <div className="kpi-card"><span>This Month</span><strong>{data?.jobsThisMonth ?? 0}</strong></div>
      </div>
      <div className="card chart-card">
        <h3>Jobs by Company</h3>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={companyData}>
            <XAxis dataKey="name" /><YAxis /><Tooltip />
            <Bar dataKey="count" fill="#2563eb" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
