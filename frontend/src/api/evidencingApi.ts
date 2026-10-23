import api from './client';

export type PostVisibility = 'PUBLIC' | 'PRIVATE';
export type PublicationStatus = 'DRAFT' | 'PUBLISHING' | 'PUBLISHED' | 'FAILED';
export type MediaAssetStatus = 'UPLOADING' | 'PROCESSING' | 'READY' | 'FAILED';
export type MediaType = 'IMAGE' | 'VIDEO';

export interface RecipientDetail {
  userId: string;
  displayName: string;
  avatarUrl?: string;
  role: string;
}

export interface MediaDetail {
  mediaId: string;
  mediaType: MediaType;
  url: string;
  thumbnailUrl?: string;
  width?: number;
  height?: number;
  durationSeconds?: number;
  displayOrder: number;
}

export interface PostResponse {
  id: string;
  caption: string;
  visibility: PostVisibility;
  publicationStatus: PublicationStatus;
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
  version: number;

  schoolId: string;
  schoolName: string;
  branchId: string;
  branchName: string;
  classId?: string;
  className?: string;

  authorId: string;
  authorName: string;
  authorAvatarUrl?: string;
  authorRole: string;

  taggedPeople: RecipientDetail[];

  domainId: string;
  domainName: string;
  domainDescription?: string;
  domainIcon?: string;

  media: MediaDetail[];

  likeCount: number;
  commentCount: number;
  viewCount: number;
  isLikedByCurrentUser: boolean;

  canEdit: boolean;
  canDelete: boolean;
  canLike: boolean;
  canComment: boolean;
}

export interface PostCreateRequest {
  schoolId: string;
  branchId: string;
  classId?: string;
  categoryId: string;
  caption?: string;
  visibility: PostVisibility;
  taggedRecipientIds?: string[];
  mediaAssetIds: string[];
  idempotencyKey?: string;
}

export interface PostUpdateRequest {
  categoryId: string;
  caption?: string;
  visibility: PostVisibility;
  taggedRecipientIds?: string[];
  mediaAssetIds?: string[];
  version: number;
}

export interface PostFilterCriteria {
  categoryId?: string;
  authorId?: string;
  visibility?: PostVisibility;
  classId?: string;
  branchId?: string;
  search?: string;
  fromDate?: string;
  toDate?: string;
}

export interface PostCategoryResponse {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  active: boolean;
  displayOrder: number;
}

export interface CommentResponse {
  id: string;
  postId: string;
  userId: string;
  authorName: string;
  authorAvatarUrl?: string;
  authorRole: string;
  commentText: string;
  createdAt: string;
}

export interface EligibleRecipientResponse {
  userId: string;
  displayName: string;
  avatarUrl?: string;
  role: string;
  schoolId: string;
  branchId: string;
  classId?: string;
  className?: string;
}

export interface UserScopeResponse {
  userId: string;
  displayName: string;
  role: string;
  avatarUrl?: string;
  schoolId: string;
  schoolName: string;
  branchId: string;
  branchName: string;
  classId?: string;
  className?: string;
  permissions: string[];
}

export interface NotificationResponse {
  id: string;
  postId: string;
  creatorId: string;
  creatorName: string;
  message: string;
  read: boolean;
  link: string;
  createdAt: string;
}

export interface MediaInitiateResponse {
  mediaAssetId: string;
  uploadUrl: string;
  storageKey: string;
  mediaType: MediaType;
  status: MediaAssetStatus;
}

export interface MediaAssetResponse {
  mediaAssetId: string;
  originalFilename: string;
  mediaType: MediaType;
  status: MediaAssetStatus;
  url?: string;
  thumbnailUrl?: string;
  width?: number;
  height?: number;
  durationSeconds?: number;
  errorMessage?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  empty: boolean;
}

export interface SchoolHierarchy {
  id: string;
  name: string;
  code: string;
  branches: {
    id: string;
    name: string;
    code: string;
    classes: {
      id: string;
      name: string;
      gradeLevel?: string;
      section?: string;
    }[];
  }[];
}

