import React, { useEffect, useState, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  evidencingApi,
  PostResponse,
  PostCategoryResponse,
  UserScopeResponse,
  PostFilterCriteria,
} from '../../api/evidencingApi';
import PostCard from '../../components/evidencing/PostCard';
import RoleSwitcher from '../../components/evidencing/RoleSwitcher';
import NotificationsDrawer from '../../components/evidencing/NotificationsDrawer';

const PAGE_SIZE = 15;

export default function EvidencingFeedPage() {
  const navigate = useNavigate();

  const [currentUser, setCurrentUser] = useState<UserScopeResponse | null>(null);
  const [posts, setPosts] = useState<PostResponse[]>([]);
  const [categories, setCategories] = useState<PostCategoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Filters & Pagination State
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [selectedVisibility, setSelectedVisibility] = useState<string>('');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [showScrollTop, setShowScrollTop] = useState(false);

  // Infinite Scroll Sentinel & Concurrency Guards
  const sentinelRef = useRef<HTMLDivElement | null>(null);
  const isFetchingRef = useRef(false);

  const hasViewPermission = currentUser?.permissions?.includes('EVIDENCING_VIEW');
  const hasCreatePermission = currentUser?.permissions?.includes('EVIDENCING_CREATE');
  const hasMore = currentPage < totalPages - 1;

  useEffect(() => {
    loadCategories();
  }, []);

  // Monitor scroll for back-to-top button
  useEffect(() => {
    const handleScroll = () => {
      setShowScrollTop(window.scrollY > 600);
    };
    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Reset & load page 0 whenever current user or filters change
  useEffect(() => {
    if (currentUser) {
      if (hasViewPermission) {
        fetchInitialFeed();
      } else {
        setLoading(false);
      }
    }
  }, [currentUser, selectedCategory, selectedVisibility, searchQuery]);

  const loadCategories = async () => {
    try {
      const cats = await evidencingApi.getActiveCategories();
      setCategories(cats);
    } catch (err) {
      console.error('Failed to load categories', err);
    }
  };

  const buildCriteria = (): PostFilterCriteria => {
    const criteria: PostFilterCriteria = {};
    if (selectedCategory) criteria.categoryId = selectedCategory;
    if (selectedVisibility === 'PUBLIC' || selectedVisibility === 'PRIVATE') {
      criteria.visibility = selectedVisibility;
    }
    if (searchQuery.trim()) criteria.search = searchQuery.trim();
    return criteria;
  };

  const fetchInitialFeed = async () => {
    setLoading(true);
    setError(null);
    setCurrentPage(0);
    isFetchingRef.current = true;

    try {
      const criteria = buildCriteria();
      const pageData = await evidencingApi.getFeed(criteria, 0, PAGE_SIZE);
      setPosts(pageData.content);
      setTotalPages(pageData.totalPages || 1);
      setTotalElements(pageData.totalElements || 0);
      setCurrentPage(0);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to fetch evidencing feed.');
    } finally {
      setLoading(false);
      isFetchingRef.current = false;
    }
  };

  const loadMorePosts = useCallback(async () => {
    if (isFetchingRef.current || loading || loadingMore || !hasMore) {
      return;
    }

    isFetchingRef.current = true;
    setLoadingMore(true);

    try {
      const nextPage = currentPage + 1;
      const criteria = buildCriteria();
      const pageData = await evidencingApi.getFeed(criteria, nextPage, PAGE_SIZE);

      setPosts((prev) => {
        const existingIds = new Set(prev.map((p) => p.id));
        const newPosts = pageData.content.filter((p) => !existingIds.has(p.id));
        return [...prev, ...newPosts];
      });

      setCurrentPage(pageData.number);
      setTotalPages(pageData.totalPages || 1);
      setTotalElements(pageData.totalElements || 0);
    } catch (err: any) {
      console.error('Failed to load more posts for infinite scroll', err);
    } finally {
      setLoadingMore(false);
      isFetchingRef.current = false;
    }
  }, [currentPage, totalPages, hasMore, loading, loadingMore, selectedCategory, selectedVisibility, searchQuery]);

  // IntersectionObserver for Infinite Scrolling
  useEffect(() => {
    if (!hasViewPermission || loading) return;

    const sentinel = sentinelRef.current;
    if (!sentinel) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loadingMore && !isFetchingRef.current) {
          loadMorePosts();
        }
      },
      {
        threshold: 0.1,
        rootMargin: '300px', // Fetch 300px before reaching the bottom for frictionless infinite scrolling
      }
    );

    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasViewPermission, loading, hasMore, loadingMore, loadMorePosts]);

  const handlePostDeleted = (postId: string) => {
    setPosts((prev) => prev.filter((p) => p.id !== postId));
    setTotalElements((prev) => Math.max(0, prev - 1));
  };

  const handlePostUpdated = (updated: PostResponse) => {
    setPosts((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
  };

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="evidencing-container">
      {/* Role / Persona Simulator */}
      <RoleSwitcher
        onUserChanged={(user) => {
          setCurrentUser(user);
        }}
      />

      {/* Module Header */}
      <div className="evidencing-header">
        <div className="evidencing-title-group">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <h2>School Evidencing Feed</h2>
            {totalElements > 0 && (
              <span
                style={{
                  background: '#e0e7ff',
                  color: '#3730a3',
                  fontSize: '0.75rem',
                  fontWeight: 600,
                  padding: '0.2rem 0.6rem',
                  borderRadius: '12px',
                }}
              >
                {totalElements.toLocaleString()} Activities
              </span>
            )}
          </div>
          <p className="evidencing-subtitle">
            Authentic learning records, classroom activities, and school milestones with seamless infinite scrolling
          </p>
        </div>

        <div className="evidencing-actions-group">
          <NotificationsDrawer />
          {hasCreatePermission && (
            <button
              type="button"
              className="btn-primary"
              onClick={() => navigate('/evidencing/new')}
            >
              ➕ Share Post
            </button>
          )}
        </div>
      </div>

      {/* Permissions Verification / Deny-by-default Access Check */}
      {!currentUser || (loading && posts.length === 0) ? (
        <div className="evidencing-loading" style={{ margin: '3rem auto' }}>
          <div className="evidencing-spinner" />
          <p>Verifying staff permissions and scope...</p>
        </div>
      ) : !hasViewPermission ? (
        <div className="access-denied-card">
          <div className="access-denied-icon">🚫</div>
          <h3 style={{ margin: '0 0 0.5rem', color: '#1e293b' }}>Evidencing Module Access Denied</h3>
          <p style={{ color: '#64748b', fontSize: '0.9rem', lineHeight: 1.6, marginBottom: '1rem' }}>
            Your current staff role <strong>({currentUser?.role || 'Staff'})</strong> does not have the{' '}
            <code style={{ background: '#f1f5f9', padding: '0.2rem 0.4rem', borderRadius: 4 }}>
              EVIDENCING_VIEW
            </code>{' '}
            permission.
          </p>
          <div
            style={{
              background: '#fef2f2',
              border: '1px solid #fecaca',
              padding: '0.75rem',
              borderRadius: '8px',
              fontSize: '0.85rem',
              color: '#991b1b',
            }}
          >
            🛡️ <strong>Deny-by-default Authorization Rule:</strong> Support staff roles such as Nanny, Peon, and
            Driver have no Evidencing access by default and cannot view posts or media.
          </div>
        </div>
      ) : (
        <>
          {/* Feed Filter Bar */}
          <div className="feed-filter-bar">
            {/* Domain Filter */}
            <div className="filter-item">
              <select
                value={selectedCategory}
                onChange={(e) => {
                  setSelectedCategory(e.target.value);
                }}
              >
                <option value="">All Domains / Categories ({categories.length})</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>

            {/* Visibility Filter */}
            <div className="filter-item" style={{ maxWidth: '160px' }}>
              <select
                value={selectedVisibility}
                onChange={(e) => {
                  setSelectedVisibility(e.target.value);
                }}
              >
                <option value="">All Visibility</option>
                <option value="PUBLIC">🌐 Public Only</option>
                <option value="PRIVATE">🔒 Private Only</option>
              </select>
            </div>

            {/* Keyword Search */}
            <div className="filter-item" style={{ flex: 2 }}>
              <input
                type="text"
                placeholder="Search captions or categories across 10,000+ posts..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          {/* Error Message */}
          {error && (
            <div className="error" style={{ marginBottom: '1.5rem' }}>
              {error}
            </div>
          )}

          {/* Feed Content */}
          {posts.length === 0 && !loading ? (
            <div
              style={{
                background: '#fff',
                border: '1px solid #e2e8f0',
                borderRadius: '16px',
                padding: '3rem',
                textAlign: 'center',
                color: '#64748b',
              }}
            >
              <div style={{ fontSize: '3rem', marginBottom: '0.5rem' }}>📸</div>
              <h3 style={{ color: '#1e293b', marginBottom: '0.25rem' }}>No Evidencing Posts Found</h3>
              <p style={{ fontSize: '0.9rem', marginBottom: '1rem' }}>
                There are no posts visible in your authorized school/branch scope matching the selected filters.
              </p>
              {hasCreatePermission && (
                <button
                  type="button"
                  className="btn-primary"
                  onClick={() => navigate('/evidencing/new')}
                >
                  Create First Post
                </button>
              )}
            </div>
          ) : (
            <div>
              {/* Loaded posts stream */}
              {posts.map((post) => (
                <PostCard
                  key={post.id}
                  post={post}
                  onPostDeleted={handlePostDeleted}
                  onPostUpdated={handlePostUpdated}
                />
              ))}

              {/* Infinite Scroll Sentinel & Status Indicator */}
              <div
                ref={sentinelRef}
                style={{
                  padding: '2rem 1rem',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '0.5rem',
                  minHeight: '80px',
                }}
              >
                {loadingMore && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: '#6366f1', fontWeight: 500 }}>
                    <div className="evidencing-spinner" style={{ width: '20px', height: '20px', borderWidth: '2px' }} />
                    <span>Loading more school activities... (Page {currentPage + 2} of {totalPages})</span>
                  </div>
                )}

                {!hasMore && posts.length > 0 && !loading && (
                  <div
                    style={{
                      background: '#f8fafc',
                      border: '1px solid #e2e8f0',
                      padding: '0.75rem 1.5rem',
                      borderRadius: '24px',
                      color: '#64748b',
                      fontSize: '0.85rem',
                      fontWeight: 500,
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem',
                    }}
                  >
                    <span>🎉</span>
                    <span>
                      You're all caught up! All <strong>{posts.length.toLocaleString()}</strong> activities loaded for your scope.
                    </span>
                  </div>
                )}

                {hasMore && !loadingMore && (
                  <button
                    type="button"
                    onClick={loadMorePosts}
                    className="btn-secondary"
                    style={{
                      fontSize: '0.85rem',
                      padding: '0.5rem 1.25rem',
                      borderRadius: '20px',
                      color: '#6366f1',
                      borderColor: '#c7d2fe',
                    }}
                  >
                    Load More ({posts.length} of {totalElements.toLocaleString()} displayed)
                  </button>
                )}
              </div>
            </div>
          )}
        </>
      )}

      {/* Floating Scroll to Top Button */}
      {showScrollTop && (
        <button
          type="button"
          onClick={scrollToTop}
          title="Scroll to Top"
          style={{
            position: 'fixed',
            bottom: '2rem',
            right: '2rem',
            background: '#6366f1',
            color: '#fff',
            width: '46px',
            height: '46px',
            borderRadius: '50%',
            border: 'none',
            boxShadow: '0 4px 14px rgba(99, 102, 241, 0.4)',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: '1.25rem',
            zIndex: 99,
            transition: 'transform 0.2s, background 0.2s',
          }}
          onMouseEnter={(e) => (e.currentTarget.style.transform = 'scale(1.1)')}
          onMouseLeave={(e) => (e.currentTarget.style.transform = 'scale(1.0)')}
        >
          ⬆️
        </button>
      )}
    </div>
  );
}
