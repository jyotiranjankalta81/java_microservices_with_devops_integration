# Evidencing Module - School Activity Intelligence & Staff Media Feed

An Instagram-style evidencing and media sharing module built for school management platforms. It enables teachers, branch administrators, coordinators, and school leaders to capture, document, and share rich multimedia evidence of classroom learning and campus activities with strict role-based access control, hierarchical scoping, and fine-grained public/private visibility.

---

## 1. System Architecture

```
                                  +------------------------------------+
                                  |           React Frontend           |
                                  |   (Vite, TS, Instagram-Style UI)   |
                                  +-----------------+------------------+
                                                    |
                                    REST / Presigned Uploads / WS
                                                    |
                                                    v
                                  +-----------------+------------------+
                                  |         Spring Cloud Gateway       |
                                  |          (:8080 or Direct)         |
                                  +-----------------+------------------+
                                                    |
                                         /api/evidencing/**
                                                    |
                                                    v
+---------------------------------------------------------------------------------------------------+
| com.jobintel.evidencing (Port :8087)                                                              |
|                                                                                                   |
|  [Controllers]                                                                                    |
|   PostController | MediaController | InteractionController | NotificationController               |
|                                                                                                   |
|  [Security & Scoping Engine]                                                                      |
|   EvidencingAccessService (Deny-by-Default RBAC, School/Branch Scoping, Visibility Gates)         |
|                                                                                                   |
|  [Application Services]                                                                           |
|   PostService | MediaService | MediaProcessingService | SqsQueueService | NotificationService     |
|                                                                                                   |
|  [Data Layer - Spring Data JPA & PostgreSQL]                                                      |
|   Post | PostMedia | MediaAsset | PostRecipient | PostCategory | PostLike | PostComment           |
+-------------------+---------------------------+-----------------------------------+---------------+
                    |                           |                                   |
                    v                           v                                   v
          +---------+---------+       +---------+---------+               +---------+---------+
          |    PostgreSQL     |       |      AWS S3       |               |      AWS SQS      |
          |  (evidencing_db)  |       |  Private Buckets  |               | Publication Queue |
          |  Flyway V1 & V2   |       | Presigned GET/PUT |               |   & Dead-Letter   |
          +-------------------+       +-------------------+               +-------------------+
```

---

## 2. Role-Based Permissions & Scoping Matrix

### Configurable Permission Set
- `EVIDENCING_VIEW`: Access the evidencing module, feed, and authorized post media.
- `EVIDENCING_CREATE`: Author and publish school activity posts.
- `EVIDENCING_EDIT_OWN`: Modify captions, categories, or visibility of own posts.
- `EVIDENCING_DELETE_OWN`: Remove own posts.

### Deny-by-Default Role Matrix
| Role | View Feed / Posts | Create Posts | Edit Own | Delete Own | Notes |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Branch Admin** | ✅ | ✅ | ✅ | ✅ | Branch-wide scope |
| **School Admin** | ✅ | ✅ | ✅ | ✅ | Multi-branch / school scope |
| **Teacher** | ✅ | ✅ | ✅ | ✅ | Assigned branch & class scope |
| **Coordinator** | ✅ | ✅ | ✅ | ✅ | Branch-wide academic scope |
| **Admission Coordinator**| ✅ | ✅ | ✅ | ✅ | Branch public events scope |
| **Nanny** | ❌ (Denied) | ❌ (Denied) | ❌ (Denied) | ❌ (Denied) | Zero access to module or media |
| **Peon** | ❌ (Denied) | ❌ (Denied) | ❌ (Denied) | ❌ (Denied) | Zero access to module or media |
| **Driver** | ❌ (Denied) | ❌ (Denied) | ❌ (Denied) | ❌ (Denied) | Zero access to module or media |

> **Deny-by-Default Guarantee**: Employment at a school does not grant access. Unassigned roles or roles without explicit `EVIDENCING_*` permissions receive HTTP `403 Forbidden` on all endpoints and cannot generate media URLs.

---

## 3. Public vs. Private Visibility Rules

| Dimension | PUBLIC Posts | PRIVATE Posts |
| :--- | :--- | :--- |
| **Audience** | All authorized staff in the post's school/branch/class scope. | **Author + explicitly tagged recipients only**. |
| **Admin Bypass** | Admins within branch scope view public posts. | **No Admin Bypass**: School/Branch Admins **cannot** view untagged private posts. |
| **Recipient Tagging**| **No tagging field**. No audience tags stored. | **Mandatory**: Minimum 1 eligible recipient required. |
| **Feed / Detail UI** | Clean header, no recipient chips. | **Recipient tags displayed prominently ABOVE the media**. |
| **Visibility Switching**| Warns user of expanded audience, **clears all tags** on confirmation. | Requires selecting at least one tagged recipient before saving. |
| **Notifications** | Scoped broadcast to eligible branch users. | Targeted notification exclusively to tagged eligible users. |

---

## 4. Post Creation Form Sequencing

