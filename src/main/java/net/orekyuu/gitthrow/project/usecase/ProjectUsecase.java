package net.orekyuu.gitthrow.project.usecase;

import net.orekyuu.gitthrow.activity.usecase.ActivityUsecase;
import net.orekyuu.gitthrow.project.domain.model.Project;
import net.orekyuu.gitthrow.project.domain.policy.ProjectMemberPolicy;
import net.orekyuu.gitthrow.project.port.ProjectRepository;
import net.orekyuu.gitthrow.project.port.table.ProjectUserDao;
import net.orekyuu.gitthrow.project.port.table.ProjectUserTable;
import net.orekyuu.gitthrow.service.exceptions.ProjectExistsException;
import net.orekyuu.gitthrow.user.domain.model.User;
import net.orekyuu.gitthrow.user.util.BotUserUtil;
import net.orekyuu.gitthrow.util.PolicyException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProjectUsecase {


    private final ProjectRepository projectRepository;
    private final ProjectUserDao projectUserDao;
    private final ActivityUsecase activityUsecase;

    public ProjectUsecase(ProjectRepository projectRepository, ProjectUserDao projectUserDao, ActivityUsecase activityUsecase) {
        this.projectRepository = projectRepository;
        this.projectUserDao = projectUserDao;
        this.activityUsecase = activityUsecase;
    }


    @Transactional(readOnly = false)
    public Project createProject(String projectId, String projectName, User owner) throws ProjectExistsException {
        Project project = projectRepository.create(projectId, projectName, owner);
        activityUsecase.createInitRepositoryActivity(project, owner);
        return project;
    }

    @Transactional(readOnly = false)
    public Project join(Project project, User user) {
        List<User> member = project.getMember();
        if (!member.contains(user)) {
            if (!new ProjectMemberPolicy(project).check(user)) {
                throw new PolicyException();
            }
            if (BotUserUtil.isProjectBot(project.getId(), user.getId())) {
                throw new PolicyException();
            }

            try {
                projectUserDao.insert(new ProjectUserTable(project.getId(), user.getId()));
                member.add(user);
            } catch (DuplicateKeyException e) {
                throw new PolicyException();
            }
            return new Project(project.getId(), project.getName(), project.getOwner(), member);
        } else {
            throw new PolicyException();
        }
    }

    @Transactional(readOnly = false)
    public Project withdraw(Project project, User user) {
        List<User> member = project.getMember();
        if (member.contains(user)) {
            if (!new ProjectMemberPolicy(project).check(user)) {
                throw new PolicyException();
            }

            if (project.getOwner().equals(user)) {
                throw new PolicyException();
            }
            projectUserDao.delete(new ProjectUserTable(project.getId(), user.getId()));
            member.remove(user);
            return new Project(project.getId(), project.getName(), project.getOwner(), member);
        } else {
            throw new PolicyException();
        }
    }

    @Transactional(readOnly = false)
    public Project save(Project project) {
        return projectRepository.save(project);
    }

    @Transactional(readOnly = false)
    public Project delete(Project project) {
        return projectRepository.delete(project);
    }

    public Optional<Project> findById(String projectId) {
        return projectRepository.findById(projectId);
    }

    public List<Project> findByUser(User user) {
        return projectRepository.findByUser(user);
    }

    public boolean isJoined(String projectId, String userId) {
        return projectUserDao.findByUserAndProject(projectId, userId).isPresent();
    }
}
