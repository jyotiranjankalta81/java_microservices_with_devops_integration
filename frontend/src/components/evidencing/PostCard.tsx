import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  evidencingApi,
  PostResponse,
  CommentResponse,
} from '../../api/evidencingApi';

interface PostCardProps {
  post: PostResponse;
  onPostUpdated?: (updated: PostResponse) => void;
  onPostDeleted?: (postId: string) => void;
}

export default function PostCard({ post, onPostUpdated, onPostDeleted }: PostCardProps) {
  const navigate = useNavigate();
  const [currentMediaIndex, setCurrentMediaIndex] = useState(0);
  const [isLiked, setIsLiked] = useState(post.isLikedByCurrentUser);
  const [likeCount, setLikeCount] = useState(post.likeCount);
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [showComments, setShowComments] = useState(false);
  const [commentsLoaded, setCommentsLoaded] = useState(false);
  const [commentText, setCommentText] = useState('');
  const [isSubmittingComment, setIsSubmittingComment] = useState(false);
  const [isExpandedCaption, setIsExpandedCaption] = useState(false);

  const mediaList = post.media || [];
  const currentMedia = mediaList[currentMediaIndex];

  const handleToggleLike = async () => {
    try {
      // Optimistic update
      const nextLiked = !isLiked;
      setIsLiked(nextLiked);
      setLikeCount((prev) => (nextLiked ? prev + 1 : Math.max(0, prev - 1)));

      const res = await evidencingApi.toggleLike(post.id);
      setIsLiked(res.liked);
      setLikeCount(res.likeCount);
    } catch (err) {
      // Revert on error
      setIsLiked(isLiked);
      setLikeCount(post.likeCount);
      console.error('Like failed', err);
    }
  };

  const handleToggleComments = async () => {
    const nextShow = !showComments;
    setShowComments(nextShow);
    if (nextShow && !commentsLoaded) {
      try {
        const list = await evidencingApi.getComments(post.id);
        setComments(list);
        setCommentsLoaded(true);
      } catch (err) {
        console.error('Failed to load comments', err);
      }
    }
  };

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentText.trim() || isSubmittingComment) return;

    setIsSubmittingComment(true);
    try {
      const newComment = await evidencingApi.addComment(post.id, commentText.trim());
      setComments((prev) => [...prev, newComment]);
      setCommentText('');
      if (onPostUpdated) {
        onPostUpdated({
          ...post,
          commentCount: post.commentCount + 1,
        });
      }
    } catch (err) {
      console.error('Failed to add comment', err);
    } finally {
      setIsSubmittingComment(false);
    }
  };

  const handleDeletePost = async () => {
    if (!window.confirm('Are you sure you want to delete this evidencing post?')) return;
    try {
      await evidencingApi.deletePost(post.id);
      if (onPostDeleted) onPostDeleted(post.id);
    } catch (err) {
      alert('Failed to delete post.');
    }
  };

  const formatTimeAgo = (dateStr?: string) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const now = new Date();
    const diffMin = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));
    if (diffMin < 1) return 'Just now';
    if (diffMin < 60) return `${diffMin}m ago`;
    const diffHours = Math.floor(diffMin / 60);
    if (diffHours < 24) return `${diffHours}h ago`;
    return date.toLocaleDateString();
  };

  return (
    <article className="post-card">
      {/* 1. Post Header */}
      <div className="post-card-header">
        <div className="author-info">
          {post.authorAvatarUrl ? (
            <img src={post.authorAvatarUrl} alt={post.authorName} className="author-avatar" />
          ) : (
            <div className="author-avatar" style={{ background: '#e2e8f0', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold' }}>
              {post.authorName?.charAt(0) || 'U'}
            </div>
          )}
          <div className="author-text">
            <div className="author-name-row">
              <span className="author-name">{post.authorName}</span>
              <span className="author-role-badge">{post.authorRole}</span>
            </div>
            <div className="post-meta-row">
              <span className="scope-tag">
                🏫 {post.branchName}
                {post.className ? ` • 🎓 ${post.className}` : ''}
              </span>
              <span>•</span>
              <span>{formatTimeAgo(post.publishedAt || post.createdAt)}</span>
            </div>
          </div>
        </div>

        <div className="post-header-badges">
          {/* Domain / Category Badge */}
          <span className="domain-badge" title={post.domainDescription}>
            <span>🏷️</span>
            <span>{post.domainName}</span>
          </span>

          {/* Public / Private Indicator */}
          <span
            className={`visibility-badge ${post.visibility === 'PUBLIC' ? 'public' : 'private'}`}
            title={post.visibility === 'PUBLIC' ? 'Public to school branch' : 'Private to tagged people'}
          >
            {post.visibility === 'PUBLIC' ? '🌐 Public' : '🔒 Private'}
          </span>

          {/* Delete Action (if permitted) */}
          {post.canDelete && (
            <button
              type="button"
              onClick={handleDeletePost}
              style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#94a3b8', fontSize: '1rem', marginLeft: '0.25rem' }}
              title="Delete post"
            >
              🗑️
            </button>
          )}
        </div>
      </div>

      {/* 2. Private Recipient Tags (RENDERED ABOVE MEDIA FOR PRIVATE POSTS ONLY) */}
      {post.visibility === 'PRIVATE' && post.taggedPeople && post.taggedPeople.length > 0 && (
        <div className="private-tags-container">
          <span className="tags-label">
            <span>🔒 Tagged Recipients:</span>
          </span>
          {post.taggedPeople.map((recipient) => (
            <span key={recipient.userId} className="recipient-chip">
              {recipient.avatarUrl ? (
                <img src={recipient.avatarUrl} alt={recipient.displayName} className="recipient-chip-avatar" />
              ) : (
                <span>👤</span>
              )}
              <span>{recipient.displayName}</span>
              <span style={{ fontSize: '0.65rem', color: '#64748b' }}>({recipient.role})</span>
            </span>
          ))}
        </div>
      )}

      {/* 3. Media Carousel & Player */}
      {mediaList.length > 0 && currentMedia && (
        <div className="post-media-wrapper">
          {currentMedia.mediaType === 'VIDEO' ? (
            <video
              src={currentMedia.url}
              poster={currentMedia.thumbnailUrl}
              controls
              className="post-media-video"
            />
          ) : (
            <img
              src={currentMedia.url}
              alt={post.caption || 'Post image'}
              className="post-media-image"
              loading="lazy"
            />
          )}

          {/* Carousel Next / Prev Controls */}
          {mediaList.length > 1 && (
            <>
              {currentMediaIndex > 0 && (
                <button
                  type="button"
                  className="carousel-nav-btn prev"
                  onClick={() => setCurrentMediaIndex((i) => i - 1)}
                  aria-label="Previous image"
                >
                  ‹
                </button>
              )}
              {currentMediaIndex < mediaList.length - 1 && (
                <button
                  type="button"
                  className="carousel-nav-btn next"
                  onClick={() => setCurrentMediaIndex((i) => i + 1)}
                  aria-label="Next image"
                >
                  ›
                </button>
              )}

              {/* Dot Indicators */}
              <div className="carousel-dots">
                {mediaList.map((_, idx) => (
                  <span
                    key={idx}
                    className={`carousel-dot ${idx === currentMediaIndex ? 'active' : ''}`}
                    onClick={() => setCurrentMediaIndex(idx)}
                  />
                ))}
              </div>
            </>
          )}
        </div>
      )}

      {/* 4. Post Action Bar (Likes, Comments, Views) */}
      <div className="post-interactions-bar">
        <div className="interaction-left">
          {/* Like Button */}
          <button
            type="button"
            className={`interaction-btn ${isLiked ? 'liked' : ''}`}
            onClick={handleToggleLike}
            aria-label={isLiked ? 'Unlike post' : 'Like post'}
          >
            <span>{isLiked ? '❤️' : '🤍'}</span>
            <span>{likeCount}</span>
          </button>

          {/* Comment Toggle */}
          <button
            type="button"
            className="interaction-btn"
            onClick={handleToggleComments}
            aria-label="Comments"
          >
            <span>💬</span>
            <span>{post.commentCount || comments.length}</span>
          </button>
        </div>

        {/* View Count */}
        <div className="interaction-btn view-count" title="Total view count">
          <span>👁️</span>
          <span>{post.viewCount} views</span>
        </div>
      </div>

      {/* 5. Caption & Details */}
      <div className="post-content-section">
        {post.caption && (
          <div className="post-caption">
            <strong>{post.authorName}</strong>{' '}
            {post.caption.length > 180 && !isExpandedCaption ? (
              <>
                {post.caption.substring(0, 180)}...
                <button
                  type="button"
                  className="caption-toggle-btn"
                  onClick={() => setIsExpandedCaption(true)}
                >
                  more
                </button>
              </>
            ) : (
              <>
                {post.caption}
                {post.caption.length > 180 && (
                  <button
                    type="button"
                    className="caption-toggle-btn"
                    onClick={() => setIsExpandedCaption(false)}
                  >
                    less
                  </button>
                )}
              </>
            )}
          </div>
        )}

        {/* 6. Expandable Comments Section */}
        {showComments && (
          <div className="comments-container">
            <div className="comment-list">
              {comments.length === 0 ? (
                <div style={{ color: '#94a3b8', fontSize: '0.8rem', padding: '0.5rem 0' }}>
                  No comments yet. Start the conversation!
                </div>
              ) : (
                comments.map((c) => (
                  <div key={c.id} className="comment-item">
                    {c.authorAvatarUrl ? (
                      <img src={c.authorAvatarUrl} alt={c.authorName} className="comment-avatar" />
                    ) : (
                      <div className="comment-avatar" style={{ background: '#cbd5e1', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.7rem' }}>
                        {c.authorName?.charAt(0) || 'U'}
                      </div>
                    )}
                    <div className="comment-bubble">
                      <span className="comment-author">{c.authorName}</span>
                      <span style={{ fontSize: '0.7rem', color: '#64748b', marginRight: '0.5rem' }}>({c.authorRole})</span>
                      <span className="comment-text">{c.commentText}</span>
                      <div className="comment-time">{formatTimeAgo(c.createdAt)}</div>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Comment Form */}
            <form onSubmit={handleAddComment} className="comment-form">
              <input
                type="text"
                placeholder="Add a comment as school staff..."
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                className="comment-input"
              />
              <button
                type="submit"
                disabled={!commentText.trim() || isSubmittingComment}
                className="comment-submit-btn"
              >
                Post
              </button>
            </form>
          </div>
        )}
      </div>
    </article>
  );
}
