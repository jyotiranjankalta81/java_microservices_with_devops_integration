import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../api/client';

export default function JobFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: '', company: '', role: '', description: '',
    skills: 'Java,Spring', technologies: 'PostgreSQL,Kafka',
    country: 'USA', state: '', city: 'Remote',
    workMode: 'REMOTE', employmentType: 'FULL_TIME',
    jobUrl: '', applyUrl: '', status: 'ACTIVE', version: 0,
  });

  useEffect(() => {
    if (id) {
      api.get(`/jobs/${id}`).then((res) => {
        const j = res.data;
        setForm({
          title: j.title || '',
          company: j.company || '',
          role: j.role || '',
          description: j.description || '',
          skills: (j.skills || []).join(', '),
          technologies: (j.technologies || []).join(', '),
          country: j.country || 'USA',
          state: j.state || '',
          city: j.city || 'Remote',
          workMode: j.workMode || 'REMOTE',
          employmentType: j.employmentType || 'FULL_TIME',
          jobUrl: j.jobUrl || '',
          applyUrl: j.applyUrl || '',
          status: j.status || 'ACTIVE',
          version: j.version || 0,
        });
      }).catch(err => console.error('Failed to load job details', err));
    }
  }, [id]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    const payload = {
      ...form,
      skills: form.skills.split(',').map((s) => s.trim()).filter(Boolean),
      technologies: form.technologies.split(',').map((t) => t.trim()).filter(Boolean),
    };
    if (id) {
      await api.put(`/jobs/${id}`, payload);
    } else {
      await api.post('/jobs', payload);
    }
    navigate('/jobs');
  };

  return (
    <div>
      <h2>{id ? 'Edit Job' : 'New Job'}</h2>
      <form className="card form-grid" onSubmit={submit}>
        <label>Title<input required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></label>
        <label>Company<input required value={form.company} onChange={(e) => setForm({ ...form, company: e.target.value })} /></label>
        <label>Role<input required value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })} /></label>
        <label>Skills (comma-separated)<input value={form.skills} onChange={(e) => setForm({ ...form, skills: e.target.value })} /></label>
        <label>Technologies<input value={form.technologies} onChange={(e) => setForm({ ...form, technologies: e.target.value })} /></label>
        <label>Country<input required value={form.country} onChange={(e) => setForm({ ...form, country: e.target.value })} /></label>
        <label>City<input required value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} /></label>
        <label>Job URL<input value={form.jobUrl} onChange={(e) => setForm({ ...form, jobUrl: e.target.value })} /></label>
        <label>Apply Link / URL<input placeholder="https://careers.company.com/apply" value={form.applyUrl} onChange={(e) => setForm({ ...form, applyUrl: e.target.value })} /></label>
        <label style={{ gridColumn: '1 / -1' }}>Description<textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></label>
        <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '1rem', marginTop: '1rem' }}>
          <button className="btn-primary" type="submit">Save Job</button>
          <button className="btn-secondary" type="button" onClick={() => navigate('/jobs')}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
