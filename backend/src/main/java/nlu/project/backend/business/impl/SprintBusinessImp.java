package nlu.project.backend.business.impl;

import lombok.NoArgsConstructor;
import nlu.project.backend.DAO.*;
import nlu.project.backend.business.IssueBusiness;
import nlu.project.backend.business.ProjectBusiness;
import nlu.project.backend.business.SprintBusiness;
import nlu.project.backend.entry.filter.SprintFilterParams;
import nlu.project.backend.entry.sprint.CreateSprintParams;
import nlu.project.backend.entry.sprint.EditSprintParams;
import nlu.project.backend.entry.sprint.IssueInSprintSearchParams;
import nlu.project.backend.entry.sprint.StartSprintParams;
import nlu.project.backend.model.*;
import nlu.project.backend.model.security.CustomUserDetails;
import nlu.project.backend.repository.SprintVelocityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public class SprintBusinessImp implements SprintBusiness {

    @Autowired
    SprintDAO sprintDAO;

    @Autowired
    UserDAO userDAO;

    @Autowired
    ProjectDAO projectDAO;

    @Autowired
    IssueDAO issueDAO;

    @Autowired
    ProjectBusiness productBusiness;

    @Autowired
    SprintVelocityDAO sprintVelocityDAO;

    @Override
    public List<Sprint> findByFilter(SprintFilterParams filter) {
        return sprintDAO.findByFilter(filter);
    }

    @Override
    public Sprint create(CreateSprintParams entry, User user) throws InvalidParameterException {
        if (!userDAO.isProductOwner(user.getId(), entry.projectId))
            throw new InvalidParameterException("Invalid parameters!");
        Project project = projectDAO.getProjectById(entry.projectId);
        String nextName = sprintDAO.getNextName(entry.projectId);
        if (nextName == null)
            throw new InternalError("Can not generate next project's sprint name, projectId[ " + entry.projectId + "]");
        List<Sprint> workingSprints = sprintDAO.findWorkingSprints(entry.projectId);
        Sprint adding = new Sprint();
        adding.setProject(project);
        adding.setName(nextName);
        adding.setStatus(0);
        adding.setIssues(Collections.emptyList());
        if (workingSprints.size() > 0)
            adding.setOrder(workingSprints.get(workingSprints.size() - 1).getOrder() + 1);
        else
            adding.setOrder(0);
        Sprint result = sprintDAO.add(adding);
        if (result == null)
            throw new InternalError("Can not save next sprint name, name[ " + nextName + "]");
        return result;
    }

    @Override
    public Sprint endSprint(Integer sprintId, User user) throws InvalidParameterException {

        Sprint sprint = sprintDAO.getOne(sprintId);
        if (!userDAO.isProductOwner(user.getId(), sprint.getProject().getId()))
            throw new InvalidParameterException("Invalid parameters!");
        sprint.setDateEnd(new Date(System.currentTimeMillis()));
        sprint.setStatus(2);
        Project project = sprint.getProject();

        WorkFlowItem endStatus = project.getCurrentWorkFlow()
                .getItems().stream()
                .filter(status->status.isEnd()).findFirst().get();

        WorkFlowItem startStatus = project.getCurrentWorkFlow()
                .getItems().stream()
                .filter(status -> status.isStart())
                .findFirst().get();

        List<Issue> issues = sprint.getIssues();

        //saving velocity
        List<Issue> doneIssues = sprint.getIssues().stream()
                .filter(iss -> iss.getStatus().getId() == endStatus.getId())
                .collect(Collectors.toList());

        int totalStoryPoint = 0;
        for(Issue doneIssue : doneIssues){
            totalStoryPoint += doneIssue.getStoryPoint();
        }
        int totalExpectStoryPoint = 0;
        for(Issue issue : issues){
            totalExpectStoryPoint+=issue.getStoryPoint();
        }
        SprintVelocity velocity = new SprintVelocity();
        velocity.setDate(new Date());
        velocity.setProjectId(sprint.getProject().getId());
        velocity.setSprintName(sprint.getName());
        velocity.setTotalStoryPoint(totalStoryPoint);
        velocity.setSprintId(sprint.getId());
        velocity.setTotalExpectStoryPoint(totalExpectStoryPoint);
        //end saving velocity


        // moving the status isn't done to not started
        if (issues.size() > 0)
            issues.forEach(new Consumer<Issue>() {
                @Override
                public void accept(Issue issue) {
                    if (issue.getStatus().isStart()) {
                        issue.setSprint(null);
                        issueDAO.update(issue);
                    }
                    if(!issue.getStatus().isStart() && !issue.getStatus().isEnd()){
                        issue.setSprint(null);
                        issue.setStatus(startStatus);
                        issueDAO.update(issue);
                    }
                }
            });
        sprintVelocityDAO.save(velocity);
        sprintDAO.update(sprint);
        return sprint;
    }

    @Override
    public Sprint startSprint(StartSprintParams entry, User user) throws InvalidParameterException {

        Sprint sprint = sprintDAO.getOne(entry.sprintId);
        if (!userDAO.isProductOwner(user.getId(), sprint.getProject().getId()))
            throw new InvalidParameterException("Invalid parameters!");
        sprint.setDateBegin(entry.startDate);
        sprint.setPlanDateEnd(entry.endDate);
        sprint.setName(entry.name);
        sprint.setGoal(entry.goal);
        sprint.setStatus(1);
        sprintDAO.update(sprint);
        return sprint;
    }

    @Override
    public List<Sprint> moveUp(Integer sprintId, User user) throws InvalidParameterException {
        Sprint requestedSprint = sprintDAO.getOne(sprintId);
        Project ownerProject = requestedSprint.getProject();

        List<Sprint> sprints = sprintDAO.findWorkingSprints(ownerProject.getId());
        int idoSprint = sprints.indexOf(requestedSprint);
        if (idoSprint < 0) throw new InternalError("Cannot find index of sprint: " + sprintId);
        if (idoSprint == 0) {
            return sprints;
        }
        int previousOrder = requestedSprint.getOrder();
        requestedSprint.setOrder(sprints.get(idoSprint - 1).getOrder());
        sprints.get(idoSprint - 1).setOrder(previousOrder);
        sprintDAO.update(requestedSprint);
        sprintDAO.update(sprints.get(idoSprint - 1));

        Collections.sort(sprints, new Comparator<Sprint>() {
            @Override
            public int compare(Sprint o1, Sprint o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
        return sprints;
    }

    @Override
    public List<Sprint> moveDown(Integer sprintId, User user) throws InvalidParameterException {
        Sprint requestedSprint = sprintDAO.getOne(sprintId);
        Project ownerProject = requestedSprint.getProject();

        List<Sprint> sprints = sprintDAO.findWorkingSprints(ownerProject.getId());
        int idoSprint = sprints.indexOf(requestedSprint);
        if (idoSprint < 0) throw new InternalError("Cannot find index of sprint: " + sprintId);
        if (idoSprint == (sprints.size() - 1)) {
            return sprints;
        }
        int previousOrder = requestedSprint.getOrder();
        requestedSprint.setOrder(sprints.get(idoSprint + 1).getOrder());
        sprints.get(idoSprint + 1).setOrder(previousOrder);
        sprintDAO.update(requestedSprint);
        sprintDAO.update(sprints.get(idoSprint + 1));

        Collections.sort(sprints, new Comparator<Sprint>() {
            @Override
            public int compare(Sprint o1, Sprint o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
        return sprints;
    }

    @Override
    public Sprint deleteSprint(Integer sprintId, User user) throws InvalidParameterException {

        Sprint sprint = sprintDAO.getOne(sprintId);
        Project ownerProject = sprint.getProject();
        if (!userDAO.isProductOwner(user.getId(), ownerProject.getId()))
            throw new InvalidParameterException("Invalid parameters!");
        List<Sprint> sprints = sprintDAO.findWorkingSprints(ownerProject.getId());

        int idoSprint = sprints.indexOf(sprint);
        if (idoSprint >= 0 && idoSprint < (sprints.size() - 1)) {
            Sprint nextSprint = sprints.get(idoSprint + 1);
            List<Issue> issues = nextSprint.getIssues();
            if (issues != null) {
                sprint.getIssues().forEach(issue ->
                        issue.setSprint(nextSprint)
                );
                issues.addAll(sprint.getIssues());
            }
        } else if (idoSprint > 0) {
            sprint.getIssues().forEach(issue ->
                    issue.setSprint(null)
            );
        }
        sprint.setStatus(-1);
        sprintDAO.update(sprint);
        return sprint;
    }

    @Override
    public Sprint updateSprint(EditSprintParams entry, User user) throws InvalidParameterException {

        Sprint sprint = sprintDAO.getOne(entry.sprintId);
        if (!userDAO.isProductOwner(user.getId(), sprint.getProject().getId()))
            throw new InvalidParameterException("Invalid parameters!");
        sprint.setName(entry.name);
        sprint.setGoal(entry.goal);
        sprintDAO.update(sprint);
        return sprint;
    }

    @Override
    public List<Issue> issueInSprintSearchParams(IssueInSprintSearchParams entry,User user) {
        Sprint sprint = sprintDAO.getOne(entry.sprintId);
        List<Issue> issues = sprint.getIssues();
        List<Issue> result  = issues.stream().filter(iss -> iss.getIssueType().getId() == entry.issueTypeId)
                .filter(iss -> iss.getDescription().toUpperCase().contains(entry.name.toUpperCase())).collect(Collectors.toList());
        return result;
    }

}
