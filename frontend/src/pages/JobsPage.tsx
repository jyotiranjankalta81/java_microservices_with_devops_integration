import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import api from '../api/client';
import { SearchableSelect } from '../components/SearchableSelect';
import { useAuth } from '../hooks/useAuth';

interface JobItem {
  id: number;
  title: string;
  company: string;
  role: string;
  country?: string;
  state?: string;
  city?: string;
  location?: string;
  status: string;
  workMode: string;
  employmentType?: string;
  salaryMin?: number;
  salaryMax?: number;
  salaryCurrency?: string;
  description?: string;
  skills?: string[];
  technologies?: string[];
  jobUrl?: string;
  applyUrl?: string;
}

export default function JobsPage() {
  const { isAdmin } = useAuth();
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [scope, setScope] = useState<'global' | 'my'>('global');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [selectedJob, setSelectedJob] = useState<JobItem | null>(null);

  // Fetch dropdown lookup options from API
  const { data: countries = [] } = useQuery<string[]>({
    queryKey: ['job-countries'],
    queryFn: async () => (await api.get('/jobs/countries')).data,
  });

  const { data: cities = [], isLoading: loadingCities } = useQuery<string[]>({
    queryKey: ['job-cities', filters.country],
    queryFn: async () => (await api.get('/jobs/cities', { params: { country: filters.country || '' } })).data,
  });

  const { data: roles = [] } = useQuery<string[]>({
    queryKey: ['job-roles'],
    queryFn: async () => (await api.get('/jobs/roles')).data,
  });

  const { data: companies = [] } = useQuery<string[]>({
    queryKey: ['job-companies'],
    queryFn: async () => (await api.get('/jobs/companies')).data,
  });

  const cleanParams = () => {
    const params: Record<string, string | number | boolean> = {
      page,
      size,
    };
    if (scope === 'global') {
      params.global = true;
    }
    Object.entries(filters).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v.trim() !== '') {
        params[k] = v.trim();
      }
    });
    return params;
  };

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['jobs', scope, page, size, filters],
    queryFn: async () => (await api.get('/jobs', { params: cleanParams() })).data,
  });

  const exportJobs = async () => {
    await api.post('/export', cleanParams());
    alert('Export requested — check Exports page');
  };

  const handleCountryChange = (countryVal: string) => {
    setFilters(prev => ({ ...prev, country: countryVal, city: '' }));
    setPage(0);
  };

  const totalPages = data?.totalPages ?? 1;
  const totalElements = data?.totalElements ?? 0;

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
        <div>
          <h2 style={{ margin: 0 }}>Jobs ({totalElements})</h2>
          <div style={{ marginTop: '0.5rem', display: 'flex', gap: '0.5rem' }}>
            <button 
              className={scope === 'global' ? 'btn-primary' : 'btn-secondary'} 
              onClick={() => { setScope('global'); setPage(0); }}
            >
              All Platform Jobs ({scope === 'global' ? totalElements : '207'})
            </button>
            <button 
              className={scope === 'my' ? 'btn-primary' : 'btn-secondary'} 
              onClick={() => { setScope('my'); setPage(0); }}
            >
              My Posted Jobs
            </button>
          </div>
        </div>
        <div className="actions" style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="btn-secondary" onClick={exportJobs}>Export Excel</button>
          <Link className="btn-primary" to="/jobs/new">Add Job</Link>
        </div>
      </div>

      {/* Cascading & Searchable Select Dropdown Filters */}
      <div className="filters card" style={{ marginBottom: '1.5rem', padding: '1rem', backgroundColor: '#0f172a', borderRadius: '12px', border: '1px solid #1e293b' }}>
        <h4 style={{ margin: '0 0 0.8rem 0', color: '#94a3b8', fontSize: '0.9rem', fontWeight: 600 }}>Filter Job Postings</h4>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', alignItems: 'end' }}>
          
          {/* Keyword Search */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
            <label style={{ fontSize: '0.85rem', color: '#94a3b8', fontWeight: 500 }}>Keyword Search</label>
            <input
              type="text"
              placeholder="Title, description..."
              value={filters.search ?? ''}
              onChange={(e) => { setFilters({ ...filters, search: e.target.value }); setPage(0); }}
              style={{
                padding: '8px 12px',
                backgroundColor: '#1e293b',
                border: '1px solid #334155',
                borderRadius: '8px',
                color: '#f8fafc',
                fontSize: '0.9rem',
                outline: 'none',
              }}
            />
          </div>

          {/* Searchable Country Select */}
          <SearchableSelect
            label="Country"
            placeholder="All Countries"
            value={filters.country ?? ''}
            options={countries}
            onChange={handleCountryChange}
          />

          {/* Searchable City Select (Cascading based on Country) */}
          <SearchableSelect
            label="City"
            placeholder={filters.country ? `Cities in ${filters.country}` : 'All Cities'}
            value={filters.city ?? ''}
            options={cities}
            loading={loadingCities}
            onChange={(val) => { setFilters({ ...filters, city: val }); setPage(0); }}
          />

          {/* Searchable Role Select */}
          <SearchableSelect
            label="Role"
            placeholder="All Roles"
            value={filters.role ?? ''}
            options={roles}
            onChange={(val) => { setFilters({ ...filters, role: val }); setPage(0); }}
          />

          {/* Searchable Company Select */}
          <SearchableSelect
            label="Company"
            placeholder="All Companies"
            value={filters.company ?? ''}
            options={companies}
            onChange={(val) => { setFilters({ ...filters, company: val }); setPage(0); }}
          />

          {/* Filter Reset Button */}
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button className="btn-secondary" style={{ width: '100%', padding: '8px 12px' }} onClick={() => { setFilters({}); setPage(0); }}>
              Reset Filters
            </button>
          </div>
        </div>
      </div>

      {isLoading ? <p>Loading jobs...</p> : (
        <>
          <table className="data-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Company</th>
                <th>Role</th>
                <th>Location</th>
                <th>Status</th>
                <th>Work Mode</th>
                <th style={{ textAlign: 'center' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {(data?.content ?? []).map((job: JobItem) => (
                <tr key={job.id}>
                  <td><strong>{job.title}</strong></td>
                  <td>{job.company}</td>
                  <td>{job.role}</td>
                  <td>{job.city && job.country ? `${job.city}, ${job.country}` : (job.country || 'N/A')}</td>
                  <td><span className="badge">{job.status}</span></td>
                  <td>{job.workMode}</td>
                  <td>
                    <div style={{ display: 'flex', gap: '6px', justifyContent: 'center' }}>
                      {/* View Button (Available for all users) */}
                      <button
                        onClick={() => setSelectedJob(job)}
                        style={{
                          backgroundColor: '#3b82f6',
                          color: '#ffffff',
                          border: 'none',
                          padding: '4px 10px',
                          borderRadius: '6px',
                          cursor: 'pointer',
                          fontSize: '0.8rem',
                          fontWeight: 500,
                        }}
                      >
                        View
                      </button>

                      {/* Apply Button (Available for all users) */}
                      <button
                        onClick={() => {
                          const targetUrl = job.applyUrl || job.jobUrl;
                          if (targetUrl) {
                            window.open(targetUrl.startsWith('http') ? targetUrl : `https://${targetUrl}`, '_blank');
                          } else {
                            alert(`Applying for ${job.title} at ${job.company}. Direct application link not provided.`);
                          }
                        }}
                        style={{
                          backgroundColor: '#10b981',
                          color: '#ffffff',
                          border: 'none',
                          padding: '4px 10px',
                          borderRadius: '6px',
                          cursor: 'pointer',
                          fontSize: '0.8rem',
                          fontWeight: 500,
                        }}
                      >
                        Apply
                      </button>

                      {/* Edit Button (Visible ONLY for ADMIN / SUPER_ADMIN) */}
                      {isAdmin && (
                        <Link
                          to={`/jobs/${job.id}`}
                          style={{
                            backgroundColor: '#8b5cf6',
                            color: '#ffffff',
                            padding: '4px 10px',
                            borderRadius: '6px',
                            fontSize: '0.8rem',
                            fontWeight: 500,
                            textDecoration: 'none',
                            display: 'inline-block',
                          }}
                        >
                          Edit
                        </Link>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Pagination Controls */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1.5rem' }}>
            <div>
              <span>Showing Page <strong>{page + 1}</strong> of <strong>{totalPages}</strong> ({totalElements} total items)</span>
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
              <label>Page Size: </label>
              <select value={size} onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }}>
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
                <option value={100}>100</option>
              </select>
              <button 
                className="btn-secondary" 
                disabled={page === 0} 
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                Previous
              </button>
              <button 
                className="btn-secondary" 
                disabled={page >= totalPages - 1} 
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </button>
            </div>
          </div>
        </>
      )}

      {/* View Job Modal */}
      {selectedJob && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.75)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000,
          padding: '1rem',
        }}>
          <div style={{
            backgroundColor: '#0f172a',
            border: '1px solid #334155',
            borderRadius: '16px',
            maxWidth: '650px',
            width: '100%',
            maxHeight: '85vh',
            overflowY: 'auto',
            padding: '1.5rem',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '1rem' }}>
              <div>
                <h3 style={{ margin: '0 0 0.3rem 0', color: '#f8fafc', fontSize: '1.4rem' }}>{selectedJob.title}</h3>
                <span style={{ fontSize: '1rem', color: '#60a5fa', fontWeight: 600 }}>{selectedJob.company}</span>
                <span style={{ margin: '0 8px', color: '#64748b' }}>•</span>
                <span style={{ fontSize: '0.95rem', color: '#94a3b8' }}>{selectedJob.role}</span>
              </div>
              <button
                onClick={() => setSelectedJob(null)}
                style={{
                  backgroundColor: '#1e293b',
                  border: 'none',
                  color: '#94a3b8',
                  fontSize: '1.2rem',
                  cursor: 'pointer',
                  borderRadius: '8px',
                  width: '32px',
                  height: '32px',
                }}
              >
                ✕
              </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '0.8rem', backgroundColor: '#1e293b', padding: '1rem', borderRadius: '10px', marginBottom: '1rem' }}>
              <div>
                <span style={{ color: '#64748b', fontSize: '0.8rem', display: 'block' }}>Location</span>
                <strong style={{ color: '#e2e8f0', fontSize: '0.9rem' }}>
                  {selectedJob.city && selectedJob.country ? `${selectedJob.city}, ${selectedJob.country}` : (selectedJob.country || 'Remote')}
                </strong>
              </div>
              <div>
                <span style={{ color: '#64748b', fontSize: '0.8rem', display: 'block' }}>Work Mode</span>
                <strong style={{ color: '#e2e8f0', fontSize: '0.9rem' }}>{selectedJob.workMode} ({selectedJob.employmentType || 'FULL_TIME'})</strong>
              </div>
              <div>
                <span style={{ color: '#64748b', fontSize: '0.8rem', display: 'block' }}>Salary Range</span>
                <strong style={{ color: '#10b981', fontSize: '0.9rem' }}>
                  {selectedJob.salaryMin && selectedJob.salaryMax ? `${selectedJob.salaryCurrency || 'USD'} $${selectedJob.salaryMin.toLocaleString()} - $${selectedJob.salaryMax.toLocaleString()}` : 'Not Specified'}
                </strong>
              </div>
              <div>
                <span style={{ color: '#64748b', fontSize: '0.8rem', display: 'block' }}>Status</span>
                <span className="badge" style={{ marginTop: '2px' }}>{selectedJob.status}</span>
              </div>
            </div>

            {selectedJob.description && (
              <div style={{ marginBottom: '1rem' }}>
                <h4 style={{ margin: '0 0 0.4rem 0', color: '#cbd5e1', fontSize: '0.95rem' }}>Job Description</h4>
                <p style={{ margin: 0, color: '#94a3b8', fontSize: '0.9rem', lineHeight: 1.5, whiteSpace: 'pre-wrap' }}>
                  {selectedJob.description}
                </p>
              </div>
            )}

            {selectedJob.skills && selectedJob.skills.length > 0 && (
              <div style={{ marginBottom: '1rem' }}>
                <h4 style={{ margin: '0 0 0.4rem 0', color: '#cbd5e1', fontSize: '0.95rem' }}>Required Skills</h4>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                  {selectedJob.skills.map((s, idx) => (
                    <span key={idx} style={{ backgroundColor: '#1e293b', border: '1px solid #334155', color: '#60a5fa', padding: '3px 8px', borderRadius: '6px', fontSize: '0.8rem' }}>
                      {s}
                    </span>
                  ))}
                </div>
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.8rem', marginTop: '1.5rem', paddingTop: '1rem', borderTop: '1px solid #1e293b' }}>
              <button className="btn-secondary" onClick={() => setSelectedJob(null)}>
                Close
              </button>
              <button
                className="btn-primary"
                style={{ backgroundColor: '#10b981' }}
                onClick={() => {
                  const targetUrl = selectedJob.applyUrl || selectedJob.jobUrl;
                  if (targetUrl) {
                    window.open(targetUrl.startsWith('http') ? targetUrl : `https://${targetUrl}`, '_blank');
                  } else {
                    alert(`Applying for ${selectedJob.title} at ${selectedJob.company}. Direct application link not provided.`);
                  }
                }}
              >
                Apply Now ↗
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
