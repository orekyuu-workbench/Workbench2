package net.orekyuu.workbench.pullrequest.usecase;

import net.orekyuu.workbench.project.domain.model.Project;
import net.orekyuu.workbench.pullrequest.domain.model.PullRequest;
import net.orekyuu.workbench.pullrequest.port.PullRequestRepository;
import net.orekyuu.workbench.user.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PullRequestUsecase {

    private final PullRequestRepository pullRequestRepository;

    public PullRequestUsecase(PullRequestRepository pullRequestRepository) {
        this.pullRequestRepository = pullRequestRepository;
    }

    @Transactional(readOnly = false)
    public PullRequest create(Project project, String title, String desc, User reviewer, User proponent, String base, String target) {
        return pullRequestRepository.create(project, title, desc, reviewer, proponent, base, target);
    }

    @Transactional(readOnly = false)
    public PullRequest update(PullRequest pullRequest) {
        return pullRequestRepository.save(pullRequest);
    }

    @Transactional(readOnly = false)
    public PullRequest merge(PullRequest pullRequest, String baseCommit, String targetCommit) {
        return pullRequestRepository.merge(pullRequest, baseCommit, targetCommit);
    }

    @Transactional(readOnly = false)
    public void deleteByProject(Project project) {
        pullRequestRepository.deleteByProject(project);
    }

    public List<PullRequest> findByProject(Project project) {
        return pullRequestRepository.findByProject(project);
    }

    public Optional<PullRequest> findById(Project project, int num) {
        return pullRequestRepository.findById(project, num);
    }
}
