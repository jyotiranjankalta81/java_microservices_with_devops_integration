import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { evidencingApi, PostResponse } from '../../api/evidencingApi';
import PostCard from '../../components/evidencing/PostCard';
import RoleSwitcher from '../../components/evidencing/RoleSwitcher';
import NotificationsDrawer from '../../components/evidencing/NotificationsDrawer';
import '../../styles/evidencing.css';

export default function PostDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [post, setPost] = useState<PostResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [errorCode, setErrorCode] = useState<number | null>(null);

  const fetchPost = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setErrorMessage(null);
    setErrorCode(null);
    try {
      const data = await evidencingApi.getPost(id);
      setPost(data);
    } catch (err: any) {
      console.error('Failed to load post', err);
      const status = err.response?.status;
      setErrorCode(status || 500);
      if (status === 403) {
        setErrorMessage(
          'Access Denied: You do not have permission to view this evidencing post. For private posts, only explicitly tagged recipients and the author may access the post.'
        );
      } else if (status === 404) {
        setErrorMessage('Post Not Found: This post does not exist or may have been deleted by its author.');
      } else {
        setErrorMessage(
          err.response?.data?.message || 'Failed to load post details. Please check your network connection.'
        );
      }
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchPost();
  }, [fetchPost]);

  const handleRoleChanged = () => {
    // Re-verify access under the new persona
    fetchPost();
  };

  const handlePostDeleted = () => {
    navigate('/evidencing', { replace: true });
  };

  return (
    <div className="evidencing-container">
      {/* Role Switcher Bar for testing staff RBAC personas */}
      <RoleSwitcher onUserChanged={handleRoleChanged} />

      {/* Header bar */}
      <header className="evidencing-header">
        <div className="evidencing-brand">
          <Link to="/evidencing" className="btn-icon" style={{ textDecoration: 'none', color: '#1e293b' }} title="Back to Feed">
            ⬅️
          </Link>
          <div className="evidencing-brand-icon">📸</div>
          <div>
            <h1 className="evidencing-title">Evidencing Post</h1>
            <p className="evidencing-subtitle">Viewing individual school activity post</p>
          </div>
        </div>

        <div className="evidencing-actions">
          <NotificationsDrawer />
          <Link to="/evidencing" className="btn btn-secondary">
            Feed
          </Link>
          <Link to="/evidencing/new" className="btn btn-primary">
            + New Post
          </Link>
        </div>
      </header>

      {/* Content area */}
      <main className="evidencing-main" style={{ maxWidth: '640px', margin: '1.5rem auto' }}>
        {loading ? (
          <div className="evidencing-loading">
            <div className="evidencing-spinner" />
            <p>Verifying access and loading post...</p>
          </div>
        ) : errorMessage ? (
          <div className="access-denied-container">
            <div className="access-denied-icon">
              {errorCode === 403 ? '🔒' : '📭'}
            </div>
            <h2 className="access-denied-title">
              {errorCode === 403 ? 'Post Restricted' : 'Post Unavailable'}
            </h2>
            <p className="access-denied-message">{errorMessage}</p>

            <div style={{ marginTop: '1.5rem', display: 'flex', gap: '0.75rem', justifyContent: 'center' }}>
              <button type="button" className="btn btn-secondary" onClick={fetchPost}>
                🔄 Retry Check
              </button>
              <Link to="/evidencing" className="btn btn-primary">
                Return to Feed
              </Link>
            </div>
          </div>
        ) : post ? (
          <div>
            <PostCard
              post={post}
              onPostUpdated={(updated) => setPost(updated)}
              onPostDeleted={handlePostDeleted}
            />

            {/* Complete Post Details Inspector (for audit/verification) */}
            <div style={{
              marginTop: '1.5rem',
              background: '#f8fafc',
              border: '1px solid #e2e8f0',
              borderRadius: '8px',
              padding: '1rem',
              fontSize: '0.8rem',
              color: '#475569'
            }}>
              <h4 style={{ margin: '0 0 0.5rem 0', color: '#0f172a' }}>📋 Post Metadata & Scoping Audit</h4>
              <ul style={{ margin: 0, paddingLeft: '1.25rem', lineHeight: 1.6 }}>
                <li><strong>Post ID:</strong> {post.id}</li>
                <li><strong>Visibility:</strong> {post.visibility} {post.visibility === 'PRIVATE' ? `(${post.taggedPeople?.length || 0} tagged recipients)` : '(No audience tags allowed)'}</li>
                <li><strong>School / Branch:</strong> {post.schoolName} / {post.branchName}</li>
                {post.className && <li><strong>Class Scope:</strong> {post.className}</li>}
                <li><strong>Primary Domain:</strong> {post.domainName}</li>
                <li><strong>Publication State:</strong> {post.publicationStatus} ({post.publishedAt ? new Date(post.publishedAt).toLocaleString() : 'Pending'})</li>
                <li><strong>Author:</strong> {post.authorName} ({post.authorRole})</li>
                <li><strong>Optimistic Lock Version:</strong> {post.version}</li>
                <li><strong>Media Count:</strong> {post.media?.length || 0} item(s)</li>
              </ul>
            </div>
          </div>
        ) : null}
      </main>
    </div>
  );
}