The creation wizard in `PostCreatePage.tsx` strictly follows the required 7-step sequence:
1. **School / Branch / Class Context**: Automatically prefilled from the user's active assignment.
2. **Post Domain / Category**: Single primary category selected from the 15 pre-seeded school domains.
3. **Caption / Description**: Rich text description of the learning activity.
4. **Public / Private Visibility**: Clear toggle with audience expansion warning dialog.
5. **Tag People**: Rendered **only for private posts**, positioned directly **above the media upload area**.
6. **Image / Video Upload Area**: Multi-file drag-and-drop, client-side preview, individual upload progress, deletion, and order re-arranging.
7. **Preview & Publish**: Verification view and idempotent publish submission.

---

## 5. Media Processing Pipeline & Private S3 Storage

1. **Initiate Upload**: Client calls `POST /api/evidencing/media/initiate-upload` with filename, MIME type, and byte size.
2. **Authorization**: Backend verifies caller has `EVIDENCING_CREATE` and records `MediaAsset` in state `UPLOADING`.
3. **Direct Upload**: Backend returns an AWS S3 presigned `PUT` URL (or HMAC-signed local endpoint for offline dev).
4. **Complete Upload & Async Processing**: Client calls `POST /api/evidencing/media/{id}/complete-upload`. Backend transitions asset to `PROCESSING` and triggers `MediaProcessingService` asynchronously:
   - Image variants (web, thumbnail) generated with metadata (width, height).
   - Video thumbnails captured; duration extracted.
   - Status transitions to `READY` (or `FAILED` with retry option).
5. **Publish Gate**: Backend rejects publication if any attached media is not in `READY` status.
6. **Zero Public S3 URLs**: All media access is authorized against the post's current visibility and school scope before issuing short-lived presigned GET URLs (15-minute expiration).

---

## 6. Seeded Staff Personas for Local Testing

| Persona | Role | Branch | Permissions | Use Case |
| :--- | :--- | :--- | :--- | :--- |
| **Sarah Jenkins** | Teacher | North Campus (KG-A) | VIEW, CREATE, EDIT_OWN, DELETE_OWN | Can create & view posts |
| **Emily Davis** | Teacher | North Campus (Grade 1-A)| VIEW, CREATE, EDIT_OWN, DELETE_OWN | Tagged in Sarah's private post |
| **Robert Taylor** | Branch Admin | North Campus | VIEW, CREATE, EDIT_OWN, DELETE_OWN | Branch admin (untagged on private) |
| **Jennifer White** | Coordinator | North Campus | VIEW, CREATE, EDIT_OWN, DELETE_OWN | Academic coordinator |
| **David Green** | Peon | North Campus | **None** | **Access Denied (403)** |
| **Mary Hall** | Nanny | North Campus | **None** | **Access Denied (403)** |
| **John Smith** | Driver | North Campus | **None** | **Access Denied (403)** |
| **James Wilson** | Teacher | South Campus (Grade 2-B)| VIEW, CREATE, EDIT_OWN, DELETE_OWN | Unrelated school branch |

---

## 7. Key REST Endpoints

### Posts & Feed
- `GET /api/evidencing/posts`: Scoped feed with pagination & filters (`categoryId`, `visibility`, `authorId`, `fromDate`, `toDate`, `search`).
- `GET /api/evidencing/posts/{id}`: Detailed display-ready post with enriched creator, tags, media URLs, and counts.
- `POST /api/evidencing/posts`: Idempotent post publication via SQS (`Idempotency-Key` header).
- `PUT /api/evidencing/posts/{id}`: Edit caption, domain, or visibility (with optimistic locking `@Version`).
- `DELETE /api/evidencing/posts/{id}`: Delete own post.

### Media Management
- `POST /api/evidencing/media/initiate-upload`: Request presigned upload URL.
- `POST /api/evidencing/media/{id}/complete-upload`: Trigger asynchronous variant maker.
- `GET /api/evidencing/media/{id}/status`: Polling status (`UPLOADING`, `PROCESSING`, `READY`, `FAILED`).
- `POST /api/evidencing/media/{id}/retry`: Re-trigger failed media processing.

### Interactions & Notifications
- `POST /api/evidencing/posts/{id}/like`: Toggle like (re-validates post visibility).
- `GET /api/evidencing/posts/{id}/comments`: Fetch comments for post.
- `POST /api/evidencing/posts/{id}/comments`: Add comment (re-validates post visibility).
- `GET /api/evidencing/notifications`: Fetch user notifications.
- `PUT /api/evidencing/notifications/{id}/read`: Mark notification as read.
- `POST /api/evidencing/notifications/read-all`: Mark all read.

---

## 8. Verification & Integration Test Suite

All 11 acceptance scenarios specified in the requirements are codified and verified in:
[`PostAuthorizationIntegrationTest.java`](file:///Users/jyotiranjankalta/Projects/job-intelligence-platform/services/evidencing-service/src/test/java/com/jobintel/evidencing/PostAuthorizationIntegrationTest.java)

Run the test suite:
```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home mvn test -pl services/evidencing-service -am
```

Results:
```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
