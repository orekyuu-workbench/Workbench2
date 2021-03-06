package net.orekyuu.gitthrow.pullrequest.usecase;

import net.orekyuu.gitthrow.activity.usecase.ActivityUsecase;
import net.orekyuu.gitthrow.job.TestJob;
import net.orekyuu.gitthrow.job.util.NullSseEmitter;
import net.orekyuu.gitthrow.project.domain.model.Project;
import net.orekyuu.gitthrow.project.usecase.ProjectUsecase;
import net.orekyuu.gitthrow.pullrequest.domain.model.PullRequest;
import net.orekyuu.gitthrow.pullrequest.port.PullRequestRepository;
import net.orekyuu.gitthrow.user.domain.model.User;
import net.orekyuu.gitthrow.user.usecase.UserUsecase;
import net.orekyuu.gitthrow.user.util.BotUserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PullRequestUsecase {

    private final PullRequestRepository pullRequestRepository;
    private final UserUsecase userUsecase;
    private final ActivityUsecase activityUsecase;
    private final ProjectUsecase projectUsecase;

    private static final Logger logger = LoggerFactory.getLogger(PullRequestUsecase.class);

    public PullRequestUsecase(PullRequestRepository pullRequestRepository, UserUsecase userUsecase, ActivityUsecase activityUsecase, ProjectUsecase projectUsecase) {
        this.pullRequestRepository = pullRequestRepository;
        this.userUsecase = userUsecase;
        this.activityUsecase = activityUsecase;
        this.projectUsecase = projectUsecase;
    }

    @Transactional(readOnly = false)
    public PullRequest create(Project project, String title, String desc, User reviewer, User proponent, String base, String target) {
        PullRequest pullRequest = pullRequestRepository.create(project, title, desc, reviewer, proponent, base, target);
        activityUsecase.createNewPullRequestActivity(project, pullRequest);
        TestJob job = testJob();
        job.setPrNum(pullRequest.getPullrequestNum());
        job.setHash(target);
        job.start(new NullSseEmitter(), project, userUsecase.findById(BotUserUtil.toBotUserId(project.getId())).orElseThrow(() -> new RuntimeException("BotUser not found.")));
        return pullRequest;
    }

    @Transactional(readOnly = false)
    public PullRequest update(PullRequest pullRequest) {
        return pullRequestRepository.save(pullRequest);
    }

    @Transactional(readOnly = false)
    public PullRequest merge(PullRequest pullRequest, String baseCommit, String targetCommit) {
        Project project = projectUsecase.findById(pullRequest.getProjectId()).orElse(null);
        if (project == null) {
            logger.warn("project is null.");
        } else {
            activityUsecase.createMergePullRequestActivity(project, pullRequest);
        }
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

    @Lookup
    TestJob testJob() {
        return null;
    }
}
