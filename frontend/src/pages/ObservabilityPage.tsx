import { useState } from 'react';

interface ObservabilityTool {
  id: string;
  name: string;
  category: string;
  description: string;
  url: string;
  embedUrl: string;
  port: number;
  credentials?: string;
  icon: string;
  color: string;
}

const TOOLS: ObservabilityTool[] = [
  {
    id: 'grafana',
    name: 'Grafana Dashboards',
    category: 'Metrics & System Performance',
    description: 'Real-time JVM, HTTP throughput, latency, memory utilization, and PostgreSQL DB connection pool metrics across all 7 microservices.',
    url: 'http://localhost:3001',
    embedUrl: 'http://localhost:3001/d/job-intel-main/job-intelligence-microservices-dashboard?orgId=1&kiosk=tv',
    port: 3001,
    credentials: 'admin / admin',
    icon: '📊',
    color: '#f97316',
  },
  {
    id: 'jaeger',
    name: 'Jaeger Tracing UI',
    category: 'Distributed Tracing & APM',
    description: 'Trace end-to-end request latency and span breakdown across API Gateway, Auth Service, Job Service, and Audit Service.',
    url: 'http://localhost:16686',
    embedUrl: 'http://localhost:16686/search',
    port: 16686,
    icon: '🔍',
    color: '#06b6d4',
  },
  {
    id: 'prometheus',
    name: 'Prometheus Engine',
    category: 'Metrics Scraping & Alerts',
    description: 'Inspect raw PromQL metrics, active scrape targets (7/7 UP), time-series query builder, and health alerts.',
    url: 'http://localhost:9090',
    embedUrl: 'http://localhost:9090/targets',
    port: 9090,
    icon: '📈',
    color: '#ef4444',
  },
  {
    id: 'kafka-ui',
    name: 'Kafka UI Stream Monitor',
    category: 'Event-Driven Messaging',
    description: 'Monitor Kafka brokers, topics (job.events, audit.events), consumer groups, partition offsets, and message payloads.',
    url: 'http://localhost:8090',
    embedUrl: 'http://localhost:8090',
    port: 8090,
    icon: '⚡',
    color: '#8b5cf6',
  },
  {
    id: 'swagger',
    name: 'Swagger API Specs',
    category: 'REST API Documentation',
    description: 'Interactive OpenAPI specs and live REST API test runner for API Gateway and microservice endpoints.',
    url: 'http://localhost:8080/swagger-ui.html',
    embedUrl: 'http://localhost:8080/swagger-ui.html',
    port: 8080,
    icon: '🛠',
    color: '#10b981',
  },
];

export default function ObservabilityPage() {
  const [activeToolId, setActiveToolId] = useState<string>('grafana');
  const activeTool = TOOLS.find(t => t.id === activeToolId) || TOOLS[0];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 4rem)', gap: '1rem' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ margin: 0, fontSize: '1.5rem', color: '#f8fafc' }}>Observability & Monitoring Center</h2>
          <p style={{ margin: '0.25rem 0 0 0', color: '#94a3b8', fontSize: '0.9rem' }}>
            Unified real-time browser monitoring for metrics, distributed traces, events, and API health.
          </p>
        </div>
        <a
          href={activeTool.url}
          target="_blank"
          rel="noopener noreferrer"
          className="btn-primary"
          style={{
            backgroundColor: activeTool.color,
            textDecoration: 'none',
            display: 'inline-flex',
            alignItems: 'center',
            gap: '6px',
            fontSize: '0.9rem',
          }}
        >
          Open {activeTool.name} in New Tab ↗
        </a>
      </div>

      {/* Tool Selector Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '0.75rem' }}>
        {TOOLS.map((tool) => {
          const isActive = tool.id === activeToolId;
          return (
            <div
              key={tool.id}
              onClick={() => setActiveToolId(tool.id)}
              style={{
                backgroundColor: isActive ? '#1e293b' : '#0f172a',
                border: isActive ? `2px solid ${tool.color}` : '1px solid #1e293b',
                borderRadius: '12px',
                padding: '0.85rem 1rem',
                cursor: 'pointer',
                transition: 'all 0.15s ease',
                boxShadow: isActive ? `0 0 15px -3px ${tool.color}40` : 'none',
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.4rem' }}>
                <span style={{ fontSize: '1.3rem' }}>{tool.icon}</span>
                <span style={{
                  fontSize: '0.7rem',
                  padding: '2px 8px',
                  borderRadius: '10px',
                  backgroundColor: 'rgba(16, 185, 129, 0.15)',
                  color: '#10b981',
                  fontWeight: 600,
                }}>
                  🟢 Port {tool.port}
                </span>
              </div>
              <h4 style={{ margin: '0 0 0.2rem 0', color: isActive ? '#f8fafc' : '#cbd5e1', fontSize: '0.95rem' }}>
                {tool.name}
              </h4>
              <p style={{ margin: 0, color: '#64748b', fontSize: '0.78rem', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                {tool.category}
              </p>
            </div>
          );
        })}
      </div>

      {/* Active Tool Details Banner */}
      <div style={{
        backgroundColor: '#0f172a',
        border: '1px solid #1e293b',
        borderRadius: '10px',
        padding: '0.8rem 1.2rem',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.8rem' }}>
          <span style={{ fontSize: '1.5rem' }}>{activeTool.icon}</span>
          <div>
            <h4 style={{ margin: 0, color: '#f8fafc', fontSize: '1rem' }}>
              {activeTool.name} <span style={{ fontSize: '0.85rem', color: '#94a3b8', fontWeight: 400 }}>({activeTool.category})</span>
            </h4>
            <p style={{ margin: '0.2rem 0 0 0', color: '#94a3b8', fontSize: '0.85rem' }}>
              {activeTool.description}
            </p>
          </div>
        </div>
        <div style={{ textAlign: 'right', flexShrink: 0 }}>
          {activeTool.credentials && (
            <span style={{ fontSize: '0.8rem', color: '#fbbf24', display: 'block', marginBottom: '2px' }}>
              Auth: <strong>{activeTool.credentials}</strong>
            </span>
          )}
          <span style={{ fontSize: '0.8rem', color: '#64748b' }}>
            URL: <code style={{ color: '#60a5fa' }}>{activeTool.url}</code>
          </span>
        </div>
      </div>

      {/* Embedded Live Viewport */}
      <div style={{
        flex: 1,
        backgroundColor: '#0f172a',
        border: '1px solid #1e293b',
        borderRadius: '12px',
        overflow: 'hidden',
        position: 'relative',
      }}>
        <iframe
          src={activeTool.embedUrl}
          title={activeTool.name}
          style={{
            width: '100%',
            height: '100%',
            border: 'none',
            backgroundColor: '#0f172a',
          }}
        />
      </div>
    </div>
  );
}
