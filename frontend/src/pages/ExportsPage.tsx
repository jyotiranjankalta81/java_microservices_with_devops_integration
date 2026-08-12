import { useQuery } from '@tanstack/react-query';
import api from '../api/client';

export default function ExportsPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['exports'],
    queryFn: async () => (await api.get('/export')).data,
  });

  return (
    <div>
      <h2>Export History</h2>
      {isLoading ? <p>Loading...</p> : (
        <table className="data-table">
          <thead><tr><th>ID</th><th>Status</th><th>Created</th><th>File</th></tr></thead>
          <tbody>
            {(data ?? []).map((exp: { id: string; status: string; createdAt: string; filePath?: string }) => (
              <tr key={exp.id}>
                <td>{exp.id}</td><td>{exp.status}</td><td>{exp.createdAt}</td>
                <td>{exp.filePath ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
