import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  evidencingApi,
  PostCategoryResponse,
  EligibleRecipientResponse,
  SchoolHierarchy,
  UserScopeResponse,
  MediaType,
  MediaAssetStatus,
} from '../../api/evidencingApi';

interface UploadItem {
  id: string; // client temporary id
  file: File;
  mediaType: MediaType;
  previewUrl: string;
  progress: number;
  status: MediaAssetStatus;
  mediaAssetId?: string;
  errorMessage?: string;
}

export default function PostCreatePage() {
  const navigate = useNavigate();

  // Context & Scopes
  const [userScope, setUserScope] = useState<UserScopeResponse | null>(null);
  const [hierarchy, setHierarchy] = useState<SchoolHierarchy[]>([]);
  const [categories, setCategories] = useState<PostCategoryResponse[]>([]);
  const [eligibleRecipients, setEligibleRecipients] = useState<EligibleRecipientResponse[]>([]);

  // 1. Context form state
  const [selectedSchoolId, setSelectedSchoolId] = useState('');
  const [selectedBranchId, setSelectedBranchId] = useState('');
  const [selectedClassId, setSelectedClassId] = useState<string>('');

  // 2. Domain / Category
  const [selectedCategoryId, setSelectedCategoryId] = useState('');

  // 3. Caption
  const [caption, setCaption] = useState('');

  // 4. Visibility
  const [visibility, setVisibility] = useState<'PUBLIC' | 'PRIVATE'>('PUBLIC');
  const [showSwitchWarning, setShowSwitchWarning] = useState(false);

  // 5. Tag People (only for private, above media upload)
  const [taggedRecipients, setTaggedRecipients] = useState<EligibleRecipientResponse[]>([]);
  const [recipientQuery, setRecipientQuery] = useState('');

  // 6. Media Items
  const [mediaItems, setMediaItems] = useState<UploadItem[]>([]);

  // 7. Preview and Publish state
  const [showPreviewModal, setShowPreviewModal] = useState(false);
  const [publishing, setPublishing] = useState(false);
  const [errorBanner, setErrorBanner] = useState<string | null>(null);

  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      const [scope, hier, cats] = await Promise.all([
        evidencingApi.getCurrentUserScope(),
        evidencingApi.getHierarchy(),
        evidencingApi.getActiveCategories(),
      ]);

      setUserScope(scope);
      setHierarchy(hier);
      setCategories(cats);

      // Prefill context from user scope
      if (scope.schoolId) setSelectedSchoolId(scope.schoolId);
      if (scope.branchId) {
        setSelectedBranchId(scope.branchId);
        loadRecipients(scope.branchId);
      }
      if (scope.classId) setSelectedClassId(scope.classId);
      if (cats.length > 0) setSelectedCategoryId(cats[0].id);
    } catch (err: any) {
      setErrorBanner('Failed to load school context: ' + (err.response?.data?.message || err.message));
    }
  };

  const loadRecipients = async (bId: string, q?: string) => {
    try {
      const recs = await evidencingApi.getEligibleRecipients(bId, q);
      setEligibleRecipients(recs);
    } catch (err) {
      console.error(err);
    }
  };

  const handleBranchChange = (bId: string) => {
    setSelectedBranchId(bId);
    setSelectedClassId('');
    loadRecipients(bId);
  };

  // Visibility toggle handler with audience expansion warning
  const handleSelectVisibility = (newVal: 'PUBLIC' | 'PRIVATE') => {
    if (visibility === 'PRIVATE' && newVal === 'PUBLIC') {
      if (taggedRecipients.length > 0) {
        setShowSwitchWarning(true);
        return;
      }
    }
    setVisibility(newVal);
  };

  const confirmSwitchToPublic = () => {
    setTaggedRecipients([]);
    setVisibility('PUBLIC');
    setShowSwitchWarning(false);
  };

  const cancelSwitchToPublic = () => {
    setShowSwitchWarning(false);
  };

  // Recipient Tagging
  const handleAddRecipient = (rec: EligibleRecipientResponse) => {
    if (!taggedRecipients.some((r) => r.userId === rec.userId)) {
      setTaggedRecipients([...taggedRecipients, rec]);
    }
    setRecipientQuery('');
  };

  const handleRemoveRecipient = (userId: string) => {
    setTaggedRecipients(taggedRecipients.filter((r) => r.userId !== userId));
  };

  // Media Selection & Upload
  const handleFilesSelected = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    const files = Array.from(e.target.files);

    const newItems: UploadItem[] = files.map((file) => {
      const isVideo = file.type.startsWith('video/') || file.name.endsWith('.mp4');
      return {
        id: crypto.randomUUID(),
        file,
        mediaType: isVideo ? 'VIDEO' : 'IMAGE',
        previewUrl: URL.createObjectURL(file),
        progress: 0,
        status: 'UPLOADING',
      };
    });

    setMediaItems((prev) => [...prev, ...newItems]);

    // Process each upload asynchronously
    for (const item of newItems) {
      uploadSingleMedia(item);
    }

    // Reset input
    e.target.value = '';
  };

  const uploadSingleMedia = async (item: UploadItem) => {
    try {
      // 1. Initiate upload
      const initResp = await evidencingApi.initiateMediaUpload(item.file, item.mediaType);
      const mediaAssetId = initResp.mediaAssetId;

      setMediaItems((prev) =>
        prev.map((m) => (m.id === item.id ? { ...m, mediaAssetId, progress: 30 } : m))
      );

      // 2. Upload file bytes
      await evidencingApi.uploadMediaBytes(initResp.uploadUrl, item.file, (pct) => {
        setMediaItems((prev) =>
          prev.map((m) => (m.id === item.id ? { ...m, progress: Math.min(90, pct) } : m))
        );
      });

      // 3. Complete upload to trigger Media Variant Maker
      setMediaItems((prev) =>
        prev.map((m) => (m.id === item.id ? { ...m, status: 'PROCESSING', progress: 95 } : m))
      );
      await evidencingApi.completeMediaUpload(mediaAssetId);

      // Poll until READY (or maximum 5 seconds)
      let ready = false;
      let attempts = 0;
      while (!ready && attempts < 10) {
        attempts++;
        await new Promise((r) => setTimeout(r, 600));
        const statusResp = await evidencingApi.getMediaStatus(mediaAssetId);
        if (statusResp.status === 'READY') {
          ready = true;
          setMediaItems((prev) =>
            prev.map((m) =>
              m.id === item.id ? { ...m, status: 'READY', progress: 100 } : m
            )
          );
        } else if (statusResp.status === 'FAILED') {
          throw new Error(statusResp.errorMessage || 'Media processing failed');
        }
      }
    } catch (err: any) {
      const errMsg = err.response?.data?.message || err.message || 'Upload failed';
      setMediaItems((prev) =>
        prev.map((m) =>
          m.id === item.id ? { ...m, status: 'FAILED', errorMessage: errMsg } : m
        )
      );
    }
  };

  const handleRetryUpload = (item: UploadItem) => {
    setMediaItems((prev) =>
      prev.map((m) => (m.id === item.id ? { ...m, status: 'UPLOADING', progress: 0, errorMessage: undefined } : m))
    );
    uploadSingleMedia(item);
  };

  const handleRemoveMedia = (id: string) => {
    setMediaItems(mediaItems.filter((m) => m.id !== id));
  };

  const handleMoveMedia = (index: number, direction: 'left' | 'right') => {
    const newIdx = direction === 'left' ? index - 1 : index + 1;
    if (newIdx < 0 || newIdx >= mediaItems.length) return;
    const items = [...mediaItems];
    const [moved] = items.splice(index, 1);
    items.splice(newIdx, 0, moved);
    setMediaItems(items);
  };

  // Publish Form Submission
  const handlePublish = async () => {
    setErrorBanner(null);

    // Validation
    if (!selectedSchoolId || !selectedBranchId) {
      setErrorBanner('Please select an authorized school and branch context.');
      return;
    }
    if (!selectedCategoryId) {
      setErrorBanner('Please select a post domain / category.');
      return;
    }
    if (visibility === 'PRIVATE' && taggedRecipients.length === 0) {
      setErrorBanner('Private posts require at least one tagged recipient.');
      return;
    }
    if (mediaItems.length === 0) {
      setErrorBanner('Please upload at least one image or video for evidencing.');
      return;
    }

    const unreadyMedia = mediaItems.filter((m) => m.status !== 'READY');
    if (unreadyMedia.length > 0) {
      setErrorBanner(
        `Cannot publish: ${unreadyMedia.length} media item(s) are not ready. Please wait for processing or retry failed uploads.`
      );
      return;
    }

    setPublishing(true);

    try {
      const idempotencyKey = crypto.randomUUID();
      const mediaAssetIds = mediaItems
        .map((m) => m.mediaAssetId)
        .filter((id): id is string => !!id);

      await evidencingApi.publishPost(
        {
          schoolId: selectedSchoolId,
          branchId: selectedBranchId,
          classId: selectedClassId || undefined,
          categoryId: selectedCategoryId,
          caption: caption.trim() || undefined,
          visibility,
          taggedRecipientIds:
            visibility === 'PRIVATE' ? taggedRecipients.map((r) => r.userId) : [],
          mediaAssetIds,
          idempotencyKey,
        },
        idempotencyKey
      );

      navigate('/evidencing');
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Failed to publish post.';
      setErrorBanner(msg);
      setPublishing(false);
    }
  };

  // Current School Hierarchy selection helpers
  const currentSchool = hierarchy.find((s) => s.id === selectedSchoolId);
  const branches = currentSchool?.branches || [];
  const currentBranch = branches.find((b) => b.id === selectedBranchId);
  const classes = currentBranch?.classes || [];
  const selectedCategory = categories.find((c) => c.id === selectedCategoryId);

  return (
    <div className="evidencing-container">
      <div className="evidencing-header">
        <div className="evidencing-title-group">
          <h2>Create Evidencing Post</h2>
          <p className="evidencing-subtitle">
            Share learning moments, achievements, and activities across your school community
          </p>
        </div>
        <button
          type="button"
          className="btn-secondary"
          onClick={() => navigate('/evidencing')}
        >
          Cancel
        </button>
      </div>

      {errorBanner && (
        <div className="error" style={{ marginBottom: '1.5rem' }}>
          <strong>Error:</strong> {errorBanner}
        </div>
      )}

      <div className="post-form-card">
        {/* ========================================================
            STEP 1: School, Branch, and Class Context (prefilled)
            ======================================================== */}
        <section className="form-step-group">
          <div className="form-step-title">
            <span className="form-step-badge">1</span>
            <span>School, Branch, and Class Scope</span>
          </div>
          <div className="form-grid-3">
            <div>
              <label className="form-field-label">School</label>
              <select
                value={selectedSchoolId}
                onChange={(e) => {
                  setSelectedSchoolId(e.target.value);
                  const sch = hierarchy.find((s) => s.id === e.target.value);
                  if (sch && sch.branches.length > 0) {
                    handleBranchChange(sch.branches[0].id);
                  }
                }}
                className="form-select"
              >
                {hierarchy.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="form-field-label">Branch</label>
              <select
                value={selectedBranchId}
                onChange={(e) => handleBranchChange(e.target.value)}
                className="form-select"
              >
                {branches.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="form-field-label">Class (Optional)</label>
              <select
                value={selectedClassId}
                onChange={(e) => setSelectedClassId(e.target.value)}
                className="form-select"
              >
                <option value="">Branch-wide (No specific class)</option>
                {classes.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name} {c.gradeLevel ? `(${c.gradeLevel})` : ''}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </section>

        {/* ========================================================
            STEP 2: Post Domain / Category
            ======================================================== */}
        <section className="form-step-group">
          <div className="form-step-title">
            <span className="form-step-badge">2</span>
            <span>Domain / Category (Activity Area)</span>
          </div>
          <div>
            <select
              value={selectedCategoryId}
              onChange={(e) => setSelectedCategoryId(e.target.value)}
              className="form-select"
            >
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name} — {c.description}
                </option>
              ))}
            </select>
            {selectedCategory?.description && (
              <p style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '0.35rem' }}>
                💡 <em>{selectedCategory.description}</em>
              </p>
            )}
          </div>
        </section>

        {/* ========================================================
            STEP 3: Caption or Description
            ======================================================== */}
        <section className="form-step-group">
          <div className="form-step-title">
            <span className="form-step-badge">3</span>
            <span>Caption / Description</span>
          </div>
          <div>
            <textarea
              rows={4}
              placeholder="What learning activities, milestones, or achievements took place? Add context for staff..."
              value={caption}
              onChange={(e) => setCaption(e.target.value)}
              className="form-textarea"
              maxLength={4000}
            />
            <div style={{ textAlign: 'right', fontSize: '0.75rem', color: '#94a3b8', marginTop: '0.25rem' }}>
              {caption.length} / 4000
            </div>
          </div>
        </section>

        {/* ========================================================
            STEP 4: Public / Private Visibility Selector
            ======================================================== */}
        <section className="form-step-group">
          <div className="form-step-title">
            <span className="form-step-badge">4</span>
            <span>Visibility</span>
          </div>
          <div className="visibility-toggle-group">
            <div
              className={`visibility-card ${visibility === 'PUBLIC' ? 'active public' : ''}`}
              onClick={() => handleSelectVisibility('PUBLIC')}
            >
              <div className="visibility-card-header">
                <span>🌐</span>
                <span>PUBLIC</span>
              </div>
              <p className="visibility-card-desc">
                Visible to authorized staff within this school branch/class. No recipient tags required or stored.
              </p>
            </div>

            <div
              className={`visibility-card ${visibility === 'PRIVATE' ? 'active private' : ''}`}
              onClick={() => handleSelectVisibility('PRIVATE')}
            >
              <div className="visibility-card-header">
                <span>🔒</span>
                <span>PRIVATE</span>
              </div>
              <p className="visibility-card-desc">
                Visible only to you and explicitly tagged colleagues. Requires at least one tagged recipient.
              </p>
            </div>
          </div>
        </section>

        {/* ========================================================
            STEP 5: Tag People (ONLY for Private, ABOVE Media Upload)
            ======================================================== */}
        {visibility === 'PRIVATE' && (
          <section className="tag-people-container">
            <div className="form-step-title" style={{ color: '#991b1b' }}>
              <span className="form-step-badge" style={{ background: '#fee2e2', color: '#dc2626' }}>
                5
              </span>
              <span>Tag People (Required for Private Posts)</span>
            </div>
            <p style={{ fontSize: '0.85rem', color: '#7f1d1d', margin: '0 0 0.75rem' }}>
              Select colleagues in your branch with Evidencing access. Only tagged recipients and the creator can view this post.
            </p>

            <div style={{ position: 'relative' }}>
              <input
                type="text"
                placeholder="Search staff by name or role to tag..."
                value={recipientQuery}
                onChange={(e) => {
                  setRecipientQuery(e.target.value);
                  loadRecipients(selectedBranchId, e.target.value);
                }}
                className="form-input"
              />

              {/* Autocomplete Dropdown */}
              {recipientQuery && (
                <div
                  style={{
                    position: 'absolute',
                    top: '100%',
                    left: 0,
                    right: 0,
                    background: '#fff',
                    border: '1px solid #cbd5e1',
                    borderRadius: '8px',
                    boxShadow: '0 8px 16px rgba(0,0,0,0.1)',
                    zIndex: 20,
                    maxHeight: '200px',
                    overflowY: 'auto',
                  }}
                >
                  {eligibleRecipients.filter((r) => !taggedRecipients.some((t) => t.userId === r.userId)).length === 0 ? (
                    <div style={{ padding: '0.75rem', fontSize: '0.85rem', color: '#94a3b8' }}>
                      No matching authorized staff in this branch.
                    </div>
                  ) : (
                    eligibleRecipients
                      .filter((r) => !taggedRecipients.some((t) => t.userId === r.userId))
                      .map((rec) => (
                        <div
                          key={rec.userId}
                          style={{
                            padding: '0.6rem 0.85rem',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '0.5rem',
                            borderBottom: '1px solid #f1f5f9',
                          }}
                          onClick={() => handleAddRecipient(rec)}
                        >
                          {rec.avatarUrl ? (
                            <img
                              src={rec.avatarUrl}
                              alt={rec.displayName}
                              style={{ width: 24, height: 24, borderRadius: '50%' }}
                            />
                          ) : (
                            <span>👤</span>
                          )}
                          <strong>{rec.displayName}</strong>
                          <span style={{ fontSize: '0.75rem', color: '#64748b' }}>({rec.role})</span>
                          {rec.className && (
                            <span style={{ fontSize: '0.75rem', color: '#2563eb' }}>• {rec.className}</span>
                          )}
                        </div>
                      ))
                  )}
                </div>
              )}
            </div>

            {/* Selected Tag Chips */}
            <div className="selected-tags-chips">
              {taggedRecipients.length === 0 ? (
                <span style={{ fontSize: '0.8rem', color: '#dc2626', fontWeight: 600 }}>
                  ⚠️ At least one recipient is required before publishing a private post.
                </span>
              ) : (
                taggedRecipients.map((rec) => (
                  <span key={rec.userId} className="tag-chip-interactive">
                    <span>@{rec.displayName}</span>
                    <span style={{ fontSize: '0.7rem', color: '#6b7280' }}>({rec.role})</span>
                    <button
                      type="button"
                      className="tag-remove-btn"
                      onClick={() => handleRemoveRecipient(rec.userId)}
                    >
                      ×
                    </button>
                  </span>
                ))
              )}
            </div>
          </section>
        )}

        {/* ========================================================
            STEP 6: Image / Video Upload Area
            ======================================================== */}
        <section className="form-step-group">
          <div className="form-step-title">
            <span className="form-step-badge">6</span>
            <span>Image &amp; Video Media</span>
          </div>

          <label className="media-dropzone">
            <input
              type="file"
              multiple
              accept="image/*,video/mp4,video/quicktime"
              onChange={handleFilesSelected}
              style={{ display: 'none' }}
            />
            <div style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>📷 🎥</div>
            <strong style={{ display: 'block', color: '#1e293b' }}>
              Click or drag to upload images and videos
            </strong>
            <span style={{ fontSize: '0.8rem', color: '#64748b' }}>
              JPEG, PNG, WebP up to 50MB • MP4, MOV up to 500MB • Multiple items supported
            </span>
          </label>

          {/* Media Items List with Previews, Progress, Reordering */}
          {mediaItems.length > 0 && (
            <div className="media-preview-grid">
              {mediaItems.map((item, index) => (
                <div key={item.id} className="media-preview-card">
                  {item.mediaType === 'VIDEO' ? (
                    <video src={item.previewUrl} className="media-thumb-img" />
                  ) : (
                    <img src={item.previewUrl} alt="preview" className="media-thumb-img" />
                  )}

                  <span className={`media-status-badge ${item.status}`}>
                    {item.status}
                  </span>

                  {item.status === 'UPLOADING' && (
                    <div
                      style={{
                        position: 'absolute',
                        bottom: 40,
                        left: 0,
                        right: 0,
                        height: 4,
                        background: '#334155',
                      }}
                    >
                      <div
                        style={{
                          height: '100%',
                          background: '#3b82f6',
                          width: `${item.progress}%`,
                          transition: 'width 0.2s',
                        }}
                      />
                    </div>
                  )}

                  <div className="media-card-controls">
                    <div>
                      {index > 0 && (
                        <button
                          type="button"
                          className="media-control-btn"
                          onClick={() => handleMoveMedia(index, 'left')}
                          title="Move left"
                        >
                          ◀
                        </button>
                      )}
                      {index < mediaItems.length - 1 && (
                        <button
                          type="button"
                          className="media-control-btn"
                          onClick={() => handleMoveMedia(index, 'right')}
                          title="Move right"
                          style={{ marginLeft: 4 }}
                        >
                          ▶
                        </button>
                      )}
                    </div>

                    <div style={{ display: 'flex', gap: '0.25rem' }}>
                      {item.status === 'FAILED' && (
                        <button
                          type="button"
                          className="media-control-btn"
                          onClick={() => handleRetryUpload(item)}
                          style={{ background: '#ea580c', color: '#fff' }}
                        >
                          Retry
                        </button>
                      )}
                      <button
                        type="button"
                        className="media-control-btn"
                        onClick={() => handleRemoveMedia(item.id)}
                        style={{ background: '#b91c1c', color: '#fff' }}
                      >
                        ✕
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* ========================================================
            STEP 7: Preview and Publish Actions
            ======================================================== */}
        <section style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
          <button
            type="button"
            className="btn-secondary"
            onClick={() => setShowPreviewModal(true)}
            disabled={mediaItems.length === 0}
          >
            👁️ Preview Post
          </button>
          <button
            type="button"
            className="btn-primary"
            onClick={handlePublish}
            disabled={publishing}
            style={{ minWidth: '140px' }}
          >
            {publishing ? 'Publishing...' : '🚀 Publish Post'}
          </button>
        </section>
      </div>

      {/* Confirmation Modal when switching Private -> Public */}
      {showSwitchWarning && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <strong style={{ color: '#dc2626' }}>Expand Audience to Public?</strong>
              <button
                type="button"
                onClick={cancelSwitchToPublic}
                style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.2rem' }}
              >
                ✕
              </button>
            </div>
            <div className="modal-body">
              <p>
                Switching this post to <strong>PUBLIC</strong> will make it visible to all authorized school staff in
                this branch.
              </p>
              <p style={{ color: '#64748b' }}>
                All {taggedRecipients.length} currently tagged recipient(s) will be cleared because public posts store no
                audience tags.
              </p>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn-secondary" onClick={cancelSwitchToPublic}>
                Keep Private
              </button>
              <button
                type="button"
                className="btn-primary"
                style={{ background: '#dc2626' }}
                onClick={confirmSwitchToPublic}
              >
                Confirm &amp; Clear Tags
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Preview Modal */}
      {showPreviewModal && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '640px' }}>
            <div className="modal-header">
              <strong>Post Preview</strong>
              <button
                type="button"
                onClick={() => setShowPreviewModal(false)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.2rem' }}
              >
                ✕
              </button>
            </div>
            <div className="modal-body" style={{ padding: 0 }}>
              <div className="post-card" style={{ margin: 0, border: 'none', boxShadow: 'none' }}>
                <div className="post-card-header">
                  <div className="author-info">
                    {userScope?.avatarUrl && (
                      <img src={userScope.avatarUrl} alt="Author" className="author-avatar" />
                    )}
                    <div className="author-text">
                      <div className="author-name-row">
                        <span className="author-name">{userScope?.displayName || 'Author'}</span>
                        <span className="author-role-badge">{userScope?.role || 'Staff'}</span>
                      </div>
                      <div className="post-meta-row">
                        <span className="scope-tag">
                          🏫 {currentBranch?.name || 'Branch'}
                          {selectedClassId && classes.find((c) => c.id === selectedClassId)
                            ? ` • 🎓 ${classes.find((c) => c.id === selectedClassId)?.name}`
                            : ''}
                        </span>
                        <span>• Just now</span>
                      </div>
                    </div>
                  </div>
                  <div className="post-header-badges">
                    <span className="domain-badge">
                      <span>🏷️</span>
                      <span>{selectedCategory?.name}</span>
                    </span>
                    <span
                      className={`visibility-badge ${visibility === 'PUBLIC' ? 'public' : 'private'}`}
                    >
                      {visibility === 'PUBLIC' ? '🌐 Public' : '🔒 Private'}
                    </span>
                  </div>
                </div>

                {/* Tagged people above media */}
                {visibility === 'PRIVATE' && taggedRecipients.length > 0 && (
                  <div className="private-tags-container">
                    <span className="tags-label">🔒 Tagged:</span>
                    {taggedRecipients.map((r) => (
                      <span key={r.userId} className="recipient-chip">
                        @{r.displayName} ({r.role})
                      </span>
                    ))}
                  </div>
                )}

                {/* Media Preview */}
                {mediaItems.length > 0 && (
                  <div className="post-media-wrapper">
                    {mediaItems[0].mediaType === 'VIDEO' ? (
                      <video src={mediaItems[0].previewUrl} controls className="post-media-video" />
                    ) : (
                      <img src={mediaItems[0].previewUrl} alt="preview" className="post-media-image" />
                    )}
                  </div>
                )}

                <div className="post-content-section" style={{ paddingTop: '1rem' }}>
                  <div className="post-caption">{caption || 'No caption provided.'}</div>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn-secondary"
                onClick={() => setShowPreviewModal(false)}
              >
                Close Preview
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
