package com.jobintel.evidencing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobintel.evidencing.domain.entity.MediaAsset;
import com.jobintel.evidencing.domain.entity.Post;
import com.jobintel.evidencing.domain.enums.MediaAssetStatus;
import com.jobintel.evidencing.domain.enums.PostVisibility;
import com.jobintel.evidencing.dto.PostCreateRequest;
import com.jobintel.evidencing.dto.PostResponse;
import com.jobintel.evidencing.dto.PostUpdateRequest;
import com.jobintel.evidencing.repository.MediaAssetRepository;
import com.jobintel.evidencing.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PostAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MediaAssetRepository mediaAssetRepository;

    // Seed Data constants from V2 Flyway migration
    private final UUID SCHOOL_OAKRIDGE = UUID.fromString("e0000001-0000-0000-0000-000000000001");
    private final UUID BRANCH_NORTH = UUID.fromString("b0000001-0000-0000-0000-000000000001");
    private final UUID BRANCH_SOUTH = UUID.fromString("b0000002-0000-0000-0000-000000000001");
    private final UUID CLASS_4A = UUID.fromString("ce000001-0000-0000-0000-000000000001");
    private final UUID CATEGORY_LEARNING = UUID.fromString("ca000001-0000-0000-0000-000000000001");
    private final UUID CATEGORY_ACHIEVEMENTS = UUID.fromString("ca000014-0000-0000-0000-000000000014");

    // Staff Users
    private final UUID USER_SARAH_TEACHER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID USER_EMILY_TEACHER = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID USER_MARK_TEACHER = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID USER_ROBERT_ADMIN = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private final UUID USER_DAVID_SOUTH_TEACHER = UUID.fromString("66666666-6666-6666-6666-666666666666");

    // Roles without permission (Deny By Default)
    private final UUID USER_MARY_NANNY = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private final UUID USER_JOHN_PEON = UUID.fromString("88888888-8888-8888-8888-888888888888");
    private final UUID USER_JAMES_DRIVER = UUID.fromString("99999999-9999-9999-9999-999999999999");

    // Seed Posts
    private final UUID SEED_POST_PUBLIC = UUID.fromString("f0000001-0000-0000-0000-000000000001");
    private final UUID SEED_POST_PRIVATE = UUID.fromString("f0000002-0000-0000-0000-000000000002");
    private final UUID SEED_POST_SOUTH_CAMPUS = UUID.fromString("f0000004-0000-0000-0000-000000000004");

    private MediaAsset createTestMedia(UUID uploaderId, MediaAssetStatus status) {
        MediaAsset asset = MediaAsset.builder()
                .uploaderId(uploaderId)
                .mediaType(com.jobintel.evidencing.domain.enums.MediaType.IMAGE)
                .originalFilename("test_pic.jpg")
                .contentType("image/jpeg")
                .fileSizeBytes(1024L)
                .storageKey("media/" + uploaderId + "/" + UUID.randomUUID() + ".jpg")
                .status(status)
                .build();
        return mediaAssetRepository.save(asset);
    }

    @Test
    @DisplayName("Scenario 1: Authorized teacher can publish a public post")
    void testAuthorizedTeacherCanPublishPublicPost() throws Exception {
        MediaAsset media = createTestMedia(USER_SARAH_TEACHER, MediaAssetStatus.READY);

        PostCreateRequest request = PostCreateRequest.builder()
                .schoolId(SCHOOL_OAKRIDGE)
                .branchId(BRANCH_NORTH)
                .classId(CLASS_4A)
                .categoryId(CATEGORY_LEARNING)
                .caption("Robotics club building planetary rovers!")
                .visibility(PostVisibility.PUBLIC)
                .mediaAssetIds(List.of(media.getId()))
                .build();

        mockMvc.perform(post("/api/evidencing/posts")
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.authorId").value(USER_SARAH_TEACHER.toString()))
                .andExpect(jsonPath("$.taggedPeople").isEmpty()) // Public posts have no tags
                .andExpect(jsonPath("$.media[0].url").isNotEmpty());
    }

    @Test
    @DisplayName("Scenario 2: Private post requires at least one tagged recipient")
    void testPrivatePostRequiresRecipients() throws Exception {
        MediaAsset media = createTestMedia(USER_SARAH_TEACHER, MediaAssetStatus.READY);

        PostCreateRequest request = PostCreateRequest.builder()
                .schoolId(SCHOOL_OAKRIDGE)
                .branchId(BRANCH_NORTH)
                .classId(CLASS_4A)
                .categoryId(CATEGORY_ACHIEVEMENTS)
                .caption("Confidential student merit notes")
                .visibility(PostVisibility.PRIVATE)
                .taggedRecipientIds(List.of()) // Empty recipients!
                .mediaAssetIds(List.of(media.getId()))
                .build();

        mockMvc.perform(post("/api/evidencing/posts")
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Scenario 3: Tagged, authorized user can view a private post")
    void testTaggedAuthorizedUserCanViewPrivatePost() throws Exception {
        // SEED_POST_PRIVATE is created by Sarah and tagged to Emily Clark
        mockMvc.perform(get("/api/evidencing/posts/{id}", SEED_POST_PRIVATE)
                        .header("X-User-Id", USER_EMILY_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SEED_POST_PRIVATE.toString()))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.taggedPeople[0].userId").value(USER_EMILY_TEACHER.toString()));
    }

    @Test
    @DisplayName("Scenario 4: Untagged user cannot view private post, including direct API request (HTTP 403)")
    void testUntaggedUserCannotViewPrivatePost() throws Exception {
        // Mark Wilson is in North Campus, Grade 5B, but NOT tagged in SEED_POST_PRIVATE
        mockMvc.perform(get("/api/evidencing/posts/{id}", SEED_POST_PRIVATE)
                        .header("X-User-Id", USER_MARK_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER"))
                .andExpect(status().isForbidden());

        // Branch Admin also cannot view untagged private post!
        mockMvc.perform(get("/api/evidencing/posts/{id}", SEED_POST_PRIVATE)
                        .header("X-User-Id", USER_ROBERT_ADMIN.toString())
                        .header("X-User-Roles", "BRANCH_ADMIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 5: Nanny, peon, and driver roles without permissions cannot access module or media (HTTP 403)")
    void testNannyPeonDriverRolesDeniedByDefault() throws Exception {
        // Nanny attempting to view feed
        mockMvc.perform(get("/api/evidencing/posts")
                        .header("X-User-Id", USER_MARY_NANNY.toString())
                        .header("X-User-Roles", "NANNY"))
                .andExpect(status().isForbidden());

        // Peon attempting to view post
        mockMvc.perform(get("/api/evidencing/posts/{id}", SEED_POST_PUBLIC)
                        .header("X-User-Id", USER_JOHN_PEON.toString())
                        .header("X-User-Roles", "PEON"))
                .andExpect(status().isForbidden());

        // Driver attempting to create post
        MediaAsset media = createTestMedia(USER_JAMES_DRIVER, MediaAssetStatus.READY);
        PostCreateRequest request = PostCreateRequest.builder()
                .schoolId(SCHOOL_OAKRIDGE)
                .branchId(BRANCH_NORTH)
                .categoryId(CATEGORY_LEARNING)
                .caption("Bus route updates")
                .visibility(PostVisibility.PUBLIC)
                .mediaAssetIds(List.of(media.getId()))
                .build();

        mockMvc.perform(post("/api/evidencing/posts")
                        .header("X-User-Id", USER_JAMES_DRIVER.toString())
                        .header("X-User-Roles", "DRIVER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 6: Public posts remain restricted to permitted school/branch audience")
    void testPublicPostRestrictedToPermittedBranch() throws Exception {
        // David Miller is at South Campus (Branch 2). He should not see North Campus post if branch scoped
        // And when querying feed, North Campus teachers do not receive South Campus posts!
        mockMvc.perform(get("/api/evidencing/posts")
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem(SEED_POST_PUBLIC.toString())))
                .andExpect(jsonPath("$.content[*].id", not(hasItem(SEED_POST_SOUTH_CAMPUS.toString()))));
    }

    @Test
    @DisplayName("Scenario 7: Public posts have no recipient tags in retrieval")
    void testPublicPostHasNoRecipientTags() throws Exception {
        mockMvc.perform(get("/api/evidencing/posts/{id}", SEED_POST_PUBLIC)
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.taggedPeople").isArray())
                .andExpect(jsonPath("$.taggedPeople").isEmpty());
    }

    @Test
    @DisplayName("Scenario 8: Unauthorized recipients cannot be selected or submitted through API")
    void testUnauthorizedRecipientCannotBeSubmitted() throws Exception {
        MediaAsset media = createTestMedia(USER_SARAH_TEACHER, MediaAssetStatus.READY);

        // Attempt to tag Mary (Nanny - no Evidencing permission)
        PostCreateRequest requestWithNanny = PostCreateRequest.builder()
                .schoolId(SCHOOL_OAKRIDGE)
                .branchId(BRANCH_NORTH)
                .categoryId(CATEGORY_ACHIEVEMENTS)
                .caption("Tagging nanny")
                .visibility(PostVisibility.PRIVATE)
                .taggedRecipientIds(List.of(USER_MARY_NANNY))
                .mediaAssetIds(List.of(media.getId()))
                .build();

        mockMvc.perform(post("/api/evidencing/posts")
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithNanny)))
                .andExpect(status().isForbidden());

        // Attempt to tag teacher from different branch (David Miller - South Campus)
        PostCreateRequest requestWithCrossBranch = PostCreateRequest.builder()
                .schoolId(SCHOOL_OAKRIDGE)
                .branchId(BRANCH_NORTH)
                .categoryId(CATEGORY_ACHIEVEMENTS)
                .caption("Tagging other branch")
                .visibility(PostVisibility.PRIVATE)
                .taggedRecipientIds(List.of(USER_DAVID_SOUTH_TEACHER))
                .mediaAssetIds(List.of(media.getId()))
                .build();

        mockMvc.perform(post("/api/evidencing/posts")
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithCrossBranch)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Scenario 9: Failed media processing prevents publication and offers retry")
    void testFailedMediaProcessingPreventsPublication() throws Exception {
        MediaAsset failedMedia = createTestMedia(USER_SARAH_TEACHER, MediaAssetStatus.FAILED);

        PostCreateRequest request = PostCreateRequest.builder()
                .schoolId(SCHOOL_OAKRIDGE)
                .branchId(BRANCH_NORTH)
                .categoryId(CATEGORY_LEARNING)
                .caption("Trying to publish unready media")
                .visibility(PostVisibility.PUBLIC)
                .mediaAssetIds(List.of(failedMedia.getId()))
                .build();

        mockMvc.perform(post("/api/evidencing/posts")
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Publication requires all media to be READY")));

        // Test Retry endpoint
        mockMvc.perform(post("/api/evidencing/media/{id}/retry", failedMedia.getId())
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaAssetId").value(failedMedia.getId().toString()))
                .andExpect(jsonPath("$.status", anyOf(is("PROCESSING"), is("READY"))));
    }

    @Test
    @DisplayName("Scenario 10: Duplicate publication requests do not create duplicate posts (Idempotency)")
    void testDuplicatePublicationIdempotency() throws Exception {
        MediaAsset media = createTestMedia(USER_SARAH_TEACHER, MediaAssetStatus.READY);
        String idempotencyKey = "test-idempotency-" + UUID.randomUUID();

        PostCreateRequest request = PostCreateRequest.builder()
                .schoolId(SCHOOL_OAKRIDGE)
                .branchId(BRANCH_NORTH)
                .categoryId(CATEGORY_LEARNING)
                .caption("Annual sports sprint qualifier")
                .visibility(PostVisibility.PUBLIC)
                .mediaAssetIds(List.of(media.getId()))
                .build();

        // First publication
        String firstResp = mockMvc.perform(post("/api/evidencing/posts")
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PostResponse p1 = objectMapper.readValue(firstResp, PostResponse.class);

        // Duplicate publication with same idempotency key
        String secondResp = mockMvc.perform(post("/api/evidencing/posts")
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PostResponse p2 = objectMapper.readValue(secondResp, PostResponse.class);

        assertEquals(p1.getId(), p2.getId(), "Duplicate request with same idempotency key must return the same post ID!");
    }

    @Test
    @DisplayName("Scenario 11: Visibility changes immediately affect subsequent API authorization")
    void testVisibilityChangesAffectAuthorizationImmediately() throws Exception {
        // Create private post tagged to Emily
        MediaAsset media = createTestMedia(USER_SARAH_TEACHER, MediaAssetStatus.READY);

        PostCreateRequest createReq = PostCreateRequest.builder()
                .schoolId(SCHOOL_OAKRIDGE)
                .branchId(BRANCH_NORTH)
                .categoryId(CATEGORY_LEARNING)
                .caption("Initially private post")
                .visibility(PostVisibility.PRIVATE)
                .taggedRecipientIds(List.of(USER_EMILY_TEACHER))
                .mediaAssetIds(List.of(media.getId()))
                .build();

        String respStr = mockMvc.perform(post("/api/evidencing/posts")
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PostResponse created = objectMapper.readValue(respStr, PostResponse.class);
        UUID createdId = created.getId();

        // Mark Wilson cannot view it while private
        mockMvc.perform(get("/api/evidencing/posts/{id}", createdId)
                        .header("X-User-Id", USER_MARK_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER"))
                .andExpect(status().isForbidden());

        // Update post from PRIVATE to PUBLIC
        Post postEntity = postRepository.findById(createdId).orElseThrow();
        PostUpdateRequest updateReq = PostUpdateRequest.builder()
                .categoryId(CATEGORY_LEARNING)
                .caption("Now public post for all branch staff!")
                .visibility(PostVisibility.PUBLIC)
                .version(postEntity.getVersion())
                .build();

        mockMvc.perform(put("/api/evidencing/posts/{id}", createdId)
                        .header("X-User-Id", USER_SARAH_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.taggedPeople").isEmpty());

        // Now Mark Wilson CAN immediately view it!
        mockMvc.perform(get("/api/evidencing/posts/{id}", createdId)
                        .header("X-User-Id", USER_MARK_TEACHER.toString())
                        .header("X-User-Roles", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId.toString()));
    }
}