export const evidencingApi = {
  // Feed & Posts
  getFeed: async (criteria?: PostFilterCriteria, page = 0, size = 10) => {
    const params = new URLSearchParams();
    params.set('page', String(page));
    params.set('size', String(size));
    if (criteria?.categoryId) params.set('categoryId', criteria.categoryId);
    if (criteria?.authorId) params.set('authorId', criteria.authorId);
    if (criteria?.visibility) params.set('visibility', criteria.visibility);
    if (criteria?.classId) params.set('classId', criteria.classId);
    if (criteria?.branchId) params.set('branchId', criteria.branchId);
    if (criteria?.search) params.set('search', criteria.search);
    if (criteria?.fromDate) params.set('fromDate', criteria.fromDate);
    if (criteria?.toDate) params.set('toDate', criteria.toDate);

    const { data } = await api.get<PageResponse<PostResponse>>(`/evidencing/posts?${params.toString()}`);
    return data;
  },

  getPost: async (id: string) => {
    const { data } = await api.get<PostResponse>(`/evidencing/posts/${id}`);
    return data;
  },

  publishPost: async (request: PostCreateRequest, idempotencyKey?: string) => {
    const headers: Record<string, string> = {};
    if (idempotencyKey) {
      headers['Idempotency-Key'] = idempotencyKey;
    }
    const { data } = await api.post<PostResponse>('/evidencing/posts', request, { headers });
    return data;
  },

  updatePost: async (id: string, request: PostUpdateRequest) => {
    const { data } = await api.put<PostResponse>(`/evidencing/posts/${id}`, request);
    return data;
  },

  deletePost: async (id: string) => {
    await api.delete(`/evidencing/posts/${id}`);
  },

  // Interactions: Likes & Comments
  toggleLike: async (postId: string) => {
    const { data } = await api.post<{ postId: string; liked: boolean; likeCount: number }>(
      `/evidencing/posts/${postId}/like`
    );
    return data;
  },

  getComments: async (postId: string) => {
    const { data } = await api.get<CommentResponse[]>(`/evidencing/posts/${postId}/comments`);
    return data;
  },

  addComment: async (postId: string, commentText: string) => {
    const { data } = await api.post<CommentResponse>(`/evidencing/posts/${postId}/comments`, {
      commentText,
    });
    return data;
  },

  // Categories / Domains
  getActiveCategories: async () => {
    const { data } = await api.get<PostCategoryResponse[]>('/evidencing/categories');
    return data;
  },

  getAllCategories: async () => {
    const { data } = await api.get<PostCategoryResponse[]>('/evidencing/categories/all');
    return data;
  },

  createCategory: async (category: { name: string; description?: string; icon?: string; displayOrder?: number }) => {
    const { data } = await api.post<PostCategoryResponse>('/evidencing/categories', category);
    return data;
  },

  updateCategory: async (id: string, category: { name: string; description?: string; icon?: string; active?: boolean; displayOrder?: number }) => {
    const { data } = await api.put<PostCategoryResponse>(`/evidencing/categories/${id}`, category);
    return data;
  },

  // Media
  initiateMediaUpload: async (file: File, mediaType: MediaType) => {
    const { data } = await api.post<MediaInitiateResponse>('/evidencing/media/initiate-upload', {
      originalFilename: file.name,
      contentType: file.type || (mediaType === 'VIDEO' ? 'video/mp4' : 'image/jpeg'),
      fileSizeBytes: file.size,
      mediaType,
    });
    return data;
  },

  uploadMediaBytes: async (uploadUrl: string, file: File, onProgress?: (pct: number) => void) => {
    // If relative URL (fallback local upload endpoint)
    if (uploadUrl.startsWith('/')) {
      const buffer = await file.arrayBuffer();
      await api.put(uploadUrl, buffer, {
        headers: { 'Content-Type': file.type || 'application/octet-stream' },
        onUploadProgress: (evt) => {
          if (evt.total && onProgress) {
            onProgress(Math.round((evt.loaded * 100) / evt.total));
          }
        },
      });
    } else {
      // S3 presigned PUT URL
      const buffer = await file.arrayBuffer();
      await fetch(uploadUrl, {
        method: 'PUT',
        body: buffer,
        headers: { 'Content-Type': file.type || 'application/octet-stream' },
      });
      if (onProgress) onProgress(100);
    }
  },

  completeMediaUpload: async (mediaAssetId: string) => {
    const { data } = await api.post<MediaAssetResponse>(`/evidencing/media/${mediaAssetId}/complete-upload`);
    return data;
  },

  getMediaStatus: async (mediaAssetId: string) => {
    const { data } = await api.get<MediaAssetResponse>(`/evidencing/media/${mediaAssetId}/status`);
    return data;
  },

  retryMedia: async (mediaAssetId: string) => {
    const { data } = await api.post<MediaAssetResponse>(`/evidencing/media/${mediaAssetId}/retry`);
    return data;
  },

  // User Scopes, Recipients & Hierarchy
  getCurrentUserScope: async () => {
    const { data } = await api.get<UserScopeResponse>('/evidencing/users/me');
    return data;
  },

  getAllUsers: async () => {
    const { data } = await api.get<UserScopeResponse[]>('/evidencing/users/all');
    return data;
  },

  getEligibleRecipients: async (branchId?: string, query?: string) => {
    const params = new URLSearchParams();
    if (branchId) params.set('branchId', branchId);
    if (query) params.set('query', query);
    const { data } = await api.get<EligibleRecipientResponse[]>(`/evidencing/recipients/eligible?${params.toString()}`);
    return data;
  },

  getHierarchy: async () => {
    const { data } = await api.get<SchoolHierarchy[]>('/evidencing/hierarchy');
    return data;
  },

  // Notifications
  getNotifications: async () => {
    const { data } = await api.get<NotificationResponse[]>('/evidencing/notifications');
    return data;
  },

  getUnreadCount: async () => {
    const { data } = await api.get<{ unreadCount: number }>('/evidencing/notifications/unread-count');
    return data.unreadCount;
  },

  markNotificationRead: async (id: string) => {
    await api.put(`/evidencing/notifications/${id}/read`);
  },

  markAllNotificationsRead: async () => {
    await api.post('/evidencing/notifications/read-all');
  },
};
