package net.orekyuu.workbench.controller.view.user;

import net.orekyuu.workbench.config.security.WorkbenchUserDetails;
import net.orekyuu.workbench.entity.User;
import net.orekyuu.workbench.entity.UserAvatar;
import net.orekyuu.workbench.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.Objects;

@Controller
public class UserSettingController {

    @Autowired
    private UserService userService;

    @GetMapping("/user-setting")
    public String show(Model model, @AuthenticationPrincipal WorkbenchUserDetails principal) {
        UserSettingForm form = (UserSettingForm) model.asMap().get("userSettingForm");
        form.name = principal.getUser().name;
        return "user/user-setting";
    }

    @PostMapping("/user-setting")
    public String update(@Valid UserSettingForm form, BindingResult result, @AuthenticationPrincipal WorkbenchUserDetails principal) {
        if (result.hasErrors()) {
            return "user/user-setting";
        }

        User user = principal.getUser();
        if (!Objects.equals(user.name, form.name)) {
            user.name = form.name;
            userService.update(user);
        }

        if (form.avatar != null && form.avatar.getSize() != 0) {

            try {
                byte[] bytes = form.avatar.getBytes();
                UserAvatar userAvatar = new UserAvatar();
                userAvatar.id = user.id;
                userAvatar.avatar = bytes;
                userService.updateIcon(userAvatar);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return "redirect:/user-setting";
    }

    @ModelAttribute
    public UserSettingForm userSettingForm() {
        return new UserSettingForm();
    }

    public static class UserSettingForm {
        @Size(min = 3, max = 16)
        private String name;

        private MultipartFile avatar;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public MultipartFile getAvatar() {
            return avatar;
        }

        public void setAvatar(MultipartFile avatar) {
            this.avatar = avatar;
        }
    }
}