package blog.controllers;

import blog.forms.LoginForm;
import blog.forms.PostForm;
import blog.models.Post;
import blog.models.User;
import blog.services.NotificationService;
import blog.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import javax.validation.Valid;

import static java.util.stream.Collectors.*;

@Controller
public class HomeController {

    @Autowired
    private PostService postService;

    @Autowired
    private NotificationService notificationService;

    @RequestMapping("/")
    public String home(Model model) {
        List<Post> latest5Posts = postService.findLatest5().stream()
                .limit(5).collect(toList());
        model.addAttribute("latest5posts", latest5Posts);

        List<Post> latest3Posts = latest5Posts.stream()
                .limit(3).collect(toList());
        model.addAttribute("latest3posts", latest3Posts);

        return "index";
    }

    @RequestMapping("/posts/view/{id}")
    public String view(@PathVariable("id") Long id,
                       Model model) {
        Post post = postService.findById(id);

        if (post == null) {
            notificationService.addErrorMessage(
                    "Cannot find post: " + id);
            return "redirect:/";
        }

        model.addAttribute("post", post);
        return "/posts/index";
    }
    
    @RequestMapping("/posts")
    public String findAllPosts(Model model) {
        List<Post> allPosts = postService.findAll();
        model.addAttribute("allPosts", allPosts);
        return "/posts/details";
    }
    
    @RequestMapping("/posts/create")
    public String createPost(PostForm postForm) {
        return "posts/create";
    }
    
    @RequestMapping(value = "/posts/create",
            method = RequestMethod.POST)
    public String createPost(
            @Valid PostForm postForm,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            notificationService.addErrorMessage(
                    "Invalid Post");
            return "posts/create";
        }

        Post post = new Post();
        post.setBody(postForm.getBody());
        post.setTitle(postForm.getTitle());
        User user = new User();
        user.setId(Long.parseLong(postForm.getAuthor()));
        post.setAuthor(user);
        postService.save(post);
        
        // Post successful
        notificationService.addInfoMessage("Post Successful!");
        return "index";
    }
}