package com.fastcampus.sns.controller;

import com.fastcampus.sns.controller.request.PostCommentRequest;
import com.fastcampus.sns.controller.request.PostCreateRequest;
import com.fastcampus.sns.controller.request.PostModifyRequest;
import com.fastcampus.sns.controller.response.CommentResponse;
import com.fastcampus.sns.controller.response.PostResponse;
import com.fastcampus.sns.controller.response.Response;
import com.fastcampus.sns.model.Post;
import com.fastcampus.sns.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public Response<Void> create(@RequestBody PostCreateRequest request, Authentication authentication) { // JwtTokenFilter에서 Authentication context를 넣어줬던 부분을 그대로 받아와서 getName을 하여 name을 뽑아내면 Token에서 userName을 뽑아낸 셈이다.
        // 즉, context에 setAuthentication으로 filter에서 만든 Authentication를 넣어줬기 때문에 이 authentication을 여기서 param으로 받아올 수 있게 된다.
        postService.create(request.getTitle(), request.getBody(), authentication.getName());
        return Response.success();
    }

    @PutMapping("/{postId}")
    public Response<PostResponse> modify(@PathVariable Integer postId, @RequestBody PostModifyRequest request, Authentication authentication) {
        Post post = postService.modify(request.getTitle(), request.getBody(), authentication.getName(), postId);
        return Response.success(PostResponse.fromPost(post));
    }

    @DeleteMapping("/{postId}") // 이번 프로젝트에서는 delete api가 있는 것은 post의 경우에만 존재하는데 이때 soft delete으로 들어가게 된다.
    // 근데 post를 delete할때 post만 delete되는게 아니라 post에 딸린 comment나 like도 함께 delete(얘네도 soft delete)가 되어야 한다.
    public Response<Void> delete(@PathVariable Integer postId, Authentication authentication) {
        postService.delete(authentication.getName(), postId);
        return Response.success();
    }

    @GetMapping
    public Response<Page<PostResponse>> list(Pageable pageable, Authentication authentication) { // Paging을 위해서 Pageable로 받아와서 이 Pageable class안에 있는 메소드들로 Paging(Query param, sorting 등)을 구현하자
        return Response.success(postService.list(pageable).map(PostResponse::fromPost));
    }

    @GetMapping("/my")
    public Response<Page<PostResponse>> my(Pageable pageable, Authentication authentication) {
        return Response.success(postService.my(authentication.getName(), pageable).map(PostResponse::fromPost));
    }

    @PostMapping("/{postId}/likes")
    public Response<Void> like(@PathVariable Integer postId, Authentication authentication) { // 좋아요만 counting 되고 난 후에 가져올게 없으므로 그냥 Void로 Response를 받는다.
        postService.like(postId, authentication.getName());
        return Response.success();
    }

    @GetMapping("/{postId}/likes")
    public Response<Long> likeCount(@PathVariable Integer postId, Authentication authentication) {
        return Response.success(postService.likeCount(postId));
    }

    @PostMapping("/{postId}/comments")
    public Response<Void> comment(@PathVariable Integer postId, @RequestBody PostCommentRequest request, Authentication authentication) {
        postService.comment(postId, authentication.getName(), request.getComment());
        return Response.success();
    }

    @GetMapping("/{postId}/comments") // comment list를 가져오는 API인데, 이때도 역시 Paging처리를 해줘야 한다.
    public Response<Page<CommentResponse>> commentList(@PathVariable Integer postId, Pageable pageable, Authentication authentication) {
        return Response.success(postService.getComments(postId, pageable).map(CommentResponse::fromComment));
    }

}
